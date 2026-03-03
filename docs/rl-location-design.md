# 强化学习动态库位系统设计（DQN + Java ONNX）

## 1. 目标与原则
- 在现有规则推荐稳定运行基础上，新增 RL 策略能力。
- 线上推理仅使用 Java ONNX Runtime，不在生产机训练。
- 发布策略固定为：`rule -> shadow -> manual -> auto`。
- 任意异常场景必须自动回退规则引擎。

## 2. MDP 定义
### 状态 S（首版）
- 入库请求：`material_id, inbound_qty, season_tag, temperature_level, preferred_location`
- 候选库位特征：`capacity, current_load, available_ratio, zone, priority, temp_match`
- 统计占位：`material_recent_in_cnt_7d, material_recent_out_cnt_7d, location_recent_pick_cnt_7d`（首版日志字段预留）

### 动作 A
- 在候选库位集合中选择一个 `location_code`。

### 候选筛选
- 仅 `status = 1` 的库位进入候选。
- 先按温层匹配筛选；若无匹配则降级使用全部启用库位。
- 优先使用 `available >= inbound_qty` 的候选；若全部不足，降级选择最大可用容量候选。

### 奖励 R
- `R = w1*capacity_balance + w2*pick_cost_negative + w3*temperature_match + w4*stability - w5*overflow_penalty`
- 各分量必须落库到 `reward_breakdown_json`。

## 3. 线上推理架构
- `RulePolicyEngine`：现有规则打分逻辑封装。
- `OnnxPolicyEngine`：加载 `model.onnx`，对候选逐个推理并排序。
- `LocationAllocationServiceImpl`：模式编排、置信门控、回退、事件记录。

## 4. 模式行为
- `rule`：仅规则策略。
- `shadow`：规则执行，RL旁路评估并记录对比。
- `manual`：优先返回 RL 建议，要求人工确认后入库提交。
- `auto`：RL自动执行，置信度低于阈值或动作非法自动回退规则。

## 5. 回退触发
- 模型未加载或加载失败。
- 推理异常。
- 动作不合法（容量不足、库位不存在、库位停用）。
- 置信度低于阈值（默认 `0.65`）。

## 6. 运行配置（sys_config）
- `location.policy.mode`
- `location.policy.modelVersion`
- `location.policy.modelPath`
- `location.policy.confidenceThreshold`
- `location.policy.shadowSampleRate`

## 7. 观测与审计
- 关键日志表：`location_policy_event`, `location_allocation_log`
- 每条策略决策都带 `trace_id`。
- 可按时间窗口统计：样本量、回退率、平均奖励、平均置信度。
