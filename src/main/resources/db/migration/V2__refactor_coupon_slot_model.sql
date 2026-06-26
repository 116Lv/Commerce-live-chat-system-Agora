-- Refactor coupon domain from Coupon/CouponEvent/CouponIssue to CouponEvent + Coupon slot model.
-- Existing issued coupon history is copied from coupon_issues into the new coupons slot table.

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE coupon_events
    ADD COLUMN type VARCHAR(30) NULL AFTER id,
    ADD COLUMN discount_amount INT NULL AFTER end_at,
    ADD COLUMN min_order_amount INT NULL AFTER discount_amount,
    ADD COLUMN valid_days INT NULL AFTER min_order_amount;

UPDATE coupon_events ce
LEFT JOIN (
    SELECT coupon_event_id, MIN(coupon_id) AS coupon_id
    FROM coupon_issues
    GROUP BY coupon_event_id
) issued_policy ON issued_policy.coupon_event_id = ce.id
LEFT JOIN coupons c ON c.id = issued_policy.coupon_id
SET
    ce.type = COALESCE(c.type, 'FIRST_COME'),
    ce.discount_amount = COALESCE(c.discount_amount, 0),
    ce.min_order_amount = COALESCE(c.min_order_amount, 0),
    ce.valid_days = COALESCE(c.valid_days, 30);

ALTER TABLE coupon_events
    MODIFY COLUMN type VARCHAR(30) NOT NULL,
    MODIFY COLUMN discount_amount INT NOT NULL,
    MODIFY COLUMN min_order_amount INT NOT NULL,
    MODIFY COLUMN valid_days INT NOT NULL;

RENAME TABLE coupons TO coupon_policies;

CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_event_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at DATETIME(6) NULL,
    expires_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupons_event_user (coupon_event_id, user_id),
    KEY idx_coupons_event_status (coupon_event_id, status),
    KEY idx_coupons_user_status (user_id, status),
    CONSTRAINT fk_coupons_coupon_event
        FOREIGN KEY (coupon_event_id) REFERENCES coupon_events (id),
    CONSTRAINT fk_coupons_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO coupons (id, coupon_event_id, user_id, status, issued_at, expires_at)
SELECT
    ci.id,
    ci.coupon_event_id,
    ci.user_id,
    ci.status,
    ci.issued_at,
    DATE_ADD(ci.issued_at, INTERVAL COALESCE(cp.valid_days, ce.valid_days, 30) DAY)
FROM coupon_issues ci
JOIN coupon_events ce ON ce.id = ci.coupon_event_id
LEFT JOIN coupon_policies cp ON cp.id = ci.coupon_id;

INSERT INTO coupons (coupon_event_id, user_id, status, issued_at, expires_at)
SELECT
    ce.id,
    NULL,
    'AVAILABLE',
    NULL,
    NULL
FROM coupon_events ce
JOIN (
    SELECT
        ones.n
        + tens.n * 10
        + hundreds.n * 100
        + thousands.n * 1000
        + ten_thousands.n * 10000
        + 1 AS n
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
    CROSS JOIN
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
    CROSS JOIN
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) hundreds
    CROSS JOIN
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) thousands
    CROSS JOIN
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ten_thousands
) seq ON seq.n <= GREATEST(ce.total_quantity - ce.issued_quantity, 0);

SET FOREIGN_KEY_CHECKS = 1;
