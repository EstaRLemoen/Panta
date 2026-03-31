# 分析：threshold 70 vs 100 实验（fix=2）

**日期**: 2026-03-31
**对象**: CSVParser (defects4j Csv-16f)
**目的**: 在 `enable_fixing=2` 条件下，对比 `llm_advice_activation_line_coverage=70` 与 `100` 的表现差异，并评估降低 fixing 轮次对最终覆盖率的影响

## 实验配置

| 参数 | R1 | R2 |
|---|---|---|
| 模型 | gpt-4o-mini | gpt-4o-mini |
| 最大迭代 | 5 | 5 |
| 无覆盖增长上限 | 3 | 3 |
| 修复次数 | **2** | **2** |
| `llm_advice_activation_line_coverage` | **70** | **100** |
| baseline | `smoke_test_log/baselines/CSVParser_baseline_only_test.java` |

说明：threshold=70 意味着在行覆盖率超过 70% 之前都会触发 advice；threshold=100 意味着全程 advice（因为几乎不可能达到 100%）。两者的区别实质是"覆盖率高了以后是否仍然激活 advice"，但在 CSVParser 当前场景下（baseline 仅 2.97%），两者在前期行为差异不大。

## 数据来源

| 标签 | 运行目录 |
|---|---|
| R1 | `smoke_test_log/2026-03-31_16:55:57_repeat-baseline-5iter-fix2-threshold70-vs-100_r1_CSVParser/` |
| R2 | `smoke_test_log/2026-03-31_17:12:01_repeat-baseline-5iter-fix2-threshold70-vs-100_r2_CSVParser/` |

---

## 总结结论

1. **R1 完全失败**：3 个 iteration 全部 0 增长，覆盖率停在 baseline 2.97% / 0.0%。
2. **R2 部分成功**：Iter 0 跳到 47.52% / 33.93%，但后续 3 个 iteration 全部停滞。
3. **与之前 fix=3 的实验相比，结果明显退化**。之前 fix=3 的三轮实验（threshold=50）最终覆盖率分别是 56.44%、63.37%、58.42%，而本次 fix=2 最好只有 47.52%。
4. **核心原因不是 threshold 差异，而是 fixing 轮次不够**。关键的高价值修复经常需要 round 3 才能到位，砍掉 round 3 等于砍掉了这些机会。
5. **Fixing prompt 不传递修复历史**：每一轮 fixing 只看到当前失败测试的最新代码和错误信息，不知道之前改了什么。这使得 fix=2 相比 fix=3 不只是少了一次机会，还缺少了"第二轮失败 → 第三轮换方向"的间接纠偏。
6. 两轮的 light advice 质量本身没有明显退步，design 方向与之前的实验类似。问题出在 generation 和 fixing 阶段无法把 advice 转化为通过测试。

---

## 覆盖率轨迹

### R1 (threshold=70)

| 迭代 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 | 测试通过 |
|---|---|---|---|---|---|
| baseline | - | 2.97% | - | 0.0% | - |
| 0 | 0% | 2.97% | 0% | 0.0% | 0/3 gen, 0/6 fix |
| 1 | 0% | 2.97% | 0% | 0.0% | 0/3 gen, 0/6 fix |
| 2 | 0% | 2.97% | 0% | 0.0% | 0/3 gen, 0/6 fix |

最终停在 iter 2（连续 3 次 no-coverage-increase 触发停机）。

### R2 (threshold=100)

| 迭代 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 | 测试通过 |
|---|---|---|---|---|---|
| baseline | - | 2.97% | - | 0.0% | - |
| 0 | **+44.55%** | 47.52% | **+33.93%** | 33.93% | 1 passed (gen) |
| 1 | 0% | 47.52% | 0% | 33.93% | 0 |
| 2 | 0% | 47.52% | 0% | 33.93% | 1 passed (gen, 无覆盖增量) |
| 3 | 0% | 47.52% | 0% | 33.93% | 0 |

