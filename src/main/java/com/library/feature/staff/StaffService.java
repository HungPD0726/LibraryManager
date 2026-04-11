package com.library.feature.staff;

import com.library.domain.model.Role;
import com.library.domain.model.Staff;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<Staff> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("staffId").descending());
        return staffRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Staff> findById(Integer id) {
        return staffRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Staff> findByUsername(String username) {
        return staffRepository.findByUsername(normalizeNullable(username));
    }

    @Transactional(readOnly = true)
    public Optional<Staff> findByEmail(String email) {
        return staffRepository.findByEmail(normalizeNullable(email));
    }

    @Transactional
    public Staff createStaff(Staff staff, String rawPassword, List<Integer> roleIds) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }

        normalize(staff);
        staff.setPassword(passwordEncoder.encode(rawPassword));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            staff.setRoles(Set.copyOf(roles));
        }
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updateStaff(Staff staff, String rawPassword, List<Integer> roleIds) {
        normalize(staff);
        if (StringUtils.hasText(rawPassword)) {
            staff.setPassword(passwordEncoder.encode(rawPassword));
        }
        if (roleIds != null) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            staff.setRoles(Set.copyOf(roles));
        }
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff registerStudentAccount(String fullName, String username, String email, String rawPassword) {
        String normalizedUsername = normalizeNullable(username);
        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (staffRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }

        String normalizedEmail = normalizeNullable(email);
        if (normalizedEmail != null && staffRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        Staff staff = new Staff();
        staff.setStaffName(fullName);
        staff.setUsername(username);
        staff.setEmail(email);
        staff.setPassword(passwordEncoder.encode(rawPassword));
        staff.setRoles(Set.of(resolveDefaultStudentRole()));
        normalize(staff);
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updatePassword(Staff staff, String rawPassword) {
        staff.setPassword(passwordEncoder.encode(rawPassword));
        return staffRepository.save(staff);
    }

    public void deleteById(Integer id) {
        staffRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return staffRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RoleOptionView> findAllRoleOptions() {
        return roleRepository.findAllOptions();
    }

    private Role resolveDefaultStudentRole() {
        return roleRepository.findAll().stream()
                .filter(role -> role.getRoleName() != null)
                .filter(role -> {
                    String name = role.getRoleName().trim();
                    return name.equalsIgnoreCase("student") || name.equalsIgnoreCase("role_student");
                })
                .findFirst()
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("STUDENT");
                    return roleRepository.save(role);
                });
    }

    private void normalize(Staff staff) {
        if (staff.getStaffName() != null) {
            staff.setStaffName(staff.getStaffName().trim());
        }
        staff.setUsername(normalizeNullable(staff.getUsername()));
        staff.setEmail(normalizeNullable(staff.getEmail()));
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
