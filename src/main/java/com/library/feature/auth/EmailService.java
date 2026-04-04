package com.library.feature.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${app.mail.from-name:Library Manager}")
    private String fromName;

    @Value("${app.mail.from-email:}")
    private String fromEmail;

    public boolean isConfigured() {
        return StringUtils.hasText(smtpUsername) && StringUtils.hasText(smtpPassword);
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        sendHtml(toEmail, "MÃƒÂ£ OTP Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u", templateService.buildOtpTemplate(otpCode));
    }

    public void sendHtml(String toEmail, String subject, String htmlContent) {
        if (!isConfigured()) {
            throw new IllegalStateException("Email chÃ†Â°a Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃ¡ÂºÂ¥u hÃƒÂ¬nh Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(buildFromAddress());
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            throw new IllegalStateException("KhÃƒÂ´ng thÃ¡Â»Æ’ gÃ¡Â»Â­i email OTP: " + ex.getMessage(), ex);
        }
    }

    private String buildFromAddress() throws UnsupportedEncodingException {
        String sender = StringUtils.hasText(fromEmail) ? fromEmail.trim() : smtpUsername.trim();
        return new InternetAddress(sender, fromName, "UTF-8").toString();
    }
}
