-- =============================================================================
-- SmartShop MVP - Development Seed Data
-- =============================================================================
-- Target Schema: SmartShop DDL v2.1 (V1__init_schema.sql)
-- Default Password for all seed users: Password@123
-- BCrypt Hash: $2a$10$nQRrERv7cHFLLPzoQRKwW.D8TWEbn7eLvxydQvKebQSptsJQqqrKy
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. USERS (1 Admin, 2 Sellers, 2 Customers)
-- -----------------------------------------------------------------------------
-- Roles CUSTOMER (id=1), SELLER (id=2), ADMIN (id=3) were initialized by Flyway V1.
INSERT INTO users (id, email, password_hash, full_name, phone, avatar_url, status, email_verified, created_at, updated_at) VALUES
(1, 'admin@smartshop.com', '$2a$10$nQRrERv7cHFLLPzoQRKwW.D8TWEbn7eLvxydQvKebQSptsJQqqrKy', 'Quản trị viên SmartShop', '0901000001', 'https://picsum.photos/seed/admin/200/200', 'ACTIVE', TRUE, '2026-01-01 08:00:00.000', '2026-01-01 08:00:00.000'),
(2, 'seller1@smartshop.com', '$2a$10$nQRrERv7cHFLLPzoQRKwW.D8TWEbn7eLvxydQvKebQSptsJQqqrKy', 'Nguyễn Văn Bán', '0902000002', 'https://picsum.photos/seed/seller1/200/200', 'ACTIVE', TRUE, '2026-01-02 09:00:00.000', '2026-01-02 09:00:00.000'),
(3, 'seller2@smartshop.com', '$2a$10$nQRrERv7cHFLLPzoQRKwW.D8TWEbn7eLvxydQvKebQSptsJQqqrKy', 'Trần Thị Shop', '0903000003', 'https://picsum.photos/seed/seller2/200/200', 'ACTIVE', TRUE, '2026-01-03 10:00:00.000', '2026-01-03 10:00:00.000'),
(4, 'customer1@smartshop.com', '$2a$10$nQRrERv7cHFLLPzoQRKwW.D8TWEbn7eLvxydQvKebQSptsJQqqrKy', 'Lê Văn Mua', '0904000004', 'https://picsum.photos/seed/customer1/200/200', 'ACTIVE', TRUE, '2026-01-04 11:00:00.000', '2026-01-04 11:00:00.000'),
(5, 'customer2@smartshop.com', '$2a$10$nQRrERv7cHFLLPzoQRKwW.D8TWEbn7eLvxydQvKebQSptsJQqqrKy', 'Phạm Thị Mua', '0905000005', 'https://picsum.photos/seed/customer2/200/200', 'ACTIVE', TRUE, '2026-01-05 14:00:00.000', '2026-01-05 14:00:00.000');

-- -----------------------------------------------------------------------------
-- 3. USER ROLES
-- -----------------------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id, created_at) VALUES
(1, 3, '2026-01-01 08:00:00.000'), -- Admin
(2, 2, '2026-01-02 09:00:00.000'), -- Seller 1
(3, 2, '2026-01-03 10:00:00.000'), -- Seller 2
(4, 1, '2026-01-04 11:00:00.000'), -- Customer 1
(5, 1, '2026-01-05 14:00:00.000'); -- Customer 2

