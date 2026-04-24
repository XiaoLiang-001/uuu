-- 测试数据（在已执行 schema-mysql.sql 建表后执行）
-- 登录账号与明文密码见每行注释；password 列为 BCrypt 密文，与 Spring Security 一致。

USE ncp;

INSERT INTO sys_user (username, password, role, status, created_at) VALUES
-- demo_user / user123
('demo_user', '$2a$10$5HfVYnhaYDD3cRwnAJI1vOzUuOVfY4cxA7On92veMyT2CO0AZvyae', 'USER', 1, NOW()),
-- demo_admin / admin123
('demo_admin', '$2a$10$9x9T8Q49VOh3SBEAjwx8deILh46JsHnD/9zU8jhLIxV6lvsR3S5qC', 'ADMIN', 1, NOW()),
-- tester1 / test123
('tester1', '$2a$10$AQpD3DzYLYCaeI3EXq1GZOVCoPc2LdSpIjwl9/ivDMyZA9qYAuVXK', 'USER', 1, NOW());
