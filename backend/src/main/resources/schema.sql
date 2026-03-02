-- 数据库表结构定义脚本
-- 此脚本在应用启动时执行，用于创建表结构

-- Create user table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'User ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'Password',
    name VARCHAR(50) COMMENT 'Real Name',
    role VARCHAR(20) NOT NULL COMMENT 'Role',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System User Table';

-- Create role table
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Role ID',
    name VARCHAR(50) NOT NULL COMMENT 'Role Name',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT 'Role Code',
    description VARCHAR(200) COMMENT 'Role Description',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Role Table';

-- Create permission table
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Permission ID',
    name VARCHAR(50) NOT NULL COMMENT 'Permission Name',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT 'Permission Code',
    type VARCHAR(10) NOT NULL COMMENT 'Permission Type: menu-menu, button-button',
    url VARCHAR(200) COMMENT 'Permission URL',
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent Permission ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Permission Table';

-- Create role permission relation table
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    permission_id BIGINT NOT NULL COMMENT 'Permission ID',
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role Permission Relation Table';

-- Create material category table
CREATE TABLE IF NOT EXISTS material_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Category ID',
    name VARCHAR(50) NOT NULL COMMENT 'Category Name',
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent Category ID',
    level INT DEFAULT 1 COMMENT 'Category Level',
    sort INT DEFAULT 0 COMMENT 'Sort Order',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    remark VARCHAR(200) COMMENT 'Remark',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Material Category Table';

-- Create supplier table
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Supplier ID',
    name VARCHAR(100) NOT NULL COMMENT 'Supplier Name',
    contact VARCHAR(50) COMMENT 'Contact Person',
    phone VARCHAR(20) COMMENT 'Contact Phone',
    address VARCHAR(200) COMMENT 'Address',
    email VARCHAR(100) COMMENT 'Email',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    remark VARCHAR(200) COMMENT 'Remark',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Supplier Table';

