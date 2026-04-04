package com.library.shared.config;

import com.library.feature.borrow.BorrowQueryService;
import com.library.feature.notification.NotificationService;
import com.library.feature.student.CurrentStudentService;
import com.library.shared.support.RoleSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class CurrentUserModelService {

    private final BorrowQueryService borrowQueryService;
    private final CurrentStudentService currentStudentService;
    private final NotificationService notificationService;

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
            model.addAttribute("pendingBorrowCount", borrowQueryService.countPending());
        }
        if (RoleSupport.isStudent(authentication)) {
            currentStudentService.resolveCurrentStudent(authentication)
                    .ifPresent(student -> {
                        model.addAttribute("currentStudent", student);
                        model.addAttribute("unreadNotifications", notificationService.countUnread(student.getStudentId()));
                    });
        }
    }
}
