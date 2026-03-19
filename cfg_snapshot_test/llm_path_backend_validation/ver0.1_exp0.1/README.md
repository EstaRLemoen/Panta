# ver0.1_exp0.1 验证说明

## 定位

本目录用于归档 `llm_cfg_analyzer` 路径后端原型 `ver0.1` 的首次对照实验 `exp0.1`。

该实验属于：

- `cfg_snapshot_test/` 验证层
- `CFG_REPLACEMENT_PLAN.md` 中“第二步：补齐 `LLMCFGAnalyzer` 的路径能力兼容性”相关验证
- 面向 `PromptBuilder` 路径链路的 `comex` / `llm` 后端对照实验

## 版本标识

- 代码版本标签：`ver0.1`
- 代码基线提交：`ca318ac`
- 实验标识：`exp0.1`

`ver0.1` 在这里指：

- 已支持 `PromptBuilder` 通过 `path_cfg_backend` 在 `comex` 与 `llm` 间切换
- 已接入最小可运行的 `LLMCFGAnalyzer`
- 默认后端仍可保持为 `comex`

## 实验目的

验证以下问题：

- `comex` 与 `llm` 路径后端提供的路径信息是否存在实质差异
- 这种差异是否会传导到 `PromptBuilder` 的路径选择
- 当前 `llm` 路径后端是否已经具备可运行价值

## 相关文件

- 运行索引：`cfg_snapshot_test/llm_path_backend_validation/ver0.1_exp0.1/run_index.md`
- 对照结论：`cfg_snapshot_test/llm_path_backend_validation/ver0.1_exp0.1/results.md`

## 备注

本目录当前只保存索引和结论说明，日志与快照仍保留在原位置：

- 日志：`cfg_snapshot_test/validation_runs/`
- 快照：`cfg_snapshot_test/intermediate/cfg/`
