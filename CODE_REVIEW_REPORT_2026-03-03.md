# wendang123 项目代码审查报告

- 审查时间: 2026-03-03
- 审查范围: `/opt/wendang123`（backend + frontend + rl-training）
- 代码基线: `main@4469f8c`
- 自动扫描脚本: `/tmp/wendang123_code_review_scan.sh`（按要求未放入项目目录）

## 审查方式
- 静态审查: 安全配置、鉴权链路、库存/库位一致性、RL 训练链路、前端部署配置
- 自动扫描: 鉴权放行、硬编码密钥、debug 输出、硬编码 API
- 构建验证:
  - `backend: mvn -q -DskipTests=false test` 通过
  - `frontend: npm run build` 通过（有大 chunk 警告）

## 发现的问题（按严重级别）

### P0

#### 1. 全局鉴权被放开，导致所有业务 API 可匿名访问
- 位置: `backend/src/main/java/com/wms/config/SecurityConfig.java:59`, `:61`, `:62`
- 证据:
  - `antMatchers("/system/**").permitAll()`
  - `anyRequest().permitAll()`
- 风险: 未登录即可访问/修改库存、出入库、用户、系统配置，属于完全越权。
- 建议: 默认 `anyRequest().authenticated()`，仅放行 `auth/login` 与必要公开接口。

#### 2. 系统管理接口缺乏服务端角色校验，可直接导致系统接管
- 位置:
  - `backend/src/main/java/com/wms/modules/system/controller/UserController.java:58`, `:92`, `:129`, `:148`
  - `backend/src/main/java/com/wms/modules/system/controller/SysConfigController.java:41`, `:50`, `:59`, `:68`
  - `backend/src/main/java/com/wms/modules/system/controller/SysLogController.java:44`, `:53`
- 风险:
  - 可匿名新增/删改管理员账号
  - 可匿名改策略配置、删除系统配置
  - 可匿名清理日志或伪造日志记录
- 建议: 为上述接口加 `@PreAuthorize`（如 `hasRole(admin)`）并配合统一权限矩阵。

#### 3. 默认凭据与密钥硬编码，且启动时自动创建弱口令管理员
- 位置:
  - `backend/src/main/resources/application.yml:9`, `:10`, `:53`
  - `backend/src/main/java/com/wms/config/ApplicationStartupListener.java:29`, `:43`
  - `backend/src/main/resources/application.yml:15`（每次启动执行 SQL）
- 风险:
  - 数据库 root/root、JWT 密钥泄露风险高
  - 默认 `admin/admin123`、`warehouse/admin123` 可被撞库
  - 生产环境误启动即暴露弱口令账户
- 建议: 全部改为环境变量/密钥服务；禁用生产自动建号逻辑。

### P1

#### 4. 出库审批忽略扣减库存失败，仍将单据置为“已批准”
- 位置: `backend/src/main/java/com/wms/modules/outbound/service/impl/OutboundOrderServiceImpl.java:174`, `:182`
- 证据: `reduceStock` 返回值仅打印不校验，随后强制 `order.setStatus(1)`。
- 风险: 单据状态与实际库存不一致，可能出现“审批成功但库存未扣减”。
- 建议: 任一明细扣减失败则抛异常并回滚事务，禁止状态流转。

#### 5. 库存 Controller 直接 CRUD，绕过库存服务规则与库位载荷同步
- 位置: `backend/src/main/java/com/wms/modules/inventory/controller/InventoryController.java:50`, `:74`, `:89`
- 风险:
  - 直接 `save/updateById/removeById` 不会触发 `addStock/reduceStock` 的库位载荷维护
  - 造成 `inventory.quantity` 与 `warehouse_location.current_load` 漂移
- 建议: 禁止直改库存主表，只保留业务指令型接口（入库/出库/盘点）。

#### 6. `inventory.material_id` 无唯一约束，但业务按“唯一一条”读取
- 位置:
  - `backend/src/main/resources/schema.sql:150-167`
  - `backend/src/main/java/com/wms/modules/inventory/service/impl/InventoryServiceImpl.java:61`
- 风险: 多条记录时 `getOne` 会抛异常或行为不确定，核心库存流程可能中断。
- 建议: 为 `inventory(material_id)` 建唯一索引并先做数据去重迁移。

#### 7. CORS 配置允许任意来源并携带凭证
- 位置: `backend/src/main/java/com/wms/config/CorsConfig.java:18`, `:28`
- 风险: 与当前鉴权缺陷叠加时，第三方站点可直接跨域调用敏感接口。
- 建议: 严格白名单来源，生产禁用 `*` + credentials 组合。

### P2

#### 8. 前端 API 地址硬编码为 localhost，部署依赖产物替换
- 位置: `frontend/src/utils/request.js:7`
- 风险: 环境切换容易出错，构建产物与源码行为不一致。
- 建议: 使用 `VITE_API_BASE_URL` 环境变量。

#### 9. 大量 `printStackTrace/System.out.println` 泄露内部异常细节
- 位置示例:
  - `backend/src/main/java/com/wms/modules/material/controller/MaterialController.java:27`
  - `backend/src/main/java/com/wms/modules/outbound/service/impl/OutboundOrderServiceImpl.java:170`
- 风险: 生产日志噪音与敏感信息暴露。
- 建议: 统一使用结构化日志（`log.warn/error`）并控制返回给前端的错误信息。

#### 10. 入库报表存在 N+1 查询，数据量上来后性能会明显下降
- 位置: `backend/src/main/java/com/wms/modules/report/service/impl/ReportServiceImpl.java:79`, `:91`
- 风险: 每条入库记录额外查一次物料，报表接口线性退化。
- 建议: 用联表/批量查询替代循环内 `selectById`。

#### 11. RL 训练环境状态-动作建模不一致，模型学习目标存在结构性偏差
- 位置:
  - `rl-training/env/warehouse_env.py:46`, `:67`
  - `rl-training/train_dqn.py:51-59`
- 证据:
  - 观测状态固定编码 `locations[0]`
  - 动作却选择任意 `location[action]`
- 风险: 训练到的 Q 值无法正确反映“候选库位选择”任务，线上效果不可控。
- 建议: 改成“候选集状态 + 动作索引一致”的 MDP；或改为 candidate-scoring 单步监督/RL 形式。

#### 12. `saveEventAsync` 名称与实现不一致（实为同步落库）
- 位置: `backend/src/main/java/com/wms/modules/location/service/impl/LocationPolicyEventServiceImpl.java:21-26`
- 风险: 高并发下增加请求链路时延，与“旁路采集”目标不一致。
- 建议: 使用异步执行器/消息队列，至少与主交易解耦。

## 总结
当前版本的首要问题是“鉴权与默认凭据”组合导致的系统级安全风险（P0），应优先修复；其次是“出库审批与库存一致性”问题（P1），直接影响业务数据正确性。性能与可维护性问题（P2）可在完成 P0/P1 后分批处理。

## 建议修复顺序
1. 先修 P0（安全封口）
2. 再修 P1（库存/单据一致性）
3. 最后处理 P2（性能、部署与训练链路质量）
