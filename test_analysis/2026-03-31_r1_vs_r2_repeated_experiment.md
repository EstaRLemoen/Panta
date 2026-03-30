# 分析：R1 vs R2 重复实验

**日期**: 2026-03-31
**作者**: AI (glm-5.1)
**对象**: CSVParser (defects4j Csv-16f)
**验证的代码改动**: 本次会话的 4 项改动（弱化 light advice 的 observable_behavior、行为对冲指令、advice 失败反馈机制、放宽 normalize_advice 校验）

## 实验配置

| 参数 | 值 |
|---|---|
| 模型 | gpt-4o-mini |
| 选择模式 | llm |
| Advice 激活阈值 | 30% 行覆盖率 |
| Advice 激活无增长 | 连续 3 次 |
| Advice 温度 | 0.2（light 和 full 均为） |
| 最大迭代 | 3 |
| 每轮修复次数 | 3 |
| 基线文件 | `smoke_test_log/baselines/CSVParser_baseline_only_test.java` |

## 数据来源

| 标签 | 运行目录 | CFG 快照 |
|---|---|---|
| R1 | `smoke_test_log/2026-03-31_01:12:52_from-baseline-temp02-3iter_CSVParser/` | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_011400_565b01c9` |
| R2 | `smoke_test_log/2026-03-31_01:43:31_from-baseline-temp02-3iter-r2_CSVParser/` | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_014410_69a2cad7` |

---

## 覆盖率轨迹

### R1

| 迭代 | 模式 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 |
|---|---|---|---|---|---|
| 0 | Light advice | +61.39% | 64.36% | +50.0% | 50.0% |
| 1 | Full advice | 0% | 64.36% | 0% | 50.0% |
| 2 | Full advice + feedback | +0.99% | 65.35% | +3.57% | 53.57% |

### R2

| 迭代 | 模式 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 |
|---|---|---|---|---|---|
| 0 | Light advice | +22.77% | 25.74% | +3.57% | 3.57% |
| 1 | Light advice | +38.62% | 64.36% | +39.29% | 42.86% |
| 2 | Full advice | 0% | 64.36% | 0% | 42.86% |

两次实验的行覆盖率最终收敛到几乎相同的水平（~64-65%），但分支覆盖率差异明显（R1: 53.57% vs R2: 42.86%），轨迹形状也很不同。

---

## 测试结果统计

### 按迭代拆分

**R1:**

| 迭代 | 生成设计数 | 总构建次数 | 编译错误 | 运行时错误 | 通过 | 通过率 |
|---|---|---|---|---|---|---|
| 0 (light) | 3 | 12 | 6 | 5 | 1 | 8.3% |
| 1 (full) | 1 | 8 | 0 | 8 | 0 | 0% |
| 2 (full+fb) | 1 | 16 | 0 | 14 | 2 | 12.5% |
| **合计** | **5** | **36** | **6** | **27** | **3** | **8.3%** |

**R2:**

| 迭代 | 生成设计数 | 总构建次数 | 编译错误 | 运行时错误 | 通过 | 通过率 |
|---|---|---|---|---|---|---|
| 0 (light) | 3 | 12 | 0 | 11 | 1 | 8.3% |
| 1 (light) | 3 | 12 | 4 | 7 | 1 | 8.3% |
| 2 (full) | 1 | 8 | 0 | 8 | 0 | 0% |
| **合计** | **7** | **32** | **4** | **26** | **2** | **6.3%** |

### 汇总

| 指标 | R1 | R2 | 平均 |
|---|---|---|---|
| 总构建次数 | 36 | 32 | 34 |
| 编译错误 | 6 (16.7%) | 4 (12.5%) | 10 (14.7%) |
| 运行时错误 | 27 (75.0%) | 26 (81.3%) | 53 (77.9%) |
| 通过 | 3 (8.3%) | 2 (6.3%) | 5 (7.4%) |

**运行时错误占绝大多数：约 78% 的构建失败发生在运行时，而非编译阶段。**

---

## 通过的测试详情

| 测试名 | 运行 | 迭代 | 模式 | 目标 |
|---|---|---|---|---|
| `testHeaderInitializationWithEmptyHeader` | R1 | 0 | Light | `initializeHeader()` — 基本头映射断言 |
| `testGetHeaderMapReturnsNullForNoHeaderDefined` | R1 | 2 | Full+fb | `initializeHeader()` — assertNull 检查 headerMap |
| `testGetHeaderMapReturnsNullForEmptyHeader` | R1 | 2 | Full+fb | `initializeHeader()` — assertNull 检查 headerMap |
| `testClosedParserIterator` | R2 | 0 | Light | `iterator()` + `close()` — NoSuchElementException |
| `testCSVParserWithEmptyHeader` | R2 | 1 | Light | `initializeHeader()` — 头映射断言 |