最终停在 iter 3（连续 3 次 no-coverage-increase 触发停机）。

---

## 测试结果详情

### R1

| 迭代 | 生成测试 | Gen 结果 | Fixing 结果 |
|---|---|---|---|
| 0 | `testCSVParserWithTrailingDelimiter` | runtime error | runtime × 2 |
| 0 | `testCSVParserWithCaseInsensitiveHeader` | runtime error | runtime × 2 |
| 0 | `testCSVParserWithEmptyHeader` | runtime error | comp × 2, comp × 2 |
| 1 | `testCSVParserWithTrailingDelimiter` | runtime error | runtime × 2 |
| 1 | `testCSVParserWithCaseInsensitiveHeader` | runtime error | runtime × 2 |
| 1 | `testCSVParserWithEmptyHeader` | runtime error | runtime × 2 |
| 2 | `testCSVParserWithTrailingDelimiter` | runtime error | runtime × 2 |
| 2 | `testCSVParserWithCaseInsensitiveHeader` | runtime error | runtime × 2 |
| 2 | `testCSVParserWithEmptyHeader` | runtime error | runtime × 2 |

**共 9 个生成测试，0 通过。** 每个 gen + 2 轮 fix 全部失败。

### R2

| 迭代 | 生成测试 | Gen 结果 | Fixing 结果 |
|---|---|---|---|
| 0 | `testTrailingDelimiterHandling` | **passed** | - |
| 0 | `testHeaderInitializationWithDuplicateNames` | runtime error | runtime × 2 |
| 0 | `testIgnoreHeaderCaseFunctionality` | runtime error | runtime × 2 |
| 1 | `testCSVParserWithEmptyInputAndTrailingDelimiter` | runtime error | runtime × 2 |
| 1 | `testCSVParserWithDuplicateHeaders` | runtime error | runtime × 2 |
| 1 | `testCSVParserWithCaseInsensitiveHeader` | runtime error | comp × 2 |
| 2 | `testTrailingDelimiterWithEmptyRecord` | **passed** | - |
| 2 | `testCaseInsensitiveHeader` | runtime error | runtime × 2 |
| 2 | `testDuplicateHeaders` | runtime error | runtime × 2 |
| 3 | `testCSVParserWithEmptyInputAndTrailingDelimiter` | runtime error | comp × 2 |
| 3 | `testCSVParserWithDuplicateHeaders` | runtime error | comp × 2 |
| 3 | `testCSVParserWithCaseInsensitiveHeader` | runtime error | runtime × 2 |

**共 12 个生成测试，2 通过（但 Iter 2 的 pass 没有带来新增覆盖）。**

---

## R1 为什么完全失败

R1 生成的测试与之前实验的主题完全一致（trailing delimiter / case-insensitive header / empty header），但每个测试的 generation 阶段都断言错误：

- `testCSVParserWithTrailingDelimiter`：反复断言 `null` 或 `assertArrayEquals(new String[]{"a", "b", "c"}, ...)` 而非 `""` / 包含空字段
- `testCSVParserWithCaseInsensitiveHeader`：对 `headerMap` 的 size 和 key 的预期与实际行为不符
- `testCSVParserWithEmptyHeader`：断言 `headerMap == null` 但实际不为 null

2 轮 fixing 无法修正这些行为层面的误判。在之前的 fix=3 实验中，`testEmptyRecordWithTrailingDelimiter` 在 **fix round 3** 才把 `null` 改成 `""`，直接带来 +30.7% 覆盖率。R1 没有这个机会。

## R2 为什么止步 47.52%

R2 Iter 0 的 `testTrailingDelimiterHandling` 断言了 `""` (空字符串)，恰好与实际行为吻合，直接通过并带来 +44.55% 覆盖率。这是一个 LLM 随机命中，与 threshold 配置无关。

