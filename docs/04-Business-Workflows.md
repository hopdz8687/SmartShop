# 4. Quy trình nghiệp vụ đã chốt cho MVP

> Các quy tắc dưới đây là hợp đồng nghiệp vụ cho Backend. Tên bảng/cột/enum cụ thể phải đối chiếu DDL SQL v2.1.

## 4.1. Đăng ký và duyệt Seller
1. Người dùng đăng ký và đăng nhập với vai trò Customer.
2. Customer gửi hồ sơ mở cửa hàng.
3. Hệ thống tạo Seller Application trạng thái `PENDING`.
4. Admin xem xét hồ sơ.
5. Nếu từ chối, chuyển `REJECTED` và lưu lý do.
6. Nếu duyệt, chuyển `APPROVED`, tạo Store và cấp quyền Seller.
7. MVP: một tài khoản Seller sở hữu tối đa một Store.

## 4.2. Quản lý Product và Variant
- Seller đã được duyệt quản lý Product thuộc Store của mình.
- Mỗi Product phải có ít nhất một Variant đang hoạt động và đúng một Variant mặc định.
- MVP dùng flat variant: tên biến thể (`variant_name`), SKU, giá và tồn kho nằm trên từng Product Variant.
- Các bảng Product Attribute / Attribute Value / Variant Attribute Value có trong schema để dành cho mở rộng; chưa triển khai nghiệp vụ thuộc tính động và không tạo Entity/UI cho các bảng này trong MVP nếu chưa được yêu cầu.
- Tồn kho luôn được đọc/cập nhật tại Variant, không phải Product.
- Backend kiểm tra quyền sở hữu Product/Store. Không tin `storeId` do Client gửi lên.

## 4.3. Checkout đa cửa hàng
Một lần checkout có thể chứa sản phẩm của nhiều Store:
1. Customer chọn một hoặc nhiều `cartItemIds` trong giỏ persistent.
2. Backend xác minh các Cart Item thuộc Cart của Customer hiện tại.
3. Backend đọc lại giá, trạng thái bán và tồn kho từ database; không tin giá/tổng tiền từ Frontend.
4. Backend nhóm các dòng đã chọn theo Store.
5. Tạo một Order tổng (`orders`) cho lần checkout.
6. Tạo một Seller Order (`seller_orders`) cho mỗi Store.
7. Tạo Order Item thuộc Seller Order, lưu snapshot: tên sản phẩm, ảnh, mô tả biến thể, SKU, đơn giá, số lượng và subtotal.
8. Snapshot người nhận/địa chỉ được lưu trên Order, không phụ thuộc thay đổi Address về sau.
9. Phân bổ phí vận chuyển/giảm giá ở cấp Seller Order; tổng Order cha phải bằng tổng các Seller Order con.
10. Tạo Payment COD cho từng Seller Order theo schema hiện tại.
11. Trừ tồn kho bằng cập nhật có điều kiện chống overselling.
12. Chỉ xóa các Cart Item đã chọn sau khi toàn bộ checkout thành công.
13. Toàn bộ các bước ghi dữ liệu phải nằm trong một transaction. Bất kỳ lỗi nào cũng rollback đơn, payment, tồn kho và cart.

### Chống overselling
Dùng cập nhật nguyên tử theo ý tưởng:
```sql
UPDATE product_variants
SET stock_quantity = stock_quantity - :quantity
WHERE id = :variantId
  AND stock_quantity >= :quantity;
```
Nếu số dòng bị ảnh hưởng bằng 0, dừng checkout và rollback. Xử lý Variant ID theo thứ tự tăng dần để giảm nguy cơ deadlock. Đối chiếu tên cột chính xác trong DDL.

## 4.4. Trạng thái Seller Order và quy tắc hủy
Trạng thái nghiệp vụ Seller Order:
- `PENDING`: chờ Seller xác nhận.
- `CONFIRMED`: Seller đã xác nhận.
- `PROCESSING`: đang chuẩn bị hàng.
- `SHIPPED`: đã bàn giao/gửi hàng.
- `DELIVERED`: giao thành công.
- `CANCELLED`: đã hủy.

Quy tắc:
- Customer chỉ được hủy Seller Order khi đang `PENDING` và Seller Order đó thuộc Order tổng của chính Customer.
- Seller chỉ được hủy Seller Order của Store mình khi trạng thái `PENDING` hoặc `CONFIRMED`, trước khi giao cho đơn vị vận chuyển.
- Customer update có điều kiện `WHERE status = 'PENDING'`.
- Seller update có điều kiện `WHERE status IN ('PENDING','CONFIRMED')`.
- Chỉ khi update có điều kiện thành công mới được hoàn kho; cập nhật trạng thái và hoàn kho trong cùng transaction.
- Cần bảo đảm thao tác lặp không hoàn kho hai lần.
- Nếu COD đã `PAID`, không đổi đơn giản sang `CANCELLED`; phải có quy trình hoàn tiền riêng.
- Khi hủy một Seller Order, giữ nguyên snapshot tiền ban đầu. Số tiền còn phải thanh toán được tính từ các Seller Order chưa hủy.

