package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.auth.CustomOAuth2UserService;
import com.library.feature.auth.CustomUserDetailsService;
import com.library.feature.staff.StaffService;
import com.library.shared.config.CurrentUserModelService;
import com.library.shared.config.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(controllers = StudentProfileController.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@Import({SecurityConfig.class, StudentProfileControllerTest.TestBeans.class})
class StudentProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestBeans.FakeCurrentStudentService currentStudentService;

    @Autowired
    private TestBeans.FakeStudentProfileService studentProfileService;

    @Autowired
    private TestBeans.FakeAvatarStorageService avatarStorageService;

    @Autowired
    private TestBeans.FakeCurrentUserModelService currentUserModelService;

    @BeforeEach
    void setUp() {
        Student student = student();
        Staff staff = staff();

        currentStudentService.configure(staff, student);
        studentProfileService.reset();
        avatarStorageService.reset();
        currentUserModelService.configure(student);
    }

    @Test
    void profile_shouldRenderAvatarInHeaderAndUploadField() throws Exception {
        mockMvc.perform(get("/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("student/profile"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(content().string(containsString("/uploads/avatars/minh.png")))
                .andExpect(content().string(containsString("name=\"avatarFile\"")));
    }

    @Test
    void updateProfile_shouldStoreAvatarAndRedirect() throws Exception {
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        avatarStorageService.willStore("/uploads/avatars/student-7-new.png");
        studentProfileService.willReturn(student());

        mockMvc.perform(multipart("/profile")
                        .file(avatarFile)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .param("studentName", "Nguyen Minh")
                        .param("email", "minh@example.com")
                        .param("phone", "0901234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("msg"));

        assertThat(avatarStorageService.getLastStudentId()).isEqualTo(7);
        assertThat(avatarStorageService.getLastCurrentAvatarUrl()).isEqualTo("/uploads/avatars/minh.png");
        assertThat(studentProfileService.getLastAvatarUrl()).isEqualTo("/uploads/avatars/student-7-new.png");
        assertThat(studentProfileService.getLastForm()).isNotNull();
        assertThat(studentProfileService.getLastForm().getStudentName()).isEqualTo("Nguyen Minh");
        assertThat(studentProfileService.getLastForm().getEmail()).isEqualTo("minh@example.com");
        assertThat(studentProfileService.getLastForm().getPhone()).isEqualTo("0901234567");
    }

    @Test
    void updateProfile_shouldReturnProfileWhenAvatarUploadFails() throws Exception {
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile",
                "avatar.txt",
                "text/plain",
                new byte[]{1, 2, 3}
        );

        avatarStorageService.willThrow(new IllegalArgumentException("Only image files are allowed."));

        mockMvc.perform(multipart("/profile")
                        .file(avatarFile)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .param("studentName", "Nguyen Minh")
                        .param("email", "minh@example.com")
                        .param("phone", "0901234567"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/profile"))
                .andExpect(model().attributeExists("avatarError"))
                .andExpect(content().string(containsString("Only image files are allowed.")))
                .andExpect(content().string(containsString("/uploads/avatars/minh.png")));

        assertThat(studentProfileService.getLastForm()).isNull();
    }

    private static Staff staff() {
        Staff staff = new Staff();
        staff.setStaffId(7);
        staff.setUsername("student01");
        staff.setEmail("minh@example.com");
        staff.setPassword("encoded-password");
        return staff;
    }

    private static Student student() {
        Student student = new Student();
        student.setStudentId(7);
        student.setStudentName("Nguyen Minh");
        student.setEmail("minh@example.com");
        student.setPhone("0901234567");
        student.setAvatarUrl("/uploads/avatars/minh.png");
        return student;
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

        @Bean
        FakeCurrentStudentService currentStudentService() {
            return new FakeCurrentStudentService();
        }

        @Bean
        FakeStudentProfileService studentProfileService() {
            return new FakeStudentProfileService();
        }

        @Bean
        FakeAvatarStorageService avatarStorageService() {
            return new FakeAvatarStorageService();
        }

        @Bean
        FakeStaffService staffService() {
            return new FakeStaffService();
        }

        @Bean
        FakeCurrentUserModelService currentUserModelService() {
            return new FakeCurrentUserModelService();
        }

        static class FakeCurrentStudentService extends CurrentStudentService {

            private Staff currentStaff;
            private Student currentStudent;

            FakeCurrentStudentService() {
                super(null, null);
            }

            @Override
            public Optional<Staff> findCurrentStaff(Authentication authentication) {
                return Optional.ofNullable(currentStaff);
            }

            @Override
            public Optional<Student> resolveCurrentStudent(Authentication authentication) {
                return Optional.ofNullable(currentStudent);
            }

            void configure(Staff staff, Student student) {
                this.currentStaff = staff;
                this.currentStudent = student;
            }
        }

        static class FakeStudentProfileService extends StudentProfileService {

            private ProfileForm lastForm;
            private String lastAvatarUrl;
            private Student nextStudent;

            FakeStudentProfileService() {
                super(null, null, null);
            }

            @Override
            public Student updateProfile(Staff staff, ProfileForm form, String avatarUrl) {
                this.lastForm = form;
                this.lastAvatarUrl = avatarUrl;
                return nextStudent != null ? nextStudent : student();
            }

            void willReturn(Student student) {
                this.nextStudent = student;
            }

            void reset() {
                this.lastForm = null;
                this.lastAvatarUrl = null;
                this.nextStudent = null;
            }

            ProfileForm getLastForm() {
                return lastForm;
            }

            String getLastAvatarUrl() {
                return lastAvatarUrl;
            }
        }

        static class FakeAvatarStorageService extends AvatarStorageService {

            private Integer lastStudentId;
            private String lastCurrentAvatarUrl;
            private RuntimeException nextException;
            private String nextStoredUrl;

            FakeAvatarStorageService() {
                super("target/test-uploads");
            }

            @Override
            public String storeStudentAvatar(Integer studentId, MultipartFile file, String currentAvatarUrl) {
                this.lastStudentId = studentId;
                this.lastCurrentAvatarUrl = currentAvatarUrl;
                if (nextException != null) {
                    throw nextException;
                }
                return nextStoredUrl != null ? nextStoredUrl : currentAvatarUrl;
            }

            void willStore(String avatarUrl) {
                this.nextException = null;
                this.nextStoredUrl = avatarUrl;
            }

            void willThrow(RuntimeException exception) {
                this.nextException = exception;
            }

            void reset() {
                this.lastStudentId = null;
                this.lastCurrentAvatarUrl = null;
                this.nextException = null;
                this.nextStoredUrl = null;
            }

            Integer getLastStudentId() {
                return lastStudentId;
            }

            String getLastCurrentAvatarUrl() {
                return lastCurrentAvatarUrl;
            }
        }

        static class FakeStaffService extends StaffService {

            FakeStaffService() {
                super(null, null, null);
            }
        }

        static class FakeCurrentUserModelService extends CurrentUserModelService {

            private Student currentStudent;

            FakeCurrentUserModelService() {
                super(null, null, null);
            }

            @Override
            public void enrichModel(Model model, Authentication authentication, HttpServletRequest request) {
                model.addAttribute("requestPath", request.getRequestURI());
                model.addAttribute("currentUsername", authentication != null ? authentication.getName() : null);
                model.addAttribute("isAdmin", false);
                model.addAttribute("isStaff", false);
                model.addAttribute("isStudent", true);
                model.addAttribute("unreadNotifications", 0L);
                if (currentStudent != null) {
                    model.addAttribute("currentStudent", currentStudent);
                }
            }

            void configure(Student student) {
                this.currentStudent = student;
            }
        }

        static class NoOpStudentMirrorService extends StudentMirrorService {

            NoOpStudentMirrorService() {
                super(null, null);
            }
        }
    }
}
