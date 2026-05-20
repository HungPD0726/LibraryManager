package com.library.feature.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenLibraryCoverServiceTest {

    private MockRestServiceServer server;
    private OpenLibraryCoverService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new OpenLibraryCoverService(new ObjectMapper(), builder.build());
    }

    @Test
    void suggestCovers_shouldReturnEmptyWhenTitleIsBlank() {
        assertThat(service.suggestCovers("   ", "Frank Herbert")).isEmpty();

        server.verify();
    }

    @Test
    void suggestCovers_shouldMapCoverIdsToPreviewAndLargeUrls() {
        server.expect(requestTo(containsString("https://openlibrary.org/search.json")))
                .andExpect(requestTo(containsString("title=Dune")))
                .andExpect(requestTo(containsString("author=Frank")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {
                              "key": "/works/OL893415W",
                              "title": "Dune",
                              "author_name": ["Frank Herbert"],
                              "cover_i": 8100927
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<OpenLibraryCoverService.CoverSuggestion> suggestions = service.suggestCovers(" Dune ", " Frank Herbert ");

        assertThat(suggestions).hasSize(1);
        OpenLibraryCoverService.CoverSuggestion suggestion = suggestions.get(0);
        assertThat(suggestion.title()).isEqualTo("Dune");
        assertThat(suggestion.authors()).isEqualTo("Frank Herbert");
        assertThat(suggestion.previewUrl()).isEqualTo("https://covers.openlibrary.org/b/id/8100927-M.jpg?default=false");
        assertThat(suggestion.imageUrl()).isEqualTo("https://covers.openlibrary.org/b/id/8100927-L.jpg?default=false");
        assertThat(suggestion.openLibraryUrl()).isEqualTo("https://openlibrary.org/works/OL893415W");
        server.verify();
    }

    @Test
    void suggestCovers_shouldSkipResultsWithoutCoverAndDeduplicateCoverIds() {
        server.expect(requestTo(containsString("title=Clean")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {"title": "No Cover", "author_name": ["Author One"]},
                            {"title": "Clean Code", "author_name": ["Robert C. Martin"], "cover_i": 123},
                            {"title": "Duplicate Cover", "author_name": ["Another"], "cover_i": 123},
                            {"title": "Clean Architecture", "author_name": ["Robert C. Martin"], "cover_i": 456}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<OpenLibraryCoverService.CoverSuggestion> suggestions = service.suggestCovers("Clean", null);

        assertThat(suggestions)
                .extracting(OpenLibraryCoverService.CoverSuggestion::title)
                .containsExactly("Clean Code", "Clean Architecture");
        assertThat(suggestions)
                .extracting(OpenLibraryCoverService.CoverSuggestion::imageUrl)
                .containsExactly(
                        "https://covers.openlibrary.org/b/id/123-L.jpg?default=false",
                        "https://covers.openlibrary.org/b/id/456-L.jpg?default=false"
                );
        server.verify();
    }

    @Test
    void suggestCovers_shouldReturnEmptyWhenOpenLibraryFails() {
        server.expect(requestTo(containsString("title=Dune")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThat(service.suggestCovers("Dune", null)).isEmpty();
        server.verify();
    }
}
