package com.library.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, RuntimeException.class})
    public String handleRuntime(RuntimeException ex,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        log.error("Handled runtime error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute(
                "error",
                StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "Yêu cầu không hợp lệ."
        );
        return "redirect:" + resolveRedirectTarget(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.");
        return "redirect:" + resolveRedirectTarget(request);
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (!StringUtils.hasText(referer)) {
            return "/";
        }

        try {
            java.net.URL parsed = new java.net.URL(referer);
            String path = parsed.getPath();
            String contextPath = request.getContextPath();
            if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            String query = parsed.getQuery();
            if (StringUtils.hasText(query)) {
                path = path + "?" + query;
            }
            return StringUtils.hasText(path) ? path : "/";
        } catch (Exception ignored) {
            return "/";
        }
    }
}
