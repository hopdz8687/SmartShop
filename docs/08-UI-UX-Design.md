# 8. Định hướng UI/UX

## 8.1. Công nghệ và công cụ thiết kế đã thống nhất

- ReactJS + Vite: nền tảng frontend.
- Tailwind CSS: styling, layout và responsive.
- shadcn/ui: component UI tái sử dụng, source code được quản lý trong repository.
- Lucide React: icon.
- UI UX Pro Max: Skill hỗ trợ AI Agent đưa ra quyết định UI/UX. Skill không được cài như package runtime của website.
- Browser/Playwright: công cụ kiểm tra giao diện tùy nhu cầu; ưu tiên khả năng browser sẵn có trước, chỉ bổ sung MCP/browser plugin nếu thiếu chức năng cần thiết.

## 8.2. Nguyên tắc
- Giao diện dễ hiểu, ưu tiên luồng mua hàng.
- Responsive trên desktop, tablet và mobile.
- Phân tách Customer, Seller và Admin.
- Tái sử dụng component.
- Có trạng thái loading, empty, error, success và validation.

## 8.3. Customer
- Trang chủ: danh mục, sản phẩm nổi bật, tìm kiếm.
- Danh sách sản phẩm: lọc, sắp xếp, phân trang.
- Chi tiết sản phẩm: ảnh, giá, mô tả, cửa hàng, tồn kho.
- Trang cửa hàng.
- Giỏ hàng nhóm theo cửa hàng.
- Checkout: địa chỉ, tổng tiền, COD.
- Đơn hàng của tôi.
- Chat.
- AI Assistant.
- Hồ sơ và địa chỉ.

## 8.4. Seller Dashboard
- Tổng quan cửa hàng.
- Quản lý cửa hàng.
- Danh sách và form sản phẩm.
- Quản lý đơn hàng.
- Hộp thư chat.
- Thống kê cơ bản nếu đủ thời gian.

## 8.5. Admin Dashboard
- Tổng quan hệ thống.
- Quản lý người dùng.
- Duyệt hồ sơ Seller.
- Quản lý cửa hàng.
- Quản lý danh mục.
- Kiểm duyệt sản phẩm.
- Quản lý đơn hàng.
- Quản lý coupon ở giai đoạn mở rộng.

## 8.6. Trạng thái UI
- Loading: đang tải.
- Empty: chưa có dữ liệu.
- Error: lỗi mạng/nghiệp vụ.
- Success: thao tác thành công.
- Disabled: thao tác không khả dụng.
- Validation: lỗi tại trường nhập liệu.

Wireframe/mockup chi tiết sẽ được bổ sung sau khi chốt Use Case và luồng nghiệp vụ.


## 8.7. Quy tắc Design System cho AI Agent

Trước khi xây dựng các trang, Agent phải đề xuất và thống nhất một Design System cơ sở gồm:
- Màu thương hiệu, màu nền, màu chữ, màu trạng thái.
- Typography, thang spacing, radius, shadow và breakpoint.
- Quy tắc bố cục và responsive.
- Component dùng chung: Button, Input, Select, Dialog, Sheet, Card, Badge, Table, Skeleton, Toast/Alert và các component cần thiết khác.
- Quy tắc trạng thái loading, empty, error, success, disabled và validation.

Không tự tạo phong cách khác nhau cho từng trang. Trang mới phải tái sử dụng Design System và component đã có. Những thay đổi lớn về thương hiệu hoặc Design System cần được người dùng duyệt trước khi áp dụng toàn cục.
