package com.library.feature.auth;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildOtpTemplate(String otpCode) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>KhÃƒÂ´i phÃ¡Â»Â¥c mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u</title>
                </head>
                <body style="font-family:Segoe UI,Tahoma,sans-serif;color:#1f2937;background:#f5f7fb;padding:24px;">
                    <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;box-shadow:0 14px 48px rgba(15,23,42,0.12);">
                        <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;color:#6b7280;margin-bottom:12px;">Library Manager</div>
                        <h1 style="margin:0 0 12px;font-size:24px;color:#0f172a;">XÃƒÂ¡c minh yÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u</h1>
                        <p style="margin:0 0 16px;line-height:1.6;">
                            HÃ¡Â»â€¡ thÃ¡Â»â€˜ng vÃ¡Â»Â«a nhÃ¡ÂºÂ­n Ã„â€˜Ã†Â°Ã¡Â»Â£c yÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cho tÃƒÂ i khoÃ¡ÂºÂ£n cÃ¡Â»Â§a bÃ¡ÂºÂ¡n.
                            Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p mÃƒÂ£ OTP bÃƒÂªn dÃ†Â°Ã¡Â»â€ºi Ã„â€˜Ã¡Â»Æ’ tiÃ¡ÂºÂ¿p tÃ¡Â»Â¥c.
                        </p>
                        <div style="margin:24px 0;padding:18px 20px;border-radius:16px;background:#f1f5f9;text-align:center;">
                            <div style="font-size:13px;color:#64748b;margin-bottom:8px;">MÃƒÂ£ OTP</div>
                            <div style="font-size:32px;font-weight:700;letter-spacing:.35em;color:#0f172a;">%s</div>
                        </div>
                        <p style="margin:0 0 10px;line-height:1.6;">MÃƒÂ£ cÃƒÂ³ hiÃ¡Â»â€¡u lÃ¡Â»Â±c trong 10 phÃƒÂºt vÃƒÂ  chÃ¡Â»â€° dÃƒÂ¹ng mÃ¡Â»â„¢t lÃ¡ÂºÂ§n.</p>
                        <p style="margin:0;color:#64748b;line-height:1.6;">
                            NÃ¡ÂºÂ¿u bÃ¡ÂºÂ¡n khÃƒÂ´ng thÃ¡Â»Â±c hiÃ¡Â»â€¡n thao tÃƒÂ¡c nÃƒÂ y, hÃƒÂ£y bÃ¡Â»Â qua email.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(otpCode);
    }
}
