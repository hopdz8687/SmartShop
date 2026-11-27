# SmartShop – AI Powered Multi-Vendor E-commerce

> **Tài liệu dự án dành cho người phát triển và AI coding agents (Antigravity, GitHub Copilot và các công cụ tương thích).**
>
> **Trạng thái tại 26/11/2026:** Thiết kế MVP và schema database v2.1 đã được chốt để bắt đầu triển khai. Backend/Frontend SmartShop chưa được xem là đã hoàn thành.

## 1. Đọc tài liệu theo thứ tự

1. `AGENTS.md` – quy tắc bắt buộc cho mọi AI agent.
2. `docs/01-Project-Overview.md` – mục tiêu và phạm vi.
3. `docs/02-Requirements.md` – yêu cầu và quy tắc nghiệp vụ.
4. `docs/04-Business-Workflows.md` – luồng nghiệp vụ đã chốt.
5. `docs/05-System-Architecture.md` – kiến trúc và công nghệ.
6. `docs/06-Database-Design.md` – schema vật lý v2.1 và quy tắc ánh xạ.
7. `docs/07-API-Documentation.md` – API dự kiến, những phần cần đặc tả khi code.
8. `docs/09-Test-Plan.md` – tiêu chí kiểm thử.
9. `docs/10-Progress-Log.md` – trạng thái thực tế và bước tiếp theo.
10. `docs/11-Implementation-Guide.md` và `docs/12-Decision-Log.md` – hướng dẫn triển khai và quyết định đã chốt.
11. `docs/13-AI-Agent-Workflow.md` – quy trình giao việc, giám sát và nghiệm thu AI Agent.

## 2. Quy tắc nguồn sự thật

Khi có thông tin mâu thuẫn, áp dụng thứ tự ưu tiên:

1. **DDL SQL schema v2.1 thực tế** là nguồn sự thật về tên bảng, tên cột, kiểu dữ liệu, enum, PK/FK, index và CHECK.
2. Các quyết định nghiệp vụ đã chốt trong `docs/02`, `docs/04`, `docs/06` và `docs/12`.
3. Tài liệu API/Use Case là đặc tả chức năng; nếu chưa đủ chi tiết thì phải ghi nhận câu hỏi, không tự suy đoán.
4. Code hiện có chỉ là nguồn sự thật về hành vi đã triển khai; nếu code lệch tài liệu thì báo cáo và đề xuất thay đổi, không tự âm thầm đổi nghiệp vụ.

**Không được tự tạo cột/bảng/enum hoặc thay đổi schema chỉ để làm code dễ hơn.** Nếu phát hiện thiếu sót, mô tả vấn đề, tác động, phương án và chờ người dùng xác nhận trước khi tạo migration mới.

## 3. Cấu trúc tài liệu

- `docs/01` đến `docs/10`: tài liệu phân tích, thiết kế, kiểm thử và tiến độ.
- `docs/11`: quy trình triển khai theo từng giai đoạn.
- `docs/12`: decision log – các quyết định đã khóa và các điểm chưa quyết định.
- `docs/13`: quy trình làm việc với AI coding Agent.
- `docs/diagrams`: sơ đồ Mermaid.
- `AGENTS.md`: chỉ dẫn chung cho AI agent.
- `.github/copilot-instructions.md`: hướng dẫn GitHub Copilot.
- `.agent/rules/smartshop-project.md`: quy tắc workspace cho Antigravity.

## 4. Database và Flyway

Schema v2.1 đã được báo cáo import kiểm thử thành công trên MariaDB 10.11 với 24 bảng, 35 FK, 8 CHECK và 3 role. Kết quả này chưa thay thế việc xác nhận trên máy phát triển thực tế.

Khi tích hợp Flyway:
- Đặt DDL đã được duyệt thành migration đầu tiên, ví dụ `V1__init_schema.sql`.
- File migration chạy trên database đã được cấu hình trong Spring Boot; không đưa `CREATE DATABASE`, `USE smartshop` vào migration.
- Không sửa migration đã được áp dụng. Mọi thay đổi schema sau này phải là migration mới.
- `spring.jpa.hibernate.ddl-auto=none`.
- Không bật `create`, `create-drop` hoặc `update` trên database dự án.

**Lưu ý:** ZIP tài liệu này không thay thế file DDL SQL. Đặt file SQL v2.1 đã được duyệt vào thư mục `database/` của repository ứng dụng và giữ nguyên phiên bản/nguồn gốc.

## 5. Trạng thái dự án

- Đã chốt: mô hình Multi-Vendor, MVP COD, Order/Seller Order, flat variant cho MVP, quy tắc hủy/hoàn kho, snapshot và cách tổng hợp trạng thái.
- Đã có: tài liệu Markdown và schema SQL v2.1.
- Frontend stack đã chốt: ReactJS + Vite, Tailwind CSS, shadcn/ui và các thư viện hỗ trợ được liệt kê trong `docs/05-System-Architecture.md`.
- AI workflow đã chốt: một Agent chính theo Agent Mode truyền thống; UI UX Pro Max Skill; browser tool ưu tiên built-in, Playwright/MCP tùy nhu cầu.
- Bắt đầu tiếp: kiểm tra toolchain và repository, chuẩn bị framework/dependencies/Agent instructions, smoke test; sau đó API Contract → Frontend UI với Mock Data → Backend → tích hợp.
- Chưa được đánh dấu hoàn thành: Backend, Security/JWT, API nghiệp vụ, Frontend, AI, automated tests, deployment.

Xem `docs/10-Progress-Log.md` để biết trạng thái chi tiết và `docs/11-Implementation-Guide.md` để theo dõi trình tự chuẩn bị/triển khai.
