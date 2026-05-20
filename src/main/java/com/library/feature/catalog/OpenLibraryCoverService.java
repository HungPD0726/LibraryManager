package com.library.feature.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class OpenLibraryCoverService {

    private static final String SEARCH_URL = "https://openlibrary.org/search.json";
    private static final String OPEN_LIBRARY_URL = "https://openlibrary.org";
    private static final String COVER_URL_TEMPLATE = "https://covers.openlibrary.org/b/id/%d-%s.jpg?default=false";
    private static final int SEARCH_LIMIT = 12;
    private static final int SUGGESTION_LIMIT = 6;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenLibraryCoverService(ObjectMapper objectMapper,
                                   @Value("${app.open-library.connect-timeout-ms:3000}") int connectTimeoutMs,
                                   @Value("${app.open-library.read-timeout-ms:6000}") int readTimeoutMs) {
        this(objectMapper, buildRestClient(connectTimeoutMs, readTimeoutMs));
    }

    OpenLibraryCoverService(ObjectMapper objectMapper, RestClient restClient) {
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    public List<CoverSuggestion> suggestCovers(String title, String author) {
        String normalizedTitle = normalize(title);
        if (!StringUtils.hasText(normalizedTitle)) {
            return List.of();
        }

        String normalizedAuthor = normalize(author);
        URI requestUri = buildSearchUri(normalizedTitle, normalizedAuthor);

        try {
            String responseBody = restClient.get()
                    .uri(requestUri)
                    .header(HttpHeaders.USER_AGENT, "LibraryManager/1.0")
                    .retrieve()
                    .body(String.class);
            return parseSuggestions(responseBody, normalizedTitle, normalizedAuthor);
        } catch (RestClientException ex) {
            log.warn("Open Library cover lookup failed for '{}': {}", normalizedTitle, ex.getMessage());
            return List.of();
        } catch (Exception ex) {
            log.warn("Could not parse Open Library cover response for '{}': {}", normalizedTitle, ex.getMessage());
            return List.of();
        }
    }

    private List<CoverSuggestion> parseSuggestions(String responseBody, String fallbackTitle, String fallbackAuthor) throws Exception {
        if (!StringUtils.hasText(responseBody)) {
            return List.of();
        }

        JsonNode docs = objectMapper.readTree(responseBody).path("docs");
        if (!docs.isArray()) {
            return List.of();
        }

        List<CoverSuggestion> suggestions = new ArrayList<>();
        Set<Long> seenCoverIds = new LinkedHashSet<>();
        for (JsonNode doc : docs) {
            long coverId = doc.path("cover_i").asLong(0);
            if (coverId <= 0 || !seenCoverIds.add(coverId)) {
                continue;
            }

            String title = normalize(doc.path("title").asText(fallbackTitle));
            String authors = authors(doc.path("author_name"));
            suggestions.add(new CoverSuggestion(
                    StringUtils.hasText(title) ? title : fallbackTitle,
                    StringUtils.hasText(authors) ? authors : fallbackAuthor,
                    coverUrl(coverId, "M"),
                    coverUrl(coverId, "L"),
                    openLibraryUrl(doc.path("key").asText(""), fallbackTitle, fallbackAuthor)
            ));

            if (suggestions.size() == SUGGESTION_LIMIT) {
                break;
            }
        }
        return suggestions;
    }

    private URI buildSearchUri(String title, String author) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(SEARCH_URL)
                .queryParam("title", title)
                .queryParam("fields", "key,title,author_name,cover_i")
                .queryParam("limit", SEARCH_LIMIT);
        if (StringUtils.hasText(author)) {
            builder.queryParam("author", author);
        }
        return builder.build().encode().toUri();
    }

    private String authors(JsonNode authorNames) {
        if (!authorNames.isArray()) {
            return "";
        }

        List<String> names = new ArrayList<>();
        for (JsonNode authorName : authorNames) {
            String value = normalize(authorName.asText(""));
            if (StringUtils.hasText(value)) {
                names.add(value);
            }
            if (names.size() == 3) {
                break;
            }
        }
        return String.join(", ", names);
    }

    private String openLibraryUrl(String key, String title, String author) {
        if (StringUtils.hasText(key) && key.startsWith("/")) {
            return OPEN_LIBRARY_URL + key;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(OPEN_LIBRARY_URL + "/search")
                .queryParam("title", title);
        if (StringUtils.hasText(author)) {
            builder.queryParam("author", author);
        }
        return builder.build().encode().toUriString();
    }

    private String coverUrl(long coverId, String size) {
        return String.format(COVER_URL_TEMPLATE, coverId, size);
    }

    private static RestClient buildRestClient(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Math.max(connectTimeoutMs, 1000));
        requestFactory.setReadTimeout(Math.max(readTimeoutMs, 1000));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record CoverSuggestion(
            String title,
            String authors,
            String previewUrl,
            String imageUrl,
            String openLibraryUrl
    ) {
    }
}
