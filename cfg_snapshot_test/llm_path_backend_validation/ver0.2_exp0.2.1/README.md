# ver0.2_exp0.2.1 验证说明

## 定位

本目录用于归档 `llm_cfg_analyzer` 路径后端原型 `ver0.2` 的首次 smoke 验证实验 `exp0.2.1`。

该实验属于：

- `cfg_snapshot_test/` 验证层
- `CFG_REPLACEMENT_PLAN.md` 中 `WP1` 与 `WP2` 的阶段性验证
- 面向 `PromptBuilder` 路径链路的 `llm` 后端 smoke run

## 版本标识

- 代码版本标签：`ver0.2`
- 代码基线提交：`260de5b`
- 实验标识：`exp0.2.1`

`ver0.2` 在这里指：

- 已引入确定性方法骨架提取器
- 已将 `LLMCFGAnalyzer` 改为基于 skeleton 驱动 LLM 分析
- 已引入 `WP2` 的 complexity hint、结构化 path schema 与后验校验/降级

## 实验目的

验证以下问题：

- `WP1` 是否已稳定方法集合、内部类与重载方法识别
- `WP2` 是否已改善复杂度与路径数输出质量
- `llm` 路径后端在最小 smoke run 下是否仍可稳定跑通主流程
- baseline generation 的编译错误是否较 `ver0.1` 明显下降

## 相关文件

- 运行索引：`cfg_snapshot_test/llm_path_backend_validation/ver0.2_exp0.2.1/run_index.md`
- 结果摘要：`cfg_snapshot_test/llm_path_backend_validation/ver0.2_exp0.2.1/results.md`

## 备注

本目录当前只保存索引和结论说明，原始日志与快照仍保留在原位置：

- 日志：`cfg_snapshot_test/validation_runs/`
- 快照：`cfg_snapshot_test/intermediate/cfg/`
