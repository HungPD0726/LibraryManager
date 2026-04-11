package com.library.feature.auth;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.staff.StaffService;
import com.library.feature.student.CurrentStudentService;
import com.library.feature.student.StudentMirrorService;
import com.library.shared.config.CommonModelAdvice;
import com.library.shared.config.SecurityConfig;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {AuthController.class, GoogleOAuthFallbackController.class},
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, AuthControllerTest.TestBeans.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestBeans.FakeStaffService staffService;

    @Autowired
    private TestBeans.FakeStudentMirrorService studentMirrorService;

    @Autowired
    private TestBeans.FakeCurrentStudentService currentStudentService;

    @BeforeEach
    void setUp() {
        staffService.reset();
        studentMirrorService.reset();
        currentStudentService.reset();
    }

    @Test
    void loginPage_shouldExposeResetMessage() throws Exception {
        mockMvc.perform(get("/login").param("reset", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void googleAuthorization_shouldRedirectBackToLoginWhenOauthUnavailable() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void loginSuccess_shouldRedirectAdminToDashboard() throws Exception {
        mockMvc.perform(get("/login/success")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin01").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void loginSuccess_shouldRedirectStudentToHome() throws Exception {
        Staff currentStaff = new Staff();
        currentStaff.setStaffId(11);
        currentStudentService.willFindStaff(currentStaff);
        studentMirrorService.willReturn(new Student());

        mockMvc.perform(get("/login/success")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        assertThat(studentMirrorService.getEnsureMirrorCalls()).isEqualTo(1);
        assertThat(studentMirrorService.getLastStaff()).isSameAs(currentStaff);
    }

    @Test
    void loginSuccess_shouldRedirectStudentBackToLoginWhenProfileCannotBeResolved() throws Exception {
        currentStudentService.willFindStaff(null);

        mockMvc.perform(get("/login/success")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void register_shouldReturnFormWhenValidationFails() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("staffName", "Nguyen Van A")
                        .param("username", "student01")
                        .param("email", "invalid-email")
                        .param("password", "Secure1")
                        .param("confirmPassword", "Secure1"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("form", "email"));

        assertThat(staffService.getRegistrationCalls()).isZero();
        assertThat(studentMirrorService.getEnsureMirrorCalls()).isZero();
    }

    @Test
    void register_shouldReturnFormWhenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("staffName", "Nguyen Van A")
                        .param("username", "student01")
                        .param("email", "student01@example.com")
                        .param("password", "Secure1")
                        .param("confirmPassword", "Different1"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("form", "confirmPassword"));

        assertThat(staffService.getRegistrationCalls()).isZero();
        assertThat(studentMirrorService.getEnsureMirrorCalls()).isZero();
    }

    @Test
    void register_shouldCreateStudentAccountAndMirrorOnSuccess() throws Exception {
        Staff created = new Staff();
        created.setStaffId(7);
        created.setUsername("student01");

        staffService.willCreate(created);
        studentMirrorService.willReturn(new Student());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("staffName", "Nguyen Van A")
                        .param("username", "student01")
                        .param("email", "student01@example.com")
                        .param("password", "Secure1")
                        .param("confirmPassword", "Secure1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("message"));

        assertThat(staffService.getRegistrationCalls()).isEqualTo(1);
        assertThat(staffService.getLastFullName()).isEqualTo("Nguyen Van A");
        assertThat(staffService.getLastUsername()).isEqualTo("student01");
        assertThat(staffService.getLastEmail()).isEqualTo("student01@example.com");
        assertThat(staffService.getLastPassword()).isEqualTo("Secure1");
        assertThat(studentMirrorService.getEnsureMirrorCalls()).isEqualTo(1);
        assertThat(studentMirrorService.getLastStaff()).isSameAs(created);
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
                    new FakeStudentMirrorService()
            );
        }

        @Bean
        FakeStaffService staffService() {
            return new FakeStaffService();
        }

        @Bean
        FakeStudentMirrorService studentMirrorService() {
            return new FakeStudentMirrorService();
        }

        @Bean
        FakeCurrentStudentService currentStudentService() {
            return new FakeCurrentStudentService();
        }

        static class FakeStaffService extends StaffService {

            private Staff nextCreated;
            private int registrationCalls;
            private String lastFullName;
            private String lastUsername;
            private String lastEmail;
            private String lastPassword;

            FakeStaffService() {
                super(null, null, null);
            }

            @Override
            public Staff registerStudentAccount(String fullName, String username, String email, String rawPassword) {
                registrationCalls++;
                lastFullName = fullName;
                lastUsername = username;
                lastEmail = email;
                lastPassword = rawPassword;
                return nextCreated;
            }

            void willCreate(Staff staff) {
                this.nextCreated = staff;
            }

            void reset() {
                this.nextCreated = null;
                this.registrationCalls = 0;
                this.lastFullName = null;
                this.lastUsername = null;
                this.lastEmail = null;
                this.lastPassword = null;
            }

            int getRegistrationCalls() {
                return registrationCalls;
            }

            String getLastFullName() {
                return lastFullName;
            }

            String getLastUsername() {
                return lastUsername;
            }

            String getLastEmail() {
                return lastEmail;
            }

            String getLastPassword() {
                return lastPassword;
            }
        }

        static class FakeStudentMirrorService extends StudentMirrorService {

            private Student nextStudent;
            private Staff lastStaff;
            private int ensureMirrorCalls;

            FakeStudentMirrorService() {
                super(null, null);
            }

            @Override
            public Student ensureStudentMirror(Staff staff) {
                ensureMirrorCalls++;
                lastStaff = staff;
                return nextStudent != null ? nextStudent : new Student();
            }

            void willReturn(Student student) {
                this.nextStudent = student;
            }

            void reset() {
                this.nextStudent = null;
                this.lastStaff = null;
                this.ensureMirrorCalls = 0;
            }

            int getEnsureMirrorCalls() {
                return ensureMirrorCalls;
            }

            Staff getLastStaff() {
                return lastStaff;
            }
        }

        static class FakeCurrentStudentService extends CurrentStudentService {

            private Staff currentStaff;

            FakeCurrentStudentService() {
                super(null, null);
            }

            @Override
            public Optional<Staff> findCurrentStaff(Authentication authentication) {
                return Optional.ofNullable(currentStaff);
            }

            void willFindStaff(Staff staff) {
                this.currentStaff = staff;
            }

            void reset() {
                this.currentStaff = null;
            }
        }
    }
}
