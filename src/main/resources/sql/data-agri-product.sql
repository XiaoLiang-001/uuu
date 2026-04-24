-- 测试数据（在 schema-agri-product.sql 建表后执行）
-- 图片列为 NULL：仅演示商品信息；上传图片后会把文件名写入 images 字段

USE ncp;

INSERT INTO agri_product (name, description, price, stock, status, images, created_by) VALUES
('有机西红柿', '日光温室种植，酸甜适口，适合鲜食与烹饪。', 6.80, 200, 'ON_SHELF', NULL, NULL),
('糯玉米', '现摘糯玉米，颗粒饱满，蒸煮皆宜。', 4.50, 150, 'ON_SHELF', NULL, NULL),
('高山绿茶', '明前采摘，清香回甘。', 88.00, 80, 'ON_SHELF', NULL, NULL),
('土鸡蛋 30 枚装', '散养土鸡，蛋壳厚实，蛋黄橙红。', 32.00, 60, 'ON_SHELF', NULL, NULL),
('农家红薯', '沙地红薯，口感绵甜。', 3.20, 300, 'OFF_SHELF', NULL, NULL);
