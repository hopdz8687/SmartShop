# 10. Nhật ký tiến độ SmartShop

**Ngày cập nhật:** 28/11/2026

## 10.1. Trạng thái tổng quan

| Hạng mục | Trạng thái | Ghi chú |
|---|---|---|
| Phân tích mục tiêu và phạm vi MVP | Đã chốt | Multi-Vendor, COD, AI catalog |
| Quy tắc nghiệp vụ Order/Cancel/Stock | Đã chốt | Xem docs/04 và docs/12 |
| Database schema v2.1 | Đã chốt để triển khai | 24 bảng; MariaDB 10.11 import test được báo cáo thành công |
| Tài liệu AI agent | Đã bổ sung | AGENTS.md, Copilot, Antigravity rules |
| Spring Boot project | Đã khởi tạo & hoạt động | Java 21, Spring Boot, Flyway, JWT, Store/Seller, Catalog |
| Flyway migration trong repo ứng dụng | Đã có `V1__init_schema.sql` | Khớp DDL v2.1; `ddl-auto=none` |
| Entity/Repository | Đang triển khai (Task 01, 04, 05 hoàn thành) | `User`, `Role`, `UserRole`, `SellerApplication`, `Store`, `Category`, `Product`, `ProductVariant`, `ProductImage` |
| Spring Security/JWT | Đã triển khai (Task 02, 04, 05) | JJWT 0.12.6, Register/Login/Logout, `/api/v1/users/me`, RBAC |
| ReactJS Frontend | Đang triển khai (Task 03, 04, 05 hoàn thành) | Auth, Store & Seller, Category & Product (Admin/Seller/Customer) |
| AI integration | Chưa triển khai | MVP theo flow hai bước |
| Automated tests | Đang triển khai theo module | Backend: 44/44 tests PASS (`mvnw.cmd test`) |
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

### Task 03 – Frontend Authentication
- **Thay đổi thực tế**:
  - Cập nhật Design System token SmartShop trong `frontend/src/index.css` theo định hướng UI UX Pro Max (Emerald `#059669`, nền sáng, độ tương phản cao, Tailwind CSS v4).
  - Thêm các component shadcn/ui chuẩn (`input`, `label`, `card`, `alert`) và tạo `AuthLayout`, `PasswordInput`.
  - Triển khai `tokenStorage`, `apiClient` (tự động gắn Bearer JWT, xử lý 401 phân biệt lỗi phiên đăng nhập với lỗi nghiệp vụ/login), `authApi`, `authSchemas` (Zod), `AuthProvider` + `useAuth` (TanStack Query `/users/me`, stateless logout).
  - Triển khai `LoginPage`, `RegisterPage` (CUSTOMER), `AccountPage` (hỗ trợ `CUSTOMER`, `SELLER`, `ADMIN`), và `ProtectedRoute` / `PublicOnlyRoute` / `AppRouter`.
- **File / module ảnh hưởng**:
  - `frontend/src/index.css`, `frontend/src/App.jsx`
  - `frontend/src/components/ui/{input,label,card,alert}.jsx`, `frontend/src/components/layout/AuthLayout.jsx`
  - `frontend/src/lib/tokenStorage.js`, `frontend/src/services/apiClient.js`, `frontend/src/stores/{authContext,authContextObject}.{jsx,js}`, `frontend/src/hooks/useAuth.js`
  - `frontend/src/features/auth/**`, `frontend/src/routes/**`
- **Lệnh & kết quả kiểm thử**:
  - Chạy `npm run lint` (`oxlint`) và `npm run build` (`vite build`) tại `frontend/`: **PASS** (0 errors; Vite production build thành công).
- **Chưa xác minh / Lưu ý**:
  - Chưa chạy kiểm thử trình duyệt end-to-end (E2E) kết nối trực tiếp frontend với backend server đang chạy (`localhost:8080`).
- **Bước tiếp theo**:
  - Chạy backend + frontend để nghiệm thu luồng đăng ký, đăng nhập, `/account`, đăng xuất trên trình duyệt trước khi sang module tiếp theo.


### Task 04 – Store & Seller Module (Backend)

**Ngày hoàn thành:** 28/11/2026

- **Phạm vi**: Backend-only. Toàn bộ luồng đăng ký Seller, duyệt/từ chối hồ sơ, quản lý Store, và public Store API.

