package com.library.shared.config;

import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.feature.auth.CustomOAuth2UserService;
import com.library.feature.auth.CustomUserDetailsService;
import com.library.feature.student.StudentMirrorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestSecurityEndpoints.class,
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, SecurityRoutingTest.TestBeans.class})
class SecurityRoutingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPage_shouldBePublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void home_shouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void home_shouldAllowStudentRole() throws Exception {
        mockMvc.perform(get("/home")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void chatbot_shouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/chatbot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void chatbot_shouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void adminOrders_shouldRejectStudentRole() throws Exception {
        mockMvc.perform(get("/admin/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOrders_shouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/admin/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin01").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void pendingCount_shouldOnlyBeVisibleToAdminOrStaff() throws Exception {
        mockMvc.perform(get("/api/pending-count")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pending-count")
                        .with(SecurityMockMvcRequestPostProcessors.user("staff01").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(0));
    }

    @Test
    void chatbotPost_shouldAllowAuthenticatedUserWithoutCsrf() throws Exception {
        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"Xin chào"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("ok"));
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

        static class NoOpStudentMirrorService extends StudentMirrorService {

            NoOpStudentMirrorService() {
                super(null, null);
            }

            @Override
            public Student ensureStudentMirror(Staff staff) {
                return new Student();
            }
        }
    }
}
