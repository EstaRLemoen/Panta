# 分析：相同 baseline 与相同配置下的三轮重复 3-iter 实验

**日期**: 2026-03-31
**作者**: AI (openai/gpt-5.4)
**对象**: CSVParser (defects4j Csv-16f)
**目的**: 评估在完全相同 baseline 与配置下，3 轮重复运行的覆盖率方差、advice 模式切换行为、修复阶段稳定性，以及 advice feedback 的可观测性

## 实验配置

| 参数 | 值 |
|---|---|
| 模型 | gpt-4o-mini |
| 选择模式 | llm |
| 最大迭代 | 3 |
| 无覆盖增长上限 | 3 |
| Advice 激活阈值 | 50% 行覆盖率 |
| 每轮修复次数 | 3 |
| Light advice 温度 | 0.2 |
| Path advice 温度 | 0.2 |
| baseline 文件 | `smoke_test_log/baselines/CSVParser_baseline_only_test.java` |
| 运行方式 | 每轮都从同一个 baseline 恢复测试文件后重新开始 |

## 数据来源

### 运行目录

| 标签 | 运行目录 |
|---|---|
| R1 | `smoke_test_log/2026-03-31_13:41:10_repeat-baseline-3iter-r1_CSVParser/` |
| R2 | `smoke_test_log/2026-03-31_13:57:19_repeat-baseline-3iter-r2_CSVParser/` |
| R3 | `smoke_test_log/2026-03-31_14:07:45_repeat-baseline-3iter-r3_CSVParser/` |

### CFG snapshot

