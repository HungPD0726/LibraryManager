package com.library.feature.auth;

import com.library.domain.model.Staff;
import com.library.domain.repository.StaffRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    public CustomUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        Staff staff = staffRepository.findByUsername(normalized)
                .or(() -> staffRepository.findByEmail(normalized))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();
        staff.getRoles().forEach(role -> {
            String roleName = role.getRoleName() == null ? "" : role.getRoleName().trim().toUpperCase(Locale.ROOT);
            if (roleName.startsWith("ROLE_")) {
                authorities.add(new SimpleGrantedAuthority(roleName));
            } else if (!roleName.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            }
        });

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }

        return new org.springframework.security.core.userdetails.User(
                staff.getUsername(),
                staff.getPassword() != null ? staff.getPassword() : "",
                authorities
        );
    }
}
