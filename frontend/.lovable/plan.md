

# Trang Web Quản Lý Thư Viện Trường Học

Phong cách: **Chuyên nghiệp & truyền thống** — giao diện rõ ràng, dễ sử dụng cho thủ thư và nhân viên quản lý.

> **Giai đoạn 1: Giao diện trước** — Xây dựng toàn bộ UI với dữ liệu mẫu. Backend sẽ được tích hợp sau.

---

## 1. Đăng nhập / Đăng ký
- Trang đăng nhập với email & mật khẩu
- Trang đăng ký tài khoản mới
- Giao diện đơn giản, chuyên nghiệp

## 2. Dashboard (Trang tổng quan)
- Thống kê tổng quan: số lượng sách, số thành viên, lượt mượn trong tháng, sách quá hạn
- Biểu đồ lượt mượn theo thời gian (dùng Recharts)
- Danh sách sách sắp đến hạn trả
- Thông báo nhanh (sách quá hạn, yêu cầu mới)

## 3. Quản lý Sách
- Bảng danh sách sách với tìm kiếm, lọc theo thể loại/tác giả/trạng thái
- Thêm / sửa / xóa sách (form với các trường: tên sách, tác giả, thể loại, ISBN, số lượng, ảnh bìa)
- Xem chi tiết sách với lịch sử mượn/trả
- Upload file sách (PDF/ebook) đính kèm

## 4. Mượn / Trả Sách
- Tạo phiếu mượn: chọn thành viên + chọn sách + ngày hẹn trả
- Danh sách phiếu mượn đang hoạt động
- Xác nhận trả sách
- Đánh dấu sách quá hạn, tính phí phạt trễ

## 5. Bán Sách
- Danh sách sách bán với giá bán
- Tạo đơn bán sách
- Lịch sử giao dịch bán

## 6. Quản lý Thành viên (Độc giả)
- Danh sách thành viên với tìm kiếm
- Thêm / sửa / xóa thành viên (họ tên, lớp, email, số điện thoại)
- Xem lịch sử mượn sách của từng thành viên

## 7. Thống kê & Báo cáo
- Biểu đồ sách được mượn nhiều nhất
- Thống kê theo thể loại, theo tháng
- Báo cáo doanh thu bán sách
- Xuất báo cáo (giao diện xem trước)

## 8. Layout chung
- Sidebar menu bên trái với các mục: Dashboard, Sách, Mượn/Trả, Bán sách, Thành viên, Thống kê
- Header với tên người dùng, thông báo, đăng xuất
- Responsive cho tablet (hỗ trợ cơ bản)

