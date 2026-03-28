package com.library.config;

import com.library.service.BorrowService;
import com.library.service.StudentContextService;
import com.library.support.RoleSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class CommonModelAdvice {

    private final BorrowService borrowService;
    private final StudentContextService studentContextService;

    @ModelAttribute
    public void enrichModel(Model model, Authentication authentication, HttpServletRequest request) {
        model.addAttribute("requestPath", request.getRequestURI());

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("isAdmin", RoleSupport.isAdmin(authentication));
        model.addAttribute("isStaff", RoleSupport.isStaff(authentication));
        model.addAttribute("isStudent", RoleSupport.isStudent(authentication));

        if (RoleSupport.isAdmin(authentication) || RoleSupport.isStaff(authentication)) {
            model.addAttribute("pendingBorrowCount", borrowService.countPending());
        }
        if (RoleSupport.isStudent(authentication)) {
            studentContextService.resolveCurrentStudent(authentication)
                    .ifPresent(student -> model.addAttribute("currentStudent", student));
        }
    }
}
