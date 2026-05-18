-- 农产品单表（图片文件名逗号分隔，存在 app.upload.dir/products/ 下）
-- 若曾建过 agri_product_image，请先 DROP 旧表后再执行本脚本

USE ncp;

-- 按外键依赖逆序删除：先删关联表，再删字典表，最后删商品主表
DROP TABLE IF EXISTS agri_product_image;

DROP TABLE IF EXISTS agri_product_province;

DROP TABLE IF EXISTS ncp_province;

DROP TABLE IF EXISTS agri_product_category;

DROP TABLE IF EXISTS ncp_category;

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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (id),
  KEY idx_agri_product_status (status),
  KEY idx_agri_product_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品';

CREATE TABLE ncp_province (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(32) NOT NULL COMMENT '省份名称',
  code VARCHAR(16) NULL COMMENT '省份编码（可空）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ncp_province_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='省份字典';

CREATE TABLE agri_product_province (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  product_id BIGINT NOT NULL COMMENT 'agri_product.id',
  province_id BIGINT NOT NULL COMMENT 'ncp_province.id',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_province (product_id),
  KEY idx_agri_product_province_province (province_id),
  CONSTRAINT fk_app_product FOREIGN KEY (product_id) REFERENCES agri_product(id) ON DELETE CASCADE,
  CONSTRAINT fk_app_province FOREIGN KEY (province_id) REFERENCES ncp_province(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品-省份关联（每个商品一个省份）';

CREATE TABLE ncp_category (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(32) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ncp_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类字典';

CREATE TABLE agri_product_category (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  product_id BIGINT NOT NULL COMMENT 'agri_product.id',
  category_id BIGINT NOT NULL COMMENT 'ncp_category.id',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_category (product_id),
  KEY idx_agri_product_category_category (category_id),
  CONSTRAINT fk_apc_product FOREIGN KEY (product_id) REFERENCES agri_product(id) ON DELETE CASCADE,
  CONSTRAINT fk_apc_category FOREIGN KEY (category_id) REFERENCES ncp_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品-分类关联（每个商品一个分类）';
