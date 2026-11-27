# 11. Hướng dẫn triển khai SmartShop

## 11.1. Giai đoạn 0 – Chuẩn bị repository và quy tắc Agent
- Tạo repository với cấu trúc `smartshop-backend/`, `smartshop-frontend/`, `database/`, `docs/`.
- Đưa `AGENTS.md`, `.github/copilot-instructions.md` và `.agent/rules/smartshop-project.md` vào đúng vị trí ở root repository.
- Đặt SQL schema v2.1 đã duyệt trong `database/source/` để lưu bản import gốc.
- Tạo Flyway migration `smartshop-backend/src/main/resources/db/migration/V1__init_schema.sql` từ DDL đã duyệt; loại bỏ `CREATE DATABASE`, `USE smartshop` và các lệnh chỉ phục vụ import tay.
- Giữ bản gốc không chỉnh sửa; ghi rõ nguồn/phiên bản migration.

## 11.2. Giai đoạn 1 – Kiểm tra môi trường và khởi tạo Backend
Stack mục tiêu:
- Java 21.
- Maven.
- Spring Boot.
- Spring Web.
- Spring Data JPA.
- MariaDB Java Client hoặc MySQL Connector/J phù hợp môi trường thực tế.
- Flyway.
- Spring Validation.
- Spring Security (cấu hình khi bắt đầu Auth).
- JUnit 5 và Spring Boot Test.

Cấu hình nguyên tắc:
- Database URL, username, password lấy từ environment variables.
- `spring.jpa.hibernate.ddl-auto=none`.
- Bật Flyway và kiểm tra migration được áp dụng.
- Không commit `.env`, password hoặc JWT secret.
- Chọn dialect/driver phù hợp phiên bản Spring Boot và MariaDB đang dùng; không thêm cấu hình thừa theo mẫu cũ.


## 11.3. Giai đoạn 2 – Khởi tạo Frontend, Design System và UI

### Stack đã chốt
- ReactJS + Vite.
- Tailwind CSS.
- shadcn/ui.
- Lucide React.
- React Router.
- Axios.
- TanStack Query.
- React Hook Form + Zod.

### Quy tắc cài đặt
1. Kiểm tra `node --version`, `npm --version`, `package.json` và cấu trúc frontend trước khi thay đổi.
2. Dùng phiên bản package tương thích với toolchain hiện tại; không tự nâng cấp major version nếu không có lý do.
3. Cấu hình Tailwind theo đúng phiên bản được cài; không trộn hướng dẫn cấu hình giữa các major version.
4. Khởi tạo shadcn/ui, kiểm tra `components.json`, alias import và CSS entry.
5. Chỉ thêm các component shadcn/ui cần dùng; component source phải được quản lý trong repository.
6. Cài các dependencies còn thiếu; không cài trùng hoặc thêm thư viện UI khác nếu chưa được duyệt.
7. Chạy frontend dev server và build để xác minh.
8. Sau khi thống nhất API Contract tối thiểu, xây dựng Design System và các trang giao diện theo thứ tự ưu tiên bằng Mock Data/Mock Service.
9. Tách mock data khỏi API client để có thể thay bằng Axios + TanStack Query khi Backend sẵn sàng.

Ví dụ nhóm package runtime có thể cài sau khi kiểm tra project:
```bash
npm install lucide-react react-router-dom axios @tanstack/react-query react-hook-form zod @hookform/resolvers
```
Lệnh này không thay thế bước cấu hình riêng Tailwind CSS và shadcn/ui. Agent phải xác minh phiên bản và hướng dẫn cài đặt tương ứng trước khi chạy.

## 11.4. Giai đoạn 3 – Chuẩn bị AI Agent và browser tools

- Dùng một Agent chính ở chế độ Agent Mode truyền thống.
- Đặt `AGENTS.md` tại root để chứa quy tắc chung.
- Duy trì `.github/copilot-instructions.md` cho GitHub Copilot và `.agent/rules/smartshop-project.md` cho Antigravity.
- Cấu hình UI UX Pro Max Skill trong môi trường Agent được sử dụng. Nếu sử dụng cả Antigravity và Copilot, xác minh khả năng tương thích và cài/cấu hình riêng cho từng môi trường.
- Browser: ưu tiên công cụ browser có sẵn. Chỉ cài Playwright hoặc MCP browser khi cần browser automation, screenshot/interaction hoặc kiểm thử mà công cụ hiện có không đáp ứng.
- Không cài Multi-Agent, Autopilot, Ralph Loop hay plugin bổ sung theo mặc định.
- Không cấp quyền rộng không cần thiết; yêu cầu xác nhận trước các thao tác phá hủy, xóa dữ liệu, thay đổi schema, cài đặt hệ thống hoặc tác động bên ngoài.

## 11.5. Giai đoạn 4 – Smoke test toàn bộ môi trường

