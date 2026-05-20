package com.library.feature.auth;

import com.library.domain.model.Staff;
import com.library.domain.repository.StaffRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    public static final String RESET_USERNAME = OtpSessionService.RESET_USERNAME;
    public static final String RESET_OTP_HASH = OtpSessionService.RESET_OTP_HASH;
    public static final String RESET_OTP_EXPIRES_AT = OtpSessionService.RESET_OTP_EXPIRES_AT;
    public static final String RESET_OTP_ATTEMPTS = OtpSessionService.RESET_OTP_ATTEMPTS;
    public static final String RESET_OTP_VERIFIED = OtpSessionService.RESET_OTP_VERIFIED;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration OTP_TTL = Duration.ofMinutes(10);

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpSessionService otpSessionService;
    private final OtpGeneratorHasher otpGeneratorHasher;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public void startReset(String identity, HttpSession session) {
        if (!emailService.isConfigured()) {
            throw new IllegalStateException(buildMissingMailConfigurationMessage());
        }

        Staff staff = findStaff(identity)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản phù hợp."));

        String recoveryEmail = StringUtils.hasText(staff.getEmail()) ? staff.getEmail().trim() : null;
        if (!StringUtils.hasText(recoveryEmail)) {
            throw new IllegalArgumentException("Tài khoản này chưa có email để nhận OTP.");
        }

        String otp = otpGeneratorHasher.generateOtp();
        otpSessionService.start(
                session,
                staff.getUsername(),
                otpGeneratorHasher.sha256(otp),
                System.currentTimeMillis() + OTP_TTL.toMillis()
        );
        try {
            emailService.sendOtpEmail(recoveryEmail, otp);
        } catch (RuntimeException ex) {
            otpSessionService.clear(session);
            throw ex;
        }
    }

    public void verifyOtp(String otp, HttpSession session) {
        ensurePendingReset(session);

        if (System.currentTimeMillis() > otpSessionService.getExpiresAt(session)) {
            otpSessionService.clear(session);
            throw new IllegalArgumentException("OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        int attempts = otpSessionService.getAttempts(session);
        if (attempts >= MAX_ATTEMPTS) {
            otpSessionService.clear(session);
            throw new IllegalArgumentException("Bạn đã nhập sai quá số lần cho phép.");
        }

        String expectedHash = otpSessionService.getExpectedHash(session);
        if (!otpGeneratorHasher.sha256(otp).equals(expectedHash)) {
            otpSessionService.incrementAttempts(session);
            throw new IllegalArgumentException("OTP không đúng. Bạn còn " + (MAX_ATTEMPTS - attempts - 1) + " lần thử.");
        }

        otpSessionService.markVerified(session);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resetPassword(String password, String confirmPassword, HttpSession session) {
        ensureVerified(session);
        passwordPolicyValidator.validate(password, confirmPassword);

        String username = otpSessionService.getPendingUsername(session);
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản để đặt lại mật khẩu."));

        staff.setPassword(passwordEncoder.encode(password));
        staffRepository.save(staff);
        otpSessionService.clear(session);
    }

    public boolean hasPendingReset(HttpSession session) {
        return otpSessionService.hasPendingReset(session);
    }

    public boolean isVerified(HttpSession session) {
        return otpSessionService.isVerified(session);
    }

    public String getPendingUsername(HttpSession session) {
        return otpSessionService.getPendingUsername(session);
    }

    public void clear(HttpSession session) {
        otpSessionService.clear(session);
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
        if (!otpSessionService.hasPendingReset(session) || otpSessionService.getExpectedHash(session) == null) {
            throw new IllegalArgumentException("Không có yêu cầu đặt lại mật khẩu đang chờ.");
        }
    }

    private void ensureVerified(HttpSession session) {
        if (!otpSessionService.isVerified(session)) {
            throw new IllegalArgumentException("Bạn cần xác minh OTP trước khi đặt lại mật khẩu.");
        }
    }

    private String buildMissingMailConfigurationMessage() {
        List<String> missingKeys = emailService.missingMailConfigurationKeys();
        if (missingKeys.size() == 1) {
            return "Thiếu cấu hình " + missingKeys.get(0) + ".";
        }
        if (missingKeys.size() == 2) {
            return "Thiếu cấu hình " + missingKeys.get(0) + " và " + missingKeys.get(1) + ".";
        }
        return "Hệ thống email chưa được cấu hình.";
    }
}
