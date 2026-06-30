-- Store trade creation timestamps for my-activity trade history ordering and responses.

ALTER TABLE trades
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER price;
