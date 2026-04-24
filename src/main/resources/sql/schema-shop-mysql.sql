-- 购物车、订单、商品评论（在用户库 ncp 中执行）
USE ncp;

CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT 'sys_user.id',
  product_id BIGINT NOT NULL COMMENT 'agri_product.id',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cart_user_product (user_id, product_id),
  KEY idx_cart_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车项';

CREATE TABLE IF NOT EXISTS shop_order (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT 'sys_user.id',
  order_no VARCHAR(40) NOT NULL COMMENT '订单号',
  total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总额',
  status VARCHAR(16) NOT NULL DEFAULT 'ORDERED' COMMENT 'ORDERED 已下单',
  receiver_name VARCHAR(64) NOT NULL COMMENT '收货人',
  receiver_phone VARCHAR(32) NOT NULL COMMENT '联系电话',
  receiver_address VARCHAR(512) NOT NULL COMMENT '收货地址',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_shop_order_no (order_no),
  KEY idx_shop_order_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

CREATE TABLE IF NOT EXISTS order_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_id BIGINT NOT NULL COMMENT 'shop_order.id',
  product_id BIGINT NOT NULL COMMENT 'agri_product.id',
  product_name VARCHAR(128) NOT NULL COMMENT '下单时商品名快照',
  unit_price DECIMAL(10, 2) NOT NULL COMMENT '下单时单价',
  quantity INT NOT NULL COMMENT '数量',
  subtotal DECIMAL(12, 2) NOT NULL COMMENT '小计',
  PRIMARY KEY (id),
  KEY idx_order_item_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE IF NOT EXISTS product_review (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  product_id BIGINT NOT NULL COMMENT 'agri_product.id',
  user_id BIGINT NOT NULL COMMENT 'sys_user.id',
  username VARCHAR(64) NOT NULL COMMENT '评论时用户名快照',
  content VARCHAR(2000) NOT NULL COMMENT '评论内容',
  rating TINYINT NOT NULL DEFAULT 5 COMMENT '1-5 星',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_product_review_product (product_id),
  KEY idx_product_review_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论';
