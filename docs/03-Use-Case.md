# 3. Phân tích Use Case

## 3.1. Actors

| Actor | Mô tả |
|---|---|
| Customer | Người mua |
| Seller | Người bán |
| Admin | Quản trị viên |
| AI API | Dịch vụ AI bên ngoài |
| Payment Gateway | Cổng thanh toán khi mở rộng |
| Shipping Provider | Đơn vị vận chuyển khi tích hợp |

## 3.2. Use Case của Customer
- UC-C01: Đăng ký.
- UC-C02: Đăng nhập/đăng xuất.
- UC-C03: Cập nhật hồ sơ.
- UC-C04: Xem và tìm kiếm sản phẩm.
- UC-C05: Xem chi tiết sản phẩm.
- UC-C06: Quản lý giỏ hàng.
- UC-C07: Đặt hàng.
- UC-C08: Xem lịch sử đơn.
- UC-C09: Hủy Seller Order thuộc Order của mình khi trạng thái PENDING.
- UC-C10: Chat với cửa hàng.
- UC-C11: Hỏi trợ lý AI.
- UC-C12: Đánh giá sản phẩm (mở rộng).

## 3.3. Use Case của Seller
- UC-S01: Gửi yêu cầu mở cửa hàng.
- UC-S02: Cập nhật cửa hàng.
- UC-S03: Quản lý sản phẩm.
- UC-S04: Xem đơn hàng của cửa hàng.
- UC-S05: Cập nhật trạng thái đơn.
- UC-S06: Chat với khách hàng.

## 3.4. Use Case của Admin
- UC-A01: Quản lý người dùng.
- UC-A02: Duyệt hồ sơ Seller.
- UC-A03: Quản lý cửa hàng.
- UC-A04: Quản lý danh mục.
- UC-A05: Kiểm duyệt sản phẩm.
- UC-A06: Quản lý/giám sát đơn hàng.
- UC-A07: Quản lý mã giảm giá (mở rộng).

## 3.5. Đặc tả UC-C07 – Đặt hàng

**Actor:** Customer.

**Tiền điều kiện:** Đã đăng nhập; giỏ có sản phẩm hợp lệ.

**Hậu điều kiện:** Đơn được tạo; tồn kho cập nhật nhất quán.

**Luồng chính:**
1. Customer mở giỏ hàng.
2. Hệ thống hiển thị sản phẩm và số lượng.
3. Customer chọn địa chỉ nhận hàng.
4. Customer xác nhận COD.
5. Backend kiểm tra sản phẩm, giá và tồn kho.
6. Backend nhóm sản phẩm theo cửa hàng.
7. Tạo Order tổng và các Seller Order.
8. Lưu Order Item snapshot.
9. Cập nhật tồn kho và giỏ trong transaction.
10. Trả kết quả đặt hàng.

**Ngoại lệ:** Giỏ rỗng, sản phẩm ngừng bán, tồn kho không đủ hoặc lỗi lưu đơn. Nếu transaction thất bại phải rollback.

## 3.6. Đặc tả UC-S03 – Quản lý sản phẩm

**Tiền điều kiện:** Seller đã được duyệt và đăng nhập.

**Luồng chính:**
1. Seller mở trang quản lý sản phẩm.
2. Hệ thống chỉ hiển thị sản phẩm thuộc Store của Seller.
3. Seller chọn thêm/sửa/ẩn/xóa.
4. Seller nhập thông tin.
5. Backend kiểm tra dữ liệu và quyền sở hữu.
6. Hệ thống lưu và trả kết quả.

## 3.7. Đặc tả UC-C10 – Chat

**Actors:** Customer và Seller.

**Luồng chính:**
1. Customer mở trang Store và chọn Chat.
2. Hệ thống tìm conversation hiện có hoặc tạo mới.
3. Customer gửi tin nhắn.
4. Backend kiểm tra thành viên.
5. Backend lưu Message.
6. Seller xem và trả lời.
7. Tin nhắn được lưu vào lịch sử.

## 3.8. Đặc tả UC-C11 – AI

1. Customer nhập câu hỏi.
2. Backend tiếp nhận.
3. Backend tìm sản phẩm phù hợp trong catalog.
4. Backend gửi câu hỏi và dữ liệu sản phẩm phù hợp đến AI API.
5. AI tạo câu trả lời.
6. Backend trả kết quả cho Frontend.

AI không được tự thay đổi dữ liệu nghiệp vụ.


## 3.9. Quy tắc Use Case cần tuân thủ
- Checkout nhận các `cartItemIds` được chọn, tạo một Order cha và Seller Order theo Store.
- Customer hủy Seller Order PENDING của chính mình; Seller hủy Seller Order PENDING/CONFIRMED thuộc Store mình trước shipment.
- OrderService tổng hợp trạng thái Order cha; không dùng `PARTIALLY_CANCELLED`.
- Xem `docs/04-Business-Workflows.md` và `AGENTS.md` để biết đầy đủ điều kiện và transaction invariants.
