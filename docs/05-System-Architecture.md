# 5. Thiết kế kiến trúc hệ thống

## 5.1. Định hướng

Đề xuất **Modular Monolith kết hợp Layered Architecture**.

- Modular Monolith: một ứng dụng Spring Boot, chia thành module nghiệp vụ.
- Layered Architecture: tách Controller, Service, Repository và Entity/DTO.
- REST API: giao tiếp chính giữa ReactJS và Spring Boot.
- WebSocket: bổ sung cho chat realtime khi cần.

Phù hợp với quy mô thực tập hơn Microservices, đồng thời vẫn có ranh giới module rõ ràng.

## 5.2. Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Frontend framework/build | ReactJS, Vite |
| Styling | Tailwind CSS |
| UI components | shadcn/ui |
| Icons | Lucide React |
| Routing | React Router |
| HTTP client | Axios |
| Server state/cache | TanStack Query |
| Forms | React Hook Form + Zod |
| AI design guidance | UI UX Pro Max Skill (agent-side, not app runtime) |
| Backend | Java 21, Spring Boot |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL-compatible; development target MariaDB/XAMPP |
| Authentication | Spring Security, JWT |
| API | RESTful API |
| Chat MVP | REST API |
| Chat mở rộng | Spring WebSocket, STOMP |
| AI | External AI API |
| Version Control | Git, GitHub |
| API Testing | Postman / Thunder Client |
| Unit Testing | JUnit, Mockito |
| Deployment mở rộng | Docker |

## 5.3. Các lớp Backend
- Controller: nhận request và trả response.
- Service: xử lý nghiệp vụ và transaction.
- Repository: truy cập dữ liệu.
- Entity: ánh xạ bảng.
- DTO: dữ liệu đầu vào/đầu ra.
- Mapper: chuyển đổi Entity/DTO.
- Security: xác thực và phân quyền.
- Common: exception, response chung và tiện ích.

## 5.4. Module nghiệp vụ
`auth`, `user`, `store`, `category`, `product`, `cart`, `order`, `chat`, `ai`; các module `review`, `shipping`, `promotion` là phần mở rộng.

## 5.5. Cấu trúc Backend tham khảo

```text
smartshop-backend/
└── src/main/java/com/smartshop/
    ├── config/
    ├── security/
    ├── common/
    │   ├── exception/
    │   ├── response/
    │   └── util/
    ├── modules/
    │   ├── auth/
    │   ├── user/
    │   ├── store/
    │   ├── category/
    │   ├── product/
    │   ├── cart/
    │   ├── order/
    │   ├── chat/
    │   ├── ai/
    │   ├── review/
    │   ├── shipping/
    │   └── promotion/
    └── SmartShopApplication.java
```

Mỗi module có thể chứa `controller`, `service`, `repository`, `entity`, `dto`, `mapper`. Không cần tạo module mở rộng trước khi triển khai.

## 5.6. Cấu trúc Frontend tham khảo

```text
smartshop-frontend/
└── src/
    ├── assets/
    ├── components/
    ├── layouts/
    ├── pages/
    │   ├── customer/
    │   ├── seller/
    │   └── admin/
    ├── services/
    ├── hooks/
    ├── contexts/
    ├── routes/
    └── utils/
```

## 5.7. Bảo mật và tích hợp
- Băm mật khẩu bằng thuật toán phù hợp, ví dụ BCrypt.
- Bảo vệ endpoint theo vai trò và quyền sở hữu.
- Seller chỉ truy cập dữ liệu Store của mình.
- Chat kiểm tra thành viên ở REST API và WebSocket.
- Không đưa AI API key vào Frontend hoặc repository.
- Backend chỉ gửi dữ liệu sản phẩm cần thiết đến AI.
- Tạo đơn và cập nhật tồn kho phải nhất quán bằng transaction.

## 5.8. Mở rộng
Có thể nâng cấp polling chat sang WebSocket mà giữ Conversation/Message; bổ sung Review, Shipping, Promotion, Payment; thêm Docker/CI/CD sau khi ứng dụng ổn định.

---

## Cập nhật quyết định kiến trúc – 25/11/2026

- Chat MVP dùng REST API; WebSocket/STOMP chỉ triển khai khi cần realtime.
- Thanh toán MVP chỉ COD; chưa tích hợp cổng thanh toán online.
- AI chỉ nhận dữ liệu catalog sản phẩm SmartShop đã được Backend truy xuất và chọn lọc.
- Checkout, tạo Order/Seller Order/Order Item/Payment và cập nhật tồn kho phải chạy trong transaction.
- Cần xử lý đồng thời để tránh overselling; có thể dùng pessimistic locking hoặc chiến lược optimistic locking phù hợp sau khi thiết kế chi tiết.


## 5.9. Quyết định triển khai đã khóa – 26/11/2026
- Database schema v2.1 là nguồn sự thật vật lý; xem `docs/06-Database-Design.md`.
- Hibernate không quản lý schema: `spring.jpa.hibernate.ddl-auto=none`.
- Flyway quản lý migration; không sửa migration đã áp dụng.
- Java mapping: signed BIGINT → Long; DECIMAL → BigDecimal; enum → String.
- Checkout/cancel/inventory logic phải nằm trong Service transaction.
- Xem `AGENTS.md` để biết các invariant bắt buộc đối với AI coding agent.


## 5.10. Frontend implementation stack – quyết định ngày 26/11/2026

Frontend MVP sử dụng bộ công nghệ sau:

| Công nghệ | Vai trò |
|---|---|
| ReactJS + Vite | Framework UI và công cụ phát triển/build |
| Tailwind CSS | Utility-first styling, layout và responsive |
| shadcn/ui | Các UI component có source code trong dự án, có thể tùy chỉnh |
| Lucide React | Icon |
| React Router | Điều hướng phía client |
| Axios | HTTP client gọi Spring Boot REST API |
| TanStack Query | Server-state, cache và trạng thái request |
| React Hook Form | Quản lý form |
| Zod | Schema validation; kết hợp `@hookform/resolvers` với React Hook Form |
| UI UX Pro Max | Skill hướng dẫn AI Agent về UI/UX; không phải dependency runtime của frontend |

### Nguyên tắc sử dụng
- Tailwind CSS và shadcn/ui là hai lớp bổ trợ: Tailwind xử lý styling; shadcn/ui cung cấp component nền để tái sử dụng và tùy chỉnh.
- Không thêm Bootstrap hoặc một UI framework khác nếu chưa có lý do và phê duyệt.
- Ưu tiên component shadcn/ui phù hợp; không cài toàn bộ component nếu không dùng.
- Tránh tạo class Tailwind động không thể được scanner phát hiện; dùng ánh xạ class đầy đủ, tĩnh.
- Agent phải đọc `package.json`, cấu hình Vite/Tailwind và `components.json` (nếu có) trước khi cài hoặc nâng cấp package.
- Chưa khóa số phiên bản package trong tài liệu này. Agent phải kiểm tra tính tương thích với phiên bản Node.js, React, Vite và Tailwind đang dùng; không tự nâng major version hoặc thay đổi toolchain không cần thiết.
