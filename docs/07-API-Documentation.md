# 7. Thiết kế API – Hợp đồng triển khai MVP

> Các endpoint dưới đây là thiết kế API ban đầu, không có nghĩa tất cả đã được triển khai. Khi code phải đặc tả DTO, response, mã lỗi, phân trang và quyền truy cập. Không tự tạo endpoint trái với nghiệp vụ hoặc schema.

## 7.1. Quy ước chung
- Base path: `/api/v1`.
- JSON request/response.
- Bearer JWT cho endpoint yêu cầu đăng nhập.
- Controller nhận/trả DTO; không expose JPA Entity.
- Backend tự xác định user hiện tại từ Security Context; không nhận `userId` để quyết định quyền.
- Seller endpoint luôn giới hạn theo Store của Seller hiện tại.
- Customer endpoint luôn giới hạn theo dữ liệu của Customer hiện tại.
- Client không được gửi giá cuối cùng, shipping total hoặc trạng thái nhạy cảm để Backend tin trực tiếp.

## 7.2. Authentication và User

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/auth/register` | Đăng ký Customer |
| POST | `/auth/login` | Đăng nhập |
| POST | `/auth/logout` | Đăng xuất/thu hồi token nếu cơ chế triển khai hỗ trợ |
| GET | `/users/me` | Lấy hồ sơ người dùng hiện tại |
| PUT | `/users/me` | Cập nhật hồ sơ hiện tại |

## 7.3. Address

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/addresses` | Danh sách địa chỉ của user hiện tại |
| POST | `/addresses` | Tạo địa chỉ |
| PUT | `/addresses/{id}` | Cập nhật địa chỉ thuộc user hiện tại |
| DELETE | `/addresses/{id}` | Xóa địa chỉ thuộc user hiện tại |

Checkout phải copy snapshot địa chỉ vào Order; không chỉ lưu một tham chiếu Address.

## 7.4. Seller Application và Store

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/seller-applications` | Gửi hồ sơ mở Store |
| GET | `/seller-applications/me` | Xem hồ sơ của mình |
| GET | `/admin/seller-applications` | Admin xem hồ sơ |
| PUT | `/admin/seller-applications/{id}/approve` | Duyệt |
| PUT | `/admin/seller-applications/{id}/reject` | Từ chối |
| GET | `/stores/{id}` | Xem Store công khai |
| GET | `/seller/stores/me` | Seller xem Store của mình |
| PUT | `/seller/stores/me` | Seller cập nhật Store của mình |

## 7.5. Category, Product và Variant

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/categories` | Danh sách danh mục |
| POST | `/admin/categories` | Admin tạo danh mục |
| PUT | `/admin/categories/{id}` | Admin cập nhật danh mục |
| DELETE | `/admin/categories/{id}` | Admin xóa/ẩn danh mục theo quy tắc |
| GET | `/products` | Danh sách sản phẩm |
| GET | `/products/{id}` | Chi tiết sản phẩm và variants |
| GET | `/products/search` | Tìm kiếm/lọc/sắp xếp/phân trang |
| POST | `/seller/products` | Seller tạo Product và Variant |
| PUT | `/seller/products/{id}` | Seller cập nhật Product thuộc Store mình |
| DELETE | `/seller/products/{id}` | Seller xóa mềm/ẩn Product theo DDL |
| POST | `/seller/products/{productId}/variants` | Tạo Variant |
| PUT | `/seller/variants/{variantId}` | Cập nhật Variant thuộc Store mình |
| DELETE | `/seller/variants/{variantId}` | Ngừng hoạt động Variant theo quy tắc |

MVP dùng flat variants. Chưa cần endpoint quản lý attribute động trừ khi phạm vi được thay đổi. Giá lọc/sắp xếp sản phẩm phải tính từ giá các Variant theo query; không giả định có `products.min_price`.

## 7.6. Cart và Checkout

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/cart` | Xem giỏ hiện tại |
| POST | `/cart/items` | Thêm Variant vào giỏ |
| PUT | `/cart/items/{id}` | Cập nhật số lượng |
| DELETE | `/cart/items/{id}` | Xóa một Cart Item |
| POST | `/orders` | Checkout các `cartItemIds` đã chọn, COD |
| GET | `/orders/my` | Lịch sử Order của Customer |
| GET | `/orders/{id}` | Chi tiết Order của chính Customer |
| PUT | `/seller-orders/{id}/cancel` | Customer hủy Seller Order nếu PENDING và thuộc mình |
| GET | `/seller/orders` | Seller xem Seller Order của Store mình |
| GET | `/seller/orders/{id}` | Seller xem chi tiết đơn của Store mình |
| PUT | `/seller/orders/{id}/confirm` | Seller xác nhận đơn |
| PUT | `/seller/orders/{id}/processing` | Seller bắt đầu chuẩn bị hàng |
| PUT | `/seller/orders/{id}/ship` | Seller đánh dấu gửi hàng, tạo/cập nhật Shipment |
| PUT | `/seller/orders/{id}/cancel` | Seller hủy đơn PENDING/CONFIRMED của Store mình |

`POST /orders` chỉ nhận lựa chọn Cart Item và thông tin checkout cần thiết. Backend tự tính giá, nhóm Store, tạo Order cha/con, Payment COD, trừ kho và xóa đúng Cart Item đã chọn trong transaction.

## 7.7. Shipment và COD

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/seller-orders/{id}/shipment` | Xem thông tin vận chuyển theo quyền |
| PUT | `/admin/shipments/{id}/delivered` | Admin/operator xác nhận giao thành công trong MVP |
| PUT | `/admin/payments/{id}/mark-paid` | Actor được phân quyền xác nhận đã thu COD |

Các endpoint trên là gợi ý thiết kế; trước khi triển khai phải kiểm tra tên cột, enum, vai trò được phép và trạng thái chuyển tiếp trong schema/workflow. Không tạo luồng refund tự động nếu chưa đặc tả.

## 7.8. Chat

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/conversations` | Danh sách conversation của user |
| POST | `/conversations` | Tạo hoặc lấy conversation theo Store |
| GET | `/conversations/{id}/messages` | Lịch sử tin nhắn nếu là thành viên |
| POST | `/conversations/{id}/messages` | Gửi tin nếu là thành viên |

WebSocket/STOMP chỉ là mở rộng, không phải MVP.

## 7.9. AI

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/ai/chat` | Trợ lý mua sắm dựa trên catalog |
| POST | `/ai/search` | Tìm sản phẩm bằng ngôn ngữ tự nhiên |

Backend kiểm soát dữ liệu sản phẩm gửi cho AI; response phải dựa trên kết quả query thật. Không để AI thực hiện mutation.

## 7.10. Response và lỗi
Response thành công đề xuất:
```json
{
  "success": true,
  "message": "Request successful",
  "data": {}
}
```
Response lỗi nên có mã lỗi nghiệp vụ ổn định, message an toàn và validation details phù hợp. Không trả stack trace, SQL, token hoặc bí mật cho Client.

## 7.11. Các phần cần đặc tả trước khi hoàn thiện endpoint
- DTO request/response cụ thể và quy tắc validation.
- Mã lỗi nghiệp vụ cho hết hàng, sản phẩm ngừng bán, không đủ quyền, trạng thái không hợp lệ.
- Phân trang, sort whitelist và filter.
- Cách tính/hiển thị phí vận chuyển và discount.
- Actor được quyền xác nhận COD PAID.
- Quy tắc Product moderation/approval nếu Admin kiểm duyệt trước khi sản phẩm hiển thị.
