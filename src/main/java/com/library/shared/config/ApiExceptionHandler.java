package com.library.shared.config;

import com.library.feature.chatbot.ChatbotController;
import com.library.feature.chatbot.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(annotations = RestController.class, assignableTypes = ChatbotController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError && StringUtils.hasText(fieldError.getDefaultMessage())) {
                        return fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("Dữ liệu yêu cầu không hợp lệ.");
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_JSON",
                "Nội dung JSON không hợp lệ.",
                request,
                ex
        );
    }

    @ExceptionHandler(ChatbotService.ChatbotException.class)
    public ResponseEntity<ApiErrorResponse> handleChatbot(ChatbotService.ChatbotException ex,
                                                          HttpServletRequest request) {
        return buildResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex,
                                                             HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.",
                request,
                ex
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String code,
                                                           String message,
                                                           HttpServletRequest request,
                                                           Exception ex) {
        if (status.is5xxServerError()) {
            log.error("API error at {} [{}]: {}", request.getRequestURI(), code, ex.getMessage(), ex);
        } else {
            log.warn("API error at {} [{}]: {}", request.getRequestURI(), code, ex.getMessage());
        }

        return ResponseEntity.status(status).body(new ApiErrorResponse(
                message,
                code,
                request.getRequestURI(),
                Instant.now().toString()
        ));
    }

    record ApiErrorResponse(String error, String code, String path, String timestamp) {
    }
}
