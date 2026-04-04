package com.library.shared.config;

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

    @ModelAttribute
    public void enrichModel(Model model, Authentication authentication, HttpServletRequest request) {
        currentUserModelService.enrichModel(model, authentication, request);
    }
}
