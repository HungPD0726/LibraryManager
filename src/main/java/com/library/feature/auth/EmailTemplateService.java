package com.library.feature.auth;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    public String buildBorrowDueSoonReminderTemplate(String studentName, Integer borrowId, LocalDate dueDate) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Nhắc hạn trả sách</title>
                </head>
                <body style="font-family:Segoe UI,Tahoma,sans-serif;color:#1f2937;background:#f5f7fb;padding:24px;">
                    <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;box-shadow:0 14px 48px rgba(15,23,42,0.12);">
                        <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;color:#6b7280;margin-bottom:12px;">Library Manager</div>
                        <h1 style="margin:0 0 12px;font-size:24px;color:#0f172a;">Sắp đến hạn trả sách</h1>
                        <p style="margin:0 0 16px;line-height:1.6;">Xin chào %s,</p>
                        <p style="margin:0 0 16px;line-height:1.6;">
                            Phiếu mượn <strong>#%s</strong> của bạn sẽ đến hạn vào ngày <strong>%s</strong>.
                            Vui lòng sắp xếp trả sách đúng hạn để tránh bị chuyển sang quá hạn.
                        </p>
                        <div style="margin:24px 0;padding:18px 20px;border-radius:16px;background:#ecfeff;border:1px solid #99f6e4;">
                            <div style="font-size:13px;color:#0f766e;margin-bottom:8px;">Thông tin cần lưu ý</div>
                            <div style="font-size:16px;font-weight:700;color:#0f172a;">Hạn trả: %s</div>
                        </div>
                        <p style="margin:0;color:#64748b;line-height:1.6;">
                            Bạn có thể theo dõi thêm trong mục thông báo của hệ thống Library Manager.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(studentName, borrowId, formatDate(dueDate), formatDate(dueDate));
    }

    public String buildBorrowOverdueReminderTemplate(String studentName, Integer borrowId, LocalDate dueDate, long overdueDays) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Phiếu mượn đã quá hạn</title>
                </head>
                <body style="font-family:Segoe UI,Tahoma,sans-serif;color:#1f2937;background:#f5f7fb;padding:24px;">
                    <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;box-shadow:0 14px 48px rgba(15,23,42,0.12);">
                        <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;color:#6b7280;margin-bottom:12px;">Library Manager</div>
                        <h1 style="margin:0 0 12px;font-size:24px;color:#991b1b;">Phiếu mượn đã quá hạn</h1>
                        <p style="margin:0 0 16px;line-height:1.6;">Xin chào %s,</p>
                        <p style="margin:0 0 16px;line-height:1.6;">
                            Phiếu mượn <strong>#%s</strong> của bạn đã quá hạn <strong>%s ngày</strong> kể từ ngày <strong>%s</strong>.
                            Vui lòng trả sách sớm để tránh phát sinh xử lý bổ sung.
                        </p>
                        <div style="margin:24px 0;padding:18px 20px;border-radius:16px;background:#fff1f2;border:1px solid #fecdd3;">
                            <div style="font-size:13px;color:#be123c;margin-bottom:8px;">Trạng thái hiện tại</div>
                            <div style="font-size:16px;font-weight:700;color:#0f172a;">Quá hạn từ: %s</div>
                        </div>
                        <p style="margin:0;color:#64748b;line-height:1.6;">
                            Hãy kiểm tra mục thông báo trong hệ thống để xem chi tiết phiếu mượn của bạn.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(studentName, borrowId, overdueDays, formatDate(dueDate), formatDate(dueDate));
    }

    public String formatDate(LocalDate date) {
        if (date == null) {
            return "--/--/----";
        }
        return DATE_FORMATTER.format(date);
    }
}
