-- Track whether a nego offer has already used its single allowed extension request.

ALTER TABLE nego_offers
    ADD COLUMN extension_requested BIT NOT NULL DEFAULT 0 AFTER status;
