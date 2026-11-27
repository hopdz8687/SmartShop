# 9. Kế hoạch kiểm thử

## 9.1. Mục tiêu
- Kiểm tra chức năng theo yêu cầu.
- Phát hiện lỗi nghiệp vụ và phân quyền.
- Đảm bảo dữ liệu nhất quán khi xử lý đơn.
- Kiểm tra tích hợp ReactJS, Spring Boot và MySQL.

## 9.2. Phạm vi

| Module | Nội dung |
|---|---|
| Authentication | Đăng ký, email trùng, đăng nhập sai, token hết hạn |
| Seller | Gửi hồ sơ, duyệt/từ chối, quyền Seller |
| Product | CRUD, giá không hợp lệ, tồn kho, phân trang |
| Cart | Thêm/sửa/xóa, sản phẩm ngừng bán |
| Order | Đặt hàng, thiếu kho, chia đơn, hủy đơn |
| Authorization | Truy cập đúng phạm vi Customer/Seller/Admin |
| Chat | Gửi tin, lịch sử, chặn người ngoài conversation |
| AI | Câu hỏi, không có kết quả, lỗi AI API |
| Review | Chỉ người mua hợp lệ được đánh giá |
| Shipping | Provider, mã vận đơn, trạng thái |
| Promotion | Hạn dùng, điều kiện, giới hạn lượt |

## 9.3. Cấp độ kiểm thử
- Unit Test: JUnit/Mockito cho Service.
- Integration Test: Repository, Database và API.
- API Test: Postman/Thunder Client.
- UI Test thủ công trên trình duyệt.
- Security Test cơ bản cho phân quyền và truy cập trái phép.

## 9.4. Tình huống quan trọng

### Đặt hàng
- Giỏ rỗng.
- Sản phẩm ngừng bán.
- Tồn kho không đủ.
- Giỏ có sản phẩm nhiều cửa hàng.
- Lỗi giữa quá trình tạo đơn phải rollback.
- Hai yêu cầu đồng thời mua sản phẩm cuối cùng.

### Phân quyền
- Customer gọi API Seller.
- Seller truy cập sản phẩm/đơn của cửa hàng khác.
- Người ngoài conversation đọc lịch sử chat.
- Token không hợp lệ hoặc hết hạn.

### AI
- Có sản phẩm phù hợp.
- Không tìm thấy sản phẩm.
- AI API không khả dụng.
- Đầu vào yêu cầu AI thao tác ngoài phạm vi.

## 9.5. Ghi nhận test case
Mỗi test case gồm: ID, module, tiền điều kiện, dữ liệu đầu vào, bước thực hiện, kết quả mong đợi, kết quả thực tế, Pass/Fail và ghi chú/bằng chứng.

---

## Cập nhật kết quả kiểm thử database – 25/11/2026

Đã import schema 24 bảng và seed data vào database `smartshop`. phpMyAdmin đã hiển thị quan hệ một Order tổng với hai Seller Order:
- `SS-20261125-0001-A`: 418.000đ.
- `SS-20261125-0001-B`: 470.000đ.
- Order tổng: 888.000đ.

Cần xác nhận tiếp bằng truy vấn SQL:
- Hai Payment có `payment_method = 'COD'`, trạng thái ban đầu `PENDING`.
- Tồn kho sau checkout theo Variant.
- Các kiểm thử seed này chỉ xác nhận dữ liệu mẫu; chưa thay thế unit/integration test cho logic transaction và xử lý đồng thời trong Spring Boot.
