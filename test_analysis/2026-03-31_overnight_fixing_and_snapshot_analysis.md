# 分析：Overnight 三轮实验的修复阶段与 Snapshot 可观测性

**日期**: 2026-03-31
**作者**: AI (openai/gpt-5.4)
**对象**: CSVParser (defects4j Csv-16f)
**范围**: 三轮 overnight 实验的 generation/fixing 行为、修复成功模式、advice feedback 可观测性

## 数据来源

### 运行日志

| 标签 | 运行目录 |
|---|---|
| R1 | `smoke_test_log/2026-03-31_04:10:32_11iter-nocov3_CSVParser/` |
| R2 | `smoke_test_log/2026-03-31_04:50:09_11iter-nocov6_CSVParser/` |
| R3 | `smoke_test_log/2026-03-31_05:20:48_11iter-nocov11_CSVParser/` |

### CFG / Prompt Snapshot

| 运行 | CFG snapshot |
|---|---|
| R1 | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_041045_6a94cf1c/` |
| R2 | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_045046_0dfd5c2b/` |
| R3 | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_052104_7e2fdd72/` |

说明：R2 的 snapshot 目录名来自实际日志与目录检查；若后续发现更精确目录，可补充修正，但不影响本文结论。

---

## 总结结论

1. 修复阶段不是完全无效，但它的成功主要集中在少数低阶问题：修正 API 用法、修正测试输入、修正明显错误断言、规避测试名冲突。
2. 对真正困难的运行时语义误判，尤其是 trailing delimiter 和 header 触发条件，修复阶段大多只是重复同一错误假设。
3. 修复成功一般发生得不晚，主要集中在 `fix round 1` 或 `fix round 2`，拖到 `fix round 3` 才成功的情况较少。
4. 当前 snapshot 可以部分看到“上一轮 advice 长什么样”，但通常看不到“上一轮失败后拼进下一轮 advice prompt 的完整 feedback 文本”。这使得我们很难判断到底是 feedback 没起作用，还是根本没被完整观察到。

---

## 整体统计

| 实验 | pass | 编译错误 | 运行时错误 | timeout | 修复成功数 | 最终覆盖率 |
|---|---:|---:|---:|---:|---:|---|
| R1 (`nocov=3`) | 8 | 3 | 58 | 0 | 5 | 56.44% line / 39.29% branch |
| R2 (`nocov=6`) | 6 | 24 | 28 | 0 | 2 | 23.76% line / 3.57% branch |
| R3 (`nocov=11`) | 6 | 1 | 75 | 0 | 2 | 69.31% line / 60.71% branch |

### 修复成功发生代次

| 实验 | 修复成功位置 |
|---|---|
| R1 | Iter 0 round 2；Iter 5 round 2 ×2；Iter 6 round 2；Iter 7 round 1 |
| R2 | Iter 5 round 1；Iter 5 round 3 |
| R3 | Iter 0 round 1；Iter 4 round 3 |

观察：

- `round 1` 和 `round 2` 是主力成功位置。
- `round 3` 偶尔能成功，但通常只出现在模型终于改正了 API 触发条件或语义前提时。
- 如果一个错误在前两轮修复里仍完全没有改变输入/配置/断言核心假设，第三轮通常也不会突然成功。

---

## R1 详细分析

### 迭代摘要

| Iteration | generation 结果 | fixing 结果 | 备注 |
|---|---|---|---|
| 0 | 3 个测试，1 pass，2 runtime error | 2 个失败测试继续修，round 2 修成 1 个 | 初次就出现修复成功 |
| 1 | 2 个测试，全 runtime error | 3 轮修复全部失败 | 典型 trailing delimiter 死循环 |
| 2 | 3 个测试，1 pass，2 runtime error | 3 轮修复全部失败 | `testParseNoHeaders` 直接通过 |
| 3 | 2 个测试，1 pass，1 runtime error | 3 轮修复全部失败 | 修复版本仅改名，不改语义 |
| 4 | 1 个测试，runtime error | 3 轮修复全部失败 | 无有效变化 |
| 5 | 4 个测试，全失败 | round 2 修成 2 个 | 少数高产出修复轮 |
| 6 | 2 个测试，1 compilation + 1 runtime | round 2 修成 1 个 | 同时暴露编译和语义问题 |
| 7 | 2 个测试，全 runtime error | round 1 修成 1 个 | 输入与 format 对齐后成功 |
| 8 | 1 个测试，runtime error | 3 轮修复全失败 | 无有效变化 |
| 9 | 1 个测试，runtime error | 3 轮修复全失败 | 无有效变化 |

### R1 修复成功案例

| 测试 | 成功位置 | 修复类型 | 关键变化 |
|---|---|---|---|
| `testEmptyRecordWithTrailingDelimiter` | Iter 0 round 2 | 断言修正 | `null` 改为 `""` |
| `testParseCSVWithTrailingDelimiters` | Iter 5 round 2 | 行为理解修正 | 从“2 条记录”改成“1 条记录 + 空字段” |
| `testEmptyRecordWithTrailingDelimiterTrue` | Iter 5 round 2 | 行为理解修正 | 从“多一条空记录”改成单记录空字段 |
| `testEmptyRecordWithoutTrailingDelimiterFixed` | Iter 6 round 2 | 输入修正 | 去掉末尾逗号，让断言与输入一致 |
| `testEmptyInputWithoutTrailingDelimiterFixed` | Iter 7 round 1 | 输入/format 对齐 | 分隔符从 `;` 改为 `,`，并去掉尾分隔符 |

### R1 结论

- R1 是三轮里修复阶段最有产出的 run。
- 但这些成功多数不意味着模型真正学会了复杂语义，而是修到了更保守、更接近真实实现的输入和断言。
- 一旦进入“trailing delimiter 会不会多出空记录/空字段/null”这种细粒度行为猜测，失败会反复出现。

---

## R2 详细分析

### 迭代摘要

| Iteration | generation 结果 | fixing 结果 | 备注 |
|---|---|---|---|
| 0 | 3 个测试，1 pass，1 compilation，1 runtime | 3 轮修复全失败 | 初始覆盖率提升但修复无效 |
| 1 | 3 个测试，1 pass，2 runtime | 3 轮修复全失败 | trailing delimiter 与 empty header 重复失败 |
| 2 | 3 个测试，1 pass，2 runtime | 3 轮修复全失败 | duplicate header 方向继续有 pass |
| 3 | 3 个测试，2 runtime，1 compilation | 3 轮修复多数编译失败 | API 误用开始明显增多 |
| 4 | 2 个测试，全 compilation | 3 轮修复全 compilation | 彻底卡在 API/代码层 |
| 5 | 3 个测试，1 pass，2 compilation | round 1 修成 1 个，round 3 再修成 1 个 | 唯一修复有效的迭代 |
| 6 | 1 个测试，runtime | 修复后转 compilation，仍失败 | 最终停机 |

### R2 修复成功案例

| 测试 | 成功位置 | 修复类型 | 关键变化 |
|---|---|---|---|
| `testParseNullStringNew` | Iter 5 round 1 | 编译层修复 | 主要是改名，避开与现有测试方法重名 |
| `testCSVParserWithDuplicateHeadersNew` | Iter 5 round 3 | API 触发条件修复 | 从 `CSVFormat.DEFAULT` 改成 `withHeader("header1", "header1")` |

### R2 结论

- R2 的修复成功非常少，但含有一个最有代表性的“正确修复”：模型终于从“CSV 输入首行重复 header”切换到“必须显式启用 header 初始化逻辑”。
- 这说明修复提示词并非完全无法帮助模型修正根本理解错误，但成功概率很低。
- R2 的另一个明显问题是编译错误占比偏高，说明很多修复尝试甚至没到行为判断这一步。

---

## R3 详细分析

### 迭代摘要

| Iteration | generation 结果 | fixing 结果 | 备注 |
|---|---|---|---|
| 0 | 3 个测试，1 pass，1 compilation，1 runtime | round 1 修成 1 个 | 初始阶段已有修复成功 |
| 1 | 3 个测试，全 runtime | 3 轮修复全 runtime | 完全无增长 |
| 2 | 3 个测试，全 runtime | 3 轮修复全 runtime | duplicate/empty header 思路仍错 |
| 3 | 2 个测试，全 runtime | 3 轮修复全 runtime | 空记录假设持续错误 |
| 4 | 1 个测试，runtime | round 3 修成 1 个 | 这是本 run 最关键的一次修复 |
| 5 | 2 个测试，全 runtime | 3 轮修复全失败 | trailing delimiter 继续锁死 |
| 6 | 3 个测试，全 runtime | 3 轮修复全失败 | header 组合假设依然错误 |
| 7 | 2 个测试，1 pass，1 runtime | 修复未能救活另一个 | `getHeaderMap` 方向保守测试通过 |
| 8 | 1 个测试，pass | 无修复需求 | 单测直接带来增量 |
| 9 | 1 个测试，pass | 无修复需求 | 单测直接带来增量 |
| 10 | 无新的有效设计 | 收尾 | 达到最大迭代数 |

### R3 修复成功案例

| 测试 | 成功位置 | 修复类型 | 关键变化 |
|---|---|---|---|
| `testGetRecordsWithTrailingDelimiter` | Iter 0 round 1 | 编译/API 修复 | 从 `values().toArray()` 改成直接 `values()`，并补 import |
| `testParseWithDuplicateHeaders` | Iter 4 round 3 | API 触发条件修复 | 从 `CSVFormat.DEFAULT` 改成 `CSVFormat.DEFAULT.withHeader()` |

### R3 结论

- R3 的最高覆盖率并不是因为修复阶段特别强，而是因为它有足够多的 iteration，让 generation 阶段后来又碰到几个正确的、偏保守的测试。
- 修复阶段真正关键的一次成功是 Iter 4 round 3：它修正了 duplicate headers 的触发方式，并直接带来大幅覆盖率提升。
- 但除这个少数案例外，R3 的绝大多数修复仍然只是 runtime error 的重复堆积。

---

## 修复阶段成功模式

### 成功类型分类

| 类型 | 说明 | 代表案例 |
|---|---|---|
| 断言值修正 | 把 `null` 改为 `""`，把记录数改为更符合实现的值 | R1 Iter 0 round 2 |
| 输入修正 | 改 CSV 文本，使其与断言和 format 匹配 | R1 Iter 6/7 |
| format / API 触发条件修正 | 用 `withHeader()`、`withHeader("a", "a")` 等真正触发目标逻辑 | R2 Iter 5 round 3；R3 Iter 4 round 3 |
| 编译/命名修正 | 改 API 调用形式、补 import、改测试名 | R2 Iter 5 round 1；R3 Iter 0 round 1 |

### 失败类型分类

| 类型 | 说明 |
|---|---|
| 运行时语义死循环 | 模型在同一个错误预期上连续多轮改写，但没有改变核心输入/配置/断言逻辑 |
| trailing delimiter 误判 | 把空字段、空记录、`null`、`""` 的关系理解错 |
| header 初始化误判 | 不理解异常必须在某些 format 条件下才会触发 |
| API 误用 | `values().toArray()` 之类不符合真实返回类型 |
| 测试名冲突 | 与已有测试方法同名，导致编译问题 |

---

## 我们现在能看到什么 advice feedback 链路

## 当前可见性结论

### 能看到的部分

- 上一轮生成出来的 advice 结构化结果，通常能从对应 snapshot stage 的 `generation_outcome.json` 里看到。
- 当前轮 advice / selection 的摘要信息，通常能从 `rendered_prompt_context.json` 的 summary 内容里看到。

### 看不到或无法精确重建的部分

- `previous_advice_feedback` 的完整文本
- 生成该 feedback 时用到的 `iteration_error_summary`
- 下一轮 advice prompt 的完整 user prompt 文本
- 明确的“这一轮引用了哪一轮 advice / feedback”的结构化链接关系

### 原因

这批 `2026-03-31` 运行使用的是 snapshot `summary` 模式，而不是 `full_text`。因此：

- advice 的结果被保存了
- prompt 的摘要被保存了
- 但真正拼进去的完整反馈文本没有落盘

结果是：

- 我们可以大致知道上一轮 advice 是什么
- 但无法精确验证：上一轮失败总结到底是如何被拼进下一轮 advice prompt 的

---

## Snapshot 应如何调整

### 最重要的最小改动

1. 对 `prompt_builder_llm_advice` 和 `prompt_builder_llm_selection` 永远保存完整 prompt 文本
2. 单独保存 `previous_advice_feedback.txt`
3. 单独保存 `iteration_error_summary.json`
4. 在 snapshot meta 里加入：
   - `iteration`
   - `phase`
   - `used_previous_advice_feedback`
   - `previous_advice_stage_sequence`

### 建议增加的高价值产物

| 文件/字段 | 用途 |
|---|---|
| `failed_test_runs.json` | 还原本轮修复输入，看到每个失败测试的原始错误信息 |
| `iteration_error_summary.json` | 还原 `_build_advice_feedback()` 的输入 |
| `rendered_user_prompt.txt` | 精确看到 advice / test-gen 实际吃到的 prompt |
| `raw_llm_response.txt` | 对比 raw 输出和 normalize 后结果 |
| `normalized_advice.json` | 明确 normalize 过程产物 |
| `validation_results.json` | 每个生成/修复测试的 pass/fail 类型及错误摘要 |
| `coverage_before_after.json` | 让 coverage 增量分析更直接 |
| `selection_state.json` | 还原 mode、last_advice、history 等内部状态 |

---

## 最终判断

1. 修复提示词目前不是“完全无效”，但它的主要收益仍集中在低阶修补，而不是稳定纠正复杂行为预测。
2. 真正稀缺且有价值的修复，是那种把测试从错误的 API/format 前提，修到真正能触发目标逻辑的版本。
3. 如果不提升 snapshot 的可观测性，我们很难继续严谨回答以下问题：
   - 修复阶段失败时，到底传给 advice 模型的反馈是什么？
   - 模型有没有真正看到那段反馈？
   - 是 feedback 无效，还是 prompt 结构让它没被利用？
4. 因此下一步最值得做的，不只是继续调 prompt，还包括把 advice feedback 链路完整落盘。
