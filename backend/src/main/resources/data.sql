-- 角色数据
INSERT IGNORE INTO roles (id, role_name, description, status, created_at, updated_at) VALUES
(1, 'ADMIN', '系统管理员', 1, NOW(), NOW()),
(2, 'SUPERVISOR', '系统总监', 1, NOW(), NOW()),
(3, 'MAJOR_SHAREHOLDER', '系统大股东', 1, NOW(), NOW());

-- 管理员用户数据
-- 默认密码: admin123 (BCrypt加密后的值)
INSERT IGNORE INTO users (id, username, nickname, email, password, role_id, status, deleted, created_at, updated_at) VALUES
(1, 'admin', '系统管理员', 'admin@fivebear.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKV4o2eZKtbkyy/QGCnwKfZhp2xG', 1, 1, false, NOW(), NOW());

-- 如果用户已存在，更新其角色和状态
UPDATE users SET role_id = 1, status = 1, deleted = false WHERE username = 'admin'; 