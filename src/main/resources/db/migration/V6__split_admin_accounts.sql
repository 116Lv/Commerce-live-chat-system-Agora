CREATE TABLE IF NOT EXISTS admins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admins_email (email)
);

INSERT INTO admins (email, password, nickname, role, status)
SELECT u.email,
       u.password,
       CASE u.role
           WHEN 'ROOT_ADMIN' THEN 'root-admin'
           WHEN 'USER_ADMIN' THEN 'user-admin'
           WHEN 'PRODUCT_ADMIN' THEN 'product-admin'
           WHEN 'SETTLEMENT_ADMIN' THEN 'settlement-admin'
       END,
       u.role,
       u.status
FROM users u
WHERE u.role IN ('ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM admins a
      WHERE LOWER(a.email) = LOWER(u.email)
  );

DELETE FROM refresh_tokens
WHERE user_id IN (
    SELECT id
    FROM users
    WHERE role IN ('ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN')
);

UPDATE users
SET role = 'ROLE_USER',
    status = 'DELETED',
    deleted_at = CURRENT_TIMESTAMP,
    email = CONCAT('migrated-admin-user-', id, '@agora.local'),
    phone = NULL,
    nickname = 'deleted-admin'
WHERE role IN ('ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN');
