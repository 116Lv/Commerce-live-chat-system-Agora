INSERT INTO users (id, email, password, nickname, phone, smile_score, role, status, deleted_at)
VALUES
    (1, 'seller@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', '판매자동네', '010-1111-1111', 82, 'ROLE_USER', 'ACTIVE', NULL),
    (2, 'buyer@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', '구매자동네', '010-2222-2222', 75, 'ROLE_USER', 'ACTIVE', NULL),
    (3, 'user2@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', '이웃사용자', '010-3333-3333', 91, 'ROLE_USER', 'ACTIVE', NULL),
    (4, 'admin@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', '관리자', '010-4444-4444', 60, 'ROOT_ADMIN', 'ACTIVE', NULL),
    (5, 'blocked@test.com', '$2a$10$5XA3pQtBuOJfBIqcOZzcSeKMTBHqQpqFswCqh/fqLyxd5o7vmkY02', '차단회원', '010-5555-5555', 20, 'ROLE_USER', 'BLOCKED', NULL);

INSERT INTO regions (id, name, code, sido, sigungu, eupmyeondong)
VALUES
    (1, '서울특별시 강남구 역삼동', '1168010100', '서울특별시', '강남구', '역삼동'),
    (2, '서울특별시 송파구 잠실동', '1171010100', '서울특별시', '송파구', '잠실동'),
    (3, '경기도 성남시 분당구 정자동', '4113510300', '경기도', '성남시 분당구', '정자동'),
    (4, '경기도 수원시 영통구 광교동', '4111710300', '경기도', '수원시 영통구', '광교동'),
    (5, '인천광역시 연수구 송도동', '2818510600', '인천광역시', '연수구', '송도동');

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
    (9, 3, 5, FALSE);

INSERT INTO products (id, seller_id, region_id, title, description, price, category, status, view_count, like_count, deleted_at)
VALUES
    (1, 1, 1, '아이폰 15 128GB 블랙', '상태 좋고 배터리 성능 92%입니다. 직거래 선호합니다.', 850000, '디지털기기', 'SELLING', 31, 5, NULL),
    (2, 1, 1, '맥북 에어 M2 13인치', '문서 작업 위주로 사용했습니다. 생활 기스 조금 있습니다.', 720000, '디지털기기', 'SELLING', 54, 9, NULL),
    (3, 2, 3, '캠핑 의자 2개 세트', '가볍고 접이식이라 캠핑 입문자에게 좋아요.', 45000, '스포츠/레저', 'SELLING', 12, 2, NULL),
    (4, 3, 2, '원목 책상', '이사 정리로 내놓습니다. 직접 가져가셔야 합니다.', 60000, '가구/인테리어', 'SOLD', 40, 4, NULL),
    (5, 2, 4, '자전거 헬멧', '사이즈 M, 사용감 적습니다.', 25000, '스포츠/레저', 'SELLING', 8, 1, NULL),
    (6, 3, 5, '커피 원두 1kg', '선물 받았는데 마시지 않아 판매합니다.', 18000, '생활/식품', 'SELLING', 5, 0, NULL);

INSERT INTO coupon_events (id, name, total_quantity, issued_quantity, start_at, end_at, status)
VALUES
    (1, '동네 첫 거래 선착순 쿠폰', 100, 2, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE'),
    (2, '종료된 테스트 쿠폰 이벤트', 10, 10, '2026-01-01 00:00:00', '2026-01-31 23:59:59', 'ENDED');

INSERT INTO coupons (id, name, discount_amount, min_order_amount, type, status, valid_days)
VALUES
    (1, '선착순 5천원 할인 쿠폰', 5000, 10000, 'FIRST_COME', 'ACTIVE', 30),
    (2, '매너점수 우수 쿠폰', 3000, 10000, 'SMILE_REWARD', 'ACTIVE', 30);

INSERT INTO coupon_issues (id, coupon_id, coupon_event_id, user_id, status, issued_at)
VALUES
    (1, 1, 1, 1, 'ISSUED', '2026-06-23 09:00:00'),
    (2, 1, 1, 2, 'ISSUED', '2026-06-23 09:05:00');

INSERT INTO chat_rooms (id, product_id, seller_id, buyer_id, status, created_at)
VALUES
    (1, 2, 1, 2, 'ACTIVE', '2026-06-23 09:10:00'),
    (2, 1, 1, 3, 'ACTIVE', '2026-06-23 09:20:00');

INSERT INTO chat_messages (id, chat_room_id, sender_id, content, message_type, created_at)
VALUES
    (1, 1, 2, '맥북 아직 판매 중인가요?', 'TEXT', '2026-06-23 09:11:00'),
    (2, 1, 1, '네, 아직 판매 중입니다.', 'TEXT', '2026-06-23 09:12:00'),
    (3, 1, 2, '58만원 가능할까요?', 'TEXT', '2026-06-23 09:13:00'),
    (4, 2, 3, '아이폰 직거래 위치가 어디인가요?', 'TEXT', '2026-06-23 09:21:00');

INSERT INTO nego_offers (id, chat_room_id, requester_id, offer_price, expires_at, status, created_at, responded_at)
VALUES
    (1, 1, 2, 580000, '2026-06-24 09:14:00', 'ACCEPTED', '2026-06-23 09:14:00', '2026-06-23 09:15:00'),
    (2, 2, 3, 800000, '2026-06-24 09:22:00', 'PENDING', '2026-06-23 09:22:00', NULL);

INSERT INTO trades (id, product_id, seller_id, buyer_id, status, price, completed_at)
VALUES
    (1, 2, 1, 2, 'PAYMENT_PENDING', 580000, NULL),
    (2, 4, 3, 2, 'COMPLETED', 60000, '2026-06-22 18:00:00');

INSERT INTO payments (id, trade_id, payer_id, amount, order_id, payment_key, status, requested_at, paid_at, refunded_at)
VALUES
    (1, 1, 2, 580000, 'order-dummy-trade-1', 'payment-key-dummy-1', 'PAID', '2026-06-23 09:30:00', '2026-06-23 09:31:00', NULL),
    (2, 2, 2, 60000, 'order-dummy-trade-2', 'payment-key-dummy-2', 'PAID', '2026-06-22 17:50:00', '2026-06-22 17:51:00', NULL);

INSERT INTO settlements (id, payment_id, seller_id, amount, status, created_at, settled_at)
VALUES
    (1, 1, 1, 580000, 'HELD', '2026-06-23 09:31:00', NULL),
    (2, 2, 3, 60000, 'SETTLED', '2026-06-22 17:51:00', '2026-06-22 18:01:00');

INSERT INTO reviews (id, trade_id, reviewer_id, target_user_id, rating, content, created_at)
VALUES
    (1, 2, 2, 3, 5, '시간 약속을 잘 지켜주셨어요.', '2026-06-22 18:10:00');

INSERT INTO reports (id, reporter_id, reported_user_id, product_id, reason, status, admin_memo, created_at, resolved_at)
VALUES
    (1, 2, 1, 1, '상품 설명과 실제 상태가 다를 수 있어 신고합니다.', 'PENDING', NULL, '2026-06-23 10:00:00', NULL),
    (2, 3, 5, 5, '반복적으로 부적절한 메시지를 보냅니다.', 'RESOLVED', '신고 확인 후 사용자 차단 처리', '2026-06-22 16:00:00', '2026-06-22 16:30:00');
