ALTER TABLE admin_approval_requests
    MODIFY target_admin_id BIGINT NULL,
    MODIFY requested_role VARCHAR(30) NULL;

CREATE TABLE IF NOT EXISTS admin_coupon_approval_payloads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    approval_request_id BIGINT NOT NULL,
    coupon_event_id BIGINT NOT NULL,
    event_type VARCHAR(30) NULL,
    event_name VARCHAR(100) NULL,
    start_at DATETIME NULL,
    end_at DATETIME NULL,
    total_quantity INT NULL,
    discount_amount INT NULL,
    min_order_amount INT NULL,
    valid_days INT NULL,
    target_user_ids TEXT NULL,
    input_count INT NOT NULL,
    valid_target_count INT NOT NULL,
    duplicate_count INT NOT NULL,
    excluded_count INT NOT NULL,
    planned_issue_count INT NOT NULL,
    expected_issued_quantity INT NOT NULL,
    exceeds_remaining_quantity BIT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_coupon_approval_payload_request (approval_request_id),
    KEY idx_admin_coupon_approval_payload_event (coupon_event_id),
    CONSTRAINT fk_admin_coupon_approval_payload_request
        FOREIGN KEY (approval_request_id) REFERENCES admin_approval_requests (id)
);
