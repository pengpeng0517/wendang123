-- 数据初始化脚本
-- 此脚本在应用启动时执行，用于初始化基础数据

-- 插入用户数据（如果不存在）
INSERT IGNORE INTO sys_user (id, username, password, name, role, status) VALUES
(1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System Administrator', 'admin', 1),
(2, 'warehouse', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Warehouse Administrator', 'warehouse', 1);

-- 插入分类数据（如果不存在）
INSERT IGNORE INTO material_category (id, name, parent_id, level, sort, status) VALUES
(1, '皮革', 0, 1, 1, 1),
(2, '布料', 0, 1, 2, 1),
(3, '胶料', 0, 1, 3, 1),
(4, '鞋带', 0, 1, 4, 1);

-- 插入供应商数据（如果不存在）
INSERT IGNORE INTO supplier (id, name, contact, phone, address, email, status) VALUES
(1, '皮革供应商A', '张三', '13800138001', '广东省广州市', 'supplier1@example.com', 1),
(2, '布料供应商B', '李四', '13800138002', '广东省深圳市', 'supplier2@example.com', 1),
(3, '胶料供应商C', '王五', '13800138003', '广东省东莞市', 'supplier3@example.com', 1);
