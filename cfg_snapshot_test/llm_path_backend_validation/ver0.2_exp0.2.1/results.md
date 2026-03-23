# ver0.2_exp0.2.1 结果摘要

## 一句话结论

`ver0.2` 的 `WP1 + WP2` 已经把 `llm` 路径后端从“方法集合漂移且 baseline 易编译失败”的状态，推进到“方法集合稳定、重载可区分、结构化路径可输出、baseline smoke run 可稳定通过”的状态。

## WP1 的直接效果

- 方法集合稳定为 `2 classes / 13 methods`
- 内部类 `CSVRecordIterator` 已进入方法骨架与快照
- `parse(...)` 重载已按 `qualified_method_key` 区分，不再塌成单个方法名

关键证据：

- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260323_212946_f37558c0/003_prompt_builder_cfg/cfg_summary.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260323_212946_f37558c0/003_prompt_builder_cfg/cfg_testable_methods_statistics.json`

## WP2 的直接效果

- `complexity` 输出开始参考 deterministic hint，不再完全随 LLM 漂移
- path 输出已升级为结构化 schema：
  - `path_count`
  - `path_id`
  - `kind`
  - `summary`
  - `conditions`
  - `effects`
  - `line_hints`
- `nextRecord()`、`hasNext()`、`next()` 等复杂方法已能输出更像“主分支摘要”的路径列表

## smoke run 行为

- 主流程完整跑通
- baseline generation 一次返回 6 个测试，且 6 个测试全部通过
- 未开启 fixing
- 最终 coverage：`2.97% / 0.0%`

## 与 ver0.1 相比的显著改进

- 方法集合不再由 LLM 随机决定
- 内部类方法不再缺失
- baseline tests 不再大面积因裸 `null` 触发重载歧义而编译失败
- `testable_methods_statistics` 不再按纯方法名覆盖重载

## 当前仍存在的问题

- 这次 smoke run 仍停留在 baseline generation，不足以证明 control 路径选择收益
- `nextRecord() = 14`、`remove() = 2` 这类 complexity 仍可能偏激进
- `line_hints` 质量仍需进一步验证
- `miss line / branch -> path` 的归属与打分尚未进入 `WP3/WP4`

## 对后续的启示

- `ver0.2` 的下一步应继续围绕 `WP2` 收敛 complexity/path 数量的稳定性
- 随后进入 `WP3`：定义“什么样的 path 才真正可用于覆盖引导”
- 最后再进入 `WP4`：改进 miss-to-path 归属与打分
