package com.library.feature.auth;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.student.StudentMirrorService;
import com.library.shared.config.CommonModelAdvice;
import com.library.shared.config.SecurityConfig;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = PasswordRecoveryController.class,
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, PasswordRecoveryControllerTest.TestBeans.class})
class PasswordRecoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestBeans.FakePasswordRecoveryService passwordRecoveryService;

    @BeforeEach
    void setUp() {
        passwordRecoveryService.reset();
    }

    @Test
    void forgotPasswordPage_shouldBePublic() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void forgotPassword_shouldRedirectToVerifyOtpWhenOtpIsSent() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("identity", "student01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verify-otp"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void forgotPassword_shouldRenderExplicitMailConfigErrorWhenMailConfigIsMissing() throws Exception {
        passwordRecoveryService.failStartReset("Thiếu cấu hình MAIL_USERNAME và MAIL_PASSWORD.");

        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("identity", "student01"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(model().attribute("error", "Thiếu cấu hình MAIL_USERNAME và MAIL_PASSWORD."));
    }

    @Test
    void verifyOtp_shouldRedirectToForgotPasswordWhenSessionWasClearedByService() throws Exception {
        passwordRecoveryService.startPendingReset("student01");
        passwordRecoveryService.failVerifyAndClear("OTP da het han. Vui long yeu cau ma moi.");

        mockMvc.perform(post("/verify-otp")
                        .with(csrf())
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password"))
                .andExpect(flash().attribute("error", "OTP da het han. Vui long yeu cau ma moi."));
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            StaffRepository repository = Mockito.mock(StaffRepository.class);
            return new CustomUserDetailsService(repository);
        }

        @Bean
        CustomOAuth2UserService customOAuth2UserService() {
            return new CustomOAuth2UserService(
                    Mockito.mock(StaffRepository.class),
                    Mockito.mock(RoleRepository.class),
                    new NoOpStudentMirrorService()
            );
        }

        @Bean
        FakePasswordRecoveryService passwordRecoveryService() {
            return new FakePasswordRecoveryService();
        }

        static class NoOpStudentMirrorService extends StudentMirrorService {

            NoOpStudentMirrorService() {
                super(null, null);
            }

            @Override
            public Student ensureStudentMirror(Staff staff) {
                return new Student();
            }
        }

        static class FakePasswordRecoveryService extends PasswordRecoveryService {

            private boolean pendingReset;
            private boolean verified;
            private String pendingUsername;
            private RuntimeException nextStartResetException;
            private RuntimeException nextVerifyOtpException;
            private boolean clearPendingResetOnVerifyFailure;

            FakePasswordRecoveryService() {
                super(null, null, null, null, null, null);
            }

            @Override
            public void startReset(String identity, HttpSession session) {
                if (nextStartResetException != null) {
                    throw nextStartResetException;
                }
                pendingReset = true;
                pendingUsername = identity;
            }

            @Override
            public void verifyOtp(String otp, HttpSession session) {
                if (nextVerifyOtpException != null) {
                    if (clearPendingResetOnVerifyFailure) {
                        pendingReset = false;
                        pendingUsername = null;
                    }
                    throw nextVerifyOtpException;
                }
                verified = true;
            }

            @Override
            public void resetPassword(String password, String confirmPassword, HttpSession session) {
            }

            @Override
            public boolean hasPendingReset(HttpSession session) {
                return pendingReset;
            }

            @Override
            public boolean isVerified(HttpSession session) {
                return verified;
            }

            @Override
            public String getPendingUsername(HttpSession session) {
                return pendingUsername;
            }

            void startPendingReset(String username) {
                this.pendingReset = true;
                this.pendingUsername = username;
            }

            void failStartReset(String message) {
                this.nextStartResetException = new IllegalStateException(message);
            }

            void failVerifyAndClear(String message) {
                this.nextVerifyOtpException = new IllegalArgumentException(message);
                this.clearPendingResetOnVerifyFailure = true;
            }

            void reset() {
                this.pendingReset = false;
                this.verified = false;
                this.pendingUsername = null;
                this.nextStartResetException = null;
                this.nextVerifyOtpException = null;
                this.clearPendingResetOnVerifyFailure = false;
            }
        }
    }
}
