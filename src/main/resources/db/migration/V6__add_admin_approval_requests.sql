CREATE TABLE IF NOT EXISTS admin_approval_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operation VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requester_id BIGINT NOT NULL,
    target_admin_id BIGINT NOT NULL,
    requested_role VARCHAR(30) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    approver_id BIGINT NULL,
    decision_memo VARCHAR(500) NULL,
    decided_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_admin_approval_requests_status_created (status, created_at),
    KEY idx_admin_approval_requests_requester_created (requester_id, created_at),
    CONSTRAINT fk_admin_approval_requests_requester FOREIGN KEY (requester_id) REFERENCES admins (id),
    CONSTRAINT fk_admin_approval_requests_target_admin FOREIGN KEY (target_admin_id) REFERENCES admins (id),
    CONSTRAINT fk_admin_approval_requests_approver FOREIGN KEY (approver_id) REFERENCES admins (id)
);
