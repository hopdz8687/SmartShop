# 13. Quy trình làm việc với AI Coding Agent

**Ngày thống nhất:** 26/11/2026

## 13.1. Mục tiêu

Dùng AI Agent để tăng tốc triển khai SmartShop nhưng vẫn giữ quyền kiểm soát của người dùng đối với phạm vi, nghiệp vụ, kiến trúc và chất lượng.

## 13.2. Mô hình Agent

- Chỉ một Agent chính làm việc tại một thời điểm.
- Ưu tiên Agent Mode truyền thống trong Antigravity hoặc GitHub Copilot.
- Không mặc định dùng Autopilot, Ralph Loop, Multi-Agent hay plugin bổ sung.
- Antigravity và Copilot cùng tuân thủ tài liệu trong repository; Skill/tool có thể phải cấu hình riêng theo môi trường.
- UI UX Pro Max là Skill định hướng thiết kế UI/UX, không phải package của website.
- Browser tool ưu tiên tính năng tích hợp sẵn. Playwright/MCP browser chỉ thêm khi có nhu cầu kiểm thử tự động rõ ràng.

## 13.3. Vòng lặp cho từng nhiệm vụ

1. **Yêu cầu:** Người dùng mô tả mục tiêu, phạm vi và tiêu chí nghiệm thu.
2. **Khảo sát:** Agent đọc `AGENTS.md`, tài liệu liên quan, source tree và cấu hình hiện có.
3. **Kế hoạch:** Agent nêu cách làm, file/module dự kiến tác động và rủi ro.
4. **Triển khai:** Agent thực hiện một increment nhỏ, không tự mở rộng phạm vi.
5. **Xác minh:** Chạy build/test/lint/browser check phù hợp; chỉ báo Pass khi đã thực sự chạy.
6. **Review:** Người dùng xem kết quả và yêu cầu điều chỉnh hoặc nghiệm thu.
7. **Lưu vết:** Cập nhật progress log, tài liệu liên quan và Git commit khi người dùng đồng ý.

## 13.4. Khi nào Agent phải dừng và hỏi?

Agent phải xin xác nhận trước khi:
- Thay đổi schema, migration đã áp dụng hoặc quyết định nghiệp vụ LOCKED.
- Thực hiện xóa dữ liệu, reset database, xóa file diện rộng hoặc thao tác không dễ khôi phục.
- Thêm dịch vụ trả phí, gửi dữ liệu ra ngoài, thay đổi quyền truy cập hoặc xử lý secrets.
- Nâng major version hoặc thay đổi framework/toolchain có thể gây ảnh hưởng lớn.
- Tự quyết định nội dung chưa được đặc tả mà ảnh hưởng đến nghiệp vụ, bảo mật hoặc dữ liệu.

Với lựa chọn kỹ thuật nhỏ, an toàn và có thể đảo ngược, Agent có thể đề xuất assumption, ghi rõ và tiếp tục trong phạm vi task.

## 13.5. Trình tự tổng thể SmartShop

1. Kiểm tra môi trường và repository.
2. Cài/khởi tạo frameworks và dependencies đã thống nhất.
3. Cấu hình Agent instructions, UI UX Pro Max Skill và browser tool nếu cần.
4. Smoke test toàn bộ môi trường.
5. Phân tích nghiệp vụ và API Contract.
6. Thiết kế Design System.
7. Xây dựng Frontend với Mock Data.
8. Xây dựng Backend theo API Contract.
9. Tích hợp API.
10. Kiểm thử, sửa lỗi, nghiệm thu và commit.

## 13.6. Mẫu yêu cầu Agent cho một task

```text
Đọc AGENTS.md và các tài liệu liên quan trước khi thực hiện.

Mục tiêu:
[ mô tả một tính năng hoặc trang cụ thể ]

Phạm vi:
[ module/trang được phép thay đổi ]

Tiêu chí nghiệm thu:
[ hành vi, giao diện, responsive, validation, test ]

Yêu cầu:
- Khảo sát code hiện tại trước khi sửa.
- Lập kế hoạch ngắn và nêu file dự kiến thay đổi.
- Không thay đổi nghiệp vụ, schema hoặc API contract ngoài phạm vi.
- Triển khai theo increment nhỏ.
- Chạy build/test phù hợp và báo đúng kết quả thực tế.
- Cập nhật tài liệu/progress log nếu có thay đổi.
- Dừng và hỏi nếu gặp quyết định quan trọng chưa được chốt.

Kết quả cần báo cáo:
- File đã thay đổi.
- Chức năng đã hoàn thành.
- Lệnh/test đã chạy và kết quả.
- Điểm chưa xác minh hoặc việc cần người dùng quyết định.
```
