ALTER TABLE products
    ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';

UPDATE products
SET approval_status = 'REJECTED'
WHERE status IN ('HIDDEN', 'DELETED');
