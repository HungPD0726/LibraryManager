package com.library.service;

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
            Bạn là trợ lý AI cho hệ thống Library Manager.
            Hãy trả lời bằng tiếng Việt, ngắn gọn, rõ ràng và ưu tiên hướng dẫn thực tế.
            Nếu câu hỏi liên quan đến sách, mượn trả, quy trình thư viện hoặc cách dùng hệ thống,
            hãy bám đúng ngữ cảnh quản lý thư viện. Nếu thiếu dữ liệu cụ thể, hãy nói rõ giới hạn.
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
            throw new ChatbotException("Chatbot chưa được cấu hình GROQ_API_KEY.");
        }

        List<ChatMessage> sanitized = sanitize(messages);
        if (sanitized.isEmpty()) {
            throw new ChatbotException("Nội dung hội thoại không hợp lệ.");
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
                throw new ChatbotException("Groq không trả về nội dung phản hồi.");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode firstChoice = root.path("choices").path(0).path("message").path("content");
            String reply = firstChoice.isMissingNode() ? "" : firstChoice.asText("");
            if (!StringUtils.hasText(reply)) {
                throw new ChatbotException("Groq không trả về nội dung phản hồi.");
            }

            String responseModel = root.path("model").asText(model);
            return new ChatResult(reply.trim(), responseModel);
        } catch (RestClientResponseException ex) {
            throw new ChatbotException(buildGroqErrorMessage(ex), ex);
        } catch (RestClientException ex) {
            throw new ChatbotException("Không thể kết nối tới Groq. Vui lòng thử lại sau ít phút.", ex);
        } catch (Exception ex) {
            throw new ChatbotException("Không thể đọc phản hồi từ Groq: " + ex.getMessage(), ex);
        }
    }

    private String buildGroqErrorMessage(RestClientResponseException ex) {
        String detail = extractGroqErrorMessage(ex.getResponseBodyAsString());
        if (StringUtils.hasText(detail)) {
            return "Groq trả về lỗi " + ex.getStatusCode().value() + ": " + detail;
        }
        return "Groq trả về lỗi " + ex.getStatusCode().value() + ".";
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
