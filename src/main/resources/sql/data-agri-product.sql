-- 测试数据（在 schema-agri-product.sql 建表后执行）
-- 图片列为 NULL：仅演示商品信息；上传图片后会把文件名写入 images 字段

USE ncp;

INSERT INTO agri_product (name, description, price, stock, status, images, created_by) VALUES
('有机西红柿', '日光温室种植，酸甜适口，适合鲜食与烹饪。', 6.80, 200, 'ON_SHELF', NULL, NULL),
('糯玉米', '现摘糯玉米，颗粒饱满，蒸煮皆宜。', 4.50, 150, 'ON_SHELF', NULL, NULL),
('高山绿茶', '明前采摘，清香回甘。', 88.00, 80, 'ON_SHELF', NULL, NULL),
('土鸡蛋 30 枚装', '散养土鸡，蛋壳厚实，蛋黄橙红。', 32.00, 60, 'ON_SHELF', NULL, NULL),
('农家红薯', '沙地红薯，口感绵甜。', 3.20, 300, 'OFF_SHELF', NULL, NULL);

INSERT IGNORE INTO ncp_category(name) VALUES
('蔬菜'),
('水果'),
('粮油'),
('茶饮'),
('蛋奶'),
('杂粮');

-- 2) 按商品 id 绑定省份（可重复执行，已存在则更新）
INSERT INTO agri_product_province (product_id, province_id) VALUES
(4,  (SELECT id FROM ncp_province WHERE name='云南' LIMIT 1)),
(5,  (SELECT id FROM ncp_province WHERE name='山东' LIMIT 1)),
(6,  (SELECT id FROM ncp_province WHERE name='河北' LIMIT 1)),
(7,  (SELECT id FROM ncp_province WHERE name='福建' LIMIT 1)),
(9,  (SELECT id FROM ncp_province WHERE name='山东' LIMIT 1)),
(12, (SELECT id FROM ncp_province WHERE name='河北' LIMIT 1)),
(13, (SELECT id FROM ncp_province WHERE name='云南' LIMIT 1)),
(14, (SELECT id FROM ncp_province WHERE name='福建' LIMIT 1)),
(15, (SELECT id FROM ncp_province WHERE name='山东' LIMIT 1)),
(16, (SELECT id FROM ncp_province WHERE name='河北' LIMIT 1)),
(17, (SELECT id FROM ncp_province WHERE name='云南' LIMIT 1)),
(18, (SELECT id FROM ncp_province WHERE name='福建' LIMIT 1)),
(19, (SELECT id FROM ncp_province WHERE name='山东' LIMIT 1)),
(20, (SELECT id FROM ncp_province WHERE name='河北' LIMIT 1)),
(21, (SELECT id FROM ncp_province WHERE name='云南' LIMIT 1)),
(22, (SELECT id FROM ncp_province WHERE name='福建' LIMIT 1))
ON DUPLICATE KEY UPDATE province_id = VALUES(province_id);

-- 3) 按商品 id 绑定分类（可重复执行，已存在则更新）
INSERT INTO agri_product_category (product_id, category_id) VALUES
(4,  (SELECT id FROM ncp_category WHERE name='蔬菜' LIMIT 1)),
(5,  (SELECT id FROM ncp_category WHERE name='蔬菜' LIMIT 1)),
(6,  (SELECT id FROM ncp_category WHERE name='粮油' LIMIT 1)),
(7,  (SELECT id FROM ncp_category WHERE name='茶饮' LIMIT 1)),
(9,  (SELECT id FROM ncp_category WHERE name='杂粮' LIMIT 1)),
(12, (SELECT id FROM ncp_category WHERE name='水果' LIMIT 1)),
(13, (SELECT id FROM ncp_category WHERE name='蔬菜' LIMIT 1)),
(14, (SELECT id FROM ncp_category WHERE name='茶饮' LIMIT 1)),
(15, (SELECT id FROM ncp_category WHERE name='杂粮' LIMIT 1)),
(16, (SELECT id FROM ncp_category WHERE name='水果' LIMIT 1)),
(17, (SELECT id FROM ncp_category WHERE name='蔬菜' LIMIT 1)),
(18, (SELECT id FROM ncp_category WHERE name='粮油' LIMIT 1)),
(19, (SELECT id FROM ncp_category WHERE name='蛋奶' LIMIT 1)),
(20, (SELECT id FROM ncp_category WHERE name='蔬菜' LIMIT 1)),
(21, (SELECT id FROM ncp_category WHERE name='蔬菜' LIMIT 1)),
(22, (SELECT id FROM ncp_category WHERE name='杂粮' LIMIT 1))
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id);