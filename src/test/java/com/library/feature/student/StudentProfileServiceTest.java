package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StaffRepository;
import com.library.domain.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;

    private StudentProfileService studentProfileService;
    private FakeStudentMirrorService studentMirrorService;

    @BeforeEach
    void setUp() {
        studentMirrorService = new FakeStudentMirrorService();
        studentProfileService = new StudentProfileService(studentRepository, staffRepository, studentMirrorService);
    }

    @Test
    void updateProfile_shouldNormalizeFieldsAndSyncStaffEmail() {
        Staff staff = new Staff();
        staff.setStaffId(5);
        staff.setEmail("old@example.com");
        staff.setUsername("student01");

        Student student = new Student();
        student.setStudentId(5);

        ProfileForm form = new ProfileForm();
        form.setStudentName("  Nguyen Van A  ");
        form.setEmail("  NEW@Example.com  ");
        form.setPhone("  0901234567 ");

        studentMirrorService.willReturn(student);
        when(studentRepository.save(student)).thenReturn(student);
        when(staffRepository.save(staff)).thenReturn(staff);

        Student updated = studentProfileService.updateProfile(staff, form);

        assertThat(updated).isSameAs(student);
        assertThat(student.getStudentName()).isEqualTo("Nguyen Van A");
        assertThat(student.getEmail()).isEqualTo("new@example.com");
        assertThat(student.getPhone()).isEqualTo("0901234567");
        assertThat(staff.getEmail()).isEqualTo("new@example.com");
        verify(studentRepository).save(student);
        verify(staffRepository).save(staff);
    }

    @Test
    void buildDisplayName_shouldPreferStudentNameThenStaffNameThenUsername() {
        Staff staff = new Staff();
        staff.setStaffName("Staff Name");
        staff.setUsername("staff01");

        Student student = new Student();
        student.setStudentName("Student Name");

        assertThat(studentProfileService.buildDisplayName(staff, student)).isEqualTo("Student Name");

        student.setStudentName("   ");
        assertThat(studentProfileService.buildDisplayName(staff, student)).isEqualTo("Staff Name");

        staff.setStaffName("   ");
        assertThat(studentProfileService.buildDisplayName(staff, student)).isEqualTo("staff01");
    }

    private static final class FakeStudentMirrorService extends StudentMirrorService {

        private Student nextStudent;

        private FakeStudentMirrorService() {
            super(null, null);
        }

        @Override
        public Student ensureStudentMirror(Staff staff) {
            return nextStudent;
        }

        private void willReturn(Student student) {
            this.nextStudent = student;
        }
    }
}
