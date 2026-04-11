package com.library.shared.config;

import com.library.shared.support.StatusDisplaySupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class CommonModelAdvice {

    private final CurrentUserModelService currentUserModelService;
    private final StatusDisplaySupport statusDisplaySupport;

    @ModelAttribute
    public void enrichModel(Model model, Authentication authentication, HttpServletRequest request) {
        model.addAttribute("statusDisplay", statusDisplaySupport);
        currentUserModelService.enrichModel(model, authentication, request);
    }
}
