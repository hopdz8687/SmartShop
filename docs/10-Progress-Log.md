# 10. Nhật ký tiến độ SmartShop

**Ngày cập nhật:** 26/11/2026

## 10.1. Trạng thái tổng quan

| Hạng mục | Trạng thái | Ghi chú |
|---|---|---|
| Phân tích mục tiêu và phạm vi MVP | Đã chốt | Multi-Vendor, COD, AI catalog |
| Quy tắc nghiệp vụ Order/Cancel/Stock | Đã chốt | Xem docs/04 và docs/12 |
| Database schema v2.1 | Đã chốt để triển khai | 24 bảng; MariaDB 10.11 import test được báo cáo thành công |
| Tài liệu AI agent | Đã bổ sung | AGENTS.md, Copilot, Antigravity rules |
| Spring Boot project | Chưa xác nhận đã khởi tạo | Chuẩn bị môi trường và khởi tạo sau khi kiểm tra toolchain |
| Flyway migration trong repo ứng dụng | Đã có `V1__init_schema.sql` | Khớp DDL v2.1; `ddl-auto=none` |
| Entity/Repository | Đang triển khai (Task 01 hoàn thành) | Đã hoàn thành `User`, `Role`, `UserRole` + Repository + Integration Test |
| Spring Security/JWT | Đã triển khai (Task 02 hoàn thành) | JJWT 0.12.6, Register/Login/Logout, `/api/v1/users/me`, RBAC |
| ReactJS Frontend | Chưa xác nhận đã khởi tạo | Công nghệ đã chốt; dự kiến triển khai UI trước bằng Mock Data sau API contract |
| AI integration | Chưa triển khai | MVP theo flow hai bước |
| Automated tests | Chưa triển khai | Phải thêm theo từng module |
| Docker/CI/CD/Deployment | Chưa triển khai | Để giai đoạn sau |

## 10.2. Các quyết định đã chốt
- Modular Monolith + Layered Architecture.
- Backend: Java 21, Spring Boot, Spring Data JPA/Hibernate.
- Frontend: ReactJS + Vite, Tailwind CSS, shadcn/ui, Lucide React, React Router, Axios, TanStack Query, React Hook Form + Zod.
- UI UX Pro Max là Skill hỗ trợ AI Agent, không phải dependency của website.
- Chỉ sử dụng một Agent chính theo Agent Mode truyền thống; không mặc định dùng Autopilot hoặc Multi-Agent.
- Một Seller sở hữu tối đa một Store trong MVP.
- Sản phẩm có Variant; tồn kho và giá ở cấp Variant.
- MVP dùng flat variant; attribute tables để mở rộng.
- Một checkout tạo một Order cha và một Seller Order cho mỗi Store.
- Payment COD, một Payment trên mỗi Seller Order theo schema hiện tại.
- Cart persistent; checkout theo selected `cartItemIds`.
- Snapshot địa chỉ/recipient ở Order; snapshot sản phẩm/variant/giá ở Order Item.
- Quy tắc hủy, hoàn kho, status aggregation, shipment, COD được mô tả trong docs/04.
- AI chỉ dùng catalog sản phẩm công khai; không vector DB trong MVP.
- Chat REST API trong MVP; WebSocket/STOMP để mở rộng.
- JPA `ddl-auto=none`, Flyway quản lý schema.

## 10.3. Kiểm thử database được báo cáo
Theo kết quả kiểm thử được cung cấp:
- DDL v2.1 import trên MariaDB 10.11 vào database mới, không lỗi.
- Có 24 bảng, 35 FK, 8 CHECK và 3 role.
- Đã thử CHECK tổng tiền với dữ liệu không hợp lệ bị chặn và dữ liệu hợp lệ được chấp nhận.
- `payments.payment_status` có `CANCELLED`.
- `orders.order_status` không có `PARTIALLY_CANCELLED`.
- Một seed Order tổng 888.000đ được quan sát tách thành hai Seller Order 418.000đ và 470.000đ.

Giới hạn: đây là kết quả được báo cáo từ lần chạy MariaDB 10.11, chưa phải xác nhận trực tiếp trên mọi máy XAMPP/MySQL. Seed data không chứng minh logic transaction, rollback hay chống overselling trong Backend.

