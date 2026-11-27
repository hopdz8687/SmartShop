# 2. Đặc tả yêu cầu hệ thống

## 2.1. Quy ước
- **MVP:** phạm vi phiên bản đầu phải triển khai.
- **Mở rộng:** không tự triển khai khi chưa có yêu cầu/phê duyệt.
- Các quy tắc trạng thái và dữ liệu chi tiết xem `docs/04-Business-Workflows.md`, `docs/06-Database-Design.md`, `docs/12-Decision-Log.md`.

## 2.2. Yêu cầu chức năng

### FR-01. Tài khoản và xác thực – MVP
- Đăng ký, đăng nhập, đăng xuất.
- Xem/cập nhật hồ sơ cá nhân.
- Vai trò CUSTOMER, SELLER, ADMIN.
- Mật khẩu phải được hash; không lưu plaintext.
- Seller role chỉ được cấp sau khi Admin duyệt Seller Application.

### FR-02. Cửa hàng và Seller – MVP
- Customer gửi yêu cầu mở cửa hàng.
- Hồ sơ có trạng thái PENDING, APPROVED hoặc REJECTED.
- Admin duyệt/từ chối và lưu lý do.
- Khi duyệt, tạo Store và cấp quyền Seller.
- Một tài khoản Seller sở hữu tối đa một Store trong MVP.

### FR-03. Danh mục và sản phẩm – MVP
- Admin quản lý Category.
- Seller quản lý Product thuộc Store của mình.
- Product có một hoặc nhiều Product Variant.
- MVP dùng flat Variant: tên biến thể, SKU, giá, tồn kho.
- Tồn kho và giá được quản lý ở cấp Variant.
- Mỗi Product phải có ít nhất một Variant active và đúng một default Variant; Backend kiểm tra.
- Customer xem, tìm kiếm, lọc, sắp xếp và phân trang.
- Không triển khai dynamic attribute management trong MVP dù schema có các bảng attribute.

### FR-04. Giỏ hàng – MVP
- Một Cart persistent cho mỗi User.
- Thêm, cập nhật số lượng và xóa Variant trong Cart.
- Một Cart có thể chứa Variant từ nhiều Store.
- Checkout chỉ xử lý selected `cartItemIds`.
- Kiểm tra giá, trạng thái bán và tồn kho từ Backend trước checkout.

### FR-05. Đặt hàng – MVP
- Chọn Address và đặt COD.
- Một checkout tạo một parent Order.
- Tạo một Seller Order cho mỗi Store.
- Seller chỉ xem và xử lý Seller Order thuộc Store mình.
- Order lưu snapshot người nhận/địa chỉ.
- Order Item lưu snapshot sản phẩm/Variant/giá.
- Tạo Payment COD cho từng Seller Order theo schema.
- Checkout, trừ kho, tạo order/payment và xóa Cart Item đã chọn phải nhất quán trong transaction.
- Hủy đơn theo quyền và trạng thái trong workflow.

### FR-06. Chat – MVP cơ bản
- Customer bắt đầu hoặc mở lại conversation với Store.
- Customer và Seller gửi tin nhắn văn bản.
- Lưu lịch sử.
- Chỉ thành viên hợp lệ được truy cập.
- MVP dùng REST API; WebSocket/STOMP là mở rộng.

### FR-07. AI – MVP
- Customer hỏi trợ lý mua sắm/tìm kiếm tự nhiên.
- LLM trích xuất filter có cấu trúc; Backend query catalog; LLM diễn đạt kết quả thật.
- Trả text cùng product cards từ kết quả Backend.
- Chỉ dùng public SmartShop catalog, không dùng lịch sử mua hàng cá nhân.
- AI không được bịa sản phẩm/giá/tồn kho hoặc thực hiện mutation.
- Không dùng Vector DB trong MVP.

### FR-08. Đánh giá – Mở rộng
- Chỉ người dùng sở hữu Order Item và Seller Order đã DELIVERED mới được đánh giá.

### FR-09. Vận chuyển – MVP cơ bản / tích hợp mở rộng
- Lưu Shipment và trạng thái logistics.
- MVP Seller tạo/cập nhật thông tin shipment; Admin/operator được phân quyền xác nhận giao thành công.
- Tích hợp API carrier là mở rộng.

### FR-10. Mã giảm giá – Mở rộng
- Coupon, điều kiện, hạn dùng và giới hạn sử dụng.

### FR-11. Thanh toán trực tuyến – Mở rộng
- Tích hợp cổng thanh toán sau MVP; không lưu dữ liệu thẻ nhạy cảm.

## 2.3. Yêu cầu phi chức năng
- Bảo mật: xác thực, phân quyền, kiểm tra ownership ở Backend.
- Toàn vẹn: FK, CHECK, validation, transaction.
- Chống overselling: conditional atomic stock update.
- Bảo trì: Modular Monolith, module/layer rõ ràng.
- Hiệu năng: phân trang, truy vấn hợp lý, tránh N+1.
- Xử lý lỗi: response nhất quán, không lộ stack trace/secrets.
- Kiểm thử: unit, integration, API và security tests theo nghiệp vụ.
- Responsive UI và trạng thái loading/empty/error/success.

## 2.4. Quy tắc nghiệp vụ bắt buộc
- BR-01: Một tài khoản Seller tối đa một Store trong MVP.
- BR-02: Chỉ Seller Application APPROVED mới được dùng quyền Seller.
- BR-03: Seller chỉ truy cập dữ liệu Store của mình.
- BR-04: Cart Item và checkout tham chiếu Variant; tồn kho thuộc Variant.
- BR-05: Một checkout tạo một Order tổng và Seller Order theo Store.
- BR-06: Checkout lưu snapshot và COD Payment theo Seller Order.
- BR-07: Checkout transaction rollback toàn bộ nếu một bước thất bại.
- BR-08: Customer chỉ hủy Seller Order PENDING thuộc mình.
- BR-09: Seller chỉ hủy Seller Order PENDING/CONFIRMED của Store mình trước shipment.
- BR-10: Hoàn kho chỉ sau conditional cancellation thành công, cùng transaction.
- BR-11: Order parent status được OrderService tổng hợp từ Seller Orders chưa hủy.
- BR-12: AI chỉ tư vấn dựa trên catalog được Backend truy vấn.
- BR-13: Chat chỉ cho thành viên Conversation truy cập.

## 2.5. Ngoài phạm vi MVP
Online payment, coupon, carrier API, WebSocket chat, vector DB, multi-store ownership, inventory ledger nâng cao, livestream, ví nội bộ, affiliate, auction và hoa hồng phức tạp.
