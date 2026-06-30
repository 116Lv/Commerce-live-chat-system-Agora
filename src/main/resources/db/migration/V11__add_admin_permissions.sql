CREATE TABLE IF NOT EXISTS admin_permissions (
    admin_id BIGINT NOT NULL,
    permission VARCHAR(40) NOT NULL,
    PRIMARY KEY (admin_id, permission),
    CONSTRAINT fk_admin_permissions_admin
        FOREIGN KEY (admin_id) REFERENCES admins (id)
);
