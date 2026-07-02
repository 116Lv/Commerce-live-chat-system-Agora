ALTER TABLE payments
    ADD COLUMN original_amount DECIMAL(12, 0) NOT NULL DEFAULT 0 AFTER amount,
    ADD COLUMN discount_amount DECIMAL(12, 0) NOT NULL DEFAULT 0 AFTER original_amount,
    ADD COLUMN coupon_id BIGINT NULL AFTER discount_amount;

UPDATE payments
SET original_amount = amount
WHERE original_amount = 0;

CREATE INDEX idx_payments_coupon ON payments (coupon_id);

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons (id);
