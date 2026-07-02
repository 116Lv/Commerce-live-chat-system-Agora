ALTER TABLE admin_approval_requests
    ADD COLUMN target_product_id BIGINT NULL,
    ADD COLUMN target_product_title VARCHAR(100) NULL,
    ADD KEY idx_admin_approval_requests_target_product (target_product_id);
