ALTER TABLE payments
    ADD COLUMN refund_requested_at DATETIME NULL,
    ADD COLUMN refund_reason VARCHAR(500) NULL;

ALTER TABLE admin_approval_requests
    ADD COLUMN target_payment_id BIGINT NULL,
    ADD KEY idx_admin_approval_requests_target_payment (target_payment_id);
