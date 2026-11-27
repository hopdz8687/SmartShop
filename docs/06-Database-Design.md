# 6. Thiết kế cơ sở dữ liệu – Schema v2.1

<badge> </badge>

> **Trạng thái:** DDL v2.1 được chốt làm nền tảng triển khai MVP. Không còn là bản thiết kế logic 19 bảng ở các bản tài liệu cũ. Danh sách dưới đây mô tả 24 bảng vật lý; tên cột, kiểu dữ liệu, constraint và enum chính xác phải lấy từ file SQL v2.1 đã duyệt.

## 6.1. Nguồn sự thật và phiên bản
- Nguồn sự thật về cấu trúc vật lý: file DDL `smartshop_schema_mysql_v2_1_fixed.sql` đã được duyệt.
- Database hiện hướng tới MariaDB/XAMPP; kết quả import trên MariaDB 10.11 được báo cáo thành công.
- Số liệu kiểm tra được báo cáo: 24 bảng, 35 FK, 8 CHECK, 3 role.
- Chưa được suy diễn rằng mọi môi trường MySQL/MariaDB khác đều đã được kiểm thử.
- Không chỉnh sửa migration đã áp dụng; thay đổi sau này phải tạo migration mới.

## 6.2. Danh sách 24 bảng vật lý

### A. Tài khoản và địa chỉ
1. `users`
2. `roles`
3. `user_roles`
4. `addresses`

### B. Seller và Store
5. `seller_applications`
6. `stores`

### C. Catalog và Variant
7. `categories`
8. `products`
9. `product_attributes`
10. `product_attribute_values`
11. `product_variants`
12. `product_variant_attribute_values`
13. `product_images`

### D. Cart
14. `carts`
15. `cart_items`

### E. Order và thanh toán
16. `orders`
17. `seller_orders`
18. `order_items`
19. `payments`

### F. Vận chuyển
20. `shipping_providers`
21. `shipments`

### G. Review và Chat
22. `reviews`
23. `conversations`
24. `messages`

## 6.3. Các quan hệ nghiệp vụ quan trọng
- `users` ↔ `roles`: nhiều-nhiều qua `user_roles`.
- `users` → `stores`: tối đa một Store trên một tài khoản trong MVP.
- `stores` → `products`: một-nhiều.
- `products` → `product_variants`: một-nhiều.
- `products` → `product_images`: một-nhiều; ảnh có thể gắn Variant theo DDL.
- `users` → `carts`: một-một trong MVP.
- `carts` → `cart_items`: một-nhiều; mỗi cart item tham chiếu Variant.
- `users` → `orders`: một-nhiều.
- `orders` → `seller_orders`: một-nhiều.
- `stores` → `seller_orders`: một-nhiều.
- `seller_orders` → `order_items`: một-nhiều.
- `seller_orders` → `payments`: một-một theo schema MVP hiện tại.
- `seller_orders` → `shipments`: một-một theo schema hiện tại.
- `conversations` thuộc một Customer và một Store; `messages` thuộc Conversation và có Sender.
- Review tham chiếu người dùng, sản phẩm và Order Item theo DDL.

## 6.4. Các quyết định ánh xạ và triển khai
- SQL `BIGINT` signed → Java `Long`.
- Tiền `DECIMAL` → Java `BigDecimal`; không dùng `double`/`float` cho tiền.
- Enum trong Java dùng `@Enumerated(EnumType.STRING)`; chuỗi enum phải khớp DDL.
- `spring.jpa.hibernate.ddl-auto=none`.
- Flyway quản lý schema. DDL khởi tạo được đặt trong migration đầu tiên sau khi loại bỏ các lệnh dành cho import tay như `CREATE DATABASE` và `USE`.
- Không để Hibernate tự tạo/cập nhật schema.
- Không sửa file migration đã áp dụng; tạo migration version mới.
- Ảnh lưu ngoài MySQL (local storage trong dev hoặc dịch vụ object/image hosting sau này); DB lưu URL/path và metadata.

## 6.5. Flat Variant trong MVP
Schema có các bảng attribute để hỗ trợ cấu hình thuộc tính sản phẩm về sau. Tuy nhiên, MVP triển khai flat variant:
- Dùng `product_variants.variant_name`, SKU, price và stock.
- Không triển khai giao diện quản lý Product Attributes/Attribute Values trong MVP.
- Chưa cần tạo JPA Entity/repository/service cho các bảng attribute nếu không có yêu cầu mới.
- Product phải có ít nhất một Variant active và đúng một default Variant; kiểm tra trong Service.
- Mọi thao tác giỏ hàng, checkout, tồn kho và Order Item đều tham chiếu Variant, không tham chiếu tồn kho tổng ở Product.

