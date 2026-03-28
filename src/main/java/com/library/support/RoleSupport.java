package com.library.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public final class RoleSupport {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_STAFF = "ROLE_STAFF";
    public static final String ROLE_LIBRARIAN = "ROLE_LIBRARIAN";
    public static final String ROLE_STUDENT = "ROLE_STUDENT";

    private RoleSupport() {
    }

    public static boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, ROLE_ADMIN);
    }

    public static boolean isStaff(Authentication authentication) {
        return hasRole(authentication, ROLE_STAFF) || hasRole(authentication, ROLE_LIBRARIAN);
    }

    public static boolean isStudent(Authentication authentication) {
        return hasRole(authentication, ROLE_STUDENT);
    }

    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
