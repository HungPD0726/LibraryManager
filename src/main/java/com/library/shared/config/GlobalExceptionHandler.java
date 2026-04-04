package com.library.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, RuntimeException.class})
    public String handleRuntime(RuntimeException ex,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        log.error("Handled runtime error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + trimContextPath(referer);
        }

        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", "Ã„ÂÃƒÂ£ xÃ¡ÂºÂ£y ra lÃ¡Â»â€”i hÃ¡Â»â€¡ thÃ¡Â»â€˜ng. Vui lÃƒÂ²ng thÃ¡Â»Â­ lÃ¡ÂºÂ¡i.");
        return "redirect:/";
    }

    private String trimContextPath(String referer) {
        try {
            java.net.URL parsed = new java.net.URL(referer);
            String path = parsed.getPath();
            String contextPath = "/libraryManager";
            if (path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
            return path.isBlank() ? "/" : path;
        } catch (Exception ignored) {
            return "/";
        }
    }
}