**全部 5 个通过的测试都针对简单、可预测的行为：**
- 异常期望（3/5）：`assertNull`、`NoSuchElementException`、`IllegalArgumentException`
- 基本大小/值检查（2/5）：`assertEquals(headerMap.size(), 2)`、`assertTrue(headerMap.containsKey(...))`
- 没有一个需要精确预测返回值

---

## 失败测试分析

### Trailing Delimiter 陷阱（仍然占主导）

| 运行 | 迭代 | 目标 trailing delimiter 的测试 | 通过 | 失败模式 |
|---|---|---|---|---|
| R1 | 0 | 1 (`testTrailingDelimiterHandling`) | 0 | 编译错误（API 调用错误 `.values()`） |
| R1 | 1 | 2 (`testTrailingDelimiterHandling` + `testEmptyRecordWithTrailingDelimiter`) | 0 | 运行时错误（assertNull vs 实际 ""） |
| R1 | 2 | 2（同名） | 0 | 运行时错误（同样的错误断言） |
| R2 | 0 | 1 (`testTrailingDelimiterHandling`) | 0 | 运行时错误 × 3 轮修复全败 |
| R2 | 1 | 1 (`testCSVParserWithTrailingDelimiter`) | 0 | 编译→运行时循环，3 轮修复全败 |
| R2 | 2 | 2 (`testGetRecordsWithEmptyTrailingRecord` + `testGetRecords...NoTrailingDelimiter`) | 0 | 运行时错误 × 3 轮修复全败 |

**Trailing delimiter 总计：9 个测试设计 × ~3 轮修复 = ~27 次构建。零通过。**

根本原因不变：advice 模型预测 trailing delimiter 字段值为 `null` 或 `empty`，但实际代码返回 `""`。修复 prompt（加指南前的版本）没有指示 LLM 相信观测值而非预测值。

### 其他运行时错误

| 测试模式 | 运行 | 原因 |
|---|---|---|
| `testCaseSensitivityInHeaderMapping` | R2 I0 | 预期 `IllegalArgumentException` 但代码不抛出 |
| `testCommentHandlingInRecords` | R1 I0 | 编译错误→运行时——comment 格式 API 调用错误 |
| `testCSVParserWithCaseInsensitiveHeader` | R2 I1 | 运行时——`withIgnoreHeaderCase()` 链式调用或断言不匹配 |
| `testGetHeaderMapReturnsNullForEmptyHeaderLine` | R1 I2 | 预期 assertNull 但 headerMap 非 null |
| `testGetHeaderMapReturnsNullForWhitespaceHeader` | R1 I2 | 同上——行为预测错误 |

这些都遵循同样的模式：**LLM 猜了一个行为，写了断言，猜错了。**

### 修复 prompt 效果

| 错误类型 | 总修复尝试 | 修复成功 | 修复率 |
|---|---|---|---|
| 编译错误（R1×6 + R2×4） | ~10 轮修复 | ~2-3 | ~25% |
| 运行时错误（R1×27 + R2×26） | ~53 轮修复 | 0 | **0%** |

**修复 prompt 对运行时（断言）错误完全无效。** 它能修复部分编译错误（API 调用不对），但从未修正过错误的行为预测。这是最大的瓶颈。

---

## Light Advice vs Full Advice

| 指标 | Light advice | Full advice |
|---|---|---|
| 总迭代次数（R1+R2） | 4 | 3 |
| 每迭代设计数 | 3 | 1 |
| 总构建次数 | 48 | 24 |
| 通过 | 4 | 1 |
| 每构建通过率 | 8.3% | 4.2% |
| 设计多样性 | trailing delimiter + header + iterator + close + case-sensitivity | 几乎只有 trailing delimiter |

关键观察：
1. **Light advice 每次迭代生成 3 个设计**，覆盖不同方法；**Full advice 每次只生成 1 个设计**，锁定在单一目标。
2. Light advice 通过的测试涉及 4 个不同领域（header init、close/iterator、header map、empty header）。Full advice 只通过了 header map 测试（R1 Iter 2，feedback 强制切换后）。
3. Full advice **更具体但也更错误**——详细的 CFG 路径分析让 advice 自信地预测 `addRecordValue(true)` 的行为，但预测是错的。

---

## Advice 失败反馈机制（改动 #3）

