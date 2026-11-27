CREATE DATABASE IF NOT EXISTS smartshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartshop;
SET NAMES utf8mb4;

-- MySQL 8.0.16+ / InnoDB. Run on a new database.
CREATE TABLE users (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 email VARCHAR(255) NOT NULL,
 password_hash VARCHAR(255) NOT NULL,
 full_name VARCHAR(150) NOT NULL,
 phone VARCHAR(20),
 avatar_url VARCHAR(1000),
 status ENUM('ACTIVE','INACTIVE','LOCKED','PENDING') NOT NULL DEFAULT 'ACTIVE',
 email_verified BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 deleted_at DATETIME(3),
 UNIQUE KEY uk_users_email(email), KEY idx_users_phone(phone), KEY idx_users_status(status)
) ENGINE=InnoDB;

CREATE TABLE roles (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(50) NOT NULL,
 description VARCHAR(255),
 UNIQUE KEY uk_roles_name(name)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
 user_id BIGINT NOT NULL,
 role_id BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(user_id,role_id), KEY idx_user_roles_role(role_id),
 CONSTRAINT fk_user_roles_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT fk_user_roles_role FOREIGN KEY(role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

CREATE TABLE addresses (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 recipient_name VARCHAR(150) NOT NULL,
 phone VARCHAR(20) NOT NULL,
 province VARCHAR(100) NOT NULL,
 district VARCHAR(100) NULL,
 ward VARCHAR(100) NOT NULL,
 address_detail VARCHAR(500) NOT NULL,
 is_default BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 KEY idx_addresses_user_default(user_id,is_default),
 CONSTRAINT fk_addresses_user FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE seller_applications (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 business_name VARCHAR(200) NOT NULL,
 applicant_note TEXT,
 status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
 reviewed_by BIGINT,
 reviewed_at DATETIME(3),
 rejection_reason VARCHAR(1000),
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 KEY idx_seller_app_user_status(user_id,status), KEY idx_seller_app_status_created(status,created_at),
 CONSTRAINT fk_seller_app_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT fk_seller_app_reviewer FOREIGN KEY(reviewed_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE stores (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 store_name VARCHAR(200) NOT NULL,
 slug VARCHAR(220) NOT NULL,
 description TEXT,
 logo_url VARCHAR(1000),
 contact_email VARCHAR(255),
 contact_phone VARCHAR(20),
 status ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 deleted_at DATETIME(3),
 UNIQUE KEY uk_stores_user(user_id), UNIQUE KEY uk_stores_slug(slug), KEY idx_stores_status(status),
 CONSTRAINT fk_stores_user FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE categories (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 parent_id BIGINT,
 name VARCHAR(150) NOT NULL,
 slug VARCHAR(180) NOT NULL,
 description VARCHAR(1000),
 status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_categories_slug(slug), KEY idx_categories_parent_status(parent_id,status),
 CONSTRAINT fk_categories_parent FOREIGN KEY(parent_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE products (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT NOT NULL,
 category_id BIGINT NOT NULL,
 name VARCHAR(255) NOT NULL,
 slug VARCHAR(280) NOT NULL,
 description LONGTEXT,
 brand VARCHAR(150),
 status ENUM('DRAFT','PENDING','ACTIVE','REJECTED','INACTIVE') NOT NULL DEFAULT 'DRAFT',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 deleted_at DATETIME(3),
 UNIQUE KEY uk_products_store_slug(store_id,slug),
 KEY idx_products_category_status(category_id,status),
 KEY idx_products_store_status_created(store_id,status,created_at),
 KEY idx_products_name(name),
 CONSTRAINT fk_products_store FOREIGN KEY(store_id) REFERENCES stores(id),
 CONSTRAINT fk_products_category FOREIGN KEY(category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE product_attributes (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 product_id BIGINT NOT NULL,
 name VARCHAR(100) NOT NULL,
 display_order INT UNSIGNED NOT NULL DEFAULT 0,
 UNIQUE KEY uk_product_attribute_name(product_id,name),
 CONSTRAINT fk_product_attributes_product FOREIGN KEY(product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE product_attribute_values (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 attribute_id BIGINT NOT NULL,
 value VARCHAR(100) NOT NULL,
 display_order INT UNSIGNED NOT NULL DEFAULT 0,
 UNIQUE KEY uk_attribute_value(attribute_id,value),
 CONSTRAINT fk_attribute_values_attribute FOREIGN KEY(attribute_id) REFERENCES product_attributes(id)
) ENGINE=InnoDB;

-- MVP: flat variants via variant_name/SKU/price/stock; attribute matrix is reserved for later.
-- ProductService must ensure each product has at least one ACTIVE and exactly one default variant.
CREATE TABLE product_variants (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 product_id BIGINT NOT NULL,
 sku VARCHAR(100) NOT NULL,
 variant_name VARCHAR(255),
 price DECIMAL(15,2) NOT NULL,
 stock_quantity INT UNSIGNED NOT NULL DEFAULT 0,
 is_default BOOLEAN NOT NULL DEFAULT FALSE,
 status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_product_variants_sku(sku),
 KEY idx_variants_product_status(product_id,status), KEY idx_variants_price(price),
 CONSTRAINT fk_product_variants_product FOREIGN KEY(product_id) REFERENCES products(id),
 CONSTRAINT chk_variant_price CHECK(price >= 0),
 CONSTRAINT chk_variant_stock CHECK(stock_quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE product_images (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 product_id BIGINT NOT NULL,
 variant_id BIGINT,
 image_url VARCHAR(1000) NOT NULL,
 alt_text VARCHAR(255),
 display_order INT UNSIGNED NOT NULL DEFAULT 0,
 is_primary BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_product_images_product_order(product_id,display_order), KEY idx_product_images_variant(variant_id),
 CONSTRAINT fk_product_images_product FOREIGN KEY(product_id) REFERENCES products(id),
 CONSTRAINT fk_product_images_variant FOREIGN KEY(variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB;

CREATE TABLE product_variant_attribute_values (
 variant_id BIGINT NOT NULL,
 attribute_value_id BIGINT NOT NULL,
 PRIMARY KEY(variant_id,attribute_value_id), KEY idx_pvav_attribute_value(attribute_value_id),
 CONSTRAINT fk_pvav_variant FOREIGN KEY(variant_id) REFERENCES product_variants(id),
 CONSTRAINT fk_pvav_attribute_value FOREIGN KEY(attribute_value_id) REFERENCES product_attribute_values(id)
) ENGINE=InnoDB;

-- One persistent cart per user; checkout removes only the selected cart_items.
CREATE TABLE carts (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_carts_user(user_id),
 CONSTRAINT fk_carts_user FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE cart_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 cart_id BIGINT NOT NULL,
 variant_id BIGINT NOT NULL,
 quantity INT UNSIGNED NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_cart_item_variant(cart_id,variant_id), KEY idx_cart_items_variant(variant_id),
 CONSTRAINT fk_cart_items_cart FOREIGN KEY(cart_id) REFERENCES carts(id),
 CONSTRAINT fk_cart_items_variant FOREIGN KEY(variant_id) REFERENCES product_variants(id),
 CONSTRAINT chk_cart_item_quantity CHECK(quantity > 0)
) ENGINE=InnoDB;

-- Parent order status is derived from child seller_orders and updated only by OrderService.
CREATE TABLE orders (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 order_code VARCHAR(40) NOT NULL,
 recipient_name VARCHAR(150) NOT NULL,
 recipient_phone VARCHAR(20) NOT NULL,
 shipping_province VARCHAR(100) NOT NULL,
 shipping_district VARCHAR(100) NULL,
 shipping_ward VARCHAR(100) NOT NULL,
 shipping_address VARCHAR(500) NOT NULL,
 customer_note VARCHAR(1000),
 subtotal DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 total_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 order_status ENUM('PENDING','PROCESSING','PARTIALLY_SHIPPED','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_orders_order_code(order_code),
 KEY idx_orders_user_created(user_id,created_at), KEY idx_orders_status_created(order_status,created_at),
 CONSTRAINT fk_orders_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT chk_orders_amounts CHECK(subtotal>=0 AND shipping_fee>=0 AND discount_amount>=0 AND total_amount>=0 AND total_amount = subtotal + shipping_fee - discount_amount)
) ENGINE=InnoDB;

-- Seller order owns business lifecycle; shipments.shipment_status owns logistics tracking.
CREATE TABLE seller_orders (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 order_id BIGINT NOT NULL,
 store_id BIGINT NOT NULL,
 seller_order_code VARCHAR(40) NOT NULL,
 subtotal DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 total_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
 status ENUM('PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
 seller_note VARCHAR(1000),
 cancellation_reason VARCHAR(1000),
 cancelled_by_user_id BIGINT,
 cancelled_at DATETIME(3),
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_seller_order_code(seller_order_code), UNIQUE KEY uk_order_store(order_id,store_id),
 KEY idx_seller_orders_store_status(store_id,status,created_at), KEY idx_seller_orders_order(order_id), KEY idx_seller_orders_cancelled_by(cancelled_by_user_id),
 CONSTRAINT fk_seller_orders_order FOREIGN KEY(order_id) REFERENCES orders(id),
 CONSTRAINT fk_seller_orders_store FOREIGN KEY(store_id) REFERENCES stores(id),
 CONSTRAINT fk_seller_orders_cancelled_by FOREIGN KEY(cancelled_by_user_id) REFERENCES users(id),
 CONSTRAINT chk_seller_orders_amounts CHECK(subtotal>=0 AND shipping_fee>=0 AND discount_amount>=0 AND total_amount>=0 AND total_amount = subtotal + shipping_fee - discount_amount)
) ENGINE=InnoDB;

CREATE TABLE order_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 seller_order_id BIGINT NOT NULL,
 variant_id BIGINT NOT NULL,
 product_name VARCHAR(255) NOT NULL,
 product_image VARCHAR(1000),
 variant_description VARCHAR(500),
 sku VARCHAR(100) NOT NULL,
 unit_price DECIMAL(15,2) NOT NULL,
 quantity INT UNSIGNED NOT NULL,
 subtotal DECIMAL(15,2) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_order_items_seller_order(seller_order_id), KEY idx_order_items_variant(variant_id),
 CONSTRAINT fk_order_items_seller_order FOREIGN KEY(seller_order_id) REFERENCES seller_orders(id),
 CONSTRAINT fk_order_items_variant FOREIGN KEY(variant_id) REFERENCES product_variants(id),
 CONSTRAINT chk_order_item_values CHECK(unit_price>=0 AND quantity>0 AND subtotal>=0)
) ENGINE=InnoDB;

-- MVP: one COD payment per Seller Order. Online parent-order payments need a later redesign.
CREATE TABLE payments (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 seller_order_id BIGINT NOT NULL,
 payment_method ENUM('COD') NOT NULL DEFAULT 'COD',
 amount DECIMAL(15,2) NOT NULL,
 payment_status ENUM('PENDING','PAID','FAILED','CANCELLED','REFUNDED') NOT NULL DEFAULT 'PENDING',
 paid_at DATETIME(3),
 collected_by VARCHAR(150),
 note VARCHAR(1000),
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_payments_seller_order(seller_order_id), KEY idx_payments_status_created(payment_status,created_at),
 CONSTRAINT fk_payments_seller_order FOREIGN KEY(seller_order_id) REFERENCES seller_orders(id),
 CONSTRAINT chk_payments_amount CHECK(amount>=0)
) ENGINE=InnoDB;

CREATE TABLE shipping_providers (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 code VARCHAR(50) NOT NULL,
 phone VARCHAR(20),
 website VARCHAR(500),
 status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
 UNIQUE KEY uk_shipping_providers_code(code)
) ENGINE=InnoDB;

CREATE TABLE shipments (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 seller_order_id BIGINT NOT NULL,
 provider_id BIGINT,
 tracking_code VARCHAR(100),
 shipment_status ENUM('PENDING','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','FAILED','RETURNED') NOT NULL DEFAULT 'PENDING',
 shipped_at DATETIME(3),
 delivered_at DATETIME(3),
 note VARCHAR(1000),
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_shipments_seller_order(seller_order_id),
 KEY idx_shipments_provider_tracking(provider_id,tracking_code), KEY idx_shipments_status(shipment_status),
 CONSTRAINT fk_shipments_seller_order FOREIGN KEY(seller_order_id) REFERENCES seller_orders(id),
 CONSTRAINT fk_shipments_provider FOREIGN KEY(provider_id) REFERENCES shipping_providers(id)
) ENGINE=InnoDB;

CREATE TABLE reviews (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 product_id BIGINT NOT NULL,
 order_item_id BIGINT NOT NULL,
 rating TINYINT UNSIGNED NOT NULL,
 comment TEXT,
 status ENUM('VISIBLE','HIDDEN') NOT NULL DEFAULT 'VISIBLE',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_reviews_order_item(order_item_id),
 KEY idx_reviews_product_status_created(product_id,status,created_at), KEY idx_reviews_user(user_id),
 CONSTRAINT fk_reviews_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT fk_reviews_product FOREIGN KEY(product_id) REFERENCES products(id),
 CONSTRAINT fk_reviews_order_item FOREIGN KEY(order_item_id) REFERENCES order_items(id),
 CONSTRAINT chk_reviews_rating CHECK(rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE conversations (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 customer_id BIGINT NOT NULL,
 store_id BIGINT NOT NULL,
 status ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_conversation_customer_store(customer_id,store_id),
 KEY idx_conversations_store_updated(store_id,updated_at),
 CONSTRAINT fk_conversations_customer FOREIGN KEY(customer_id) REFERENCES users(id),
 CONSTRAINT fk_conversations_store FOREIGN KEY(store_id) REFERENCES stores(id)
) ENGINE=InnoDB;

CREATE TABLE messages (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 conversation_id BIGINT NOT NULL,
 sender_id BIGINT NOT NULL,
 content TEXT NOT NULL,
 message_type ENUM('TEXT','IMAGE','SYSTEM') NOT NULL DEFAULT 'TEXT',
 read_at DATETIME(3),
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_messages_conversation_created(conversation_id,created_at), KEY idx_messages_sender(sender_id),
 CONSTRAINT fk_messages_conversation FOREIGN KEY(conversation_id) REFERENCES conversations(id),
 CONSTRAINT fk_messages_sender FOREIGN KEY(sender_id) REFERENCES users(id)
) ENGINE=InnoDB;

INSERT INTO roles(name,description) VALUES
('CUSTOMER','Khách hàng'),('SELLER','Người bán'),('ADMIN','Quản trị viên');


-- ============================================================
-- SMARTSHOP MVP IMPLEMENTATION RULES (APPLICATION LAYER)
-- ============================================================
-- 1. CART:
--    carts.user_id is UNIQUE. Keep one persistent cart per user.
--    Checkout accepts selected cartItemIds and deletes only those cart_items
--    after the entire checkout transaction succeeds.
--
-- 2. PRODUCT VARIANTS:
--    MVP uses flat product_variants.variant_name/SKU/price/stock_quantity.
--    Attribute tables remain reserved; do not create their JPA entities until used.
--    ProductService must ensure each product has at least one ACTIVE variant and
--    exactly one default variant. This cross-row rule is enforced in Service.
--
-- 3. SELLER ORDER STATUS (business lifecycle):
--    PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED.
--    Customer may cancel only PENDING.
--    Seller may cancel PENDING or CONFIRMED before shipment and must provide a reason.
--    Cancellation must be an atomic conditional update, for example:
--      UPDATE seller_orders
--      SET status='CANCELLED', cancellation_reason=:reason,
--          cancelled_by_user_id=:actorId, cancelled_at=CURRENT_TIMESTAMP(3)
--      WHERE id=:id AND status IN ('PENDING','CONFIRMED');
--    If affected rows = 0, do not restore stock; report a state conflict.
--    Restore stock only after the conditional status update succeeds, in the same
--    transaction. If restoration fails, roll back the cancellation too.
--
-- 4. PARENT ORDER STATUS:
--    orders.order_status is derived centrally by OrderService; clients and Seller
--    endpoints must not write it directly.
--    If every child Seller Order is CANCELLED => parent CANCELLED.
--    Otherwise ignore CANCELLED children when deriving fulfilment progress:
--      all remaining children DELIVERED => DELIVERED;
--      any remaining child SHIPPED/DELIVERED while another is not complete
--        => PARTIALLY_SHIPPED (or SHIPPED if all remaining are SHIPPED/DELIVERED);
--      all remaining children PENDING => PENDING;
--      otherwise => PROCESSING.
--    The UI separately displays which child Seller Orders were cancelled.
--    The parent order monetary snapshot remains unchanged after partial cancellation.
--
-- 5. SHIPMENTS AND DELIVERY:
--    shipments.shipment_status is logistics tracking; seller_orders.status is
--    business lifecycle. In MVP, an authorized Admin/operator confirms delivery
--    (or a trusted shipping integration reports it). A single OrderService method
--    validates shipment_status=DELIVERED and transitions Seller Order to DELIVERED.
--    Do not let arbitrary Seller clients directly mark an order DELIVERED.
--
-- 6. PAYMENTS (COD):
--    PENDING = COD not yet collected; PAID = COD collection confirmed;
--    CANCELLED = Seller Order cancelled before collection;
--    FAILED = collection/processing failure where applicable;
--    REFUNDED = money was collected and subsequently returned.
--    Mark PAID only when collection is confirmed, normally at successful delivery.
--    If already PAID, cancellation requires a separate refund workflow; do not
--    simply change PAID to CANCELLED.
--    Current UNIQUE seller_order_id and COD-only ENUM are MVP limitations.
--    Online parent-order payment attempts require a later schema redesign.
--
-- 7. MONEY AND PARTIAL CANCELLATION:
--    Keep order/seller_order subtotal, shipping_fee, discount_amount and total_amount
--    as immutable checkout snapshots. Do not subtract cancelled child amounts from
--    the parent or child snapshot columns. Display payable/active amounts by deriving
--    them from non-cancelled Seller Orders. Allocate shipping/discount to each
--    Seller Order at checkout; parent totals must equal the sum of child totals.
--
-- 8. INVENTORY / ANTI-OVERSELLING:
--    Decrement stock atomically inside checkout transaction:
--      UPDATE product_variants
--      SET stock_quantity = stock_quantity - :qty
--      WHERE id = :variantId AND stock_quantity >= :qty;
--    If affected rows = 0, abort and roll back the whole checkout.
--    When multiple variants are involved, process variant IDs in ascending order.
--    Restore stock only after a successful conditional cancellation, exactly once.
--
-- 9. SERVICE VALIDATIONS:
--    - Variant attribute values (when enabled) must belong to the same Product.
--    - A variant image must belong to the same Product as product_id.
--    - Review user must own the Order Item and its Seller Order must be DELIVERED.
--    - Seller may manage only products/orders belonging to their Store.
--    - Product must have exactly one default variant; validate in ProductService.
--
-- 10. JPA AND MIGRATIONS:
--    Use spring.jpa.hibernate.ddl-auto=none because SQL/migrations own the schema.
--    Use Flyway from the first version (V1__init_schema.sql); do not edit a migration
--    after it has been applied to a shared database—create a new version instead.
--    Map enums with EnumType.STRING and verify mappings with MySQL integration tests.
--    IDs are signed BIGINT to map naturally to Java Long.
--
-- 11. IMAGE STORAGE:
--    Store image files outside MySQL (local storage for development or an image
--    hosting provider later). Persist only URL/path and metadata in the database.