## 4.5. Trạng thái Order tổng
`orders.order_status` được OrderService tổng hợp từ các Seller Order, không để Controller hoặc từng module tự cập nhật tùy tiện.

Quy tắc:
- Nếu tất cả Seller Order đều `CANCELLED` → Order tổng `CANCELLED`.
- Khi còn Seller Order chưa hủy, bỏ qua các Seller Order đã hủy khi tính trạng thái tổng.
- Nếu tất cả Seller Order còn hiệu lực đều `DELIVERED` → `DELIVERED`.
- Nếu có Seller Order `SHIPPED`/`DELIVERED` nhưng vẫn còn đơn chưa hoàn tất → trạng thái vận chuyển một phần (`PARTIALLY_SHIPPED`) hoặc `SHIPPED` khi tất cả đơn còn hiệu lực đã được gửi/đã giao, theo ma trận trạng thái được triển khai thống nhất.
- Nếu tất cả Seller Order còn hiệu lực đều `PENDING` → `PENDING`.
- Các trường hợp còn lại → `PROCESSING`.
- Không sử dụng `PARTIALLY_CANCELLED`; UI hiển thị các Seller Order đã hủy riêng.

## 4.6. Shipment và xác nhận giao hàng
- `seller_orders.status` phản ánh vòng đời xử lý đơn của Seller.
- `shipments.shipment_status` chỉ phản ánh tiến trình logistics.
- Khi chuyển `PROCESSING` → `SHIPPED`, Seller hoặc actor được phân quyền thực hiện quy trình gửi hàng: tạo/cập nhật Shipment, ghi mã vận đơn nếu có, sau đó cập nhật Seller Order.
- Xác nhận giao hàng MVP do Admin/operator được phân quyền thực hiện. Khi Shipment được xác nhận `DELIVERED`, OrderService kiểm tra điều kiện và chuyển Seller Order sang `DELIVERED`.
- Đây là cách làm đơn giản cho MVP/demo; xác nhận bởi Customer hoặc tự động từ carrier là hướng nâng cấp, chưa thuộc phạm vi hiện tại.

## 4.7. Thanh toán COD
- MVP chỉ hỗ trợ `COD`.
- Mỗi Seller Order có một Payment theo schema hiện tại.
- `PENDING`: chưa xác nhận thu tiền.
- `PAID`: đã xác nhận thu tiền, thông thường sau giao hàng thành công.
- `CANCELLED`: đơn bị hủy trước khi thu tiền.
- `FAILED`: thu tiền/thanh toán thất bại.
- `REFUNDED`: tiền đã thu và được hoàn lại.
- Không đánh dấu `PAID` chỉ vì đơn được tạo hoặc đã gửi hàng.
- Nếu Payment đã `PAID`, việc hủy cần quy trình refund, không dùng `CANCELLED`.

## 4.8. Chat
### MVP – REST API
1. Customer chọn Chat tại Store.
2. Backend tìm Conversation theo Customer và Store; nếu chưa có thì tạo.
3. Backend xác thực người gửi là thành viên Conversation.
4. Tin nhắn được lưu và trả về qua REST API.
5. Frontend tải lịch sử và polling để lấy tin mới.

### Mở rộng – WebSocket
WebSocket/STOMP chỉ triển khai sau MVP khi có nhu cầu realtime; phải xác thực kết nối và quyền gửi/subscribe.

## 4.9. AI tư vấn và tìm kiếm
MVP dùng quy trình hai bước:
1. LLM phân tích câu hỏi thành JSON filter có cấu trúc (ví dụ keyword/category/minPrice/maxPrice).
2. Backend kiểm tra/chuẩn hóa filter và truy vấn MySQL lấy 5–10 sản phẩm đang hoạt động, còn hàng.
3. LLM tạo câu trả lời dựa trên đúng kết quả sản phẩm Backend trả về.
4. API trả nội dung tư vấn cùng dữ liệu sản phẩm để Frontend hiển thị card.

Giới hạn:
- AI chỉ sử dụng catalog sản phẩm công khai của SmartShop; không dùng lịch sử mua hàng cá nhân trong MVP.
- AI không được tự bịa sản phẩm, giá hoặc tồn kho.
- AI không được thực hiện mutation như đặt hàng, sửa giá, sửa tồn kho.
- Không dùng Vector DB trong MVP.
- Backend phải xử lý timeout, rate limit, lỗi provider và fallback về tìm kiếm thường.
- Không đưa API key ra Frontend.

## 4.10. Review và chức năng mở rộng
- Review chỉ được tạo khi người dùng sở hữu Order Item tương ứng và Seller Order đã `DELIVERED`.
- Shipping provider API, online payment, coupon, WebSocket và các tính năng mở rộng khác chưa thuộc MVP.
