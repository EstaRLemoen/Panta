# ver0.2_exp0.2.25 验证说明

## 定位

本目录归档 `ver0.2` 阶段最近一轮 `llm path backend` 状态确认材料，重点覆盖：

- `direct_path_text` 实验链路
- 单路径单 outcome prompt 收紧
- 逐 method 调用 LLM 的实验
- 当前主流程 smoke 基线

## 重点结论

- 当前 `llm_analyzer` 与 `PromptBuilder`、`panta.py`、`unit_test_generator.py` 的集成链路仍可运行。
- `PromptBuilder` 已重新回到“消费 CFG 侧提供的 membership/path text”的角色，没有继续在 prompt 里拼接 metadata dump。
- `direct_path_text` 在简单/中等复杂度方法上开始出现可用形态，但对 `nextRecord()` 这类高复杂度方法仍会出现过宽 path。
- 逐 method 调用 LLM 能显著减少复杂方法直接缺席的问题。

## 相关文件

- `run_index.md`
- `results.md`
