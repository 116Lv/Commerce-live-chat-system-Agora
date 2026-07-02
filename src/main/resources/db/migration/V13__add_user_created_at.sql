ALTER TABLE users
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER status;

CREATE INDEX idx_users_created_at ON users (created_at);
