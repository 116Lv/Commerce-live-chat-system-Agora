ALTER TABLE admin_approval_requests
    ADD COLUMN pending_request_key VARCHAR(120) NULL;

UPDATE admin_approval_requests
SET pending_request_key = CONCAT(requester_id, ':', target_admin_id, ':', requested_role)
WHERE status = 'PENDING';

CREATE UNIQUE INDEX uk_admin_approval_pending_request_key
    ON admin_approval_requests (pending_request_key);