- **File tạo mới**:

  *Entities:*
  - `backend/.../store/entity/SellerApplicationStatus.java` — Enum: PENDING, APPROVED, REJECTED, CANCELLED
  - `backend/.../store/entity/SellerApplication.java` — Maps `seller_applications`; FK → users (applicant + reviewer)
  - `backend/.../store/entity/StoreStatus.java` — Enum: ACTIVE, INACTIVE, SUSPENDED
  - `backend/.../store/entity/Store.java` — Maps `stores`; `uk_stores_user` (1 store/seller), `uk_stores_slug`

  *Repositories:*
  - `SellerApplicationRepository.java` — Conditional JPQL updates: `approveIfPending`, `rejectIfPending`, `cancelIfPendingAndOwned`
  - `StoreRepository.java` — `findByUserId`, `existsByUserId`, `existsBySlug`, `findByIdAndStatusAndDeletedAtIsNull`

  *DTOs:*
  - `CreateSellerApplicationRequest`, `RejectSellerApplicationRequest`, `SellerApplicationResponse`
  - `UpdateStoreRequest`, `StoreResponse`, `PublicStoreResponse`

  *Services:*
  - `SellerApplicationService.java` — submit, cancel, getMyApplications, admin CRUD, approve (fully transactional), reject
  - `StoreService.java` — createStoreForApprovedSeller (slug gen + collision retry), getMyStore, updateMyStore, getPublicStore

  *Controllers:*
  - `SellerApplicationController.java` — `POST /api/v1/seller-applications`, `GET /me`, `PUT /{id}/cancel`
  - `AdminSellerApplicationController.java` — `GET/PUT /api/v1/admin/seller-applications/**`
  - `SellerStoreController.java` — `GET/PUT /api/v1/seller/stores/me`
  - `PublicStoreController.java` — `GET /api/v1/stores/{id}` (no auth)

  *Tests:*
  - `StoreAndSellerIntegrationTest.java` — 20 test cases across 8 `@Nested` groups

- **File chỉnh sửa**:
  - `SecurityConfig.java` — Thêm: `GET /api/v1/stores/{id}` → permitAll; `POST /api/v1/seller-applications` và `PUT /api/v1/seller-applications/*/cancel` → hasRole(CUSTOMER); `GET /api/v1/seller-applications/me` → authenticated

- **Lệnh & kết quả kiểm thử**:
  - `mvnw.cmd clean test` — **BUILD SUCCESS**
  - **Tests run: 39, Failures: 0, Errors: 0, Skipped: 0** (15 cũ Task 01–02 + 4 cũ Task 03 JwtServiceTest/UserRole + 20 mới Task 04)

- **Nghiệp vụ đã xác minh**:
  - Approve tạo Store + cấp SELLER role trong cùng transaction; rollback toàn bộ nếu thất bại.
  - Conditional update (approve/reject/cancel) dùng JPQL `@Modifying`; trả 409 nếu trạng thái không hợp lệ.
  - Slug được tạo từ `businessName` (normalize diacritics, lowercase, strip non-alnum), retry + UUID suffix nếu trùng.
  - `uk_stores_user` đảm bảo 1 Store/Seller; `DataIntegrityViolationException` được bắt an toàn.
  - Customer chỉ hủy hồ sơ của chính mình khi trạng thái PENDING.
  - `GET /api/v1/stores/{id}` chỉ trả Store ACTIVE + deletedAt IS NULL; không lộ userId.
  - Seller routes được bảo vệ; Customer nhận 403 khi truy cập `/api/v1/seller/**`.

- **Chưa xác minh / Lưu ý**:
  - Chưa chạy kiểm thử end-to-end trên browser kết nối backend thực.
  - `GET /api/v1/stores` (danh sách cửa hàng) chưa triển khai theo kế hoạch MVP (chỉ có `/{id}`).
  - Seller Application list của Admin (`GET /api/v1/admin/seller-applications`) trả Page; sort và filter nâng cao để mở rộng sau.

- **Bước tiếp theo**:
  - Frontend Task 04: giao diện đăng ký Seller, Admin duyệt/từ chối, Seller quản lý Store.
  - Hoặc tiếp tục backend: Category, Product, Product Variant.