| 运行 | 反馈触发？ | 效果 |
|---|---|---|
| R1 | 是——Iter 1 无增长（full advice），触发反馈 | Iter 2 从 trailing delimiter 切换到 header 相关设计，2 个测试通过 |
| R2 | 否——只有 1 次无增长迭代（Iter 2）就达到上限 | 反馈机制没有机会发挥作用 |

反馈机制在 R1 展现了有希望的效果，但 R2 因为迭代次数不够而无法验证。

**评估：有希望，但需要更多迭代才能充分评估。**

## 行为对冲（改动 #2）

两次运行中，检查了所有测试生成输出中是否存在对冲行为（同一设计生成多个不同预期结果的测试）。

**结果：没有发现任何对冲证据。** LLM 一致地为每个设计生成一个测试、一套断言。对冲指令可能太隐蔽，或者与 YAML schema（期望扁平的 `new_tests` 列表）冲突。

可能原因：
1. YAML schema 强制扁平的 `new_tests` 列表，不是按设计分组
2. 指令是建议性的（"consider..."）而非强制性的
3. LLM 可能不理解"生成不同预期结果的多个测试"在实际中意味着什么

## 放宽 Normalize Advice（改动 #4）

两次运行中没有发生 normalize-advice 失败。Light advice YAML 在所有情况下都被接受。这项改动防止了潜在的崩溃，但没有可观察到的行为变化。

## 弱化 Observable Behavior（改动 #1）

Light advice 的 `observable_behavior` 字段是开放式的定性描述，而非精确预测。这可能有助于 light advice 产生更多样的测试设计。但是，**test-gen LLM 在写断言时仍然填入了具体的值**，所以弱化 advice 并没有阻止生成测试中的错误行为预测。

---

## Advice 内容详细分析

### Advice → 测试 → 结果的可追溯性

每次迭代生成 advice（light 或 full），经过 selection 步骤不变，然后输入给 test-gen LLM。Snapshot 目录保存了 advice 输出和 selection 透传数据。

**图例**：snapshot 步骤 `00X_prompt_builder_llm_{light_}advice` = advice 生成，`00X_prompt_builder_llm_selection` = 测试生成 prompt。数据来源 `cfg_snapshot_test/intermediate/cfg/<snapshot_id>/<step>/generation_outcome.json`。

#### R1 Advice 链路

| 迭代 | 模式 | 步骤 | Advice 设计 | 生成的测试 | 测试结果 |
|---|---|---|---|---|---|
| 0 | Light | 002→003 | **[0] Trailing Delimiter**（`addRecordValue`，obs: "verify how parser handles trailing delimiter"） | `testTrailingDelimiterHandling` | 编译错误 × 3 轮修复 |
| 0 | Light | 002→003 | **[1] Header Init Empty**（`initializeHeader`，obs: "check if header map is correctly populated"） | `testHeaderInitializationWithEmptyHeader` | **通过** |
| 0 | Light | 002→003 | **[2] Comment Handling**（`nextRecord`，obs: "verify comments are correctly captured"） | `testCommentHandlingInRecords` | 编译错误 × 3 轮修复 |
| 1 | Full | 004→005 | **[0] Trailing Delimiter**（`addRecordValue`，obs: "list should contain records without adding null or empty value"） | `testTrailingDelimiterHandling` + `testEmptyRecordWithTrailingDelimiter` | 运行时错误 × 8 次构建 |
| 2 | Full+fb | 006→007 | **[0] Empty Headers**（`initializeHeader`，obs: "getHeaderMap() should return null"） | `testGetHeaderMapReturnsNullForEmptyHeader` 等 4 个测试 | **通过** × 2，运行时错误 × 2 |

#### R2 Advice 链路