-- -----------------------------------------------------------------------------
-- 4. ADDRESSES
-- -----------------------------------------------------------------------------
INSERT INTO addresses (id, user_id, recipient_name, phone, province, district, ward, address_detail, is_default, created_at, updated_at) VALUES
(1, 4, 'Lê Văn Mua', '0904000004', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', 'Số 123 Đường Cầu Giấy', TRUE, '2026-01-04 11:15:00.000', '2026-01-04 11:15:00.000'),
(2, 5, 'Phạm Thị Mua', '0905000005', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', 'Số 456 Đường Lê Lợi', TRUE, '2026-01-05 14:20:00.000', '2026-01-05 14:20:00.000'),
(3, 2, 'Nguyễn Văn Bán', '0902000002', 'Hà Nội', 'Quận Ba Đình', 'Phường Kim Mã', 'Số 10 Phố Kim Mã', TRUE, '2026-01-02 09:10:00.000', '2026-01-02 09:10:00.000'),
(4, 3, 'Trần Thị Shop', '0903000003', 'Đà Nẵng', 'Quận Hải Châu', 'Phường Hải Châu 1', 'Số 20 Đường Bạch Đằng', TRUE, '2026-01-03 10:15:00.000', '2026-01-03 10:15:00.000');

-- -----------------------------------------------------------------------------
-- 5. SELLER APPLICATIONS
-- -----------------------------------------------------------------------------
INSERT INTO seller_applications (id, user_id, business_name, applicant_note, status, reviewed_by, reviewed_at, rejection_reason, created_at, updated_at) VALUES
(1, 2, 'TechZone Store', 'Đăng ký bán thiết bị điện tử, công nghệ chính hãng', 'APPROVED', 1, '2026-01-02 10:00:00.000', NULL, '2026-01-02 09:30:00.000', '2026-01-02 10:00:00.000'),
(2, 3, 'Fashionista Studio', 'Đăng ký bán quần áo và phụ kiện thời trang thiết kế', 'APPROVED', 1, '2026-01-03 11:00:00.000', NULL, '2026-01-03 10:30:00.000', '2026-01-03 11:00:00.000');

-- -----------------------------------------------------------------------------
-- 6. STORES (1 Store per Seller)
-- -----------------------------------------------------------------------------
INSERT INTO stores (id, user_id, store_name, slug, description, logo_url, contact_email, contact_phone, status, created_at, updated_at) VALUES
(1, 2, 'TechZone Store', 'techzone-store', 'Chuyên thiết bị công nghệ, phụ kiện máy tính và âm thanh chính hãng', 'https://picsum.photos/seed/techzone-logo/200/200', 'contact@techzone.vn', '0902000002', 'ACTIVE', '2026-01-02 10:30:00.000', '2026-01-02 10:30:00.000'),
(2, 3, 'Fashionista Studio', 'fashionista-studio', 'Thời trang phong cách trẻ, hiện đại, dẫn đầu xu hướng thị trường', 'https://picsum.photos/seed/fashionista-logo/200/200', 'contact@fashionista.vn', '0903000003', 'ACTIVE', '2026-01-03 11:30:00.000', '2026-01-03 11:30:00.000');

-- -----------------------------------------------------------------------------
-- 7. CATEGORIES (5 Categories)
-- -----------------------------------------------------------------------------
INSERT INTO categories (id, parent_id, name, slug, description, status, created_at, updated_at) VALUES
(1, NULL, 'Điện tử & Thiết bị', 'dien-tu-thiet-bi', 'Các sản phẩm công nghệ, âm thanh và phụ kiện thông minh', 'ACTIVE', '2026-01-01 09:00:00.000', '2026-01-01 09:00:00.000'),
(2, 1, 'Phụ kiện di động', 'phu-kien-di-dong', 'Tai nghe, cáp sạc, pin dự phòng và ốp lưng', 'ACTIVE', '2026-01-01 09:05:00.000', '2026-01-01 09:05:00.000'),
(3, 1, 'Phụ kiện máy tính', 'phu-kien-may-tinh', 'Bàn phím cơ, chuột gaming, lót chuột cao cấp', 'ACTIVE', '2026-01-01 09:10:00.000', '2026-01-01 09:10:00.000'),
(4, NULL, 'Thời trang & May mặc', 'thoi-trang-may-mac', 'Trang phục thời trang nam, nữ và phụ kiện phong cách', 'ACTIVE', '2026-01-01 09:15:00.000', '2026-01-01 09:15:00.000'),
(5, 4, 'Thời trang Nam', 'thoi-trang-nam', 'Áo thun, quần jean, áo khoác dành cho phái mạnh', 'ACTIVE', '2026-01-01 09:20:00.000', '2026-01-01 09:20:00.000');

-- -----------------------------------------------------------------------------
-- 8. PRODUCTS (6 Products: 3 in Store 1, 3 in Store 2)
-- -----------------------------------------------------------------------------
INSERT INTO products (id, store_id, category_id, name, slug, description, brand, status, created_at, updated_at) VALUES
-- Store 1: TechZone
(1, 1, 2, 'Tai nghe không dây Pro Sound X1', 'tai-nghe-khong-day-pro-sound-x1', 'Tai nghe chống ồn chủ động ANC thế hệ mới, Bluetooth 5.3, pin bền bỉ 30 giờ.', 'SoundX', 'ACTIVE', '2026-01-06 09:00:00.000', '2026-01-06 09:00:00.000'),
(2, 1, 2, 'Củ sạc nhanh SmartCharge 65W GaN', 'cu-sac-nhanh-smartcharge-65w-gan', 'Củ sạc nhỏ gọn công nghệ GaN 3 cổng ra (2 Type-C, 1 Type-A), chuẩn sạc nhanh PD 65W.', 'SmartCharge', 'ACTIVE', '2026-01-06 09:30:00.000', '2026-01-06 09:30:00.000'),
(3, 1, 3, 'Bàn phím cơ không dây MechKey K8', 'ban-phim-co-khong-day-mechkey-k8', 'Bàn phím cơ kết nối 3 chế độ (Bluetooth/2.4G/Dây), layout 75%, hot-swap tiện lợi.', 'MechKey', 'ACTIVE', '2026-01-06 10:00:00.000', '2026-01-06 10:00:00.000'),
-- Store 2: Fashionista
(4, 2, 5, 'Áo thun Polo nam Cotton Compact', 'ao-thun-polo-nam-cotton-compact', 'Áo polo phom dáng Regular Fit lịch lãm, sợi cotton compact chống xù lông thoáng mát.', 'Fashionista', 'ACTIVE', '2026-01-07 09:00:00.000', '2026-01-07 09:00:00.000'),
(5, 2, 5, 'Quần Jean nam Slim Fit Co giãn', 'quan-jean-nam-slim-fit-co-gian', 'Vải denim co giãn 4 chiều mềm mại, phom dáng slim-fit tôn dáng năng động.', 'Fashionista', 'ACTIVE', '2026-01-07 09:30:00.000', '2026-01-07 09:30:00.000'),
(6, 2, 4, 'Áo khoác gió Unisex Windbreaker', 'ao-khoac-gio-unisex-windbreaker', 'Áo khoác dù 2 lớp chống gió, trượt nước nhẹ, thiết kế tối giản phù hợp cả nam và nữ.', 'Fashionista', 'ACTIVE', '2026-01-07 10:00:00.000', '2026-01-07 10:00:00.000');

-- -----------------------------------------------------------------------------
-- 9. PRODUCT VARIANTS (Each product has >=1 ACTIVE and exactly 1 default variant)
-- -----------------------------------------------------------------------------
INSERT INTO product_variants (id, product_id, sku, variant_name, price, stock_quantity, is_default, status, created_at, updated_at) VALUES
-- Variants for Product 1 (Tai nghe Pro Sound X1)
-- Stock adjusted: 50 - 1 (bought in order 1) = 49
(1, 1, 'TZ-X1-BLK', 'Màu Đen Huyền Bí', 890000.00, 49, TRUE, 'ACTIVE', '2026-01-06 09:05:00.000', '2026-01-06 09:05:00.000'),
(2, 1, 'TZ-X1-WHT', 'Màu Trắng Tinh Khôi', 890000.00, 40, FALSE, 'ACTIVE', '2026-01-06 09:05:00.000', '2026-01-06 09:05:00.000'),

-- Variants for Product 2 (Củ sạc GaN 65W)
-- Stock adjusted: 100 - 1 (bought in order 1) = 99
(3, 2, 'TZ-GAN-65W', 'Bản Tiêu Chuẩn 65W Trắng', 450000.00, 99, TRUE, 'ACTIVE', '2026-01-06 09:35:00.000', '2026-01-06 09:35:00.000'),

-- Variants for Product 3 (Bàn phím MechKey K8)
(4, 3, 'TZ-K8-RED', 'Red Switch - Phím Êm', 1250000.00, 30, TRUE, 'ACTIVE', '2026-01-06 10:05:00.000', '2026-01-06 10:05:00.000'),
(5, 3, 'TZ-K8-BLU', 'Blue Switch - Clicky Giòn', 1250000.00, 25, FALSE, 'ACTIVE', '2026-01-06 10:05:00.000', '2026-01-06 10:05:00.000'),

-- Variants for Product 4 (Áo thun Polo nam)
-- Stock adjusted: 60 - 2 (bought in order 1) = 58
(6, 4, 'FS-POLO-M-NVY', 'Size M - Xanh Navy', 299000.00, 58, TRUE, 'ACTIVE', '2026-01-07 09:05:00.000', '2026-01-07 09:05:00.000'),
(7, 4, 'FS-POLO-L-NVY', 'Size L - Xanh Navy', 299000.00, 45, FALSE, 'ACTIVE', '2026-01-07 09:05:00.000', '2026-01-07 09:05:00.000'),

-- Variants for Product 5 (Quần Jean nam)
(8, 5, 'FS-JEAN-30-BLU', 'Size 30 - Xanh Chàm', 499000.00, 35, TRUE, 'ACTIVE', '2026-01-07 09:35:00.000', '2026-01-07 09:35:00.000'),
(9, 5, 'FS-JEAN-31-BLU', 'Size 31 - Xanh Chàm', 499000.00, 30, FALSE, 'ACTIVE', '2026-01-07 09:35:00.000', '2026-01-07 09:35:00.000'),

-- Variants for Product 6 (Áo khoác gió Unisex)
(10, 6, 'FS-WIND-BLK-F', 'Màu Đen - Freesize', 380000.00, 80, TRUE, 'ACTIVE', '2026-01-07 10:05:00.000', '2026-01-07 10:05:00.000');

-- -----------------------------------------------------------------------------
-- 10. PRODUCT IMAGES
-- -----------------------------------------------------------------------------
INSERT INTO product_images (id, product_id, variant_id, image_url, alt_text, display_order, is_primary, created_at) VALUES
(1, 1, 1, 'https://picsum.photos/seed/headphone-x1-blk/600/600', 'Tai nghe Pro Sound X1 Đen', 0, TRUE, '2026-01-06 09:10:00.000'),
(2, 1, 2, 'https://picsum.photos/seed/headphone-x1-wht/600/600', 'Tai nghe Pro Sound X1 Trắng', 1, FALSE, '2026-01-06 09:10:00.000'),
(3, 2, 3, 'https://picsum.photos/seed/charger-65w/600/600', 'Củ sạc SmartCharge 65W GaN', 0, TRUE, '2026-01-06 09:40:00.000'),
(4, 3, 4, 'https://picsum.photos/seed/mechkey-k8-red/600/600', 'Bàn phím cơ MechKey K8 Red Switch', 0, TRUE, '2026-01-06 10:10:00.000'),
(5, 4, 6, 'https://picsum.photos/seed/polo-shirt-m/600/600', 'Áo thun Polo Cotton Compact Navy', 0, TRUE, '2026-01-07 09:10:00.000'),
(6, 5, 8, 'https://picsum.photos/seed/jean-pants-30/600/600', 'Quần Jean nam Slim Fit Xanh Chàm', 0, TRUE, '2026-01-07 09:40:00.000'),
(7, 6, 10, 'https://picsum.photos/seed/windbreaker-blk/600/600', 'Áo khoác gió Unisex Windbreaker Đen', 0, TRUE, '2026-01-07 10:10:00.000');

-- -----------------------------------------------------------------------------
-- 11. CARTS & CART ITEMS (1 Cart per User)
-- -----------------------------------------------------------------------------
INSERT INTO carts (id, user_id, created_at, updated_at) VALUES
(1, 4, '2026-01-08 08:00:00.000', '2026-01-08 08:30:00.000'), -- Cart của Customer 1
(2, 5, '2026-01-08 09:00:00.000', '2026-01-08 09:30:00.000'); -- Cart của Customer 2

INSERT INTO cart_items (id, cart_id, variant_id, quantity, created_at, updated_at) VALUES
-- Items trong giỏ của Customer 1 (user_id=4)
(1, 1, 3, 1, '2026-01-08 08:10:00.000', '2026-01-08 08:10:00.000'), -- Củ sạc 65W
(2, 1, 10, 2, '2026-01-08 08:15:00.000', '2026-01-08 08:15:00.000'), -- 2 Áo khoác gió

-- Items trong giỏ của Customer 2 (user_id=5)
(3, 2, 1, 1, '2026-01-08 09:10:00.000', '2026-01-08 09:10:00.000'), -- Tai nghe X1 Đen
(4, 2, 6, 1, '2026-01-08 09:15:00.000', '2026-01-08 09:15:00.000'); -- Áo Polo size M

-- -----------------------------------------------------------------------------
-- 12. SHIPPING PROVIDERS
-- -----------------------------------------------------------------------------
INSERT INTO shipping_providers (id, name, code, phone, website, status) VALUES
(1, 'Giao Hàng Nhanh', 'GHN', '1900636677', 'https://ghn.vn', 'ACTIVE'),
(2, 'Giao Hàng Tiết Kiệm', 'GHTK', '19006092', 'https://ghtk.vn', 'ACTIVE');

-- -----------------------------------------------------------------------------
-- 13. MULTI-VENDOR ORDER (1 Parent Order, 2 Seller Orders from 2 Stores)
-- -----------------------------------------------------------------------------
-- Customer 1 (user_id=4) đặt hàng từ cả TechZone Store (id=1) và Fashionista Studio (id=2)
-- Financial Invariants:
--   Parent Subtotal     = 1,340,000 + 598,000 = 1,938,000
--   Parent Shipping Fee = 30,000 + 25,000 = 55,000
--   Parent Discount     = 40,000 + 0 = 40,000
--   Parent Total Amount = 1,938,000 + 55,000 - 40,000 = 1,953,000 (Khớp tổng 2 Seller Orders: 1,330,000 + 623,000)

INSERT INTO orders (
  id, user_id, order_code,
  recipient_name, recipient_phone,
  shipping_province, shipping_district, shipping_ward, shipping_address,
  customer_note,
  subtotal, shipping_fee, discount_amount, total_amount,
  order_status, created_at, updated_at
) VALUES (
  1, 4, 'ORD-20260109-0001',
  'Lê Văn Mua', '0904000004',
  'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', 'Số 123 Đường Cầu Giấy',
  'Giao giờ hành chính, gọi điện trước khi đến',
  1938000.00, 55000.00, 40000.00, 1953000.00,
  'PENDING', '2026-01-09 10:00:00.000', '2026-01-09 10:00:00.000'
);

-- Seller Orders for Order 1:
-- Store 1 (TechZone Store): Subtotal 1,340,000 + Fee 30,000 - Discount 40,000 = Total 1,330,000
-- Store 2 (Fashionista Studio): Subtotal 598,000 + Fee 25,000 - Discount 0 = Total 623,000
INSERT INTO seller_orders (
  id, order_id, store_id, seller_order_code,
  subtotal, shipping_fee, discount_amount, total_amount,
  status, seller_note, cancellation_reason, cancelled_by_user_id, cancelled_at,
  created_at, updated_at
) VALUES (
  1, 1, 1, 'SO-20260109-0001-S1',
  1340000.00, 30000.00, 40000.00, 1330000.00,
  'PENDING', 'Đơn hàng công nghệ - Đóng gói hộp xốp bóng khí', NULL, NULL, NULL,
  '2026-01-09 10:00:00.000', '2026-01-09 10:00:00.000'
), (
  2, 1, 2, 'SO-20260109-0001-S2',
  598000.00, 25000.00, 0.00, 623000.00,
  'PENDING', 'Đơn hàng may mặc - Túi zip chống nước', NULL, NULL, NULL,
  '2026-01-09 10:00:00.000', '2026-01-09 10:00:00.000'
);

-- -----------------------------------------------------------------------------
-- 14. ORDER ITEMS (Immutable Checkout Snapshots)
-- -----------------------------------------------------------------------------
INSERT INTO order_items (
  id, seller_order_id, variant_id,
  product_name, product_image, variant_description, sku,
  unit_price, quantity, subtotal, created_at
) VALUES
-- Items for Seller Order 1 (Store 1)
(1, 1, 1, 'Tai nghe không dây Pro Sound X1', 'https://picsum.photos/seed/headphone-x1-blk/600/600', 'Màu sắc: Màu Đen Huyền Bí', 'TZ-X1-BLK', 890000.00, 1, 890000.00, '2026-01-09 10:00:00.000'),
(2, 1, 3, 'Củ sạc nhanh SmartCharge 65W GaN', 'https://picsum.photos/seed/charger-65w/600/600', 'Phân loại: Bản Tiêu Chuẩn 65W Trắng', 'TZ-GAN-65W', 450000.00, 1, 450000.00, '2026-01-09 10:00:00.000'),

-- Item for Seller Order 2 (Store 2)
(3, 2, 6, 'Áo thun Polo nam Cotton Compact', 'https://picsum.photos/seed/polo-shirt-m/600/600', 'Kích cỡ: Size M - Xanh Navy', 'FS-POLO-M-NVY', 299000.00, 2, 598000.00, '2026-01-09 10:00:00.000');

-- -----------------------------------------------------------------------------
-- 15. PAYMENTS (COD: 1 Payment per Seller Order)
-- -----------------------------------------------------------------------------
INSERT INTO payments (
  id, seller_order_id, payment_method, amount,
  payment_status, paid_at, collected_by, note,
  created_at, updated_at
) VALUES
(1, 1, 'COD', 1330000.00, 'PENDING', NULL, NULL, 'Thanh toán tiền mặt khi nhận hàng (TechZone Store)', '2026-01-09 10:00:00.000', '2026-01-09 10:00:00.000'),
(2, 2, 'COD', 623000.00, 'PENDING', NULL, NULL, 'Thanh toán tiền mặt khi nhận hàng (Fashionista Studio)', '2026-01-09 10:00:00.000', '2026-01-09 10:00:00.000');

-- -----------------------------------------------------------------------------
-- 16. SHIPMENTS (Logistics tracking for Seller Orders)
-- -----------------------------------------------------------------------------
INSERT INTO shipments (
  id, seller_order_id, provider_id, tracking_code,
  shipment_status, shipped_at, delivered_at, note,
  created_at, updated_at
) VALUES
(1, 1, 1, 'GHN-TZ-20260109-01', 'PENDING', NULL, NULL, 'Chờ bàn giao bưu tá GHN tại kho Ba Đình', '2026-01-09 10:05:00.000', '2026-01-09 10:05:00.000'),
(2, 2, 2, 'GHTK-FS-20260109-02', 'PENDING', NULL, NULL, 'Chờ bàn giao bưu tá GHTK tại kho Hải Châu', '2026-01-09 10:05:00.000', '2026-01-09 10:05:00.000');

-- -----------------------------------------------------------------------------
-- 17. CONVERSATIONS & MESSAGES (Customer 1 inquiring Store 1)
-- -----------------------------------------------------------------------------
INSERT INTO conversations (id, customer_id, store_id, status, created_at, updated_at) VALUES
(1, 4, 1, 'OPEN', '2026-01-08 14:00:00.000', '2026-01-08 14:05:00.000');

INSERT INTO messages (id, conversation_id, sender_id, content, message_type, read_at, created_at) VALUES
(1, 1, 4, 'Shop ơi tai nghe Pro Sound X1 có hỗ trợ kết nối đồng thời 2 thiết bị không ạ?', 'TEXT', '2026-01-08 14:02:00.000', '2026-01-08 14:01:00.000'),
(2, 1, 2, 'Dạ chào bạn! Tai nghe X1 có hỗ trợ kết nối Multi-point 2 thiết bị cùng lúc bạn nhé.', 'TEXT', '2026-01-08 14:06:00.000', '2026-01-08 14:05:00.000');
