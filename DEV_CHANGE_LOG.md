# 项目开发变更记录（DEV_CHANGE_LOG）

位置：/opt/wendang123/DEV_CHANGE_LOG.md
规则：后续所有改动都按时间追加记录，禁止覆盖历史。

## 记录规范
- 时间：YYYY-MM-DD HH:mm（服务器时区）
- 类型：feat / fix / refactor / docs / ops
- 范围：backend / frontend / database / deploy
- 必填：变更内容、影响范围、验证结果

---

## 2026-03-03 11:50
- 类型：feat
- 范围：backend frontend database deploy
- 主题：动态库位功能扩展（以扩展为主，少改主流程）

### 后端变更
- 新增 location 模块：
  - WarehouseLocation（库位主数据）
  - LocationAllocationLog（分配日志）
  - LocationAllocationService（动态推荐服务，规则评分版，接口可扩展到 RL）
  - LocationController（/api/location/*）
- 新增接口：
  - GET /api/location/list
  - POST /api/location/add
  - PUT /api/location/update
  - DELETE /api/location/delete/{id}
  - POST /api/location/recommend
  - POST /api/location/allocate
- 扩展入库流程：
  - storage 实体增加 location 字段
  - 入库保存时若未填写库位，自动调用推荐服务
- 扩展库存服务：
  - InventoryService 新增带库位参数重载方法：
    - addStock(materialId, quantity, operator, locationCode)
    - reduceStock(materialId, quantity, operator, locationCode)
  - 保留原方法，兼容旧调用

### 数据库变更
- storage 表新增字段：location VARCHAR(100)
- 新增表：
  - warehouse_location
  - location_allocation_log
- 初始化 4 条默认库位数据（A/B/C 区）

### 前端变更
- 新增 frontend/src/api/location.js
- 改造 Location.vue：
  - 库位主数据管理（增删改查）
  - 推荐测试面板（物料、数量、季节、温层）
- 改造 Storage.vue：
  - 新增“入库库位”字段
  - 新增“推荐库位”按钮并自动填充

### 部署与验证
- 后端构建命令：mvn -DskipTests -Dmaven.compiler.source=8 -Dmaven.compiler.target=8 clean package（成功）
- 前端构建命令：NODE_OPTIONS=--max-old-space-size=1024 npm run build（成功）
- 前端 API 地址处理：dist 产物中将 http://localhost:8080/api 替换为 /api
- 服务状态：
  - wendang123-backend.service 运行中
  - nginx 已 reload，监听 18082
- 接口联调：
  - 登录成功
  - GET /api/location/list 成功
  - POST /api/location/recommend 成功

### 访问地址
- http://8.134.206.52:18082/

## 2026-03-03 12:08
- 类型：fix
- 范围：backend docs
- 主题：材料颜色（spec）在入库管理/库存管理不同步问题排查与修复

### 根因结论（项目还是数据库）
- 结论：主要是项目逻辑导致，不是数据库结构本身导致。
- 证据：
  - 代码中 MaterialServiceImpl.updateMaterial 仅更新 material 表，未同步 inventory 镜像字段（含 spec 颜色）。
  - 复现实验：新增物料 spec 为空 -> 更新 material.spec 为“红色/蓝色”后，修复前 inventory.spec 仍为空；修复后 inventory.spec 能同步更新。
  - 数据库侧核查：material/inventory 无触发器（SHOW TRIGGERS 结果为空），未发现数据库自动改写造成不同步。

### 最小改动修复
- 修改 MaterialServiceImpl.updateMaterial：
  - 在更新 material 后，同步更新对应 inventory 的 materialCode/materialName/spec/unit/price（并重算 amount）。
- 修改 InventoryServiceImpl：
  - 增加 syncMaterialSnapshot 私有方法。
  - 在 listInventory 查询时，对历史数据做轻量自修复（仅在快照字段不一致时更新）。
  - 在 addStock 处理已有库存分支前调用同步方法，防止入库后继续显示旧颜色。

### 验证结果
- 后端编译通过：
  - mvn -DskipTests -Dmaven.compiler.source=8 -Dmaven.compiler.target=8 clean package
- 服务重启后接口复测通过：
  - 修复前：更新 material.spec 后 inventory.spec 不变
  - 修复后：更新 material.spec 后 inventory.spec 同步为新颜色

### 审计（audit）排查办法
1. 代码审计：检查 material 更新链路是否同步更新 inventory 镜像字段（spec/unit/name/code/price）。
2. 数据库审计：
   - 检查 material 与 inventory 表字段定义是否存在 spec。
   - 执行 SHOW TRIGGERS LIKE 'material' / 'inventory'，排除库内触发器影响。
3. 接口复现实验（无生产库也可）：
   - 新增 spec 为空的物料。
   - 查询 inventory，确认 spec 为空。
   - 更新 material.spec 为颜色值。
   - 再查 inventory.spec：若仍为空为项目问题；若同步则链路正常。
4. 存量数据核查 SQL（建议在生产只读执行）：
   - SELECT i.id, i.material_id, i.spec AS inventory_spec, m.spec AS material_spec
     FROM inventory i JOIN material m ON i.material_id = m.id
     WHERE COALESCE(i.spec, '') <> COALESCE(m.spec, '');
   - 若存在结果，说明历史已发生镜像不同步，需要执行同步修复策略。

## 2026-03-03 12:45
- 类型：feat
- 范围：backend frontend database docs deploy
- 主题：强化学习动态库位系统第一阶段实现（DQN + Java ONNX，支持影子/人工/自动灰度路径）

### 后端实现
- 新增策略模式与类型枚举：rule/shadow/manual/auto 与 rule/rl。
- 新增策略决策与管理DTO：
  - PolicyDecision
  - LocationPolicyStatusResponse
  - LocationPolicyModeRequest
  - LocationPolicyReloadRequest
  - LocationPolicyEvaluationResponse
- 新增策略引擎与编码组件：
  - RulePolicyEngine（封装原规则打分）
  - OnnxPolicyEngine（Java ONNX Runtime 推理与模型热加载）
  - FeatureEncoder（业务特征向量编码）
  - RewardCalculator（奖励分解与总分计算）
- 重构 LocationAllocationServiceImpl：
  - 支持 rule/shadow/manual/auto 四模式
  - 置信度门控与动作合法性校验
  - 自动回退规则策略
  - 生成 trace_id
  - 记录策略事件与评估数据
- 扩展 LocationAllocationService 接口，新增：
  - 获取策略状态
  - 热加载模型
  - 修改策略模式
  - 策略评估查询
  - 完成事件执行回填
- 新增策略事件持久化：
  - 实体 LocationPolicyEvent
  - Mapper LocationPolicyEventMapper
  - 服务 LocationPolicyEventService / Impl

### 控制器与API
- LocationController 新增接口：
  - GET /api/location/policy/status
  - POST /api/location/policy/reload
  - PUT /api/location/policy/mode
  - GET /api/location/policy/evaluation
- 原推荐接口响应新增字段：
  - policyMode
  - policyType
  - policyVersion
  - confidence
  - fallbackReason
  - traceId

### 入库链路改造
- Storage 实体新增非持久化字段 policyTraceId，用于前后端传递策略追踪。
- StorageServiceImpl 支持 manual 模式约束：
  - manual 模式下禁止空库位直接入库
  - 需先推荐并人工确认后提交
- 入库完成后可通过 trace_id 回填执行动作。

### 数据库变更
- 扩展 location_allocation_log 字段：
  - policy_type
  - policy_version
  - confidence
  - fallback_reason
  - latency_ms
  - trace_id
- 新增 location_policy_event 训练样本表。
- 新增 sys_config 默认策略配置：
  - location.policy.mode
  - location.policy.modelVersion
  - location.policy.modelPath
  - location.policy.confidenceThreshold
  - location.policy.shadowSampleRate
- 说明：由于 MySQL 对 ADD COLUMN IF NOT EXISTS 兼容行为差异，已在数据库中手工执行一次无损 ALTER 补齐字段。

### 前端改造
- frontend/src/api/location.js 新增策略管理API封装。
- Location.vue 新增策略控制台：
  - 模式切换
  - 模型版本/路径热加载
  - 模型健康状态展示
  - 近7天策略效果对比表
- Storage.vue 新增 manual 模式人工确认流程：
  - 提交前弹窗确认 RL 推荐
  - 传递 policyTraceId 到后端

### 训练工程与设计文档
- 新增 docs 文档：
  - docs/rl-location-design.md
  - docs/rl-feature-schema.json
  - docs/rl-reward-spec.md
- 新增离线训练骨架目录 rl-training：
  - env/warehouse_env.py
  - train_dqn.py
  - evaluate.py
  - export_onnx.py
  - feature_schema.json
  - requirements.txt
  - README.md

### 验证结果
- 后端编译通过：
  - mvn -DskipTests -Dmaven.compiler.source=8 -Dmaven.compiler.target=8 clean package
- 前端编译通过：
  - npm run build
- 服务重启通过：
  - wendang123-backend.service active
  - nginx reload 成功
- 接口联调通过：
  - GET /api/location/policy/status
  - POST /api/location/recommend
  - GET /api/location/policy/evaluation
  - PUT /api/location/policy/mode
- 策略事件表已可写入并可统计 rule 指标。

## 2026-03-03 12:46
- 类型：fix
- 范围：backend database
- 主题：策略事件落库稳定性修复

### 修复内容
- 修复 location_policy_event 外键约束导致推荐接口报错的问题：
  - 当请求 material_id 在 material 表不存在时，事件写入改为 material_id 置空，仍保留 trace_id 和策略结果。
- 将事件写入从异步线程改为同步保存，确保每次推荐请求都可稳定落样本。
- 手工补齐 location_allocation_log 扩展字段（兼容 MySQL DDL 行为差异）。

### 验证结果
- POST /api/location/recommend 返回 200。
- location_policy_event 可持续新增记录。
- GET /api/location/policy/evaluation 可返回 rule 统计项。

## 2026-03-03 17:32
- 类型：test-review
- 范围：backend frontend api database
- 主题：完整回归测试与缺陷复现（脚本在项目外）

### 执行内容
- 新增并执行项目外测试脚本：
  - /tmp/wendang123_full_test.py
  - /tmp/wendang123_full_test_v2.py
- 执行构建验证：
  - backend: mvn -DskipTests=false test -q
  - frontend: npm run build --silent
- 执行数据库核验（wms）：
  - sys_user, inventory, outbound_order, sys_log, sys_config

### 产出文件
- /opt/wendang123/TEST_REPORT_2026-03-03.md
- /opt/wendang123/TEST_RESULT_2026-03-03_v1.json
- /opt/wendang123/TEST_RESULT_2026-03-03_v2.json

### 复现到的关键问题
- 未授权读取系统配置成功。
- 未授权新增用户成功。
- 未授权修改策略模式成功。
- 同一 material_id 产生多条 inventory 记录。
- 重复库存场景下入库接口报 selectOne found 3。
- 库存不足情况下出库审批仍置为已批准。
- 未授权伪造系统日志成功。

### 说明
- 本次仅新增测试报告与结果文件，未改动业务源码。
