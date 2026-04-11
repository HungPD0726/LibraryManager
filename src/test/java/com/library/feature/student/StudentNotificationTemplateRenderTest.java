package com.library.feature.student;

import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.support.ThymeleafRenderSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StudentNotificationTemplateRenderTest {

    @Test
    void notifications_shouldRenderBorrowReminderIcons() {
        Notification dueSoon = new Notification();
        dueSoon.setNotificationId(99);
        dueSoon.setStudent(student());
        dueSoon.setTitle("Sắp đến hạn trả sách");
        dueSoon.setMessage("Đơn mượn #99 sẽ đến hạn vào ngày 12/04/2026.");
        dueSoon.setType("BORROW_DUE_SOON");
        dueSoon.setIsRead(false);
        dueSoon.setCreatedDate(LocalDateTime.of(2026, 4, 4, 9, 30));

        Notification overdue = new Notification();
        overdue.setNotificationId(100);
        overdue.setStudent(student());
        overdue.setTitle("Phiếu mượn đã quá hạn");
        overdue.setMessage("Đơn mượn #100 đã quá hạn 2 ngày kể từ 09/04/2026.");
        overdue.setType("BORROW_OVERDUE");
        overdue.setIsRead(true);
        overdue.setCreatedDate(LocalDateTime.of(2026, 4, 5, 8, 0));

        String html = ThymeleafRenderSupport.render(
                "student/notifications",
                "/notifications",
                Map.of(
                        "notifications", List.of(dueSoon, overdue),
                        "unreadCount", 1L
                ),
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("/libraryManager/notifications/99/read")
                .contains("Sắp đến hạn trả sách")
                .contains("Phiếu mượn đã quá hạn")
                .contains("fa-hourglass-half")
                .contains("fa-clock");
    }

    private static Student student() {
        Student student = new Student();
        student.setStudentId(7);
        student.setStudentName("Nguyen Minh");
        student.setEmail("minh@example.com");
        return student;
    }
}
