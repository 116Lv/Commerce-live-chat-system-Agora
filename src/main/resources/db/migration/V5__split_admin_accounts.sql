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
SELECT u.email, u.password, u.nickname, u.role, u.status
FROM users u
WHERE u.role IN ('ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM admins a
      WHERE LOWER(a.email) = LOWER(u.email)
  );

UPDATE users
SET role = 'ROLE_USER'
WHERE role IN ('ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN');
