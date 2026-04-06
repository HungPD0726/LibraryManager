package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentStudentServiceTest {

    @Mock
    private StaffRepository staffRepository;

    private FakeStudentMirrorService studentMirrorService;
    private CurrentStudentService currentStudentService;

    @BeforeEach
    void setUp() {
        studentMirrorService = new FakeStudentMirrorService();
        currentStudentService = new CurrentStudentService(staffRepository, studentMirrorService);
    }

    @Test
    void findCurrentStaff_shouldNormalizeAuthenticationNameBeforeLookup() {
        Staff staff = new Staff();
        staff.setStaffId(10);
        staff.setUsername("student01");
        staff.setEmail("student01@gmail.com");

        when(staffRepository.findByUsername("student01@gmail.com")).thenReturn(Optional.empty());
        when(staffRepository.findByEmail("student01@gmail.com")).thenReturn(Optional.of(staff));

        Optional<Staff> resolved = currentStudentService.findCurrentStaff(
                new TestingAuthenticationToken(" Student01@Gmail.com ", "n/a", "ROLE_STUDENT")
        );

        assertThat(resolved).contains(staff);
        verify(staffRepository).findByUsername("student01@gmail.com");
        verify(staffRepository).findByEmail("student01@gmail.com");
    }

    @Test
    void resolveCurrentStudent_shouldCreateMirrorWhenRoleIsStudent() {
        Staff staff = new Staff();
        staff.setStaffId(10);
        staff.setUsername("student01");
        staff.setEmail("student01@gmail.com");

        Student student = new Student();
        student.setStudentId(10);
        studentMirrorService.willReturn(student);

        when(staffRepository.findByUsername("student01@gmail.com")).thenReturn(Optional.empty());
        when(staffRepository.findByEmail("student01@gmail.com")).thenReturn(Optional.of(staff));

        Optional<Student> resolved = currentStudentService.resolveCurrentStudent(
                new TestingAuthenticationToken("student01@gmail.com", "n/a", "ROLE_STUDENT")
        );

        assertThat(resolved).contains(student);
        assertThat(studentMirrorService.getLastStaff()).isSameAs(staff);
    }

    private static final class FakeStudentMirrorService extends StudentMirrorService {

        private Student nextStudent;
        private Staff lastStaff;

        private FakeStudentMirrorService() {
            super(null, null);
        }

        @Override
        public Student ensureStudentMirror(Staff staff) {
            lastStaff = staff;
            return nextStudent;
        }

        private void willReturn(Student student) {
            this.nextStudent = student;
        }

        private Staff getLastStaff() {
            return lastStaff;
        }
    }
}