-- Create material table
CREATE TABLE IF NOT EXISTS material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Material ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Material Code',
    name VARCHAR(100) NOT NULL COMMENT 'Material Name',
    category_id BIGINT COMMENT 'Category ID',
    spec VARCHAR(100) COMMENT 'Material Color',
    unit VARCHAR(20) COMMENT 'Unit',
    supplier_id BIGINT COMMENT 'Supplier ID',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Unit Price',
    min_stock INT DEFAULT 0 COMMENT 'Minimum Stock',
    max_stock INT DEFAULT 0 COMMENT 'Maximum Stock',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    remark VARCHAR(500) COMMENT 'Remark',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    create_user_id BIGINT COMMENT 'Create User ID',
    update_user_id BIGINT COMMENT 'Update User ID',
    FOREIGN KEY (category_id) REFERENCES material_category(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Material Table';

-- Create storage table
CREATE TABLE IF NOT EXISTS storage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Storage ID',
    material_id BIGINT COMMENT 'Material ID',
    material_code VARCHAR(50) COMMENT 'Material Code',
    material_name VARCHAR(100) COMMENT 'Material Name',
    quantity INT NOT NULL COMMENT 'Storage Quantity',
    batch_number VARCHAR(50) COMMENT 'Batch Number',
    storage_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Storage Time',
    operator VARCHAR(50) COMMENT 'Operator',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Storage Table';

-- Create outbound order table
CREATE TABLE IF NOT EXISTS outbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Outbound Order ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT 'Outbound Order No',
    type VARCHAR(20) NOT NULL COMMENT 'Outbound Type: production-production, sale-sale',
    recipient_id BIGINT COMMENT 'Recipient ID',
    recipient_name VARCHAR(50) COMMENT 'Recipient Name',
    total_quantity INT DEFAULT 0 COMMENT 'Total Quantity',
    status INT DEFAULT 0 COMMENT 'Status: 0-pending, 1-approved, 2-outbound, 3-cancelled',
    remark VARCHAR(500) COMMENT 'Remark',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    create_user_id BIGINT COMMENT 'Create User ID',
    create_user_name VARCHAR(50) COMMENT 'Create User Name'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbound Order Table';

-- Create outbound detail table
CREATE TABLE IF NOT EXISTS outbound_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Outbound Detail ID',
    order_id BIGINT NOT NULL COMMENT 'Outbound Order ID',
    material_id BIGINT COMMENT 'Material ID',
    material_code VARCHAR(50) COMMENT 'Material Code',
    material_name VARCHAR(100) COMMENT 'Material Name',
    spec VARCHAR(100) COMMENT 'Material Color',
    unit VARCHAR(20) COMMENT 'Unit',
    quantity INT NOT NULL COMMENT 'Outbound Quantity',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Unit Price',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'Amount',
    batch_number VARCHAR(50) COMMENT 'Batch Number',
    remark VARCHAR(200) COMMENT 'Remark',
    status INT DEFAULT 0 COMMENT 'Status: 0-pending, 1-outbound',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    FOREIGN KEY (order_id) REFERENCES outbound_order(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbound Detail Table';

-- Create inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Inventory ID',
    material_id BIGINT COMMENT 'Material ID',
    material_code VARCHAR(50) COMMENT 'Material Code',
    material_name VARCHAR(100) COMMENT 'Material Name',
    spec VARCHAR(100) COMMENT 'Material Color',
    unit VARCHAR(20) COMMENT 'Unit',
    quantity INT DEFAULT 0 COMMENT 'Inventory Quantity',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Unit Price',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'Inventory Amount',
    location VARCHAR(100) COMMENT 'Location',
    min_stock INT DEFAULT 0 COMMENT 'Minimum Stock',
    max_stock INT DEFAULT 0 COMMENT 'Maximum Stock',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory Table';

-- Create inventory record table
CREATE TABLE IF NOT EXISTS inventory_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Inventory Record ID',
    inventory_id BIGINT COMMENT 'Inventory ID',
    material_id BIGINT COMMENT 'Material ID',
    material_code VARCHAR(50) COMMENT 'Material Code',
    material_name VARCHAR(100) COMMENT 'Material Name',
    type VARCHAR(20) COMMENT 'Record Type: inbound-inbound, outbound-outbound',
    quantity INT NOT NULL COMMENT 'Quantity',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Unit Price',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'Amount',
    batch_number VARCHAR(50) COMMENT 'Batch Number',
    operator VARCHAR(50) COMMENT 'Operator',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE SET NULL,
    FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory Record Table';

-- Create inventory alert table
CREATE TABLE IF NOT EXISTS inventory_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Inventory Alert ID',
    inventory_id BIGINT COMMENT 'Inventory ID',
    material_id BIGINT COMMENT 'Material ID',
    material_code VARCHAR(50) COMMENT 'Material Code',
    material_name VARCHAR(100) COMMENT 'Material Name',
    alert_type VARCHAR(20) COMMENT 'Alert Type: low-low stock, high-high stock',
    alert_message VARCHAR(200) COMMENT 'Alert Message',
    current_stock INT COMMENT 'Current Stock',
    threshold INT COMMENT 'Threshold',
    status INT DEFAULT 0 COMMENT 'Status: 0-unprocessed, 1-processed',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE SET NULL,
    FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory Alert Table';

-- Create system config table
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Config ID',
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT 'Config Key',
    config_value VARCHAR(255) COMMENT 'Config Value',
    config_name VARCHAR(100) COMMENT 'Config Name',
    description VARCHAR(200) COMMENT 'Description',
    status INT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    create_by VARCHAR(50) COMMENT 'Create By',
    update_by VARCHAR(50) COMMENT 'Update By'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Config Table';

-- Create system log table
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Log ID',
    username VARCHAR(50) COMMENT 'Username',
    operation VARCHAR(50) COMMENT 'Operation',
    description VARCHAR(200) COMMENT 'Description',
    request_url VARCHAR(255) COMMENT 'Request URL',
    request_method VARCHAR(20) COMMENT 'Request Method',
    request_params VARCHAR(500) COMMENT 'Request Params',
    response_result VARCHAR(500) COMMENT 'Response Result',
    status INT DEFAULT 1 COMMENT 'Status: 0-failed, 1-success',
    ip VARCHAR(50) COMMENT 'IP Address',
    operate_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Operate Time',
    execute_time BIGINT DEFAULT 0 COMMENT 'Execute Time (ms)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Log Table';
