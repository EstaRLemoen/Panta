# ver0.2_exp0.2.25 结果摘要

## 一句话结论

当前 `llm path backend` 已能在主流程中真实参与选路与测试生成，但高复杂度方法的 path decomposition 仍不稳定；`direct_path_text` 已经开始在简单/中等复杂度方法上出现可用形态，不过 `nextRecord()` 仍会产生过宽 path，因此 llm smoke 虽然能涨 coverage，但仍明显弱于理想目标。

## 当前确认的事实

- `PromptBuilder` 当前不再把 metadata dump 直接拼进生成 prompt。
- `PromptBuilder` 对 llm backend 的优先级已经是：
  - `_llm_direct_path_text`
  - `_llm_path_prompt_text`
  - `_llm_precomputed_path_str`
- `_llm_membership_lines` 由 analyzer 侧主导派生，`PromptBuilder` 只消费，保留轻量 fallback。
- `selected_paths` snapshot 仍保留，可继续观察选中的 path 与 path text。

## analyzer-only 观察

### 逐 method 调用有效

- 在 `ver0.2_exp0.2.21_llm_analyzer_per_method.log` 及后续 analyzer-only 实验中，`CSVParser.nextRecord()` 能稳定回到 LLM 输出中。
- 这说明“复杂方法直接缺席”问题已经部分缓解。

### 单路径单 outcome 规则开始起作用

- `INVALID -> throw IOException`
- `EOF and isReady true -> addRecordValue(true) -> return result`
- `EOF and isReady false -> return result`

这些 path 已经表现出更好的单 outcome 倾向。

### 但 `nextRecord()` 仍残留过宽 path

在 `ver0.2_exp0.2.23_llm_analyzer_per_method.log` 中，`nextRecord()` 仍有类似：

- `TOKEN -> addRecordValue(false)`
- 再接 `EORECORD -> addRecordValue(true)`
- 最后 `return result`

这种 path 虽然比早期“全方法大拼盘”更收敛，但仍不是理想的可打分路径单元。

## smoke 观察

### 1 轮 smoke

- 日志：`ver0.2_exp0.2.24_smoke1_pre_redesign.log`
- 结果：`0.0% / 0.0%`
- 主要表现为大量 compilation error
- 说明单轮 baseline prompt 不能代表 path-guided 实际效果

### 3 轮 smoke

- 日志：`ver0.2_exp0.2.25_smoke3_current_llm.log`
- 结果：
  - Iteration 0 后：`46.53% / 30.36%`
  - Iteration 1 后：`51.49% / 32.14%`
  - Iteration 2 后：`58.42% / 32.14%`

关键现象：

- 第 1、2 轮日志中明确出现 `PromptBuilder` 选择 `nextRecord` path
- 说明 path-guided 轮次已经真实参与，不再只是 baseline prompt 在起作用
- 但仍存在 runtime error / compilation error，表明复杂 path 的质量仍然限制测试生成质量

## 当前判断

- 当前系统集成可运行，可以作为继续迭代的基线
- 当前最主要的问题已经收敛到：
  - 高复杂度方法，尤其 `nextRecord()` 的 path set 仍不够稳定
  - 这直接影响 path text 质量与测试生成效果

## 下一步建议

- 进入高复杂度方法 path decomposition 的下一阶段设计
- 重点不是继续改 `PromptBuilder`，而是改进 llm analyzer 生成复杂方法 path set 的策略