Chỉ đánh dấu hoàn thành khi kiểm tra thực tế:
- Backend build và khởi động được.
- Flyway chạy trên database development rỗng.
- Database schema/constraints được xác minh.
- Frontend dev server chạy và production build thành công.
- Tailwind CSS áp dụng style được.
- Một component shadcn/ui được render thành công.
- Router hoạt động.
- Agent đọc được `AGENTS.md` và các hướng dẫn riêng.
- Git status/commit hoạt động; secrets không bị track.

## 11.6. Giai đoạn 5 – Database smoke test
1. Tạo database dev rỗng.
2. Chạy ứng dụng để Flyway áp dụng V1.
3. Xác minh bảng và constraints.
4. Tạo integration test truy vấn `users`, `roles`, `user_roles`.
5. Không dùng Hibernate `update` để sửa schema nếu mapping sai; sửa mapping hoặc tạo migration mới sau khi được duyệt.

## 11.7. Giai đoạn 6 – User và Role
- Đọc DDL trước khi tạo Entity.
- Ánh xạ chính xác tên bảng/cột, nullability, length, precision, enum.
- Dùng `Long` cho signed BIGINT.
- Xác định cách ánh xạ bảng nối `user_roles`; không tự tạo thêm cột ID nếu schema dùng composite key.
- Không expose Entity trực tiếp từ Controller.
- Test đọc dữ liệu seed và các quan hệ.

## 11.8. Giai đoạn 7 – Authentication và Authorization
- Đăng ký Customer, đăng nhập, JWT.
- Password hash bằng BCrypt hoặc encoder phù hợp.
- Role names phải khớp dữ liệu role trong DB.
- Endpoint `/users/me` lấy user từ Security Context.
- Test token sai/hết hạn, role không đủ quyền, truy cập dữ liệu user khác.
- Không cấp role Seller trước khi hồ sơ được Admin duyệt.

## 11.9. Giai đoạn 8 – Store, Category, Product, Variant
- Seller Application và Admin approval.
- Store ownership.
- Category CRUD của Admin.
- Product/Variant CRUD của Seller.
- Flat Variant MVP; không triển khai dynamic attributes.
- Tồn kho theo Variant.
- Ảnh lưu ngoài DB; DB chỉ giữ URL/path.
- Kiểm tra Product có active Variant và đúng một default Variant.

## 11.10. Giai đoạn 9 – Cart và Order
- Một Cart persistent trên mỗi User.
- Cart Item gắn Variant.
- Checkout nhận selected `cartItemIds`.
- Server re-check giá/trạng thái/tồn kho.
- Tạo Order cha, Seller Order theo Store, Order Item snapshots và COD Payment.
- Atomic stock decrement.
- Transaction rollback toàn bộ khi lỗi.
- Chỉ xóa Cart Item được chọn sau khi checkout thành công.
- Viết test nhiều Store, thiếu kho, checkout đồng thời và rollback.

## 11.11. Giai đoạn 10 – Cancellation, Shipment và COD
- Conditional update đúng vai trò: Customer chỉ PENDING; Seller PENDING/CONFIRMED và chỉ Store của mình.
- Restore stock một lần sau update thành công, cùng transaction.
- OrderService tổng hợp Order cha.
- Shipment status không thay thế Seller Order status.
- MVP Admin/operator xác nhận DELIVERED; ghi nhận COD PAID khi có xác nhận thu tiền theo quyền được duyệt.

## 11.12. Giai đoạn 11 – Chat và AI
- Chat REST trước, kiểm tra thành viên Conversation.
- AI hai bước: LLM trích filter → Backend query catalog → LLM diễn đạt kết quả thật.
- Rate limit, timeout, fallback và không lộ API key.
- Không vector database trong MVP.

## 11.13. Giai đoạn 12 – Frontend-first, API-first và tích hợp

### Trước khi code giao diện
- Phân tích use case và dữ liệu cần hiển thị.
- Thống nhất API Contract tối thiểu: endpoint, method, request/response DTO, error shape, pagination, auth/role.
- Nếu Backend chưa có API thật, dùng Mock Data/Mock Service tách biệt; không hardcode dữ liệu giả trong component sản phẩm.
- Tổ chức route/layout theo Customer, Seller, Admin.
- Xây dựng Design System trước khi nhân rộng trang.

### Khi tích hợp
- Thay Mock Service bằng Axios API client.
- Dùng TanStack Query cho server state/cache; không tạo thêm cơ chế cache trùng lặp không cần thiết.
- Xử lý loading, empty, error, success và validation.
- Backend là nguồn quyết định quyền, giá, tồn kho, phí và tổng tiền; Frontend không được coi dữ liệu client là nguồn sự thật.
- Không tự đổi API contract; nếu phát hiện thiếu hoặc bất hợp lý, báo cáo tác động và đề xuất cập nhật đồng bộ.

## 11.14. Definition of Done cho mỗi module
Một module chỉ được coi là hoàn thành khi:
- Entity/Repository khớp DDL.
- Service xử lý nghiệp vụ và transaction phù hợp.
- Controller/API dùng DTO và validation.
- Authorization kiểm tra ở Backend.
- Có test happy path và negative path quan trọng.
- Build/test thực tế đã chạy và kết quả được ghi.
- Tài liệu API, workflow và progress log được cập nhật.
