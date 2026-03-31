# CSVFormat Threshold100 Run Notes

Date: 2026-03-31
Subject: `Csv-16f / CSVFormat.java`
Run dir: `smoke_test_log/2026-03-31_23:13:11_csvformat-6iter_r1_CSVFormat`
Snapshot dir: `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260331_231327_62717da2`
Config highlights:
- `maximum_iterations = 6`
- `llm_advice_activation_line_coverage = 100`
- `enable_fixing = 3`
- Started from no baseline test file: `CSVFormatTest_before.absent`

---

## 1. Overall Result

Final coverage:
- Line: `55.90%`
- Branch: `42.15%`

Coverage trajectory:

| Iter | Line | Branch | Delta |
|------|------|--------|-------|
| Baseline | 0.00% | 0.00% | — |
| 0 | 28.97% | 15.71% | +28.97 / +15.71 |
| 1 | 34.10% | 22.61% | +5.13 / +6.90 |
| 2 | 38.72% | 26.82% | +4.62 / +4.21 |
| 3 | 45.90% | 33.33% | +7.18 / +6.51 |
| 4 | 54.36% | 39.46% | +8.46 / +6.13 |
| 5 | 55.90% | 42.15% | +1.54 / +2.69 |

Observation:
- 这次不是像 `threshold=30` 那样过早卡死在 full advice。
- 6 轮里 6 轮都在增长，只是增长速度不均匀。
- 由于 threshold=100，这次实际只用了 `light-advice`，没有进入 full advice。

---

## 2. Advice Focus Drift

### 总体结论

advice 的关注点 **有变化**，不是完全锁死，但前中期仍然明显偏向 `equals(...)` 一组相近语义。

这次的变化轨迹可以概括为：

1. `equals` 的基础分支
2. `equals + hashCode + withNullString`
3. 继续围绕上面三类做修复和重试
4. 明显切换到 `withQuoteMode + withHeader + withEscape`
5. 最后一轮又回到 `equals`，但关注的子属性更细（quote/comment/escape）

### 各轮 advice 焦点

#### Iter 1 advice (`003_prompt_builder_llm_light_advice`)

完全聚焦 `equals(final Object obj)`：
- different objects
- null object
- same object

特点：
- 3 个 design 全打在同一个方法上
- 但这轮确实有效，吃掉了 `equals` 最基础的三条分支

#### Iter 2 advice (`005_prompt_builder_llm_light_advice`)

开始扩散到 3 个不同 target：
- `equals(final Object obj)`
- `hashCode()`
- `withNullString(String)`

特点：
- 这是第一次明显脱离“只测 equals”
- 这三个方向都成功转成了通过测试并带来增长

#### Iter 3 advice (`007_prompt_builder_llm_light_advice`)

target 仍然是：
- `equals(final Object obj)`
- `hashCode()`
- `withNullString(String)`

但输入/observable 行为更具体：
- `withNullString("N/A")` 不再只是检查属性设置，而是尝试让格式化输出表现出 null-string 行为

特点：
- 方法级关注点没变
- 但任务描述比 Iter 2 更“行为化”
- 这一轮初始生成全失败，直到 fixing round 3 才全部修好

#### Iter 4 advice (`009_prompt_builder_llm_light_advice`)

出现了最明显的关注点切换：
- `withQuoteMode(QuoteMode)`
- `withHeader(String...)`
- `withEscape(Character)`

特点：
- 这是本次 run 中最重要的一次焦点漂移
- 不再围绕 `equals/hashCode/nullString`
- 直接对应了 Iter 4 最大的一次增量之一：`+8.46% / +6.13%`

#### Iter 5 advice (`011_prompt_builder_llm_light_advice`)

又回到 `equals(final Object obj)`，但切到更细的属性差异：
- different quote modes
- different comment markers
- different escape characters

特点：
- 方法级别看是“回到 equals”
- 但语义上不再是 Iter 1 那种基础 same/null/different-object，而是更细的属性差异分支
- 这一轮仍然带来小幅增长 `+1.54% / +2.69%`

### 小结

1. advice 的关注点确实会随着覆盖区域变化而变化，但变化是“局部扩张”，不是大跳跃式探索。
2. 最大的结构性变化发生在 Iter 4：从 `equals/hashCode/nullString` 转到 `withQuoteMode/withHeader/withEscape`。
3. 即使 threshold=100 避开了 full advice，light-advice 仍然有一定锚定倾向，尤其前 3 轮仍集中在相关方法簇里。

---

## 3. Why Growth Is Slower

### 不是单纯因为文件大，但文件规模确实是因素之一

粗略对比：
- `CSVFormat.java`: `2035` 行，约 `77` 个方法声明，`58` 个唯一方法名
- `CSVParser.java`: `629` 行，约 `18` 个方法声明，`13` 个唯一方法名

