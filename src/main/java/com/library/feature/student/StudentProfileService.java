package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StaffRepository;
import com.library.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final StudentMirrorService studentMirrorService;

    @Transactional
    public Student updateProfile(Staff staff, ProfileForm form, String avatarUrl) {
        Student student = studentMirrorService.ensureStudentMirror(staff);
        student.setStudentName(form.getStudentName().trim());
        student.setEmail(normalizeEmail(form.getEmail()));
        student.setPhone(normalizePlainText(form.getPhone()));
        student.setAvatarUrl(normalizePlainText(avatarUrl != null ? avatarUrl : form.getAvatarUrl()));
        Student savedStudent = studentRepository.save(student);

        String normalizedEmail = normalizeEmail(form.getEmail());
        if (normalizedEmail != null && !normalizedEmail.equalsIgnoreCase(staff.getEmail())) {
            staff.setEmail(normalizedEmail);
            staffRepository.save(staff);
        }

        return savedStudent;
    }

    public String buildDisplayName(Staff staff, Student student) {
        if (student != null && StringUtils.hasText(student.getStudentName())) {
            return student.getStudentName().trim();
        }
        if (staff != null && StringUtils.hasText(staff.getStaffName())) {
            return staff.getStaffName().trim();
        }
        if (staff != null && StringUtils.hasText(staff.getUsername())) {
            return staff.getUsername().trim();
        }
        return "Sinh viên thư viện";
    }

    private String normalizeEmail(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePlainText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
