-- 农产品单表（图片文件名逗号分隔，存在 app.upload.dir/products/ 下）
-- 若曾建过 agri_product_image，请先 DROP 旧表后再执行本脚本

USE ncp;

DROP TABLE IF EXISTS agri_product_image;

DROP TABLE IF EXISTS agri_product;

CREATE TABLE agri_product (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(128) NOT NULL COMMENT '商品名称',
  description TEXT NULL COMMENT '描述',
  price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  status VARCHAR(16) NOT NULL DEFAULT 'ON_SHELF' COMMENT 'ON_SHELF 上架 / OFF_SHELF 下架',
  images TEXT NULL COMMENT '图片存储文件名，多个用英文逗号分隔（无空格）',
  created_by BIGINT NULL COMMENT '创建人 sys_user.id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_agri_product_status (status),
  KEY idx_agri_product_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品';
