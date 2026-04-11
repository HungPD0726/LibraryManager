package com.library.feature.auth;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateServiceTest {

    private final EmailTemplateService emailTemplateService = new EmailTemplateService();

    @Test
    void buildBorrowDueSoonReminderTemplate_shouldEmbedBorrowIdAndDueDate() {
        String html = emailTemplateService.buildBorrowDueSoonReminderTemplate(
                "Nguyen Minh",
                14,
                LocalDate.of(2026, 4, 12)
        );

        assertThat(html)
                .contains("Nhắc hạn trả sách")
                .contains("#14")
                .contains("12/04/2026")
                .contains("Nguyen Minh");
    }

    @Test
    void buildBorrowOverdueReminderTemplate_shouldEmbedBorrowIdDueDateAndLateDays() {
        String html = emailTemplateService.buildBorrowOverdueReminderTemplate(
                "Nguyen Minh",
                18,
                LocalDate.of(2026, 4, 9),
                2
        );

        assertThat(html)
                .contains("Phiếu mượn đã quá hạn")
                .contains("#18")
                .contains("09/04/2026")
                .contains("2 ngày")
                .contains("Nguyen Minh");
    }
}
