# 12. Decision Log – SmartShop

**Ngày chốt phiên bản tài liệu:** 26/11/2026

Decision Log là nơi ghi các quyết định kỹ thuật/nghiệp vụ đã được thống nhất. AI agent phải tuân thủ các quyết định trạng thái **LOCKED**. Chỉ thay đổi khi người dùng phê duyệt rõ ràng.

## 12.1. Quyết định LOCKED

| ID | Chủ đề | Quyết định |
|---|---|---|
| ADR-001 | Kiến trúc | Modular Monolith + Layered Architecture |
| ADR-002 | Backend | Java 21, Spring Boot, Spring Data JPA/Hibernate |
| ADR-003 | Frontend | ReactJS + Vite |
| ADR-004 | Database | MySQL-compatible; môi trường hiện tại MariaDB/XAMPP |
| ADR-005 | Seller/Store | Một Seller account sở hữu tối đa một Store trong MVP |
| ADR-006 | Checkout | Một Order cha, một Seller Order cho mỗi Store |
| ADR-007 | Payment | COD only; một Payment cho mỗi Seller Order trong schema MVP |
| ADR-008 | Cart | Một Cart persistent/User; checkout theo selected `cartItemIds` |
| ADR-009 | Variant | Flat Variant MVP; giá/SKU/tồn kho ở Product Variant |
| ADR-010 | Snapshot | Snapshot người nhận ở Order; snapshot sản phẩm/variant/giá ở Order Item |
| ADR-011 | Cancellation | Customer chỉ hủy PENDING; Seller hủy PENDING/CONFIRMED trước shipment |
| ADR-012 | Inventory | Conditional atomic stock decrement; restore only after successful cancellation update |
| ADR-013 | Parent status | OrderService derive parent status từ non-cancelled Seller Orders |
| ADR-014 | Shipment | Shipment status logistics-only; Seller Order status là business lifecycle |
| ADR-015 | Delivery | MVP Admin/operator được phân quyền xác nhận DELIVERED |
| ADR-016 | Chat | REST API MVP; WebSocket/STOMP future |
| ADR-017 | AI | Chỉ dùng public catalog; 2-step LLM filter + DB query + LLM response |
| ADR-018 | AI data | Không dùng purchase history cá nhân; không vector DB MVP |
| ADR-019 | ORM schema | `spring.jpa.hibernate.ddl-auto=none`; Flyway quản lý schema |
| ADR-020 | Java mapping | signed BIGINT → Long; DECIMAL → BigDecimal; enum → EnumType.STRING |
| ADR-021 | Image storage | File ảnh ngoài MySQL; DB lưu URL/path và metadata |
| ADR-022 | Frontend UI stack | ReactJS + Vite, Tailwind CSS, shadcn/ui |
| ADR-023 | Frontend utilities | Lucide React, React Router, Axios, TanStack Query, React Hook Form + Zod |
| ADR-024 | AI coding workflow | Một Agent chính, Agent Mode truyền thống có human review; không mặc định Autopilot/Multi-Agent |
| ADR-025 | UI/UX guidance | UI UX Pro Max Skill; browser tool ưu tiên built-in, Playwright/MCP tùy nhu cầu |

## 12.2. Trạng thái Order và Payment

### Seller Order
`PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED`

Cancellation:
- Customer: chỉ từ `PENDING`.
- Seller: từ `PENDING` hoặc `CONFIRMED`, trước shipment.
- `CANCELLED` là trạng thái kết thúc.
- Chuyển trạng thái phải có điều kiện, kiểm tra quyền và transaction.

### Parent Order
Enum đã chốt: `PENDING`, `PROCESSING`, `PARTIALLY_SHIPPED`, `SHIPPED`, `DELIVERED`, `CANCELLED`. Không có `PARTIALLY_CANCELLED`.

OrderService tính trạng thái từ các Seller Order chưa hủy:
- Không còn Seller Order hợp lệ (tất cả cancelled) → CANCELLED.
- Tất cả còn lại DELIVERED → DELIVERED.
- Có shipment progress trong khi vẫn còn đơn chưa hoàn tất → PARTIALLY_SHIPPED hoặc SHIPPED theo ma trận cụ thể.
- Tất cả còn lại PENDING → PENDING.
- Còn lại → PROCESSING.

