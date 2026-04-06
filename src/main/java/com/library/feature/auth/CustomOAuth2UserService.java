package com.library.feature.auth;

import com.library.domain.model.Role;
import com.library.domain.model.Staff;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.student.StudentMirrorService;
import com.library.shared.support.RoleSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final StudentMirrorService studentMirrorService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = normalize((String) attributes.get("email"));

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Email not found from Google");
        }

        Staff staff = staffRepository.findByEmail(email)
                .orElseGet(() -> createNewGoogleUser(attributes, email));

        Set<GrantedAuthority> authorities = buildAuthorities(staff);
        if (authorities.stream().anyMatch(authority -> RoleSupport.ROLE_STUDENT.equals(authority.getAuthority()))) {
            studentMirrorService.ensureStudentMirror(staff);
        }

        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private Set<GrantedAuthority> buildAuthorities(Staff staff) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        staff.getRoles().forEach(role -> {
            String roleName = normalizeRole(role.getRoleName());
            if (roleName != null) {
                authorities.add(new SimpleGrantedAuthority(roleName));
            }
        });

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(RoleSupport.ROLE_STUDENT));
        }
        return authorities;
    }

    private Staff createNewGoogleUser(Map<String, Object> attributes, String email) {
        String name = (String) attributes.get("name");

        Staff staff = new Staff();
        staff.setEmail(email);
        staff.setUsername(resolveUniqueUsername(email));
        staff.setStaffName(StringUtils.hasText(name) ? name.trim() : staff.getUsername());
        staff.setRoles(resolveStudentRoles());

        return staffRepository.save(staff);
    }

    private Set<Role> resolveStudentRoles() {
        Set<Role> roles = new HashSet<>();
        roleRepository.findByRoleName("STUDENT")
                .or(() -> roleRepository.findByRoleName("ROLE_STUDENT"))
                .ifPresent(roles::add);
        return roles;
    }

    private String resolveUniqueUsername(String email) {
        String localPart = email.split("@")[0];
        String sanitizedBase = localPart.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        String base = StringUtils.hasText(sanitizedBase) ? sanitizedBase : "student";
        String candidate = base;
        int suffix = 1;

        while (staffRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            return null;
        }
        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
