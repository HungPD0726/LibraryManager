package com.library.feature.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(null, null);
    }

    @Test
    void missingMailConfigurationKeys_shouldBeEmptyWhenMailConfigIsPresent() {
        setMailConfig("sender@example.com", "app-password");

        assertThat(emailService.isConfigured()).isTrue();
        assertThat(emailService.missingMailConfigurationKeys()).isEmpty();
    }

    @Test
    void missingMailConfigurationKeys_shouldReportMissingUsername() {
        setMailConfig("", "app-password");

        assertThat(emailService.isConfigured()).isFalse();
        assertThat(emailService.missingMailConfigurationKeys()).containsExactly("MAIL_USERNAME");
        assertThat(emailService.missingMailConfigurationMessage()).isEqualTo("Thiếu cấu hình MAIL_USERNAME.");
    }

    @Test
    void missingMailConfigurationKeys_shouldReportMissingPassword() {
        setMailConfig("sender@example.com", "");

        assertThat(emailService.isConfigured()).isFalse();
        assertThat(emailService.missingMailConfigurationKeys()).containsExactly("MAIL_PASSWORD");
        assertThat(emailService.missingMailConfigurationMessage()).isEqualTo("Thiếu cấu hình MAIL_PASSWORD.");
    }

    @Test
    void missingMailConfigurationKeys_shouldReportMissingUsernameAndPassword() {
        setMailConfig("", "");

        assertThat(emailService.isConfigured()).isFalse();
        assertThat(emailService.missingMailConfigurationKeys()).containsExactly("MAIL_USERNAME", "MAIL_PASSWORD");
        assertThat(emailService.missingMailConfigurationMessage()).isEqualTo("Thiếu cấu hình MAIL_USERNAME và MAIL_PASSWORD.");
    }

    private void setMailConfig(String username, String password) {
        ReflectionTestUtils.setField(emailService, "smtpUsername", username);
        ReflectionTestUtils.setField(emailService, "smtpPassword", password);
    }
}