后续 iteration 的失败模式与 R1 相同：advice 方向虽然换了（加入了 duplicate headers 等），但 test-gen 仍然无法精确预测行为，2 轮 fixing 不足以纠正。

Iter 2 的 `testTrailingDelimiterWithEmptyRecord` 虽然通过了，但它覆盖的代码路径与 Iter 0 的测试重叠，没有带来增量覆盖。

---

## 与之前 fix=3 实验的对比

| 实验 | Fix 轮次 | Threshold | R1 最终 cov | R2 最终 cov | R3 最终 cov |
|---|---|---|---|---|---|
| 3-iter baseline | 3 | 50 | 56.44% / 35.71% | 63.37% / 55.36% | 58.42% / 41.07% |
| overnight 11-iter | 3 | 30 | 56.44% / 39.29% | 23.76% / 3.57% | 69.31% / 60.71% |
| **本次 5-iter** | **2** | 70/100 | **2.97% / 0.0%** | **47.52% / 33.93%** | - |

关键对比：

1. 之前 fix=3 的最差一轮（R2 overnight，23.76%）是因为 advice 方向反复锁死在 trailing delimiter，与 fixing 无关。其余 fix=3 实验的最终覆盖率都在 56% 以上。
2. 本次 R1（fix=2）只有 2.97%，这在之前所有实验中从未出现过。
3. 本次 R2（fix=2）的 47.52% 也低于之前 fix=3 实验的所有成功运行。

**Fix=2 相比 fix=3 的退化是明确的。**

---

## Fixing 机制分析

### 当前 fixing 的状态传递方式

通过代码审查确认：

1. `build_prompt_for_fixing()` 在构建 prompt 时会把 `self.failed_test_runs` 序列化为文本
2. **构建完后立即清空** `self.failed_test_runs = []`（`unit_test_generator.py:919`）
3. `validate_test()` 对修复后的测试重新验证，如果仍然失败，会把**新的失败信息**加回 `failed_test_runs`
4. 下一轮 fixing 只看到"上一轮修复后的代码 + 当前错误"，**不知道这是第几轮修复，也不知道之前改了什么**

这意味着：
- fix round 1 看到的是 generation 的原始失败
- fix round 2 看到的是 round 1 的修复尝试及其新的失败
- fix round 3（如果有的话）看到的是 round 2 的修复尝试及其新的失败

**每一轮 fixing 都缺乏"我已经试过 A 和 B 了"的累积上下文。** LLM 只看到一个失败测试和当前的错误，容易在同一个方向上反复尝试。

### 为什么 fix=3 比 fix=2 明显好

尽管 fixing 不传递历史，round 3 仍然有价值，因为：

1. **LLM 的随机性**：即使 prompt 相似，LLM 每次生成的修复代码不同。多一轮就多一次"换方向"的机会。
2. **错误信息的变化**：round 1 改了代码后，round 2 的错误信息可能不同，引导 LLM 走向不同修复方向。round 3 又在此基础上多了一次机会。
3. **之前实验的具体证据**：`testEmptyRecordWithTrailingDelimiter` 在 fix round 3 才把 `null` → `""`，带来 +30.7% 覆盖率；`testParseWithDuplicateHeaders` 在 fix round 3 才加 `.withHeader()` 触发条件。

---

## 最终判断

1. **`enable_fixing=2` 不可取**。之前所有成功实验都用 fix=3，fix=2 明显降低了关键修复机会。
2. **threshold=70 vs 100 在本次实验中没有产生有意义的差异**，因为 CSVParser 的 baseline 太低（2.97%），两种 threshold 在前期行为相同。
3. **fixing 缺乏历史传递**是一个结构性问题，但在当前阶段 fix=3 是最低可接受配置。
4. **建议后续实验统一使用 `enable_fixing=3`**，同时可以考虑在 fixing prompt 里加入修复历史摘要来提高修复效率。
