# RL 奖励函数规范（v1）

## 1. 总公式
`R = 0.35*capacity_balance + 0.25*pick_cost_negative + 0.20*temperature_match + 0.10*stability - 0.10*overflow_penalty`

## 2. 分量定义
- `capacity_balance`: 入库后负载率接近目标值（默认 0.65）时奖励更高。
- `pick_cost_negative`: 分区越靠近拣货热点（A > B > C）值越高。
- `temperature_match`: 温层匹配为 1，不匹配为 0。
- `stability`: 与偏好库位一致为 1，否则 0。
- `overflow_penalty`: 容量不足时按不足比例惩罚。

## 3. 日志要求
每次推荐必须落库：
- `reward_total`
- `reward_breakdown_json`
- `trace_id`
- `policy_mode`
- `policy_type`
- `policy_version`

## 4. 版本管理
- 奖励参数变更必须提升版本号。
- 训练产物 `metadata.json` 记录 `reward_spec_version`。