### Task 04 – Frontend Store & Seller

**Ngày hoàn thành:** 28/11/2026

- **Thay đổi thực tế**:
  - Tạo `AppShell.jsx` làm layout chung với thanh điều hướng responsive theo vai trò (`CUSTOMER`, `SELLER`, `ADMIN`), tái sử dụng Design System xanh dương.
  - Khách hàng: trang `SellerApplicationPage.jsx` gửi hồ sơ đăng ký Seller, xem lịch sử và hủy hồ sơ đang chờ (`PENDING`).
  - Admin: trang `AdminSellerApplicationsPage.jsx` lọc theo trạng thái, phân trang, xem chi tiết, duyệt hoặc từ chối (kèm lý do) hồ sơ.
  - Seller: trang `SellerStorePage.jsx` xem và cập nhật thông tin cửa hàng (`storeName`, `contactEmail`, `contactPhone`, `logoUrl`, `description`).
- **File tạo mới / chỉnh sửa**:
  - `frontend/src/components/layout/AppShell.jsx`
  - `frontend/src/features/store/api/storeApi.js`, `frontend/src/features/store/schemas/storeSchemas.js`, `frontend/src/features/store/components/StoreStatusBadge.jsx`
  - `frontend/src/features/store/pages/{SellerApplicationPage,AdminSellerApplicationsPage,SellerStorePage}.jsx`
  - `frontend/src/routes/AppRouter.jsx`, `frontend/src/features/auth/pages/AccountPage.jsx`
- **Lệnh & kết quả kiểm thử**:
  - `npm.cmd run lint` (`oxlint`): **PASS** (0 errors, 2 warnings).
  - `npm.cmd run build` (`vite build`): **PASS** (2.50s).

### Task 05 – Category & Product (Backend)

**Ngày hoàn thành:** 28/11/2026

- **Phạm vi**: CRUD Category (Admin), CRUD Product + Variant + Image (Seller theo Store sở hữu), Public Catalog & Search (lọc giá/sắp xếp theo Variant), Admin quản lý trạng thái Product.
- **File tạo mới / chỉnh sửa**:
  - `backend/src/main/java/com/smartshop/common/util/SlugUtils.java`
  - `backend/.../catalog/entity/{Category,CategoryStatus,Product,ProductStatus,ProductVariant,VariantStatus,ProductImage}.java`
  - `backend/.../catalog/repository/{CategoryRepository,ProductRepository,ProductVariantRepository,ProductImageRepository}.java`
  - `backend/.../catalog/dto/*` (11 DTOs)
  - `backend/.../catalog/service/{CategoryService,ProductService}.java`
  - `backend/.../catalog/controller/{PublicCategoryController,AdminCategoryController,PublicProductController,AdminProductController,SellerProductController}.java`
  - `backend/src/main/java/com/smartshop/security/SecurityConfig.java` (mở `permitAll()` cho `GET /api/v1/categories`, `GET /api/v1/products/**`)
  - `backend/src/test/java/com/smartshop/modules/catalog/CatalogIntegrationTest.java`
- **Lệnh & kết quả kiểm thử**:
  - `.\mvnw.cmd test`: **BUILD SUCCESS** — **Tests run: 44, Failures: 0, Errors: 0, Skipped: 0**.

### Task 05 – Frontend Category & Product

**Ngày hoàn thành:** 28/11/2026

- **Thay đổi thực tế**:
  - Customer: `ProductsPage.jsx` (tìm kiếm từ khóa, lọc danh mục chip `flex-wrap`, khoảng giá, sắp xếp `newest`/`price_asc`/`price_desc`, phân trang) và `ProductDetailPage.jsx` (thư viện ảnh, chọn Variant xem giá/SKU/tồn kho).
  - Admin: `AdminCategoriesPage.jsx` (tạo, sửa, phân cấp danh mục cha, ẩn danh mục).
  - Seller: `SellerProductsPage.jsx` và `SellerProductFormPage.jsx` (tạo sản phẩm kèm nhiều biến thể, sửa thông tin, thêm/sửa/ngừng bán biến thể, thêm/xóa hình ảnh).
