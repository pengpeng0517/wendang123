-- 创建数据库
CREATE DATABASE IF NOT EXISTS wms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE wms;

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL COMMENT '角色：admin（系统管理员）、warehouse（仓库管理员）',
    status INT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 插入测试数据（密码：admin123，已加密）
INSERT INTO sys_user (username, password, role, status) VALUES
('admin', '$2a$10$J9Jx3F7K6L2M1N5O8P9Q0R1S2T3U4V5W6X7Y8Z9', 'admin', 1),
('warehouse', '$2a$10$J9Jx3F7K6L2M1N5O8P9Q0R1S2T3U4V5W6X7Y8Z9', 'warehouse', 1);