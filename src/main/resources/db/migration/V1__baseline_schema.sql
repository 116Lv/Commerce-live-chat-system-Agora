-- Initial schema for a fresh MySQL database.
-- This creates the legacy schema expected by V2 and the other incremental migrations.

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    phone VARCHAR(20) NULL,
    smile_score INT NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE regions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    sido VARCHAR(30) NOT NULL,
    sigungu VARCHAR(30) NOT NULL,
    eupmyeondong VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_regions_code (code),
    KEY idx_regions_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_regions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    primary_region BIT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_regions_user (user_id),
    KEY idx_user_regions_region (region_id),
    CONSTRAINT fk_user_regions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_regions_region FOREIGN KEY (region_id) REFERENCES regions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price DECIMAL(12, 0) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    view_count INT NOT NULL,
    like_count INT NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_products_seller (seller_id),
    KEY idx_products_region (region_id),
    KEY idx_products_status_deleted (status, deleted_at),
    KEY idx_products_region_status_deleted (region_id, status, deleted_at),
    KEY idx_products_category_status_deleted (category, status, deleted_at),
    KEY idx_products_title (title),
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT fk_products_region FOREIGN KEY (region_id) REFERENCES regions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_product_images_product (product_id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_likes_product_user (product_id, user_id),
    KEY idx_product_likes_user (user_id),
    CONSTRAINT fk_product_likes_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE coupon_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    total_quantity INT NOT NULL,
    issued_quantity INT NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(30) NOT NULL,
    discount_amount INT NOT NULL,
    min_order_amount INT NOT NULL,
    valid_days INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE coupon_issues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_event_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupon_issues_event_user (coupon_event_id, user_id),
    KEY idx_coupon_issues_coupon (coupon_id),
    KEY idx_coupon_issues_user_status (user_id, status),
    CONSTRAINT fk_coupon_issues_coupon_event FOREIGN KEY (coupon_event_id) REFERENCES coupon_events (id),
    CONSTRAINT fk_coupon_issues_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id),
    CONSTRAINT fk_coupon_issues_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chat_rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    seller_last_read_at DATETIME(6) NULL,
    buyer_last_read_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_rooms_product_seller_buyer (product_id, seller_id, buyer_id),
    KEY idx_chat_rooms_buyer_status (buyer_id, status),
    KEY idx_chat_rooms_seller_status (seller_id, status),
    KEY idx_chat_rooms_product (product_id),
    CONSTRAINT fk_chat_rooms_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_chat_rooms_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT fk_chat_rooms_buyer FOREIGN KEY (buyer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_chat_messages_room_id (chat_room_id, id),
    KEY idx_chat_messages_sender (sender_id),
    CONSTRAINT fk_chat_messages_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms (id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nego_offers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    offer_price DECIMAL(12, 0) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    responded_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_nego_offers_chat_room_status (chat_room_id, status),
    KEY idx_nego_offers_requester (requester_id),
    CONSTRAINT fk_nego_offers_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms (id),
    CONSTRAINT fk_nego_offers_requester FOREIGN KEY (requester_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE trades (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    price DECIMAL(12, 0) NOT NULL,
    completed_at DATETIME(6) NULL,
    payment_due_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_trades_product (product_id),
    KEY idx_trades_buyer (buyer_id),
    KEY idx_trades_seller (seller_id),
    CONSTRAINT fk_trades_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_trades_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT fk_trades_buyer FOREIGN KEY (buyer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trade_id BIGINT NOT NULL,
    payer_id BIGINT NOT NULL,
    amount DECIMAL(12, 0) NOT NULL,
    order_id VARCHAR(80) NOT NULL,
    payment_key VARCHAR(120) NULL,
    status VARCHAR(20) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    paid_at DATETIME(6) NULL,
    refunded_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_trade (trade_id),
    UNIQUE KEY uk_payments_order_id (order_id),
    KEY idx_payments_payment_key (payment_key),
    KEY idx_payments_status_requested (status, requested_at),
    KEY idx_payments_payer (payer_id),
    CONSTRAINT fk_payments_trade FOREIGN KEY (trade_id) REFERENCES trades (id),
    CONSTRAINT fk_payments_payer FOREIGN KEY (payer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE settlements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12, 0) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    settled_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_settlements_payment (payment_id),
    KEY idx_settlements_seller (seller_id),
    CONSTRAINT fk_settlements_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_settlements_seller FOREIGN KEY (seller_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trade_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_trade_reviewer (trade_id, reviewer_id),
    KEY idx_reviews_reviewer (reviewer_id),
    KEY idx_reviews_target_user (target_user_id),
    CONSTRAINT fk_reviews_trade FOREIGN KEY (trade_id) REFERENCES trades (id),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id),
    CONSTRAINT fk_reviews_target_user FOREIGN KEY (target_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    admin_memo VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_reports_status_created (status, created_at),
    KEY idx_reports_reported_user (reported_user_id),
    KEY idx_reports_reporter (reporter_id),
    KEY idx_reports_product (product_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES users (id),
    CONSTRAINT fk_reports_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
