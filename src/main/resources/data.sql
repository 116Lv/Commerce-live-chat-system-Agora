INSERT INTO users (id, email, password, nickname, phone, smile_score, role, status, deleted_at)
VALUES
    (1, 'seller@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'seller', '010-1111-1111', 82, 'ROLE_USER', 'ACTIVE', NULL),
    (2, 'buyer@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'buyer', '010-2222-2222', 75, 'ROLE_USER', 'ACTIVE', NULL),
    (3, 'user2@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'user2', '010-3333-3333', 91, 'ROLE_USER', 'ACTIVE', NULL),
    (4, 'admin@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'root-admin', '010-4444-4444', 60, 'ROOT_ADMIN', 'ACTIVE', NULL),
    (5, 'blocked@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'blocked', '010-5555-5555', 20, 'ROLE_USER', 'BLOCKED', NULL),
    (6, 'useradmin@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'user-admin', '010-6666-6666', 60, 'USER_ADMIN', 'ACTIVE', NULL),
    (7, 'productadmin@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'product-admin', '010-7777-7777', 60, 'PRODUCT_ADMIN', 'ACTIVE', NULL),
    (8, 'settlementadmin@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', 'settlement-admin', '010-8888-8888', 60, 'SETTLEMENT_ADMIN', 'ACTIVE', NULL);

INSERT INTO regions (id, name, code, sido, sigungu, eupmyeondong)
VALUES
    (1, 'Seoul Gangnam Yeoksam', '1168010100', 'Seoul', 'Gangnam-gu', 'Yeoksam-dong'),
    (2, 'Seoul Songpa Jamsil', '1171010100', 'Seoul', 'Songpa-gu', 'Jamsil-dong'),
    (3, 'Gyeonggi Seongnam Bundang Jeongja', '4113510300', 'Gyeonggi-do', 'Seongnam-si Bundang-gu', 'Jeongja-dong'),
    (4, 'Gyeonggi Suwon Yeongtong Gwanggyo', '4111710300', 'Gyeonggi-do', 'Suwon-si Yeongtong-gu', 'Gwanggyo-dong'),
    (5, 'Incheon Yeonsu Songdo', '2818510600', 'Incheon', 'Yeonsu-gu', 'Songdo-dong');

INSERT INTO user_regions (id, user_id, region_id, primary_region)
VALUES
    (1, 1, 1, TRUE),
    (2, 1, 2, FALSE),
    (3, 1, 3, FALSE),
    (4, 2, 1, TRUE),
    (5, 2, 3, FALSE),
    (6, 2, 4, FALSE),
    (7, 3, 2, TRUE),
    (8, 3, 4, FALSE),
    (9, 3, 5, FALSE),
    (10, 5, 5, TRUE);

INSERT INTO products (id, seller_id, region_id, title, description, price, category, status, view_count, like_count, deleted_at)
VALUES
    (1, 1, 1, 'iPhone 15 128GB Black', 'Good condition battery 92 percent', 850000, 'Electronics', 'SELLING', 31, 2, NULL),
    (2, 1, 1, 'MacBook Air M2 13 inch', 'Used for document work', 720000, 'Electronics', 'SOLD', 54, 1, NULL),
    (3, 2, 3, 'Camping Chair Set', 'Light and compact chair set', 45000, 'Sports', 'SELLING', 12, 1, NULL),
    (4, 3, 2, 'Wood Desk', 'Pickup required', 60000, 'Furniture', 'SOLD', 40, 1, NULL),
    (5, 2, 4, 'Bicycle Helmet', 'Size M', 25000, 'Sports', 'RESERVED', 8, 0, NULL),
    (6, 3, 5, 'Coffee Beans 1kg', 'Fresh beans', 18000, 'Food', 'HIDDEN', 5, 0, NULL),
    (7, 1, 2, 'Wireless Keyboard', 'Low noise keyboard', 35000, 'Electronics', 'SELLING', 18, 0, NULL),
    (8, 2, 3, 'Deleted Test Product', 'Deleted product for tests', 10000, 'Etc', 'DELETED', 0, 0, '2026-06-20 12:00:00');

INSERT INTO product_images (id, product_id, image_url, sort_order)
VALUES
    (1, 1, '/uploads/products/seed-product-1-main.jpg', 0),
    (2, 1, '/uploads/products/seed-product-1-detail.jpg', 1),
    (3, 2, '/uploads/products/seed-product-2-main.jpg', 0),
    (4, 3, '/uploads/products/seed-product-3-main.jpg', 0),
    (5, 4, '/uploads/products/seed-product-4-main.jpg', 0),
    (6, 5, '/uploads/products/seed-product-5-main.jpg', 0);

INSERT INTO product_likes (id, product_id, user_id)
VALUES
    (1, 1, 2),
    (2, 1, 3),
    (3, 2, 2),
    (4, 3, 1),
    (5, 4, 1);

INSERT INTO coupon_events (
    id, type, name, total_quantity, issued_quantity, start_at, end_at,
    discount_amount, min_order_amount, valid_days, status, created_at
)
VALUES
    (1, 'FIRST_COME', 'First Trade Coupon', 100, 3, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 5000, 10000, 30, 'ACTIVE', '2026-01-01 00:00:00'),
    (2, 'NEW_SIGNUP', 'Smile Reward Coupon Event', 50, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 3000, 10000, 30, 'ACTIVE', '2026-01-01 00:00:00'),
    (3, 'ADMIN_INDIVIDUAL', 'Admin Test Coupon Event', 10, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7000, 30000, 14, 'ACTIVE', '2026-01-01 00:00:00');

INSERT INTO coupons (id, coupon_event_id, user_id, status, issued_at, expires_at)
VALUES
    (1, 1, 1, 'ISSUED', '2026-06-23 09:00:00', '2026-07-23 09:00:00'),
    (2, 1, 2, 'USED', '2026-06-23 09:05:00', '2026-07-23 09:05:00'),
    (3, 1, 3, 'ISSUED', '2026-06-23 09:10:00', '2026-07-23 09:10:00'),
    (4, 1, NULL, 'AVAILABLE', NULL, NULL),
    (5, 2, 3, 'ISSUED', '2026-06-23 09:15:00', '2026-07-23 09:15:00'),
    (6, 2, NULL, 'AVAILABLE', NULL, NULL),
    (7, 3, NULL, 'AVAILABLE', NULL, NULL);

INSERT INTO chat_rooms (id, product_id, seller_id, buyer_id, status, created_at, seller_last_read_at, buyer_last_read_at)
VALUES
    (1, 2, 1, 2, 'ACTIVE', '2026-06-23 09:10:00', '2026-06-23 09:12:00', '2026-06-23 09:13:00'),
    (2, 1, 1, 3, 'ACTIVE', '2026-06-23 09:20:00', NULL, '2026-06-23 09:21:00'),
    (3, 5, 2, 1, 'ACTIVE', '2026-06-23 10:00:00', NULL, NULL);

INSERT INTO chat_messages (id, chat_room_id, sender_id, content, message_type, created_at)
VALUES
    (1, 1, 2, 'Is MacBook still available?', 'TEXT', '2026-06-23 09:11:00'),
    (2, 1, 1, 'Yes it is available.', 'TEXT', '2026-06-23 09:12:00'),
    (3, 1, 2, 'Can you do 580000?', 'TEXT', '2026-06-23 09:13:00'),
    (4, 2, 3, 'Where can we meet for iPhone?', 'TEXT', '2026-06-23 09:21:00'),
    (5, 2, 1, '/uploads/chat/seed-chat-image.jpg', 'IMAGE', '2026-06-23 09:22:00'),
    (6, 3, 1, 'Can we trade the helmet today?', 'TEXT', '2026-06-23 10:01:00');

INSERT INTO nego_offers (id, chat_room_id, requester_id, offer_price, expires_at, status, created_at, responded_at)
VALUES
    (1, 1, 2, 580000, '2099-12-31 23:59:59', 'ACCEPTED', '2026-06-23 09:14:00', '2026-06-23 09:15:00'),
    (2, 2, 3, 800000, '2099-12-31 23:59:59', 'PENDING', '2026-06-23 09:22:00', NULL),
    (3, 3, 1, 20000, '2099-12-31 23:59:59', 'REJECTED', '2026-06-23 10:05:00', '2026-06-23 10:10:00');

INSERT INTO trades (id, product_id, seller_id, buyer_id, status, price, completed_at, payment_due_at)
VALUES
    (1, 2, 1, 2, 'PAID', 580000, NULL, NULL),
    (2, 4, 3, 2, 'COMPLETED', 60000, '2026-06-22 18:00:00', NULL),
    (3, 5, 2, 1, 'PAYMENT_PENDING', 23000, NULL, '2026-06-24 10:30:00'),
    (4, 7, 1, 3, 'CANCELLED', 33000, NULL, NULL),
    (5, 3, 2, 3, 'EXPIRED', 42000, NULL, NULL);

INSERT INTO payments (id, trade_id, payer_id, amount, order_id, payment_key, status, requested_at, paid_at, refunded_at)
VALUES
    (1, 1, 2, 580000, 'order-dummy-trade-1', 'payment-key-dummy-1', 'PAID', '2026-06-23 09:30:00', '2026-06-23 09:31:00', NULL),
    (2, 2, 2, 60000, 'order-dummy-trade-2', 'payment-key-dummy-2', 'PAID', '2026-06-22 17:50:00', '2026-06-22 17:51:00', NULL),
    (3, 3, 1, 23000, 'order-dummy-trade-3', NULL, 'READY', '2026-06-23 10:30:00', NULL, NULL),
    (4, 4, 3, 33000, 'order-dummy-trade-4', 'payment-key-dummy-4', 'REFUNDED', '2026-06-23 11:00:00', '2026-06-23 11:01:00', '2026-06-23 11:30:00'),
    (5, 5, 3, 42000, 'order-dummy-trade-5', NULL, 'FAILED', '2026-06-23 12:00:00', NULL, NULL);

INSERT INTO settlements (id, payment_id, seller_id, amount, status, created_at, settled_at)
VALUES
    (1, 1, 1, 580000, 'READY', '2026-06-23 09:31:00', NULL),
    (2, 2, 3, 60000, 'SETTLED', '2026-06-22 17:51:00', '2026-06-22 18:01:00'),
    (3, 4, 1, 33000, 'HELD', '2026-06-23 11:01:00', NULL);

INSERT INTO reviews (id, trade_id, reviewer_id, target_user_id, rating, content, created_at)
VALUES
    (1, 2, 2, 3, 5, 'On time and kind.', '2026-06-22 18:10:00'),
    (2, 2, 3, 2, 4, 'Fast trade.', '2026-06-22 18:12:00');

INSERT INTO reports (id, reporter_id, reported_user_id, product_id, reason, status, admin_memo, created_at, resolved_at)
VALUES
    (1, 2, 1, 1, 'Product description differs.', 'PENDING', NULL, '2026-06-23 10:00:00', NULL),
    (2, 3, 5, 5, 'Repeated inappropriate messages.', 'RESOLVED', 'Blocked user after review', '2026-06-22 16:00:00', '2026-06-22 16:30:00'),
    (3, 1, 3, 4, 'No response after trade.', 'REJECTED', 'Insufficient evidence', '2026-06-21 13:00:00', '2026-06-21 14:00:00');