| 迭代 | 模式 | 步骤 | Advice 设计 | 生成的测试 | 测试结果 |
|---|---|---|---|---|---|
| 0 | Light | 002→003 | **[0] Trailing Delimiter**（`addRecordValue`，obs: "verify how parser handles trailing delimiter"） | `testTrailingDelimiterHandling` | 运行时错误 × 3 轮修复 |
| 0 | Light | 002→003 | **[1] Case Sensitivity Header**（`initializeHeader`，obs: "check header map for correct index assignments"） | `testCaseSensitivityInHeaderMapping` | 运行时错误 × 2 轮修复 |
| 0 | Light | 002→003 | **[2] Closed Parser**（`isClosed`，obs: "verify next() throws NoSuchElementException"） | `testClosedParserIterator` | **通过** |
| 1 | Light | 004→005 | **[0] Trailing Delimiter**（`addRecordValue`，obs: "verify record list correctly handles trailing delimiter"） | `testCSVParserWithTrailingDelimiter` | 编译→运行时循环 × 6 次构建 |
| 1 | Light | 004→005 | **[1] Empty Header**（`initializeHeader`，obs: "check header map populated correctly from first line"） | `testCSVParserWithEmptyHeader` | **通过** |
| 1 | Light | 004→005 | **[2] Case-Insensitive Header**（`initializeHeader`，obs: "verify header map maps headers regardless of case"） | `testCSVParserWithCaseInsensitiveHeader` | 运行时错误 × 2 轮修复 |
| 2 | Full | 006→007 | **[0] Trailing Delimiter**（`addRecordValue`，obs: "getRecords() returns an empty list"） | `testGetRecordsWithEmptyTrailingRecord` + `testGetRecordsWithEmptyRecordNoTrailingDelimiter` | 运行时错误 × 8 次构建 |

### Advice 质量模式

#### 1. Trailing delimiter advice：永远在场，永远错误

| 运行 | 迭代 | 模式 | `observable_behavior` 预测 | 实际行为 | 测试数 | 通过 |
|---|---|---|---|---|---|---|
| R1 | 0 | Light | "verify how parser handles trailing delimiter, whether it adds empty or ignores"（开放式） | 空字符串 `""`，不是 null | 1 | 0 |
| R1 | 1 | Full | "should contain parsed records without adding null or empty value" | 实际包含空字符串 `""` | 2 | 0 |
| R2 | 0 | Light | "verify how parser handles trailing delimiter, whether it adds empty or ignores"（开放式） | 空字符串 `""` | 1 | 0 |
| R2 | 1 | Light | "verify record list correctly handles trailing delimiter, potentially including empty value" | 空字符串 `""` 但测试断言 null | 1 | 0 |
| R2 | 2 | Full | "getRecords() returns an empty list" | 错——记录是返回的，trailing 字段值是 `""` | 2 | 0 |

**总计：7 个测试设计，0 通过。** 每一轮 advice——无论 light 还是 full——都把 trailing delimiter 作为第一个设计。Light advice 确实避免了预测精确值（"verify how..."），但 test-gen LLM 自己填入了 `assertNull` 或 `assertArrayEquals(..., null)`。Full advice 则自信地预测了错误行为（"returns empty list"、"without adding null or empty value"）。

Advice **在选择目标上并没有错**——`addRecordValue(boolean lastRecord)` 确实有未覆盖的分支。但行为预测始终不正确，因为代码的实际行为（trailing empty 字段返回 `""`）不运行代码是无法预知的。

#### 2. Header initialization advice：一贯成功

| 运行 | 迭代 | 模式 | `observable_behavior` 预测 | 测试 | 通过 |
|---|---|---|---|---|---|
| R1 | 0 | Light | "check if header map is correctly populated" | `testHeaderInitializationWithEmptyHeader` | **是** |
| R1 | 2 | Full | "getHeaderMap() should return null" | `testGetHeaderMapReturnsNullForNoHeaderDefined` | **是** |
| R1 | 2 | Full | "getHeaderMap() should return null" | `testGetHeaderMapReturnsNullForEmptyHeader` | **是** |
| R2 | 1 | Light | "check header map populated correctly from first line" | `testCSVParserWithEmptyHeader` | **是** |

**4/4 通过。** Header 初始化测试成功是因为：
- 行为简单可预测（null ↔ non-null、size check）
- Light 和 full advice 都能充分描述
- Test-gen LLM 不需要精确值预测就能写出正确断言

#### 3. Iterator/close advice：异常类预测时成功

| 运行 | 迭代 | 模式 | `observable_behavior` 预测 | 测试 | 通过 |
|---|---|---|---|---|---|
| R2 | 0 | Light | "verify next() throws NoSuchElementException when parser is closed" | `testClosedParserIterator` | **是** |

基于异常的预测之所以有效，是因为 LLM 只需要预测异常类型，不需要精确的返回值。

#### 4. 其他非 trailing delimiter 的失败 advice

| 运行 | 迭代 | 模式 | `observable_behavior` 预测 | 测试 | 通过 | 失败原因 |
|---|---|---|---|---|---|---|
| R1 | 0 | Light | "verify comments are correctly captured" | `testCommentHandlingInRecords` | 0 | 编译错误（API 调用错误） |
| R2 | 0 | Light | "check header map for correct index assignments with case-insensitive duplicates" | `testCaseSensitivityInHeaderMapping` | 0 | 预期 `IllegalArgumentException` 但未抛出 |
| R2 | 1 | Light | "verify header map maps headers regardless of case" | `testCSVParserWithCaseInsensitiveHeader` | 0 | API 链式调用错误 / 断言不匹配 |

