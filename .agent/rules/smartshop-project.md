# SmartShop Workspace Rule (Antigravity)

Always read root `AGENTS.md` before editing code. Treat it as the shared project contract for all agents.

SmartShop is a Java 21 / Spring Boot / Spring Data JPA backend, ReactJS + Vite frontend using Tailwind CSS and shadcn/ui, and MySQL-compatible multi-vendor e-commerce MVP. Follow the approved schema v2.1 and locked business decisions in `docs/`.

Do not invent or silently rename database tables/columns/enums. Inspect the approved SQL DDL before creating Entities or queries. Do not change business rules, add MVP scope, or modify an applied Flyway migration without explicit user approval.

Critical invariants:
- One parent Order per checkout; one Seller Order per Store.
- COD only in MVP; one Payment per Seller Order in current schema.
- Stock is per Product Variant; use conditional atomic decrement and rollback on insufficient stock.
- Customer cancellation only for PENDING and only their own parent Order.
- Seller cancellation only own Store's PENDING/CONFIRMED Seller Order before shipment.
- Restore stock only after successful conditional cancellation, in the same transaction.
- Keep order money snapshots unchanged after partial cancellation.
- Seller Order status is business lifecycle; Shipment status is logistics.
- Order parent status is derived by OrderService from non-cancelled Seller Orders.
- `ddl-auto=none`, Flyway for schema changes, `Long` for signed BIGINT, `BigDecimal` for money, `EnumType.STRING`.

Implement incrementally, test actual behavior, report unverified items honestly, and update progress documentation. Root `AGENTS.md` is the detailed source for all rules.


Quy tắc Frontend :
- Sử dụng Tailwind CSS + shadcn/ui làm nền tảng giao diện chính; không thêm Bootstrap hoặc framework UI khác nếu chưa được phê duyệt.
- Tuân thủ UI UX Pro Max Skill đã được cài đặt khi phát triển Frontend. Tham khảo các hướng dẫn phù hợp với từng nhiệm vụ và duy trì tính nhất quán trong thiết kế.
- Các thư viện hỗ trợ đã được chấp thuận: Lucide React, React Router, Axios, TanStack Query, React Hook Form, Zod và @hookform/resolvers.
- Được phép sử dụng thêm thư viện nếu mang lại giá trị rõ ràng, tương thích với công nghệ hiện tại và thực sự cần thiết.
- Kiểm tra các package và cấu hình hiện có trước khi cài đặt hoặc nâng cấp thư viện.
- Tuân thủ Design System hiện tại của SmartShop. Duy trì nhận diện thương hiệu xanh dương, màu sắc, kiểu chữ và bố cục responsive.
- Ưu tiên tái sử dụng component, layout, hook và API client hiện có.
- Tách biệt Mock Data khỏi API client sử dụng dữ liệu thực tế.
- Không chỉnh sửa Backend hoặc tự mở rộng nghiệp vụ trong các nhiệm vụ Frontend nếu chưa được yêu cầu.
- Ưu tiên sử dụng một Agent chính ở chế độ Agent Mode truyền thống; chỉ sử dụng plugin tự động hóa trình duyệt khi có nhu cầu cụ thể.
- Sau khi triển khai, chạy các bước kiểm tra lint và build phù hợp; báo cáo trung thực các cảnh báo và chức năng chưa được xác minh.
- Ưu tiên giao diện hiện đại, chuyên nghiệp, có tính thẩm mỹ cao, tương đương các website thương mại điện tử chất lượng cao.
- Chủ động sử dụng animation, micro-interactions, hover effects, transitions và hiệu ứng chuyển cảnh mượt mà khi phù hợp.
- Tạo chiều sâu giao diện bằng shadow, gradient, border, backdrop blur và các hiệu ứng thị giác tinh tế; tránh lạm dụng.
- Chú trọng typography, khoảng cách, căn chỉnh, màu sắc, phân cấp nội dung và tính nhất quán.
- Thiết kế responsive hoàn chỉnh cho Desktop, Tablet và Mobile.
- Các thao tác như hover, click, loading, success, error và empty state cần có phản hồi trực quan.
- Ưu tiên trải nghiệm người dùng, khả năng sử dụng và hiệu năng; không đánh đổi tốc độ tải trang để lấy hiệu ứng.
- Tránh giao diện đơn điệu, bố cục sơ sài và việc sử dụng component mặc định một cách máy móc.
- Có thể sử dụng thêm thư viện animation như Framer Motion hoặc thư viện phù hợp khác nếu thực sự cần thiết.

## Progress Log – Bắt buộc cập nhật

Sau khi hoàn thành một Task hoặc sửa lỗi đáng kể:

- Cập nhật `docs/10-Progress-Log.md`, không tạo file log trùng lặp.
- Ghi ngày, phạm vi hoàn thành, file/module thay đổi.
- Ghi lệnh kiểm tra và kết quả thực tế.
- Nêu rõ phần chưa kiểm thử hoặc chưa xác minh.
- Ghi bước tiếp theo.
- Chỉ đánh dấu hoàn thành khi kiểm tra phù hợp đã PASS.
- Chỉ bổ sung mục mới, không viết lại toàn bộ lịch sử.
- Không tự ý thay đổi quyết định nghiệp vụ đã chốt.