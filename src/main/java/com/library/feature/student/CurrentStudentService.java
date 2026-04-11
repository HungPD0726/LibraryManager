package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StaffRepository;
import com.library.domain.repository.StudentRepository;
import com.library.shared.support.RoleSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentStudentService {

    private final StaffRepository staffRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public Optional<Staff> findCurrentStaff(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String identifier = normalize(authentication.getName());
        if (!StringUtils.hasText(identifier)) {
            return Optional.empty();
        }

        return staffRepository.findByUsername(identifier)
                .or(() -> staffRepository.findByEmail(identifier));
    }

    @Transactional(readOnly = true)
    public Optional<Student> resolveCurrentStudent(Authentication authentication) {
        if (!RoleSupport.isStudent(authentication)) {
            return Optional.empty();
        }

        return findCurrentStaff(authentication)
                .flatMap(staff -> studentRepository.findById(staff.getStaffId()));
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
