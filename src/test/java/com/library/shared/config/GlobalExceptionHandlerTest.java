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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ThrowingPageController.class,
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, GlobalExceptionHandlerTest.TestBeans.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pageException_shouldRedirectAndKeepFlashMessage() throws Exception {
        mockMvc.perform(get("/test/error")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Trang mẫu bị lỗi."));
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