### Payment COD
- PENDING: chưa xác nhận thu tiền.
- PAID: đã xác nhận thu tiền.
- CANCELLED: hủy trước khi thu tiền.
- FAILED: thu tiền/thanh toán thất bại.
- REFUNDED: đã thu rồi hoàn lại.
- Đã PAID thì không được đổi thẳng sang CANCELLED.

## 12.3. Các quyết định schema v2.1
- 24 bảng vật lý.
- Xóa duplicate index `idx_seller_orders_cancelled_by`.
- Thêm `CANCELLED` vào `payments.payment_status`.
- Bỏ `PARTIALLY_CANCELLED` khỏi `orders.order_status`.
- Giữ snapshot tiền khi hủy một phần.
- Phí ship/discount được phân bổ ở Seller Order; tổng cha bằng tổng con.
- Schema có attribute tables nhưng MVP không triển khai dynamic attributes.
- Giá/tồn kho thuộc Variant, không phải Product.

## 12.4. Chưa quyết định đầy đủ – không tự suy đoán
1. Chính sách duyệt/kiểm duyệt Product trước khi hiển thị công khai.
2. Actor và quy trình chính xác xác nhận COD đã thu (`PAID`), ngoài nguyên tắc chỉ đánh dấu khi đã xác nhận thu tiền.
3. Quy tắc xử lý giao hàng thất bại, hoàn hàng, đổi trả.
4. Chi tiết công thức phân bổ shipping fee/discount (đã chốt cấp Seller Order, chưa chốt công thức).
5. AI có cho Guest dùng hay bắt buộc đăng nhập.
6. Nhà cung cấp AI và giới hạn chi phí/rate limit cụ thể.
7. Dịch vụ lưu ảnh production (hiện chỉ chốt lưu file ngoài DB).
8. Ma trận chi tiết trạng thái cha cho mọi tổ hợp Seller Order nếu cần khác quy tắc tổng quát ở trên.

Khi cần một quyết định chưa chốt, agent phải nêu câu hỏi hoặc đề xuất các lựa chọn có ưu/nhược điểm; không tự đóng quyết định.

## 12.5. Quy trình thay đổi quyết định
Mỗi thay đổi cần ghi:
- ID quyết định liên quan.
- Quyết định cũ và mới.
- Lý do.
- Bảng/API/module bị ảnh hưởng.
- Migration/data migration cần thiết.
- Test cần cập nhật.
- Ngày và người phê duyệt.


## 12.6. Quyết định công nghệ và quy trình mới – 26/11/2026

### ADR-022 – Frontend UI stack (LOCKED)
- ReactJS + Vite.
- Tailwind CSS cho styling, layout và responsive.
- shadcn/ui cho UI component source có thể tái sử dụng/tùy chỉnh.
- Không thêm Bootstrap hoặc UI framework khác nếu chưa được duyệt.

### ADR-023 – Frontend utilities (LOCKED)
- Lucide React: icons.
- React Router: client-side routing.
- Axios: HTTP client.
- TanStack Query: server-state/cache.
- React Hook Form + Zod: form và validation; dùng `@hookform/resolvers` để tích hợp.
- Chưa khóa số phiên bản package. Cần xác minh tương thích với toolchain thực tế trước khi cài.

### ADR-024 – AI coding workflow (LOCKED)
- Một Agent chính làm việc theo Agent Mode truyền thống.
- Người dùng giữ quyền quyết định phạm vi, nghiệp vụ, kiến trúc quan trọng và nghiệm thu.
- Không mặc định dùng Autopilot, Ralph Loop, Multi-Agent hoặc nhiều plugin. Chỉ cân nhắc khi có nhu cầu cụ thể và được người dùng duyệt.

### ADR-025 – UI/UX Skill và browser tools (LOCKED)
- UI UX Pro Max là Skill hỗ trợ Agent thiết kế UI/UX; không phải dependency runtime của website.
- Ưu tiên browser tool tích hợp sẵn. Playwright/MCP browser là tùy chọn, chỉ thêm khi có nhu cầu kiểm thử tự động cụ thể.
- Antigravity và Copilot dùng chung repository instructions nhưng có thể cần cài/cấu hình Skill và tool riêng cho từng môi trường.

### Quy trình triển khai đã thống nhất
Chuẩn bị toolchain/framework/dependencies và Agent instructions → smoke test → phân tích nghiệp vụ/API Contract → Frontend Design System/UI với Mock Data → Backend theo API Contract → tích hợp → kiểm thử và nghiệm thu.

