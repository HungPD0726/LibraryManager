package com.library.feature.catalog;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.auth.CustomOAuth2UserService;
import com.library.feature.auth.CustomUserDetailsService;
import com.library.feature.student.StudentMirrorService;
import com.library.shared.config.CommonModelAdvice;
import com.library.shared.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class,
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, BookCoverSuggestionControllerTest.TestBeans.class})
class BookCoverSuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private PublisherService publisherService;
    @MockBean
    private AuthorService authorService;
    @MockBean
    private OpenLibraryCoverService openLibraryCoverService;

    @Test
    void coverSuggestions_shouldAllowAdminRole() throws Exception {
        when(openLibraryCoverService.suggestCovers("Dune", "Frank Herbert"))
                .thenReturn(List.of(new OpenLibraryCoverService.CoverSuggestion(
                        "Dune",
                        "Frank Herbert",
                        "https://covers.openlibrary.org/b/id/8100927-M.jpg?default=false",
                        "https://covers.openlibrary.org/b/id/8100927-L.jpg?default=false",
                        "https://openlibrary.org/works/OL893415W"
                )));

        mockMvc.perform(get("/admin/books/cover-suggestions")
                        .param("title", "Dune")
                        .param("author", "Frank Herbert")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin01").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[0].authors").value("Frank Herbert"))
                .andExpect(jsonPath("$[0].previewUrl").value("https://covers.openlibrary.org/b/id/8100927-M.jpg?default=false"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://covers.openlibrary.org/b/id/8100927-L.jpg?default=false"))
                .andExpect(jsonPath("$[0].openLibraryUrl").value("https://openlibrary.org/works/OL893415W"));
    }

    @Test
    void coverSuggestions_shouldAllowStaffRole() throws Exception {
        when(openLibraryCoverService.suggestCovers("Dune", null)).thenReturn(List.of());

        mockMvc.perform(get("/admin/books/cover-suggestions")
                        .param("title", "Dune")
                        .with(SecurityMockMvcRequestPostProcessors.user("staff01").roles("STAFF")))
                .andExpect(status().isOk());
    }

    @Test
    void coverSuggestions_shouldRejectStudentRole() throws Exception {
        mockMvc.perform(get("/admin/books/cover-suggestions")
                        .param("title", "Dune")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(openLibraryCoverService);
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            return new CustomUserDetailsService(Mockito.mock(StaffRepository.class));
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
