package com.library.feature.auth;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PasswordPolicyValidator {

    public void validate(String password, String confirmPassword) {
        if (!StringUtils.hasText(password) || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự, gồm chữ hoa, chữ thường và số.");
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char item : password.toCharArray()) {
            if (Character.isUpperCase(item)) {
                hasUpper = true;
            } else if (Character.isLowerCase(item)) {
                hasLower = true;
            } else if (Character.isDigit(item)) {
                hasDigit = true;
            }
        }
        return hasUpper && hasLower && hasDigit;
    }
}
