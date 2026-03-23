# ver0.2_exp0.2.2 验证说明

## 定位

本目录用于归档 `llm_cfg_analyzer` 路径后端原型 `ver0.2` 的第二次 smoke 验证实验 `exp0.2.2`。

该实验属于：

- `cfg_snapshot_test/` 验证层
- `CFG_REPLACEMENT_PLAN.md` 中 `WP3` 之前的行号映射与可打分路径准备验证
- 面向 `llm_line_mode`、原始/预处理双向映射、path anchor 保留能力的 smoke 验证

## 版本标识

- 代码版本标签：`ver0.2`
- 代码基线提交：`d4f6fd5`
- 实验标识：`exp0.2.2`

本次 `ver0.2` 的重点增量是：

- 引入 `llm_line_mode`
- 引入 llm 后端原始/预处理双向行号映射
- 为 skeleton 与 path anchors 增加双坐标基础
- 增强快照可观测性，以支撑后续 `WP4` 打分验证

## 实验目的

验证以下问题：

- `llm` 后端是否已经建立 `preprocessed <-> original` 双向映射
- `PromptBuilder` 与快照对外暴露的行号是否已统一回原始源码行号
- `path_cfg_backend` 与 `llm_line_mode` 是否已进入快照上下文
- 当前 `WP3` 准备阶段是否仍能稳定跑通 smoke run

## 相关文件

- 运行索引：`cfg_snapshot_test/llm_path_backend_validation/ver0.2_exp0.2.2/run_index.md`
- 结果摘要：`cfg_snapshot_test/llm_path_backend_validation/ver0.2_exp0.2.2/results.md`

## 备注

本目录当前只保存索引和结论说明，原始日志与快照仍保留在原位置：

- 日志：`cfg_snapshot_test/validation_runs/`
- 快照：`cfg_snapshot_test/intermediate/cfg/`