## 10.4. Việc chưa làm / cần làm khi triển khai
- Đưa DDL v2.1 đã duyệt vào repository ứng dụng dưới dạng Flyway `V1__init_schema.sql` (bỏ lệnh `CREATE DATABASE`/`USE` dành cho import tay).
- Import/validate trên môi trường XAMPP thực tế.
- Viết lại hoặc rà soát Seed SQL đúng schema v2.1 nếu cần data demo.
- Chốt chi tiết actor xác nhận COD PAID trong API.
- Kiểm tra Product moderation workflow trước khi triển khai hiển thị sản phẩm.
- Hoàn thiện DTO/API errors/pagination khi code từng module.
- Kiểm thử transaction, rollback, concurrency, cancellation idempotency bằng automated tests.

## 10.5. Bước tiếp theo được chốt – chuẩn bị môi trường và triển khai

1. Kiểm tra toolchain trên máy phát triển: Java 21, Maven, Node.js/npm, Git và MySQL/MariaDB. Ghi nhận phiên bản thực tế; không cài trùng nếu đã có.
2. Khởi tạo repository và cấu trúc `backend/`, `frontend/`, `database/`, `docs/`; đặt `AGENTS.md`, `.github/copilot-instructions.md` và `.agent/rules/smartshop-project.md` ở root.
3. Khởi tạo Spring Boot với Java 21/Maven và dependencies cần thiết; cấu hình qua environment variables, không commit secrets.
4. Đưa DDL schema v2.1 đã duyệt vào `database/source/` và tạo Flyway migration phù hợp; `spring.jpa.hibernate.ddl-auto=none`.
5. Khởi tạo ReactJS + Vite; cấu hình Tailwind CSS và shadcn/ui theo hướng dẫn tương thích với phiên bản thực tế.
6. Cài các frontend dependencies đã chốt: `lucide-react`, `react-router-dom`, `axios`, `@tanstack/react-query`, `react-hook-form`, `zod`, `@hookform/resolvers`. Chỉ cài package còn thiếu; tránh cài trùng.
7. Cấu hình UI UX Pro Max Skill cho Agent được sử dụng. Antigravity và GitHub Copilot có thể cần cấu hình Skill/rules riêng. Browser/Playwright là tùy chọn; chưa cần cài MCP nếu công cụ browser có sẵn đáp ứng.
8. Smoke test frontend, backend, database, Tailwind, shadcn/ui và Git. Chỉ ghi nhận hoàn thành sau khi chạy và quan sát kết quả.
9. Phân tích nghiệp vụ và chốt API Contract/DTO trước khi tích hợp.
10. Triển khai theo hướng Frontend-first kết hợp API-first: tạo Design System và giao diện bằng Mock Data; sau đó triển khai Backend theo API Contract; cuối cùng tích hợp API và kiểm thử end-to-end.

## 10.6. Quyết định quy trình và công cụ AI đã thống nhất – 26/11/2026

- Mô hình làm việc: một Agent chính; ưu tiên Agent Mode truyền thống có người dùng giám sát và nghiệm thu.
- Không mặc định cài Multi-Agent, Autopilot, Ralph Loop hoặc nhiều plugin. Chỉ bổ sung khi có nhu cầu cụ thể, lợi ích rõ ràng và được người dùng đồng ý.
- UI UX Pro Max là Skill thiết kế UI/UX cho Agent.
- Browser tool: ưu tiên công cụ có sẵn; Playwright/MCP chỉ bổ sung khi cần kiểm thử trình duyệt tự động hoặc khả năng tích hợp còn thiếu.
- Antigravity và GitHub Copilot dùng chung source code và tài liệu dự án, nhưng cấu hình Skill/tool có thể phải thực hiện riêng theo từng môi trường.
- Không chạy hai IDE/Agent cùng lúc trên máy phát triển hiện tại; ưu tiên tiết kiệm RAM.

## 10.7. Thứ tự triển khai đã thống nhất

1. Kiểm tra và chuẩn bị môi trường/framework/dependencies/Agent instructions.
2. Smoke test các nền tảng.
3. Phân tích nghiệp vụ và API Contract.
4. Frontend Design System và giao diện với Mock Data.
5. Backend API và nghiệp vụ.
6. Tích hợp Frontend–Backend.
7. Kiểm thử, sửa lỗi, nghiệm thu và commit Git.

## 10.8. Quy tắc ghi tiến độ
Mỗi lần cập nhật phải ghi ngày, thay đổi thực tế, file/module ảnh hưởng, lệnh/test đã chạy, kết quả quan sát, vấn đề chưa xác minh và bước tiếp theo. Không đánh dấu hoàn thành chỉ vì code đã được sinh ra; phải build/test phù hợp.

---

## 10.9. Nhật ký triển khai – 28/11/2026