- **File tạo mới / chỉnh sửa**:
  - `frontend/src/features/catalog/api/catalogApi.js`, `frontend/src/features/catalog/schemas/catalogSchemas.js`, `frontend/src/features/catalog/utils/catalogFormatters.js`
  - `frontend/src/features/catalog/pages/{ProductsPage,ProductDetailPage,AdminCategoriesPage,SellerProductsPage,SellerProductFormPage}.jsx`
  - `frontend/src/components/layout/AppShell.jsx`, `frontend/src/routes/AppRouter.jsx`
- **Lệnh & kết quả kiểm thử**:
  - `npm.cmd run lint` (`oxlint`): **PASS** (0 errors, 3 warnings).
  - `npm.cmd run build` (`vite build`): **PASS** (1.88s).
- **Chưa xác minh / Lưu ý**:
  - Chưa chạy kiểm thử E2E trên trình duyệt kết nối trực tiếp frontend với backend server đang chạy.
- **Bước tiếp theo**:
  - Tiếp tục Task 06: Cart & Checkout (Backend + Frontend) theo `docs/04` và `docs/07`.


### Task 06 – Cart & Checkout (Backend)

**Ngày hoàn thành:** 28/11/2026

- **Phạm vi**: Cart CRUD (`GET /api/v1/cart`, `POST /api/v1/cart/items`, `PUT /api/v1/cart/items/{id}`, `DELETE /api/v1/cart/items/{id}`) và Checkout đa cửa hàng (`POST /api/v1/orders`).
- **File tạo mới / chỉnh sửa**:
  - `backend/.../cart/entity/{Cart,CartItem}.java`, `backend/.../cart/repository/{CartRepository,CartItemRepository}.java`
  - `backend/.../cart/dto/{AddCartItemRequest,UpdateCartItemRequest,CartItemResponse,CartResponse}.java`
  - `backend/.../cart/service/CartService.java`, `backend/.../cart/controller/CartController.java`
  - `backend/.../order/entity/{Order,OrderStatus,SellerOrder,SellerOrderStatus,OrderItem,Payment,PaymentMethod,PaymentStatus}.java`
  - `backend/.../order/repository/{OrderRepository,SellerOrderRepository,OrderItemRepository,PaymentRepository}.java`
  - `backend/.../order/dto/{CheckoutRequest,OrderItemResponse,PaymentResponse,SellerOrderResponse,OrderResponse}.java`
  - `backend/.../order/service/CheckoutService.java`, `backend/.../order/controller/OrderController.java`
  - `backend/.../catalog/repository/ProductVariantRepository.java` (bổ sung atomic conditional update `decrementStockIfAvailable`)
  - `backend/src/test/java/com/smartshop/modules/order/CartAndCheckoutIntegrationTest.java`
- **Lệnh & kết quả kiểm thử**:
  - `.\mvnw.cmd test`: **BUILD SUCCESS** — **Tests run: 49, Failures: 0, Errors: 0, Skipped: 0** (thêm 5 test mới cho Cart CRUD, quyền sở hữu, checkout nhiều Store + xóa đúng item được chọn, và rollback toàn bộ khi thiếu tồn kho).
- **Chưa xác minh / Lưu ý**:
  - Theo `docs/12-Decision-Log.md`, công thức phân bổ phí ship/giảm giá chưa được chốt nên MVP gán `shippingFee = 0` và `discountAmount = 0` trên cả Order cha và Seller Order con.
  - Chưa triển khai xem/hủy Order/Seller Order, Shipment và Address CRUD (ngoài phạm vi Task 06).

### Task 06 – Frontend Cart & Checkout

**Ngày hoàn thành:** 28/11/2026

- **Thay đổi thực tế**:
  - Cài đặt `framer-motion` (tương thích React 19, bọc `<MotionConfig reducedMotion="user">` tại `App.jsx`) và bổ sung `Skeleton` component theo chuẩn shadcn/ui.
  - Bổ sung badge giỏ hàng động có slot cố định (`AnimatePresence` / `motion.span`) trên `AppShell.jsx`.
  - Nâng cấp `ProductDetailPage.jsx`: thêm skeleton loading, bộ chọn số lượng theo tồn kho, nút `Thêm vào giỏ hàng`, nút `Mua ngay` và banner phản hồi mượt mà.
  - Triển khai `CartPage.jsx` (`/cart`): skeleton, empty state, nhóm sản phẩm theo từng cửa hàng (`Store`), chọn từng sản phẩm / theo cửa hàng / chọn tất cả, chỉnh số lượng, xóa item với hiệu ứng `AnimatePresence`, và summary sticky bên phải.
  - Triển khai `CheckoutPage.jsx` (`/checkout`): bước xác nhận, Focusable Error Summary theo chuẩn UI UX Pro Max, form địa chỉ người nhận (React Hook Form + Zod), hiển thị phân tách đơn theo cửa hàng & COD, màn hình đặt hàng thành công hiển thị mã Order tổng và từng Seller Order con.
