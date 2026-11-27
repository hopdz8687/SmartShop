# 1. Tổng quan dự án

## 1.1. Thông tin chung

| Thuộc tính | Nội dung |
|---|---|
| Tên | SmartShop – AI Powered Multi-Vendor E-commerce |
| Mô hình | Website thương mại điện tử đa người bán |
| Frontend | ReactJS, Vite, Tailwind CSS, shadcn/ui |
| Backend | Java 21, Spring Boot |
| Database | MySQL-compatible; môi trường phát triển hiện tại MariaDB/XAMPP |
| ORM | Spring Data JPA / Hibernate |
| Version control | Git / GitHub |
| UI components | shadcn/ui |
| UI styling | Tailwind CSS |
| Frontend utilities | Lucide React, React Router, Axios, TanStack Query, React Hook Form, Zod |
| AI design guidance | UI UX Pro Max Skill (for the coding agent) |

## 1.2. Bối cảnh và mục tiêu

SmartShop tham khảo mô hình marketplace như Shopee nhưng giới hạn phạm vi để phù hợp với mục tiêu thực tập. Nhiều cửa hàng có thể kinh doanh trên cùng nền tảng. Khách hàng tìm kiếm và mua sản phẩm; Seller quản lý cửa hàng, sản phẩm và đơn hàng; Admin quản lý hoạt động chung.

Mục tiêu:
- Xây dựng website thương mại điện tử có nghiệp vụ cốt lõi.
- Áp dụng Java Spring Boot, ReactJS và MySQL.
- Xây dựng RESTful API và phân quyền.
- Tích hợp AI tư vấn/tìm kiếm sản phẩm dựa trên catalog SmartShop.
- Thiết kế module rõ ràng, thuận tiện bảo trì và mở rộng.
- Hoàn thiện sản phẩm và tài liệu báo cáo thực tập.

## 1.3. Phạm vi MVP dự kiến

- Đăng ký, đăng nhập, đăng xuất và phân quyền.
- Đăng ký mở cửa hàng, Admin duyệt thủ công.
- Quản lý danh mục và sản phẩm.
- Tìm kiếm, lọc và phân trang sản phẩm.
- Giỏ hàng đa cửa hàng và đặt hàng COD.
- Tách đơn tổng thành các đơn theo cửa hàng.
- Quản lý trạng thái đơn hàng.
- Chat cơ bản giữa Customer và Seller bằng REST API.
- AI tư vấn/tìm kiếm sản phẩm từ catalog SmartShop.

## 1.4. Chức năng mở rộng

- Đánh giá sản phẩm.
- Mã giảm giá.
- Quản lý và tích hợp đơn vị vận chuyển.
- Thanh toán trực tuyến.
- Nâng cấp chat lên WebSocket/STOMP.
- Thông báo thời gian thực.

## 1.5. Ngoài phạm vi hiện tại

Livestream, ví điện tử nội bộ, đấu giá, affiliate và hệ thống hoa hồng phức tạp.

## 1.6. Tác nhân

- Customer: khách hàng mua sắm.
- Seller: người bán, sở hữu cửa hàng.
- Admin: quản trị viên.
- AI API: dịch vụ AI bên ngoài.
- Payment Gateway và Shipping Provider: dịch vụ ngoài, chỉ khi tích hợp.

## 1.7. Nguyên tắc

Ưu tiên hoàn thiện nghiệp vụ cốt lõi trước. Thiết kế có tính đến mở rộng nhưng không triển khai sớm các chức năng làm tăng đáng kể độ phức tạp.

---

## Cập nhật quyết định đã chốt – 25/11/2026

- Mô hình Multi-Vendor.
- Một tài khoản Seller sở hữu tối đa một Store trong MVP.
- Sản phẩm có biến thể; tồn kho riêng theo từng biến thể.
- Thanh toán MVP chỉ COD.
- Địa chỉ đơn hàng là snapshot tại thời điểm checkout.
- AI chỉ dùng dữ liệu catalog sản phẩm SmartShop.
- Chat MVP dùng REST API; WebSocket/STOMP là hướng mở rộng.
- Một checkout tạo một Order tổng và chia Seller Order theo từng Store.


## 1.8. Trạng thái triển khai – 26/11/2026
Thiết kế MVP và schema vật lý v2.1 đã được chốt để bắt đầu triển khai. Backend/Frontend chưa được xác nhận là đã khởi tạo. Bộ frontend đã được thống nhất ở mức lựa chọn công nghệ: ReactJS + Vite, Tailwind CSS, shadcn/ui, Lucide React, React Router, Axios, TanStack Query, React Hook Form và Zod. UI UX Pro Max là Skill hỗ trợ AI Agent thiết kế UI/UX, không phải dependency runtime của frontend. Xem `docs/10-Progress-Log.md` và `docs/11-Implementation-Guide.md`.
