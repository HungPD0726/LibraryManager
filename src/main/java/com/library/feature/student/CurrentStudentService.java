package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StaffRepository;
import com.library.shared.support.RoleSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentStudentService {

    private final StaffRepository staffRepository;
    private final StudentMirrorService studentMirrorService;

    @Transactional(readOnly = true)
    public Optional<Staff> findCurrentStaff(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return staffRepository.findByUsername(authentication.getName());
    }

    @Transactional
    public Optional<Student> resolveCurrentStudent(Authentication authentication) {
        if (!RoleSupport.isStudent(authentication)) {
            return Optional.empty();
        }

        return findCurrentStaff(authentication)
                .map(studentMirrorService::ensureStudentMirror);
    }
}
