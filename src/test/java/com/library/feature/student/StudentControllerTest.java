package com.library.feature.student;

import com.library.domain.model.Student;
import com.library.domain.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentController studentController;

    @BeforeEach
    void setUp() {
        studentController = new StudentController(new StudentService(studentRepository));
    }

    @Test
    void create_shouldSetLocalizedFlashMessage() {
        StudentForm form = new StudentForm();
        form.setStudentName("Nguyễn Minh");
        form.setEmail("minh@example.com");
        form.setPhone("0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = studentController.create(form, bindingResult, new ExtendedModelMap(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/students");
        assertThat(redirectAttributes.getFlashAttributes().get("msg")).isEqualTo("Thêm sinh viên thành công.");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void edit_shouldThrowLocalizedMessageWhenStudentIsMissing() {
        StudentForm form = new StudentForm();
        form.setStudentName("Nguyễn Minh");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        when(studentRepository.findById(11)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentController.edit(11, form, bindingResult, new ExtendedModelMap(), new RedirectAttributesModelMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không tìm thấy sinh viên cần cập nhật.");
    }
}
