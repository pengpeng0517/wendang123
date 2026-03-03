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

-- 插入库位数据（如果不存在）
INSERT IGNORE INTO warehouse_location (id, code, zone, capacity, current_load, status, priority, temperature_level, remark) VALUES
(1, 'A-01-01', 'A区', 1000, 120, 1, 9, '常温', '高频物料近拣货口'),
(2, 'A-01-02', 'A区', 1000, 260, 1, 8, '常温', '高频补位'),
(3, 'B-02-01', 'B区', 800, 90, 1, 7, '常温', '标准存储位'),
(4, 'C-03-01', 'C区', 1500, 300, 1, 6, '低温', '温控存储位');

-- 插入动态库位策略配置（如果不存在）
INSERT IGNORE INTO sys_config (config_key, config_value, config_name, description, status, create_by, update_by) VALUES
('location.policy.mode', 'rule', '库位策略模式', 'rule|shadow|manual|auto', 1, 'system', 'system'),
('location.policy.modelVersion', 'none', '库位RL模型版本', '当前启用模型版本', 1, 'system', 'system'),
('location.policy.modelPath', '/opt/wendang123/models/location-rl/current/model.onnx', '库位RL模型路径', 'ONNX模型路径', 1, 'system', 'system'),
('location.policy.confidenceThreshold', '0.65', '库位RL置信阈值', '自动模式最低置信度', 1, 'system', 'system'),
('location.policy.shadowSampleRate', '1.0', '库位影子采样率', 'shadow模式采样比率', 1, 'system', 'system');
