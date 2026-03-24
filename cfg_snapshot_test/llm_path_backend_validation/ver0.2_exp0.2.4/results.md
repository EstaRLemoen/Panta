# ver0.2_exp0.2.4 结果摘要

## 一句话结论

在清理旧测试残留后，`WP3.5` 的 `segments + transitions` 路径链路能够真实参与 `PromptBuilder`，并在 3 次迭代 smoke 中把 coverage 提升到 `57.43% / 23.21%`；这说明新增接口已经接上，但第 2 轮后仍出现大量 runtime error 且 coverage 停滞，问题更偏向“生成内容质量/路径表达质量仍不稳定”，而不是“新增接口没有传到 `PromptBuilder`”。

## 本次验证确认的能力

- 新 path schema 已进入真实 prompt 构造链路
- `_llm_transitions` / `_llm_segments` 已稳定出现在多个 `prompt_builder_cfg` snapshot 中
- `_llm_precomputed_path_str` 已能表达：
  - path kind
  - 条件/效果
  - transitions
  - sequential path statements
- `PromptBuilder` 的方法选择日志显示，本轮确实在消费 llm path backend 的候选路径

## 快照侧关键证据

关键文件：

- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/003_prompt_builder_cfg/meta.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/003_prompt_builder_cfg/cfg_file_obj.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/004_prompt_builder_cfg/cfg_file_obj.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/005_prompt_builder_cfg/cfg_file_obj.json`

从这些快照中可观察到：

- `_llm_transitions` 与 `_llm_segments` 均存在
- `line_hints` 已比早期版本更密
- 后续轮次的 `_llm_precomputed_path_str` 中开始出现更完整的 path 语句串，而不再只是一两个 anchor

## smoke run 行为

- 初始 coverage：`0.0% / 0.0%`
- baseline 阶段先生成了一批 parse/null 相关测试，提升到：`3.96% / 0.0%`
- Iteration 1 后大幅提升到：`57.43% / 23.21%`
- Iteration 2 未继续提升，最终在 3 次迭代后收口

## 对“为什么第 3 轮没有继续涨”的分析

### 不是主因：接口没接上

本轮已经有足够证据说明接口是接上的：

- `PromptBuilder` snapshot 中能看到 `_llm_transitions` / `_llm_segments`
- `_llm_precomputed_path_str` 已包含新 schema 信息
- 日志中确实出现了基于 path 选择的方法：
  - `nextRecord`
  - `getRecords`
  - `close`

所以当前不能把“第三轮停滞”归因成“新增接口没有交给 `PromptBuilder`”。

### 更可能的主因：当前阶段产物质量仍不稳定

更可能的问题在于：

- `segments` 虽然更完整，但不少 path 仍把条件行本身塞进 `sequential` 段
- 长方法（尤其 `nextRecord()`）的 path 展开仍不稳定，沿途普通行覆盖不够完整
- prompt 文本虽然更丰富，但还没有让 test generation 稳定聚焦到真正有增量的剩余 missed paths
- 第 2 轮开始出现较多 runtime error，说明生成测试本身质量在后续轮次下降

### 一个次要问题：当前高层选路逻辑仍是旧逻辑

虽然新 metadata 已透传，但 `PromptBuilder` 目前还没有直接利用：

- `segments`
- `transitions`

去做新的 membership 判定。

也就是说：

- 新接口已经接上了
- 但当前高层 path ranking 仍主要吃旧的 missed-line 命中逻辑
- 因此新 schema 的增益现在更多是“为后续 `WP4` 准备数据”，而不是已经完全转化成筛选收益

## 对后续的启示

- 现在可以明确排除“新增接口没接进 `PromptBuilder`”这个主要嫌疑
- 下一步更该做的是：
  - 收紧 `segments` / `transitions` 的 prompt 与 validation
  - 减少把 jump-point 自身塞进 sequential 的情况
  - 提高长方法 path 展开的稳定性
  - 在 `WP4` 中真正消费 `segments` / `transitions` 做 membership 判断
