package com.library.feature.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ChatbotService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int MAX_MESSAGE_COUNT = 12;
    private static final int MAX_MESSAGE_LENGTH = 2500;
    private static final String SYSTEM_PROMPT = """
            BÃ¡ÂºÂ¡n lÃƒÂ  trÃ¡Â»Â£ lÃƒÂ½ AI cho hÃ¡Â»â€¡ thÃ¡Â»â€˜ng Library Manager.
            HÃƒÂ£y trÃ¡ÂºÂ£ lÃ¡Â»Âi bÃ¡ÂºÂ±ng tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t, ngÃ¡ÂºÂ¯n gÃ¡Â»Ân, rÃƒÂµ rÃƒÂ ng vÃƒÂ  Ã†Â°u tiÃƒÂªn hÃ†Â°Ã¡Â»â€ºng dÃ¡ÂºÂ«n thÃ¡Â»Â±c tÃ¡ÂºÂ¿.
            NÃ¡ÂºÂ¿u cÃƒÂ¢u hÃ¡Â»Âi liÃƒÂªn quan Ã„â€˜Ã¡ÂºÂ¿n sÃƒÂ¡ch, mÃ†Â°Ã¡Â»Â£n trÃ¡ÂºÂ£, quy trÃƒÂ¬nh thÃ†Â° viÃ¡Â»â€¡n hoÃ¡ÂºÂ·c cÃƒÂ¡ch dÃƒÂ¹ng hÃ¡Â»â€¡ thÃ¡Â»â€˜ng,
            hÃƒÂ£y bÃƒÂ¡m Ã„â€˜ÃƒÂºng ngÃ¡Â»Â¯ cÃ¡ÂºÂ£nh quÃ¡ÂºÂ£n lÃƒÂ½ thÃ†Â° viÃ¡Â»â€¡n. NÃ¡ÂºÂ¿u thiÃ¡ÂºÂ¿u dÃ¡Â»Â¯ liÃ¡Â»â€¡u cÃ¡Â»Â¥ thÃ¡Â»Æ’, hÃƒÂ£y nÃƒÂ³i rÃƒÂµ giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final int maxCompletionTokens;
    private final double temperature;

    @Value("${app.groq.enabled:true}")
    private boolean enabled;

    @Value("${app.groq.api-key:}")
    private String apiKey;

    @Value("${app.groq.model:llama-3.1-8b-instant}")
    private String model;

    public ChatbotService(ObjectMapper objectMapper,
                          @Value("${app.groq.connect-timeout-ms:5000}") int connectTimeoutMs,
                          @Value("${app.groq.read-timeout-ms:30000}") int readTimeoutMs,
                          @Value("${app.groq.max-completion-tokens:700}") int maxCompletionTokens,
                          @Value("${app.groq.temperature:0.4}") double temperature) {
        this.objectMapper = objectMapper;
        this.maxCompletionTokens = Math.max(maxCompletionTokens, 128);
        this.temperature = Math.max(0d, Math.min(temperature, 2d));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Math.max(connectTimeoutMs, 1000));
        requestFactory.setReadTimeout(Math.max(readTimeoutMs, 1000));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isConfigured() {
        return enabled && StringUtils.hasText(apiKey);
    }

    public String getModel() {
        return model;
    }

    public ChatResult chat(List<ChatMessage> messages) {
        if (!isConfigured()) {
            throw new ChatbotException("Chatbot chÃ†Â°a Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃ¡ÂºÂ¥u hÃƒÂ¬nh GROQ_API_KEY.");
        }

        List<ChatMessage> sanitized = sanitize(messages);
        if (sanitized.isEmpty()) {
            throw new ChatbotException("NÃ¡Â»â„¢i dung hÃ¡Â»â„¢i thoÃ¡ÂºÂ¡i khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.");
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", temperature);
        payload.put("max_completion_tokens", maxCompletionTokens);

        ArrayNode requestMessages = payload.putArray("messages");
        requestMessages.addObject()
                .put("role", "system")
                .put("content", SYSTEM_PROMPT);
        for (ChatMessage message : sanitized) {
            requestMessages.addObject()
                    .put("role", message.role())
                    .put("content", message.content());
        }

        try {
            String responseBody = restClient.post()
                    .uri(API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(responseBody)) {
                throw new ChatbotException("Groq khÃƒÂ´ng trÃ¡ÂºÂ£ vÃ¡Â»Â nÃ¡Â»â„¢i dung phÃ¡ÂºÂ£n hÃ¡Â»â€œi.");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode firstChoice = root.path("choices").path(0).path("message").path("content");
            String reply = firstChoice.isMissingNode() ? "" : firstChoice.asText("");
            if (!StringUtils.hasText(reply)) {
                throw new ChatbotException("Groq khÃƒÂ´ng trÃ¡ÂºÂ£ vÃ¡Â»Â nÃ¡Â»â„¢i dung phÃ¡ÂºÂ£n hÃ¡Â»â€œi.");
            }

            String responseModel = root.path("model").asText(model);
            return new ChatResult(reply.trim(), responseModel);
        } catch (RestClientResponseException ex) {
            throw new ChatbotException(buildGroqErrorMessage(ex), ex);
        } catch (RestClientException ex) {
            throw new ChatbotException("KhÃƒÂ´ng thÃ¡Â»Æ’ kÃ¡ÂºÂ¿t nÃ¡Â»â€˜i tÃ¡Â»â€ºi Groq. Vui lÃƒÂ²ng thÃ¡Â»Â­ lÃ¡ÂºÂ¡i sau ÃƒÂ­t phÃƒÂºt.", ex);
        } catch (Exception ex) {
            throw new ChatbotException("KhÃƒÂ´ng thÃ¡Â»Æ’ Ã„â€˜Ã¡Â»Âc phÃ¡ÂºÂ£n hÃ¡Â»â€œi tÃ¡Â»Â« Groq: " + ex.getMessage(), ex);
        }
    }

    private String buildGroqErrorMessage(RestClientResponseException ex) {
        String detail = extractGroqErrorMessage(ex.getResponseBodyAsString());
        if (StringUtils.hasText(detail)) {
            return "Groq trÃ¡ÂºÂ£ vÃ¡Â»Â lÃ¡Â»â€”i " + ex.getStatusCode().value() + ": " + detail;
        }
        return "Groq trÃ¡ÂºÂ£ vÃ¡Â»Â lÃ¡Â»â€”i " + ex.getStatusCode().value() + ".";
    }

    private String extractGroqErrorMessage(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText("");
            return StringUtils.hasText(message) ? message.trim() : "";
        } catch (Exception ignored) {
            String normalized = responseBody.trim();
            return normalized.length() > 180 ? normalized.substring(0, 180) + "..." : normalized;
        }
    }

    private List<ChatMessage> sanitize(List<ChatMessage> messages) {
        List<ChatMessage> sanitized = new ArrayList<>();
        if (messages == null) {
            return sanitized;
        }

        for (ChatMessage message : messages) {
            if (message == null || !StringUtils.hasText(message.content())) {
                continue;
            }
            String normalizedRole = normalizeRole(message.role());
            if (normalizedRole == null) {
                continue;
            }
            String content = message.content().trim();
            sanitized.add(new ChatMessage(
                    normalizedRole,
                    content.length() > MAX_MESSAGE_LENGTH ? content.substring(0, MAX_MESSAGE_LENGTH) : content
            ));
        }

        if (sanitized.isEmpty() || !"user".equals(sanitized.get(sanitized.size() - 1).role())) {
            return List.of();
        }

        if (sanitized.size() > MAX_MESSAGE_COUNT) {
            return sanitized.subList(sanitized.size() - MAX_MESSAGE_COUNT, sanitized.size());
        }
        return sanitized;
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        return ("user".equals(normalized) || "assistant".equals(normalized)) ? normalized : null;
    }

    public record ChatMessage(String role, String content) {
    }

    public record ChatResult(String reply, String model) {
    }

    public static class ChatbotException extends RuntimeException {
        public ChatbotException(String message) {
            super(message);
        }

        public ChatbotException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
