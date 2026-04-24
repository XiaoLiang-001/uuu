-- 在 MySQL 中手动执行（可先建库再建表）
-- 1) 创建数据库（若已存在可跳过）
CREATE DATABASE IF NOT EXISTS ncp
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ncp;

-- 2) 用户表（普通用户与管理员共用，role 区分）
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(64) NOT NULL COMMENT '登录名',
  password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 等密文）',
  role VARCHAR(32) NOT NULL COMMENT 'USER 或 ADMIN',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：0禁用 1启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';
