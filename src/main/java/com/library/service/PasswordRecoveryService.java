package com.library.service;

import com.library.entity.Staff;
import com.library.repository.StaffRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    public static final String RESET_USERNAME = "resetUsername";
    public static final String RESET_OTP_HASH = "resetOtpHash";
    public static final String RESET_OTP_EXPIRES_AT = "resetOtpExpiresAt";
    public static final String RESET_OTP_ATTEMPTS = "resetOtpAttempts";
    public static final String RESET_OTP_VERIFIED = "resetOtpVerified";

    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration OTP_TTL = Duration.ofMinutes(10);

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void startReset(String identity, HttpSession session) {
        if (!emailService.isConfigured()) {
            throw new IllegalStateException("Hệ thống email chưa được cấu hình.");
        }

        Staff staff = findStaff(identity)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản phù hợp."));

        String recoveryEmail = StringUtils.hasText(staff.getEmail()) ? staff.getEmail().trim() : null;
        if (!StringUtils.hasText(recoveryEmail)) {
            throw new IllegalArgumentException("Tài khoản này chưa có email để nhận OTP.");
        }

        String otp = generateOtp();
        clear(session);
        session.setAttribute(RESET_USERNAME, staff.getUsername());
        session.setAttribute(RESET_OTP_HASH, sha256(otp));
        session.setAttribute(RESET_OTP_EXPIRES_AT, System.currentTimeMillis() + OTP_TTL.toMillis());
        session.setAttribute(RESET_OTP_ATTEMPTS, 0);
        session.setAttribute(RESET_OTP_VERIFIED, Boolean.FALSE);

        emailService.sendOtpEmail(recoveryEmail, otp);
    }

    public void verifyOtp(String otp, HttpSession session) {
        ensurePendingReset(session);

        long expiresAt = Optional.ofNullable((Long) session.getAttribute(RESET_OTP_EXPIRES_AT)).orElse(0L);
        if (System.currentTimeMillis() > expiresAt) {
            clear(session);
            throw new IllegalArgumentException("OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        int attempts = Optional.ofNullable((Integer) session.getAttribute(RESET_OTP_ATTEMPTS)).orElse(0);
        if (attempts >= MAX_ATTEMPTS) {
            clear(session);
            throw new IllegalArgumentException("Bạn đã nhập sai quá số lần cho phép.");
        }

        String expectedHash = (String) session.getAttribute(RESET_OTP_HASH);
        if (!sha256(otp).equals(expectedHash)) {
            session.setAttribute(RESET_OTP_ATTEMPTS, attempts + 1);
            throw new IllegalArgumentException("OTP không đúng. Bạn còn " + (MAX_ATTEMPTS - attempts - 1) + " lần thử.");
        }

        session.setAttribute(RESET_OTP_VERIFIED, Boolean.TRUE);
        session.removeAttribute(RESET_OTP_HASH);
        session.removeAttribute(RESET_OTP_EXPIRES_AT);
        session.removeAttribute(RESET_OTP_ATTEMPTS);
    }

    public void resetPassword(String password, String confirmPassword, HttpSession session) {
        ensureVerified(session);

        if (!StringUtils.hasText(password) || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự, gồm chữ hoa, chữ thường và số.");
        }

        String username = (String) session.getAttribute(RESET_USERNAME);
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản để đặt lại mật khẩu."));

        staff.setPassword(passwordEncoder.encode(password));
        staffRepository.save(staff);
        clear(session);
    }

    public boolean hasPendingReset(HttpSession session) {
        return session != null && session.getAttribute(RESET_USERNAME) != null;
    }

    public boolean isVerified(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(RESET_OTP_VERIFIED));
    }

    public String getPendingUsername(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(RESET_USERNAME);
    }

    public void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(RESET_USERNAME);
        session.removeAttribute(RESET_OTP_HASH);
        session.removeAttribute(RESET_OTP_EXPIRES_AT);
        session.removeAttribute(RESET_OTP_ATTEMPTS);
        session.removeAttribute(RESET_OTP_VERIFIED);
    }

    private Optional<Staff> findStaff(String identity) {
        if (!StringUtils.hasText(identity)) {
            return Optional.empty();
        }

        String normalized = identity.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("@")) {
            Optional<Staff> byEmail = staffRepository.findByEmail(normalized);
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }
        Optional<Staff> byUsername = staffRepository.findByUsername(normalized);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return staffRepository.findByEmail(normalized);
    }

    private void ensurePendingReset(HttpSession session) {
        if (!hasPendingReset(session) || session.getAttribute(RESET_OTP_HASH) == null) {
            throw new IllegalArgumentException("Không có yêu cầu đặt lại mật khẩu đang chờ.");
        }
    }

    private void ensureVerified(HttpSession session) {
        if (!isVerified(session)) {
            throw new IllegalArgumentException("Bạn cần xác minh OTP trước khi đặt lại mật khẩu.");
        }
    }

    private String generateOtp() {
        int value = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(value);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(encoded.length * 2);
            for (byte item : encoded) {
                builder.append(String.format(Locale.ROOT, "%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Không thể tạo mã băm OTP.", ex);
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
