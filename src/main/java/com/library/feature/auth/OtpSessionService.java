package com.library.feature.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class OtpSessionService {

    public static final String RESET_USERNAME = "resetUsername";
    public static final String RESET_OTP_HASH = "resetOtpHash";
    public static final String RESET_OTP_EXPIRES_AT = "resetOtpExpiresAt";
    public static final String RESET_OTP_ATTEMPTS = "resetOtpAttempts";
    public static final String RESET_OTP_VERIFIED = "resetOtpVerified";

    public void start(HttpSession session, String username, String otpHash, long expiresAt) {
        clear(session);
        session.setAttribute(RESET_USERNAME, username);
        session.setAttribute(RESET_OTP_HASH, otpHash);
        session.setAttribute(RESET_OTP_EXPIRES_AT, expiresAt);
        session.setAttribute(RESET_OTP_ATTEMPTS, 0);
        session.setAttribute(RESET_OTP_VERIFIED, Boolean.FALSE);
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

    public String getExpectedHash(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(RESET_OTP_HASH);
    }

    public long getExpiresAt(HttpSession session) {
        return session == null ? 0L : java.util.Optional.ofNullable((Long) session.getAttribute(RESET_OTP_EXPIRES_AT)).orElse(0L);
    }

    public int getAttempts(HttpSession session) {
        return session == null ? 0 : java.util.Optional.ofNullable((Integer) session.getAttribute(RESET_OTP_ATTEMPTS)).orElse(0);
    }

    public void incrementAttempts(HttpSession session) {
        session.setAttribute(RESET_OTP_ATTEMPTS, getAttempts(session) + 1);
    }

    public void markVerified(HttpSession session) {
        session.setAttribute(RESET_OTP_VERIFIED, Boolean.TRUE);
        session.removeAttribute(RESET_OTP_HASH);
        session.removeAttribute(RESET_OTP_EXPIRES_AT);
        session.removeAttribute(RESET_OTP_ATTEMPTS);
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
}