失败原因各不相同：comment handling 碰到 API 复杂度，case-sensitivity 测试预测了错误的异常类型或 API 用法。

### Light vs Full advice：observable_behavior 对比

| 维度 | Light advice | Full advice |
|---|---|---|
| 每迭代设计数 | 3 | 1 |
| `observable_behavior` 风格 | 开放式："verify how..."、"check if..." | 自信预测："should return null"、"returns empty list" |
| 预测准确度 | 不适用（避免预测） | 1/3 正确（header=null ✓，trailing delimiter ✗，trailing delimiter ✗） |
| Test-gen LLM 的解读 | 自己填入具体值→经常填错 | 跟随预测→预测错了就全错 |
| 目标多样性 | 每迭代 3 个不同方法 | 每迭代 1 个方法，总是 trailing delimiter 优先 |

**关键洞察**：弱化 light advice 的 `observable_behavior`（改动 #1）成功避免了强制错误预测，但没有解决问题——test-gen LLM 仍然需要填入断言值，而且它填错了。问题不仅是 advice；更根本的是，**advice 模型和 test-gen 模型都不知道 trailing delimiter 场景下的实际运行时行为**。

### 反馈机制链路追踪（R1）

- **Iter 1**（full advice）：Trailing delimiter 设计。全部 8 次构建→运行时错误。覆盖率无增长。
- **Iter 2**（full advice + feedback）：**切换到了 `initializeHeader()`**。反馈字符串包含了 Iter 1 的错误计数 + 首条运行时错误片段。结果：2 个测试通过，+0.99% 行覆盖率。
- **R2 对比**：无反馈机会（只有 Iter 2 的 1 次无增长迭代就达到了上限）。

反馈机制在 R1 起到了作用。问题是如果 R2 有更多迭代，是否也能生效。

### 行为对冲链路追踪

检查了所有测试生成输出中是否存在对冲证据（同一设计生成不同预期结果的多个测试变体）：

**两次运行中均未发现对冲证据。** LLM 为每个设计精确生成一个测试，一套断言。改动 #2 添加的对冲指令似乎被 test-gen LLM 完全忽略了。

可能原因：
1. YAML schema 强制扁平的 `new_tests` 列表，不是按设计分组
2. 指令是建议性的（"consider..."）而非强制性的
3. LLM 可能不理解"生成不同预期结果的多个测试"在实践中意味着什么

---

## 结论

1. **根本瓶颈是运行时错误的修复率：0%。** 78% 的构建在运行时失败，修复 prompt 从未修正过任何一个。添加修复指南（已在这些运行之后完成）是最高优先级的干预。

2. **Light advice 严格优于 full advice**——在这个覆盖率水平和测试对象上。更多样的设计、更高的通过率、更少的锁定。

3. **Trailing delimiter 陷阱依然存在**，但现在主导性略减——light advice 每次迭代提供 2 个非 trailing delimiter 的设计作为"逃生通道"。

4. **覆盖率天花板大约在 ~65% 行 / ~54% 分支**（3 次迭代、当前方案）。突破这个天花板可能需要更多迭代或成功修复运行时错误。

5. **反馈机制和对冲**需要更多迭代（5+）和改进后的修复 prompt 才能充分评估。

6. **Advice 质量是双峰分布**：header/iterator advice 成功（5/5 测试通过），trailing delimiter advice 总是失败（0/7 测试通过）。问题不在目标选择，而在行为预测准确度。

7. **弱化 `observable_behavior`（改动 #1）没有解决核心问题**：light advice 不再预测了，但 test-gen LLM 自己填的值也是错的。真正的差距是 advice 模型和 test-gen 模型都不知道实际的运行时行为。

8. **行为对冲（改动 #2）完全无效**——没有任何生成测试表现出对冲行为。指令需要重构或更强力地执行。

## 下一步

1. 使用新的修复 prompt 指南重新运行，测量运行时错误修复率的改善。
2. 考虑将 `maximum_iterations` 增加到 5，给反馈机制更多空间。
3. 调查对冲指令是否真正传达给了 LLM——可能需要重构。
4. 考虑添加"探测运行"机制：对于 trailing delimiter 这样的困难目标，先生成一个只打印实际值的最小测试（如 `System.out.println(record.get(2))`），在写真正的断言之前先发现真实行为。
