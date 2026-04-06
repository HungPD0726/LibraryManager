package com.library.feature.auth;

import com.library.domain.model.Staff;
import com.library.domain.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordRecoveryService passwordRecoveryService;
    private FakeEmailService emailService;
    private FixedOtpGeneratorHasher otpGeneratorHasher;

    @BeforeEach
    void setUp() {
        emailService = new FakeEmailService();
        otpGeneratorHasher = new FixedOtpGeneratorHasher();
        passwordRecoveryService = new PasswordRecoveryService(
                staffRepository,
                passwordEncoder,
                emailService,
                new OtpSessionService(),
                otpGeneratorHasher,
                new PasswordPolicyValidator()
        );
    }

    @Test
    void startReset_shouldStoreResetStateAndSendOtpEmail() {
        MockHttpSession session = new MockHttpSession();
        Staff staff = new Staff();
        staff.setUsername("student01");
        staff.setEmail("student01@example.com");

        when(staffRepository.findByUsername("student01")).thenReturn(Optional.of(staff));

        passwordRecoveryService.startReset("student01", session);

        assertThat(session.getAttribute(PasswordRecoveryService.RESET_USERNAME)).isEqualTo("student01");
        assertThat(session.getAttribute(PasswordRecoveryService.RESET_OTP_HASH)).isNotNull();
        assertThat(session.getAttribute(PasswordRecoveryService.RESET_OTP_VERIFIED)).isEqualTo(Boolean.FALSE);

        assertThat(emailService.lastTo).isEqualTo("student01@example.com");
        assertThat(emailService.lastOtp).isEqualTo("123456");
    }

    @Test
    void verifyOtp_withWrongCodeShouldIncreaseAttempts() {
        MockHttpSession session = new MockHttpSession();
        Staff staff = new Staff();
        staff.setUsername("student01");
        staff.setEmail("student01@example.com");

        when(staffRepository.findByUsername("student01")).thenReturn(Optional.of(staff));

        passwordRecoveryService.startReset("student01", session);

        assertThatThrownBy(() -> passwordRecoveryService.verifyOtp("000000", session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP");

        assertThat(session.getAttribute(PasswordRecoveryService.RESET_OTP_ATTEMPTS)).isEqualTo(1);
    }

    @Test
    void startReset_whenEmailSendingFailsShouldClearPendingResetState() {
        MockHttpSession session = new MockHttpSession();
        Staff staff = new Staff();
        staff.setUsername("student01");
        staff.setEmail("student01@example.com");

        when(staffRepository.findByUsername("student01")).thenReturn(Optional.of(staff));
        emailService.throwOnSend = true;

        assertThatThrownBy(() -> passwordRecoveryService.startReset("student01", session))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SMTP");

        assertThat(session.getAttribute(PasswordRecoveryService.RESET_USERNAME)).isNull();
        assertThat(session.getAttribute(PasswordRecoveryService.RESET_OTP_HASH)).isNull();
        assertThat(session.getAttribute(PasswordRecoveryService.RESET_OTP_VERIFIED)).isNull();
    }

    @Test
    void resetPassword_afterOtpVerificationShouldPersistEncodedPassword() {
        MockHttpSession session = new MockHttpSession();
        Staff staff = new Staff();
        staff.setUsername("student01");
        staff.setEmail("student01@example.com");

        when(staffRepository.findByUsername("student01")).thenReturn(Optional.of(staff));
        when(passwordEncoder.encode("Secure1")).thenReturn("encoded-password");

        passwordRecoveryService.startReset("student01", session);

        passwordRecoveryService.verifyOtp(emailService.lastOtp, session);
        passwordRecoveryService.resetPassword("Secure1", "Secure1", session);

        assertThat(staff.getPassword()).isEqualTo("encoded-password");
        assertThat(passwordRecoveryService.hasPendingReset(session)).isFalse();
        verify(staffRepository).save(any(Staff.class));
    }

    private static final class FakeEmailService extends EmailService {

        private String lastTo;
        private String lastOtp;
        private boolean throwOnSend;

        private FakeEmailService() {
            super(null, null);
        }

        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public void sendOtpEmail(String toEmail, String otpCode) {
            if (throwOnSend) {
                throw new IllegalStateException("SMTP unavailable");
            }
            this.lastTo = toEmail;
            this.lastOtp = otpCode;
        }
    }

    private static final class FixedOtpGeneratorHasher extends OtpGeneratorHasher {

        @Override
        public String generateOtp() {
            return "123456";
        }

        @Override
        public String sha256(String value) {
            return "hash:" + value;
        }
    }
}
