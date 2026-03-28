package com.library.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildOtpTemplate(String otpCode) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Khôi phục mật khẩu</title>
                </head>
                <body style="font-family:Segoe UI,Tahoma,sans-serif;color:#1f2937;background:#f5f7fb;padding:24px;">
                    <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;box-shadow:0 14px 48px rgba(15,23,42,0.12);">
                        <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;color:#6b7280;margin-bottom:12px;">Library Manager</div>
                        <h1 style="margin:0 0 12px;font-size:24px;color:#0f172a;">Xác minh yêu cầu đặt lại mật khẩu</h1>
                        <p style="margin:0 0 16px;line-height:1.6;">
                            Hệ thống vừa nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                            Vui lòng nhập mã OTP bên dưới để tiếp tục.
                        </p>
                        <div style="margin:24px 0;padding:18px 20px;border-radius:16px;background:#f1f5f9;text-align:center;">
                            <div style="font-size:13px;color:#64748b;margin-bottom:8px;">Mã OTP</div>
                            <div style="font-size:32px;font-weight:700;letter-spacing:.35em;color:#0f172a;">%s</div>
                        </div>
                        <p style="margin:0 0 10px;line-height:1.6;">Mã có hiệu lực trong 10 phút và chỉ dùng một lần.</p>
                        <p style="margin:0;color:#64748b;line-height:1.6;">
                            Nếu bạn không thực hiện thao tác này, hãy bỏ qua email.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(otpCode);
    }
}
