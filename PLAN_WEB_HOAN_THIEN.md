# Kế Hoạch Hoàn Thiện Web App (quanlyluong)

Dựa trên sự đối chiếu với ứng dụng Desktop (`QUAN_LY_LUONG`), dưới đây là kế hoạch chi tiết để hoàn thiện Web App, thực hiện theo thứ tự **A -> C -> B**.

## Giai đoạn A (Admin - Quản lý cốt lõi)
1. **Quản lý Phòng ban (A1):**
   - Màn hình `/admin/phongban`
   - Thêm/Sửa/Xóa phòng ban.
   - Chuyển đổi phòng ban / chức vụ cho nhân viên.
2. **Duyệt nghỉ & Quản lý vắng mặt (A2):**
   - Màn hình `/admin/vangmat`
   - Hiển thị danh sách xin nghỉ theo ngày, Duyệt/Từ chối đơn xin nghỉ.
3. **Giao việc cho nhân viên / phòng ban (A3):**
   - Màn hình `/admin/giaoviec`
   - Tạo task mới, có deadline. Xem lịch sử giao việc, xóa task.
4. **Cải tiến Quản lý nhân viên (A4):**
   - Tích hợp Import từ Excel.
   - Chức năng cập nhật Lương & Người phụ thuộc.
   - Giao diện yêu cầu tăng ca trực tiếp.

## Giai đoạn C (Employee - Nghiệp vụ cá nhân)
1. **Đăng ký lịch làm & Xin nghỉ (C1):**
   - Màn hình `/employee/lich`
   - Đăng ký Ca 1, Ca 2 hoặc gửi đơn xin nghỉ phép.
2. **Danh bạ đồng nghiệp (C2):**
   - Màn hình `/employee/dongnghiep`
   - Xem danh sách cùng phòng, chức vụ, thông tin liên lạc.
3. **Quản lý Công việc & Thông báo (C3):**
   - Nâng cấp màn hình Thông báo: Xem công việc được giao, nút "Báo cáo hoàn thành".
4. **Quản lý Thưởng/Phạt (C4):**
   - Dành riêng cho chức vụ "Trưởng phòng".
5. **Cập nhật hồ sơ & Đổi mật khẩu (C5):**
   - Giao diện xem chi tiết hồ sơ cá nhân và đổi mật khẩu.

## Giai đoạn B (Nâng cao & Giao diện Dashboard)
1. **Biểu đồ thống kê (B1):**
   - Tròn (Admin): Cơ cấu quỹ lương.
   - Đường (Employee): Lịch sử lương các tháng.
2. **Bảng chấm công dạng lưới tuần (B2):**
   - Nâng cấp view chấm công của Admin thành ma trận T2->CN.
3. **Cảnh báo hệ thống tự động (B3):**
   - Thông báo Sinh nhật.
   - Cảnh báo quên Check-out (phạt).
4. **Chi tiết Phiếu Lương (B4):**
   - Modal hiển thị chi tiết (Khấu trừ, BHXH, Thuế TNCN) cho Admin & Employee.
