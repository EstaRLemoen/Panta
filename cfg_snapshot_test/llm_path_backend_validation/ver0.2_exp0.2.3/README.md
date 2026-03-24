# ver0.2_exp0.2.3 验证说明

## 定位

本目录用于归档 `ver0.2` 阶段 `WP3.5` 第一轮 smoke 验证实验 `exp0.2.3`。

该实验属于：

- `cfg_snapshot_test/` 验证层
- `CFG_REPLACEMENT_PLAN.md` 中 `WP3.5` 的首次真实运行验证
- 面向 `segments + transitions` path schema 的第一次主流程 smoke

## 版本标识

- 代码版本标签：`ver0.2`
- 实验标识：`exp0.2.3`

本次 `ver0.2` 的重点增量是：

- 将 path schema 从 anchor/hint 型扩展为 `transitions + segments`
- 让 `line_hints` 吸收更密的 path 行集合
- 让 `PromptBuilder` 透传 `_llm_transitions` / `_llm_segments`

## 实验目的

验证以下问题：

- `segments + transitions` 是否已进入主流程 smoke
- `PromptBuilder` 与 snapshot 是否已能看到新增 path metadata
- 在 `maximum_iterations = 3` 下，链路是否仍能跑通
- 增长后的 LLM path 输出是否会引入新的 JSON 截断或运行时问题

## 相关文件

- 运行索引：`cfg_snapshot_test/llm_path_backend_validation/ver0.2_exp0.2.3/run_index.md`
- 结果摘要：`cfg_snapshot_test/llm_path_backend_validation/ver0.2_exp0.2.3/results.md`

## 备注

本目录只保存实验说明与结论，原始日志与快照仍保留在原位置：

- 日志：`cfg_snapshot_test/validation_runs/`
- 快照：`cfg_snapshot_test/intermediate/cfg/`