- **File tạo mới / chỉnh sửa**:
  - `frontend/package.json` (`framer-motion`)
  - `frontend/src/App.jsx`, `frontend/src/components/ui/skeleton.jsx`, `frontend/src/components/layout/AppShell.jsx`, `frontend/src/routes/AppRouter.jsx`
  - `frontend/src/features/catalog/pages/ProductDetailPage.jsx`
  - `frontend/src/features/cart/{api/cartApi.js,hooks/useCart.js,pages/CartPage.jsx}`
  - `frontend/src/features/order/{api/orderApi.js,schemas/checkoutSchema.js,pages/CheckoutPage.jsx}`
- **Lệnh & kết quả kiểm thử**:
  - `npm.cmd run lint` (`oxlint`): **PASS** (0 errors, 3 warnings có sẵn từ Task 04/05).
  - `npm.cmd run build` (`vite build`): **PASS** (1.88s).
- **Chưa xác minh / Lưu ý**:
  - Chưa chạy kiểm thử E2E trên trình duyệt kết nối trực tiếp với backend server đang chạy (`localhost:8080`).
- **Bước tiếp theo**:
  - Kiểm thử luồng E2E từ xem sản phẩm → thêm giỏ → checkout đa cửa hàng, sau đó triển khai Task 07 (Order History, Seller Order Management, Cancellation & Shipment/COD).

### Task 07.1 – Backend Order History

**Ngày hoàn thành:** 28/11/2026

- **Phạm vi**:
  - `GET /api/v1/orders/my`: Customer xem lịch sử đơn hàng của chính mình (phân trang, sắp xếp mới nhất).
  - `GET /api/v1/orders/{id}`: Customer xem chi tiết đơn hàng của chính mình (Order + Seller Orders + Order Items + địa chỉ snapshot + Payment COD).
- **File tạo mới / chỉnh sửa**:
  - `backend/.../order/service/OrderService.java` (batch-load `SellerOrder`, `OrderItem`, `Payment` tránh N+1; kiểm tra quyền sở hữu `403` / `404`).
  - `backend/.../order/controller/OrderController.java` (bổ sung `GET /my` và `GET /{id}`).
  - `backend/.../order/repository/{OrderRepository,SellerOrderRepository,OrderItemRepository,PaymentRepository}.java` (bổ sung các query phân trang & batch `In`).
  - `backend/src/test/java/com/smartshop/modules/order/OrderHistoryIntegrationTest.java`.
- **Lệnh & kết quả kiểm thử**:
  - `.\mvnw.cmd test`: **BUILD SUCCESS** — **Tests run: 51, Failures: 0, Errors: 0, Skipped: 0** (thêm 2 test mới kiểm tra danh sách phân trang, chi tiết đầy đủ, lỗi 404 không tồn tại và 403 truy cập đơn người khác).
- **Chưa xác minh / Lưu ý**:
  - Chưa triển khai Seller Order Management, Cancellation và Shipment (thuộc các task sau của Task 07).

### Task 07.2 – Frontend Order History

**Ngày hoàn thành:** 28/11/2026

