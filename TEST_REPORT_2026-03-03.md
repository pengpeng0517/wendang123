# wendang123 完整测试报告

- 测试时间: 2026-03-03
- 测试范围: backend + frontend + 接口链路 + 关键业务流程
- 代码基线: main@4469f8c
- 测试环境: 8.134.206.52, nginx:18082, backend:8080

## 测试执行方式
- 自动化脚本（均在项目外）
  - `/tmp/wendang123_full_test.py`
  - `/tmp/wendang123_full_test_v2.py`
- 构建验证
  - `backend: mvn -DskipTests=false test -q`
  - `frontend: npm run build --silent`
- 数据证据
  - MySQL 表核验: `sys_user`, `inventory`, `outbound_order`, `sys_log`, `sys_config`

## 总体结果
- 自动化用例总数: 14
- 失败用例: 7
- 通过用例: 7
- 后端测试文件数: 0（`src/test` 为空）

## 关键失败项（可复现）

### 1. 未授权可读取系统配置
- 用例: `unauth_system_config_list_should_block`
- 请求: `GET /api/api/system/config/list`
- 实际: `200 + code=200`
- 期望: 未登录应阻断
- 风险: 泄露敏感配置

### 2. 未授权可新增系统用户
- 用例: `unauth_user_add_should_block`
- 请求: `POST /api/system/user/add`
- 实际: `200 + code=200`，新增了用户 `audit_u_1772515781`
- 期望: 未登录应阻断
- 风险: 直接创建管理账号，系统接管风险

### 3. 未授权可修改动态库位策略模式
- 用例: `unauth_policy_mode_update_should_block`
- 请求: `PUT /api/location/policy/mode`
- 实际: `rule -> manual` 修改成功
- 期望: 未登录应阻断
- 风险: 可远程改变策略执行行为

### 4. 同一 material_id 可插入多条 inventory
- 用例: `inventory_duplicate_row_should_not_exist`
- 请求: 连续两次 `POST /api/api/inventory/add`
- 实际: material_id=5 出现 3 条库存记录
- 期望: 每个 material_id 仅 1 条库存主记录
- 风险: 核心库存读写异常

### 5. 入库流程在重复库存场景下直接报错
- 用例: `storage_add_should_not_crash_when_inventory_has_duplicates`
- 请求: `POST /api/storage/add`
- 实际: `code=400`, 报错 `Expected one result (or null) to be returned by selectOne(), but found: 3`
- 期望: 稳定入库或有可控错误处理
- 风险: 线上入库流程中断

### 6. 库存不足仍可审批通过
- 用例: `approve_with_insufficient_stock_should_not_mark_approved`
- 请求: `PUT /api/api/outbound/order/approve/{id}`（出库数量 999, 库存仅 2）
- 实际: 订单状态变为 `1(已批准)`，库存数量仍 `2`
- 期望: 审批失败并保持未批准
- 风险: 单据状态与真实库存不一致

### 7. 未授权可伪造系统日志
- 用例: `unauth_log_record_should_block`
- 请求: `POST /api/api/system/log/record`
- 实际: `200 + code=200`，成功写入 forged 日志
- 期望: 未登录应阻断
- 风险: 审计日志可被污染

## 数据库证据摘录
- 未授权创建用户已落库
  - `sys_user`: id=4, username=`audit_u_1772515781`
- 重复库存已落库
  - `inventory`: material_id=5, `count(*)=3`
- 库存不足审批已落库
  - `outbound_order`: id=2, status=1, total_quantity=999, recipient_name=`approve-test`
  - `inventory(material_id=6).quantity=2`（审批后未扣减）
- 伪造日志已落库
  - `sys_log`: operation=`forged-op`, username=`forged-user`

## 构建与工程状态
- backend `mvn test` 退出码 0
- 但 `src/test` 无测试文件，缺乏自动化回归保护
- frontend build 通过，但存在大 chunk 警告（性能风险）

## 建议修复优先级
1. P0: 鉴权封口（系统配置、用户管理、策略控制、日志接口）
2. P0: 修复出库审批一致性（库存不足必须回滚，不得置已批准）
3. P1: 为 `inventory(material_id)` 增唯一约束并做数据清洗
4. P1: 禁止通过通用 inventory CRUD 直改库存主表，统一走业务服务
5. P2: 增加后端单元/集成测试，覆盖入库、出库审批、权限链路

## 原始测试结果文件
- `/opt/wendang123/TEST_RESULT_2026-03-03_v1.json`
- `/opt/wendang123/TEST_RESULT_2026-03-03_v2.json`