### Task 01 – User / Role / UserRole
- **Thay đổi thực tế**:
  - Tạo JPA Entity khớp DDL v2.1 (`V1__init_schema.sql`): `UserStatus`, `User` (`users`), `Role` (`roles`), `UserRoleId` (`@Embeddable`), `UserRole` (`user_roles` dùng `@EmbeddedId` + `@MapsId`, không thêm cột ID giả).
  - Tạo Repository: `UserRepository`, `RoleRepository`, `UserRoleRepository` (có `JOIN FETCH` tránh N+1).
  - Tạo Integration Test `UserRoleIntegrationTest` (`@Transactional(readOnly = true)`, dùng `Assumptions` kiểm tra sự tồn tại của dữ liệu, không xóa/reset `smartshop_dev`).
- **File / module ảnh hưởng**:
  - `backend/src/main/java/com/smartshop/modules/user/entity/{UserStatus,User,Role,UserRoleId,UserRole}.java`
  - `backend/src/main/java/com/smartshop/modules/user/repository/{UserRepository,RoleRepository,UserRoleRepository}.java`
  - `backend/src/test/java/com/smartshop/modules/user/UserRoleIntegrationTest.java`
- **Lệnh & kết quả kiểm thử**:
  - Chạy `mvnw.cmd clean test` tại `backend/`: **BUILD SUCCESS** — `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0` (`BackendApplicationTests`: 1/1 pass; `UserRoleIntegrationTest`: 3/3 pass trên datasource `smartshop_dev`).
- **Chưa xác minh / Lưu ý**:
  - Chưa triển khai thao tác ghi (insert/update) từ Java xuống DB trong task này; mới kiểm thử đọc dữ liệu seed với `@Transactional(readOnly = true)`.
- **Bước tiếp theo**:
  - Triển khai các Entity/Repository tiếp theo hoặc lớp Auth/JWT khi có chỉ định.

### Task 02 – Authentication & JWT
- **Thay đổi thực tế**:
  - Thêm `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (`0.12.6`) vào `backend/pom.xml`.
  - Triển khai `ApiResponse`, `BusinessException`/`DuplicateResourceException`/`ResourceNotFoundException` và `GlobalExceptionHandler`.
  - Triển khai `JwtProperties`, `CorsProperties`, `JwtService` (yêu cầu `JWT_SECRET` từ env >= 32 bytes, từ chối khởi động nếu thiếu/ngắn), `CustomUserDetails`, `CustomUserDetailsService`, `JwtAuthenticationFilter` (tải lại trạng thái + role từ DB trên mỗi request), `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `SecurityConfig` (stateless, CORS cấu hình explicit origin).
  - Triển khai `POST /api/v1/auth/register` (chỉ cấp `CUSTOMER` từ DB, BCrypt, `@Transactional`, bắt trùng email qua pre-check và `DataIntegrityViolationException`), `POST /api/v1/auth/login`, `POST /api/v1/auth/logout` (stateless), và `GET /api/v1/users/me`.
- **File / module ảnh hưởng**:
  - `backend/pom.xml`, `backend/src/main/resources/application.properties`, `backend/src/test/resources/application.properties`
  - `backend/src/main/java/com/smartshop/common/**`
  - `backend/src/main/java/com/smartshop/security/**`
  - `backend/src/main/java/com/smartshop/modules/auth/**`
  - `backend/src/main/java/com/smartshop/modules/user/{dto/UserResponse,service/UserService,controller/UserController}.java`
  - `backend/src/test/java/com/smartshop/security/JwtServiceTest.java`, `backend/src/test/java/com/smartshop/modules/auth/AuthControllerIntegrationTest.java`
- **Lệnh & kết quả kiểm thử**:
  - Chạy `mvnw.cmd clean test` tại `backend/`: **BUILD SUCCESS** — `Tests run: 15, Failures: 0, Errors: 0, Skipped: 0` (`BackendApplicationTests`: 1/1, `AuthControllerIntegrationTest`: 6/6, `UserRoleIntegrationTest`: 3/3, `JwtServiceTest`: 5/5).
- **Chưa xác minh / Lưu ý**:
  - Logout ở MVP là stateless (Client tự xóa token; chưa có blacklist/revocation phía server).
  - Refresh token và email verification chưa triển khai (ngoài phạm vi Task 02).
- **Bước tiếp theo**:
  - Triển khai các module tiếp theo theo kế hoạch (`Address`, `SellerApplication`/`Store`, `Category`/`Product`).