所以 `CSVFormat` 明显更大、API 面更广、状态组合更多。

### 但“涨得慢”的更关键原因不是体积，而是类的形态

`CSVFormat` 更像一个 immutable configuration/value object：
- 很多 `withXxx(...)` 方法只是返回新对象
- 很多分支是“属性差异”“空值/默认值/标志位差异”
- 覆盖增长经常来自很多小而碎的比较分支

这会带来两个现象：

1. **前期很容易拿到一大块 guard coverage**
   - line-break delimiter/quote/escape/comment marker
   - duplicate header
   - 所以 Iter 0 很高

2. **后期每个通过测试带来的新增覆盖很碎**
   - 特别是 `equals/hashCode/withXxx` 一类路径
   - 经常是一两个属性差异触发一小段分支
   - 所以虽然“每轮都在涨”，但增长更像持续啃碎片

### 另一个重要原因：修复成本开始主导后半程

Iter 3 和 Iter 4 都出现了明显的 fixing 链条：
- 初始生成质量不够稳定
- 需要多轮 fixing 才把设计转成可运行测试
- 这会拖慢“单位时间”的覆盖收益

所以本次“涨得慢”更像是：
- `CSVFormat` 目标更碎
- test design 到可执行测试的转化难度更高
- 而不是简单一句“因为文件更大”就能解释完

---

## 4. Per-Iteration Execution / Success / Fixing Counts

统计口径：
- `execution attempts`: 日志里每一次产出最终 outcome 的测试运行
- `success`: `Generated test has passed`
- `compile fail`: `Test generated with compilation error`
- `runtime fail`: `Test generated failed due to runtime error`
- `LLM calls`: 该轮生成/修复 prompt 的总调用次数
- `estimated fixing rounds`: `LLM calls - 1`

| Iter | LLM calls | Estimated fixing rounds | Execution attempts | Success | Compile fail | Runtime fail |
|------|-----------|-------------------------|--------------------|---------|--------------|--------------|
| 0 | 1 | 0 | 6 | 6 | 0 | 0 |
| 1 | 1 | 0 | 3 | 3 | 0 | 0 |
| 2 | 1 | 0 | 3 | 3 | 0 | 0 |
| 3 | 4 | 3 | 12 | 3 | 6 | 3 |
| 4 | 4 | 3 | 9 | 4 | 0 | 5 |
| 5 | 1 | 0 | 3 | 3 | 0 | 0 |

Total:
- LLM calls: `12`
- Estimated fixing rounds: `6`
- Execution attempts: `36`
- Passed tests: `22`
- Compile failures: `6`
- Runtime failures: `8`

### Iter 3

这是第一次明显进入“修复主导”阶段：
- advice 仍是 `equals/hashCode/withNullString`
- 初始生成的 3 个测试全部没直接成
- 从统计看共出现 4 次 LLM 调用，约等于 1 次生成 + 3 次 fixing
- 最终 3 个测试都在最后修成并带来 `+7.18% / +6.51%`

### Iter 4

这一轮 advice 换了焦点，但修复压力仍高：
- `withQuoteMode`
- `withHeader`
- `withEscape`

结果：
- 9 次执行尝试
- 4 个通过
- 5 个 runtime failure
- 同样用了约 3 轮 fixing

但最终这一轮仍然是整个 run 中最有价值的一轮之一：
- `+8.46% / +6.13%`

### Iter 5

重新回到 `equals` 后，生成质量反而恢复：
- 1 次 LLM 调用
- 3 个测试都直接通过
- 没有 fixing

说明：
- “后期慢”并不完全是因为 advice 退化
- 也和某些主题本身更适合直接生成、某些主题更依赖 fixing 有关

---

## 5. Current Takeaways

1. `threshold=100` 对 `CSVFormat` 明显比 `threshold=30` 更好。
2. 这次 light-advice 没有完全锁死，关注点确实会变化。
3. 但变化仍偏“近邻扩张”，不是很强的跨区域探索。
4. 覆盖率增长变慢，主因不是单纯文件更大，而是：
   - `CSVFormat` 是大而碎的配置类
   - 每个通过测试带来的新增覆盖更零散
   - Iter 3/4 的 fixing 成本明显提高
5. 当前 full advice 的默认停用判断是合理的；至少在这个类上，light-advice 的表现更健康。

---

## 6. Open Questions For Follow-Up

1. Iter 3/4 的 fixing 里，哪些具体错误是重复出现的？
2. `withHeader` / `withQuoteMode` 这类主题能不能通过更强的 prompt 约束减少 runtime failure？
3. 现在 light-advice 已经能漂移到新方法，是否还需要进一步提高 temperature，还是先只改 prompt diversification 规则？
4. `CSVFormat` 是否适合单独做一个“配置类策略”模板，而不是复用当前通用 light-advice？