| 运行 | CFG snapshot |
|---|---|
| R1 | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_134130_fdfbe040/` |
| R2 | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_135734_e49bc130/` |
| R3 | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_140806_c505291b/` |

---

## 总结结论

1. 在完全相同的 baseline 与配置下，3 轮运行的最终覆盖率仍有明显方差：R1 `56.44% / 35.71%`，R2 `63.37% / 55.36%`，R3 `58.42% / 41.07%`。line coverage 最大差异约 7%，branch coverage 最大差异接近 20%。
2. `llm_advice_activation_line_coverage = 50` 让三轮在 advice 模式上出现明显分化：R1 在前两轮一直停留在 light advice，R2 和 R3 则在 Iter 0 就突破阈值，后续都切入 full advice。
3. R2 是三轮中表现最好的一次，但它的优势主要来自 **Iter 0 的一次高收益命中**，不是来自后续 full advice 的持续推进。
4. R3 在 Iter 0 也快速冲到较高覆盖率，但随后两轮 full advice 几乎完全失效，并且 fixing 反复卡在不存在的 `CSVRecord` API（如 `getValues()`、`getRecord()`、`toArray()`）上。
5. R1 的最终追平并不是因为 advice 更强，而是因为它一直停留在 light advice 区间，Iter 2 里修复阶段终于把一个 trailing delimiter 相关断言从 `null` 修到 `""`，带来了一次大幅覆盖率跃升。
6. 这批实验进一步说明：**覆盖率结果高度依赖早期命中的测试主题与修复是否恰好修正关键行为假设**，而不只是依赖“是否启用 full advice”。
7. `previous_advice_feedback.txt` 在 R1 与 R3 的后期 snapshot 中已经能看到，但 R2 没有出现，原因是 R2 在前两轮都有覆盖率增长，没有进入“上一轮 advice 无增益”的反馈场景。

---

## 覆盖率轨迹

### R1

| 迭代 | 模式 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 |
|---|---|---|---|---|---|
| baseline | - | - | 2.97% | - | 0.0% |
| 0 | Light advice | +22.77% | 25.74% | +3.57% | 3.57% |
| 1 | Light advice | 0% | 25.74% | 0% | 3.57% |
| 2 | Light advice + feedback | +30.7% | 56.44% | +32.14% | 35.71% |

### R2

| 迭代 | 模式 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 |
|---|---|---|---|---|---|
| baseline | - | - | 2.97% | - | 0.0% |
| 0 | Light advice | +59.41% | 62.38% | +50.0% | 50.0% |
| 1 | Full advice | +0.99% | 63.37% | +5.36% | 55.36% |
| 2 | Full advice | 0% | 63.37% | 0% | 55.36% |

### R3

| 迭代 | 模式 | 行覆盖增量 | 行覆盖累计 | 分支增量 | 分支累计 |
|---|---|---|---|---|---|
| baseline | - | - | 2.97% | - | 0.0% |
| 0 | Light advice | +55.45% | 58.42% | +41.07% | 41.07% |
| 1 | Full advice | 0% | 58.42% | 0% | 41.07% |
| 2 | Full advice + feedback | 0% | 58.42% | 0% | 41.07% |

### 轨迹解读

1. 三轮的分化几乎都在 Iter 0 就发生了。R2 和 R3 在第一轮就跨过 50% 阈值，R1 则没有。
2. R2 的最终领先基本已经在 Iter 0 决定，后续只多拿了 `+0.99% line / +5.36% branch`。
3. R1 的轨迹最“后发制人”：前两轮进展很慢，但 Iter 2 突然追回一大段覆盖率。
4. R3 的轨迹最像“高开低走”：第一轮很好，后两轮完全停滞。

---

## Advice 模式分化

### 50% 阈值的实际效果

| 运行 | Iter 0 后覆盖率 | Iter 1 模式 | Iter 2 模式 | 结果 |
|---|---|---|---|---|
| R1 | 25.74% | Light | Light + feedback | 一直未进入 full advice |
| R2 | 62.38% | Full | Full | 从 Iter 1 起进入 full advice |
| R3 | 58.42% | Full | Full + feedback | 从 Iter 1 起进入 full advice |

这意味着这批实验实际上不是“同一种 advice 流程的三次重复”，而是：

1. R1 主要评估了 **light advice 连续三轮** 的表现。
2. R2 与 R3 主要评估了 **light advice 一轮命中后快速切 full advice** 的表现。

因此，三轮的最终差异不能只理解为随机波动，也包含了 **Iter 0 是否足够高效，从而改变后续策略分支** 这一结构性差异。

### Light advice 与 Full advice 的观察

1. R2 和 R3 进入 full advice 后，并没有出现稳定优于 light advice 的趋势。
2. R2 的 full advice 只带来一次很小的增量；R3 的 full advice 则完全没有收益。
3. R1 反而是在 light advice 路径上，靠修复阶段把一个原本错误的 trailing delimiter 断言修正后，取得了最大的一次后续增量。

**这一批数据不支持“只要更早进入 full advice 就更好”这个结论。**

---

## 测试结果统计

### R1

| 迭代 | 模式 | 生成测试数 | 总构建次数 | 编译错误 | 运行时错误 | 通过 | 通过率 |
|---|---|---:|---:|---:|---:|---:|---:|
| 0 | Light | 3 | 3 | 0 | 2 | 1 | 33.3% |
| 1 | Light | 3 | 5 | 4 | 0 | 1 | 20.0% |
| 2 | Light + feedback | 3 | 7 | 0 | 5 | 2 | 28.6% |
| 合计 | - | 9 | 15 | 4 | 7 | 4 | 26.7% |

### R2

| 迭代 | 模式 | 生成测试数 | 总构建次数 | 编译错误 | 运行时错误 | 通过 | 通过率 |
|---|---|---:|---:|---:|---:|---:|---:|
| 0 | Light | 3 | 5 | 0 | 4 | 1 | 20.0% |
| 1 | Full | 2 | 3 | 0 | 1 | 2 | 66.7% |
| 2 | Full | 1 | 1 | 0 | 0 | 1 | 100% |
| 合计 | - | 6 | 9 | 0 | 5 | 4 | 44.4% |

### R3

| 迭代 | 模式 | 生成测试数 | 总构建次数 | 编译错误 | 运行时错误 | 通过 | 通过率 |
|---|---|---:|---:|---:|---:|---:|---:|
| 0 | Light | 3 | 5 | 2 | 2 | 1 | 20.0% |
| 1 | Full | 2 | 6 | 6 | 0 | 0 | 0% |
| 2 | Full + feedback | 2 | 6 | 6 | 0 | 0 | 0% |
| 合计 | - | 7 | 17 | 14 | 2 | 1 | 5.9% |

### 汇总

| 指标 | R1 | R2 | R3 |
|---|---:|---:|---:|
| 总构建次数 | 15 | 9 | 17 |
| 编译错误 | 4 | 0 | 14 |
| 运行时错误 | 7 | 5 | 2 |
| 通过 | 4 | 4 | 1 |
| 最终 line coverage | 56.44% | 63.37% | 58.42% |
| 最终 branch coverage | 35.71% | 55.36% | 41.07% |

### 统计解读

1. R2 的总体通过率最高，但这并不代表它最稳定，而是代表它更早命中了高收益测试。
2. R3 的问题非常集中：不是“行为判断错很多”，而是 **fixing 后两轮几乎全部退化成编译错误**。
3. R1 的错误形态更均衡，既有 runtime error，也有少量 compilation error，但修复阶段最终确实救活了两个测试。

---

## 通过的测试详情

| 运行 | 迭代 | 测试名 | 通过方式 | 说明 |
|---|---|---|---|---|
| R1 | 0 | `testClosedParserHasNext` | 直接通过 | 关闭 parser 后 `hasNext()` 返回 false |
| R1 | 1 | `testClosedParserIterator` | 直接通过 | 关闭 parser 后 `iterator.next()` 抛 `NoSuchElementException` |
| R1 | 2 | `testClosedParserDuringIteration` | fix round 1 | 同样是 closed-parser 路径，但修复后断言形式更贴合实现 |
| R1 | 2 | `testEmptyRecordWithTrailingDelimiter` | fix round 3 | 关键变化是把 trailing delimiter 的空值从 `null` 修成 `""` |
| R2 | 0 | `testInitializeHeaderWithEmptyHeader` | fix round 1 | 加上 `.withHeader()` 后真正触发 header 初始化逻辑 |
| R2 | 1 | `testGetRecordsWithTrailingDelimiter` | 直接通过 | full advice 成功命中“trailing delimiter=true 时不追加末尾空值”的 record size 行为 |
| R2 | 1 | `testGetRecordsWithoutTrailingDelimiter` | fix round 1 | 把输入从 `a,b,\n` 改成 `a,b\n` 后通过 |
| R2 | 2 | `testParseCSVWithDuplicateHeader` | 直接通过 | duplicate header 通过 `withHeader("id", "name", "id")` 正确触发 |
| R3 | 0 | `testEmptyHeaderHandling` | fix round 2 | 加上 `.withHeader()` 后 empty header 才真正进入 headerMap 路径 |

### 通过案例的共同点

1. 真正高收益的成功，大多来自 **正确触发目标逻辑**，而不是来自更复杂的断言。
2. 与 header 初始化相关的测试，只要 `.withHeader()` 之类的前提配置正确，经常就能稳定通过。
3. trailing delimiter 不是绝对做不成，但它要求模型同时把握：输入格式、`withTrailingDelimiter(...)` 配置，以及空值究竟是“缺失”还是 `""`。

---

## 失败模式分析

### 1. Trailing delimiter 仍然是最不稳定的主题

三轮里，trailing delimiter 一直高频出现，但表现极不稳定：

1. R1 在前两轮一直把空值当成 `null`，直到 Iter 2 的 fix round 3 才修到 `""`。
2. R2 在 Iter 1 成功命中了一个更保守的版本：只断言 record size 为 2，而不去断言第三个值必须存在。
3. R3 则在 full advice 阶段持续把 trailing delimiter 问题写成不存在的 `CSVRecord` API 调用，甚至在修复轮里越改越错。

这说明这里的主要难点不只是“行为预测错”，还包括：

1. 模型容易在 `null`、`""`、缺列、空 record 之间来回混淆。
2. 一旦进入 fixing，模型可能先被 API 形式问题绊住，根本修不到行为层。

### 2. R3 的核心失败是 API 幻觉死循环

R3 Iter 1 与 Iter 2 的 full advice 基本锁死在 trailing delimiter 上，随后 fixing 连续尝试了多种并不存在的 API：

1. `records.get(0).values().toArray()`
2. `records.get(0).toArray()`
3. `records.get(0).getValues().toArray(new String[0])`
4. `records.get(0).getRecord()`

这类错误有两个特点：

1. 它们不是“接近正确但差一点”，而是对 `CSVRecord` 可用接口的根本误解。
2. fixing 没有把它收敛到已知可行形式，反而在多个不存在的方法之间震荡。

### 3. Header 相关测试成功率高于 trailing delimiter

这批实验里，header 相关方向总体比 trailing delimiter 稳定得多，尤其是：

1. `.withHeader()` 一旦补上，很多路径就能立刻触发。
2. 断言通常只需要 `headerMap` 非空、size 正确、键名映射正确，难度比精确预测 record 尾部值低。

---

## 修复阶段分析

### R1：修复阶段最有价值

R1 的修复阶段虽然不算高效，但最终起到了决定性作用：

1. Iter 2 中 `testEmptyRecordWithTrailingDelimiter` 在 fix round 3 成功把断言从 `assertNull(...)` 修到 `assertEquals("", ...)`。
2. 这个修正不只是救活了一个测试，还直接带来了 `+30.7% line / +32.14% branch` 的覆盖率提升。

这说明：在某些情况下，只要 fixing 真的修到了 **行为语义**，回报会非常大。

### R2：修复阶段精准但机会不多

R2 的修复阶段主要体现为“少量但高价值”的纠正：

1. Iter 0 中 `testInitializeHeaderWithEmptyHeader` 在 fix round 1 补上 `.withHeader()` 后通过。
2. Iter 1 中 `testGetRecordsWithoutTrailingDelimiter` 在 fix round 1 调整输入后通过。

R2 的特点不是修复轮很多，而是它更早进入了正确的问题表述，因此后续 full advice 即使收益有限，也没有把整个 run 拖垮。

### R3：修复阶段几乎崩溃

R3 的 full advice 后两轮都没有产生任何通过测试，且几乎全部变成 compilation error。最重要的结论是：

1. fixing 没有把错误从“编译问题”修回“可运行但断言错误”的状态。
2. 一旦进入 API 幻觉循环，三轮修复几乎没有提供纠偏能力。

这批数据说明 fixing 目前有明显的双峰：

1. 对“补一个 format 前提”这类低阶修正，它有时有效。
2. 对“错误 API + 错误行为预测”叠加的情况，它经常彻底失效。

---

## Advice Feedback 的可见性

### 观察结果

1. R1 的 snapshot 中存在 `previous_advice_feedback.txt`：
   `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_134130_fdfbe040/006_prompt_builder_llm_light_advice/previous_advice_feedback.txt`
2. R3 的 snapshot 中也存在对应文件：
   `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_140806_c505291b/006_prompt_builder_llm_advice/previous_advice_feedback.txt`
3. R2 的 snapshot 中没有找到该文件。

### 能从文件中确认什么

R1 的 feedback 明确写出：

1. 上一轮 advice 没有带来覆盖率增长。
2. Advice focus 是 trailing delimiter。
3. 结果统计里记录了 `2 compilation errors, 0 runtime errors, 0 timeouts`。
4. 提示模型“runtime errors 往往意味着 observable_behavior 预测不准，或者没有真正到达目标路径”。

R3 的 feedback 也明确写出：

1. 上一轮 advice focus 是 `Test handling of empty records with trailing delimiters.`
2. 结果统计里记录了 `2 compilation errors, 0 runtime errors, 0 timeouts`。
3. 关键编译错误直接指向 `CSVRecord.getRecord()` 不存在。

### 这批实验里 feedback 的意义

1. R1 中，feedback 至少在可见性上已经落盘，而且发生在 light advice 路径里。
2. R3 中，feedback 同样落盘，但从结果看它没能阻止模型继续围绕 trailing delimiter 和错误 API 打转。
3. R2 没有 feedback 文件，不是机制没工作，而是前两轮都发生了覆盖率增长，没有进入“上一轮无增益”的触发条件。

因此，这批实验更适合支持下面这个结论：

**feedback 机制已经具备基本可观测性，但它是否能稳定改变 advice 方向，当前证据仍然偏弱。**

---

## 与早期重复实验的对比

与 `test_analysis/2026-03-31_r1_vs_r2_repeated_experiment.md` 中较早的重复实验相比，这一批有几个关键差异：

1. Advice 激活阈值提高到了 50%，因此是否在 Iter 0 快速冲高，直接决定后续是继续 light 还是切 full。
2. `previous_advice_feedback.txt` 现在已经可以在部分 snapshot 中直接看到，链路可见性明显比早期更好。
3. 早期分析里 trailing delimiter 几乎总是失败；这批里 R1 与 R2 各自至少有一次在该主题上的局部成功，但成功路径不同：
   - R1 依赖 fixing 把 `null` 修成 `""`
   - R2 依赖更保守的断言策略，只检查 record size
4. 这说明问题并不是“trailing delimiter 永远不可做”，而是 **只要测试把断言写得稍微保守一些，或修复真正纠正了语义假设，就有机会成功**。

---

## 最终判断

1. 这三轮实验再次证明了重复运行方差确实存在，而且不小。
2. 方差的核心来源不是单纯随机，而是 **Iter 0 是否足够成功，从而改变后续 advice 路径**。
3. 在这批数据里，full advice 没有表现出稳定优于 light advice 的证据。
4. 修复阶段仍然是决定成败的关键变量，但它目前非常不稳定：有时能修正关键行为假设，有时会彻底陷入 API 幻觉循环。
5. trailing delimiter 仍然是最能暴露系统弱点的主题，因为它同时要求模型处理输入形态、format 配置、record 长度和空值语义。
6. advice feedback 现在已经可观测，但其效果还没有被这批三轮实验充分证明；R1 有一定正面信号，R3 则显示它不足以自动纠偏严重的 API 幻觉。

## 下一步建议

1. 继续做重复实验时，单独记录“Iter 0 是否跨过 advice 阈值”，因为这已经是后续策略分化的关键因子。
2. 对 trailing delimiter 主题，优先鼓励生成更保守的断言，例如先断言 record 数量、record size，再逐步细化空值语义。
3. 对 fixing 阶段增加对已知不存在 API 的约束，避免 `getValues()`、`getRecord()` 这类幻觉反复出现。
4. 如果后续要评估 feedback 机制，建议把 `maximum_iterations` 提高到 5，让“无增长 -> feedback -> 改向”有更多出现机会。