## 6.6. Order, snapshot và Payment
- Order lưu snapshot thông tin người nhận và địa chỉ giao hàng tại thời điểm checkout.
- Order Item lưu snapshot tên sản phẩm, ảnh, mô tả Variant, SKU, đơn giá, số lượng và subtotal.
- Snapshot không thay đổi khi người dùng sửa Address hoặc Seller cập nhật Product sau đó.
- Một checkout tạo một Order cha và một Seller Order cho mỗi Store.
- Payment hiện gắn với Seller Order, một bản ghi mỗi Seller Order trong MVP; chỉ COD.
- Khi hủy một Seller Order, không sửa snapshot tiền ban đầu. Số tiền còn phải thu tính từ các Seller Order chưa hủy.
- Phân bổ phí ship/giảm giá ở cấp Seller Order; tổng cha phải bằng tổng các con.

## 6.7. Enum và trạng thái đã chốt
- `payments.payment_status` có `PENDING`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED`.
- `orders.order_status` có `PENDING`, `PROCESSING`, `PARTIALLY_SHIPPED`, `SHIPPED`, `DELIVERED`, `CANCELLED`; không có `PARTIALLY_CANCELLED`.
- Seller Order business lifecycle: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.
- Shipment status là trạng thái logistics riêng, không thay thế Seller Order status.
- Đối chiếu DDL để biết enum chính xác của từng cột; không tự thêm giá trị.

## 6.8. Ràng buộc cần kiểm tra ở Service
Một số quy tắc liên bảng khó thể hiện bằng FK đơn giản:
- Attribute Value được gắn vào Variant phải thuộc cùng Product với Variant đó.
- Variant gắn vào Product Image phải thuộc đúng Product của ảnh.
- Review chỉ hợp lệ nếu người dùng sở hữu Order Item và Seller Order đã giao thành công.
- Product có ít nhất một Variant active và đúng một default Variant.
- Seller chỉ thao tác trên Product/Order của Store mình.
- Customer chỉ thao tác trên Cart Item/Order của chính mình.

## 6.9. Tính toàn vẹn khi checkout và hủy
- Checkout, tạo Order/Seller Order/Order Item/Payment, trừ tồn kho và xóa Cart Item đã chọn phải nằm trong một transaction.
- Chống overselling bằng atomic conditional update trên `product_variants.stock_quantity`; nếu affected rows = 0 thì rollback.
- Xử lý Variant ID tăng dần để giảm nguy cơ deadlock.
- Hủy Seller Order dùng conditional update; chỉ hoàn kho sau khi cập nhật trạng thái thành công, trong cùng transaction.
- Đảm bảo thao tác hủy lặp không hoàn kho hai lần.

## 6.10. Lịch sử thay đổi
- v1: thiết kế logic ban đầu, 19 bảng dự kiến; không dùng làm nguồn sự thật vật lý.
- v2: schema vật lý được chỉnh sửa.
- v2.1: loại index trùng `idx_seller_orders_cancelled_by`; thêm `CANCELLED` vào `payments.payment_status`; bỏ `PARTIALLY_CANCELLED` khỏi `orders.order_status`; cập nhật khối quy tắc nghiệp vụ cuối SQL.
- Kiểm thử MariaDB 10.11 được báo cáo: import mới thành công, 24 bảng, 35 FK, 8 CHECK, 3 role; CHECK tổng tiền được kiểm tra bằng ví dụ insert hợp lệ/không hợp lệ.
- Chưa khẳng định đã kiểm thử trên mọi phiên bản XAMPP/MySQL. Trước khi phát triển local, import/validate trên môi trường thực tế.

## 6.11. Không được làm
- Không dùng lại mô hình cũ 19 bảng.
- Không tạo `products.price` hoặc `products.stock` theo thiết kế cũ nếu DDL v2.1 không có các cột đó.
- Không giả định Cart Item tham chiếu Product trực tiếp; đối chiếu DDL, MVP sử dụng Variant.
- Không tạo Payment cấp Order cha hoặc nhiều payment retry trong MVP.
- Không thêm `PARTIALLY_CANCELLED`.
- Không tự đổi tên bảng/cột/enum để thuận tiện cho Java.
