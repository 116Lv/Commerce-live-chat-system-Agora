-- Add transient payment confirmation timestamp for retry recovery.

ALTER TABLE payments
    ADD COLUMN confirming_at DATETIME(6) NULL AFTER refunded_at;