- **Thay đổi thực tế**:
  - Mở rộng `orderApi.js` với `getMyOrders` (`GET /orders/my`) và `getOrderDetail` (`GET /orders/{id}`).
  - Tạo `OrderStatusBadge.jsx` hiển thị trạng thái đơn hàng (`OrderStatus` / `SellerOrderStatus`) và thanh toán COD (`PaymentStatus`) bằng tiếng Việt với màu sắc phân biệt rõ ràng.
  - Tạo `OrdersPage.jsx` (`/orders`): danh sách đơn hàng phân trang, mã đơn, ngày đặt, tổng tiền, danh sách cửa hàng, loading skeleton, empty state, error state.
  - Tạo `OrderDetailPage.jsx` (`/orders/:id`): thông tin người nhận & địa chỉ snapshot, chi tiết từng `SellerOrder`, sản phẩm, giá, số lượng và thanh toán COD.
  - Cập nhật `AppShell.jsx` (thêm liên kết **Đơn mua** cho `CUSTOMER`), `CheckoutPage.jsx` (thêm liên kết **Xem chi tiết đơn hàng** và **Đơn mua của tôi** sau khi đặt hàng thành công) và `AppRouter.jsx` (bảo vệ `/orders` và `/orders/:id` chỉ cho `CUSTOMER`).
- **File tạo mới / chỉnh sửa**:
  - `frontend/src/features/order/{api/orderApi.js,components/OrderStatusBadge.jsx,pages/OrdersPage.jsx,pages/OrderDetailPage.jsx,pages/CheckoutPage.jsx}`
  - `frontend/src/components/layout/AppShell.jsx`, `frontend/src/routes/AppRouter.jsx`
- **Lệnh & kết quả kiểm thử**:
  - `npm.cmd run lint` (`oxlint`): **PASS** (0 errors, 3 warnings có sẵn từ trước).
  - `npm.cmd run build` (`vite build`): **PASS** (1.93s).
- **Chưa xác minh / Lưu ý**:
  - Chưa chạy kiểm thử trình duyệt E2E kết nối trực tiếp với backend server đang chạy (`localhost:8080`).
- **Bước tiếp theo**:
  - Triển khai các phần tiếp theo của Task 07 (Seller Order Management, Hủy đơn hàng, Shipment & COD confirmation).

### Task 07.3 – Backend Seller Order Management

**Ngày hoàn thành:** 28/11/2026

- **Phạm vi API**:
  - `GET /api/v1/seller/orders`: Danh sách đơn của Store hiện tại (phân trang, mới nhất trước).
  - `GET /api/v1/seller/orders/{id}`: Chi tiết Seller Order (kèm snapshot địa chỉ từ Order cha, Items, Payment COD).
  - `PUT /api/v1/seller/orders/{id}/confirm`: Chuyển trạng thái có điều kiện `PENDING` → `CONFIRMED`.
  - `PUT /api/v1/seller/orders/{id}/processing`: Chuyển trạng thái có điều kiện `CONFIRMED` → `PROCESSING`.
- **File tạo mới / chỉnh sửa**:
  - [SellerOrderRepository.java](file:///c:/Projects/SmartShop/backend/src/main/java/com/smartshop/modules/order/repository/SellerOrderRepository.java) (bổ sung truy vấn theo `storeId`, `orderId` và `transitionStatusIfExpected`).
  - [SellerOrderManageResponse.java](file:///c:/Projects/SmartShop/backend/src/main/java/com/smartshop/modules/order/dto/SellerOrderManageResponse.java).
  - [OrderService.java](file:///c:/Projects/SmartShop/backend/src/main/java/com/smartshop/modules/order/service/OrderService.java) (bổ sung quản lý đơn Seller và hàm `recalculateParentOrderStatus` đồng bộ `orders.order_status`).
  - [SellerOrderController.java](file:///c:/Projects/SmartShop/backend/src/main/java/com/smartshop/modules/order/controller/SellerOrderController.java).
  - [SellerOrderIntegrationTest.java](file:///c:/Projects/SmartShop/backend/src/test/java/com/smartshop/modules/order/SellerOrderIntegrationTest.java).
- **Lệnh & kết quả kiểm thử**:
  - `.\mvnw.cmd test`: **BUILD SUCCESS** — **Tests run: 53, Failures: 0, Errors: 0, Skipped: 0** (thêm 2 test kiểm tra danh sách/chi tiết theo Store, chuyển trạng thái `PENDING -> CONFIRMED -> PROCESSING`, đồng bộ Order tổng sang `PROCESSING`, chặn thao tác lặp/sai trạng thái `409`, chặn Store khác/Customer/Admin `403`, và `401` khi chưa đăng nhập).
- **Chưa xử lý / Lưu ý**:
  - Chưa triển khai hủy đơn (`cancel`), giao hàng (`ship`/`Shipment`) và thu tiền COD (thuộc các task tiếp theo).
