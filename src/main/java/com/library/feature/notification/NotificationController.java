package com.library.feature.notification;

import com.library.domain.model.Student;
import com.library.feature.student.CurrentStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentStudentService currentStudentService;

    @GetMapping
    public String list(Authentication authentication, Model model) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));

        model.addAttribute("notifications", notificationService.findByStudent(student.getStudentId()));
        model.addAttribute("unreadCount", notificationService.countUnread(student.getStudentId()));
        return "student/notifications";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        notificationService.markRead(id);
        redirectAttributes.addFlashAttribute("msg", "Đã đánh dấu đã đọc.");
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));
        notificationService.markAllRead(student.getStudentId());
        redirectAttributes.addFlashAttribute("msg", "Đã đánh dấu tất cả đã đọc.");
        return "redirect:/notifications";
    }
}
