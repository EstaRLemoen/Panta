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

---

## 7. Follow-Up Runs From The 55.90% Baseline

在第一次 `threshold=100` 的 6-iter run 结束后，我们又做了两次 continuation：

1. `continue-after-6iter`：从第二次 continuation 的 `after` 往后继续 6 轮
2. `continue-first-after-4iter`：重新从第一次 6-iter run 的 `after` 出发，再跑 4 轮

为了避免混淆，这里统一把三次 run 记为：

- **Run A**: `2026-03-31_23:13:11_csvformat-6iter_r1_CSVFormat`
- **Run B**: `2026-03-31_23:47:07_csvformat-continue-after-6iter_r1_CSVFormat`
- **Run C**: `2026-04-01_00:42:38_csvformat-continue-first-after-4iter_r1_CSVFormat`

### Coverage summary

| Run | Start | End | Delta |
|-----|-------|-----|-------|
| A | `0.00 / 0.00` | `55.90 / 42.15` | `+55.90 / +42.15` |
| B | `55.90 / 42.15` | `63.59 / 52.11` | `+7.69 / +9.96` |
| C | `55.90 / 42.15` | `64.36 / 51.72` | `+8.46 / +9.57` |

结论：
- 从 `55.90 / 42.15` 这个 baseline 出发，覆盖率**还没涨到头**。
- Run B 和 Run C 都能继续推高 coverage。
- Run C 的 line coverage 更高，Run B 的 branch coverage 略高。

---

## 8. Advice Evolution Across The Three Runs

### Run B: 第二次 continuation（从 55.90 开始再跑 6 轮）

总体轨迹：
- Iter 0 仍然锚在 `equals(...)`
- Iter 1 开始出现 `getHeaderComments()`、`isEscapeCharacterSet()`
- Iter 2 扩到 `withNullString()`、`withCommentMarker()`
- Iter 3/4 又回到 `equals/hashCode/withIgnoreHeaderCase`
- Iter 5 后期收益明显变小

特点：
- 仍然有锚定，但已经能慢慢扩到新的 getter/config 相关路径。
- 这次更像“沿着现有属性簇缓慢外扩”。

### Run C: 第三次 continuation（从同一个 55.90 baseline 出发，改了 light-advice 数量规则后跑 4 轮）

这次一个明显变化是：**每轮都稳定产出 4 个 designs**。

按 snapshot 统计：

| Iter | Designs | Targets |
|------|---------|---------|
| 0 | 4 | 4 个都落在 `equals(...)` |
| 1 | 4 | 3 个 `equals(...)` + 1 个 `getHeaderComments()` |
| 2 | 4 | `equals` + `withRecordSeparator` + `withNullString` + `withIgnoreEmptyLines` |
| 3 | 4 | 3 个 `equals(...)` + 1 个 `print(...)` |

说明：
- 改完模板后，light-advice 已经不再被 `2-3` 个 design 上限卡住。
- 但“避免同一行为簇重复”这个规则只部分生效：
  - Iter 2 的探索最健康，4 个 design 明显分散。
  - Iter 0 / 1 / 3 仍然存在 3 个 design 都围着 `equals(...)` 的现象。

### Run C 的新增有效方向

相较于 Run B，Run C 里更明显被打开的方向包括：
- `getHeaderComments()`
- `withRecordSeparator(...)`
- `withIgnoreEmptyLines(...)`
- `withNullString(...)` 的 formatting 路径

其中最有价值的一轮是 Iter 2：
- 直接带来 `+5.13 line / +4.98 branch`
- 成功测试包括：
  - `testCSVFormatWithRecordSeparator`
  - `testCSVFormatIgnoringEmptyLines`
  - `testCSVFormatWithNullStringUpdated`

这说明“放宽 design 数量 + 避免同簇重复”这个方向是有收益的。

---

## 9. Is Fixing Worse In Run C Than In Run B?

### 结论

**不是全面更差，但 Run C 在 Iter 3 的 fixing 明显更差。**

也就是说：
- Run C 前 0-2 轮的 fixing 并不比 Run B 差，甚至有些轮次更有效。
- 但 Run C 的最后一轮（Iter 3）失败密度明显更高，compile/runtime error 一起上来，最终没有带来覆盖增长。

### Run B（前 0-3 轮）

| Iter | LLM calls | Attempts | Passed | Compile | Runtime | Coverage delta |
|------|-----------|----------|--------|---------|---------|----------------|
| 0 | 4 | 9 | 1 | 4 | 4 | `+0.00 / +0.00` |
| 1 | 4 | 6 | 2 | 0 | 4 | `+1.79 / +2.29` |
| 2 | 3 | 4 | 2 | 0 | 2 | `+4.10 / +5.37` |
| 3 | 3 | 6 | 3 | 3 | 0 | `+1.29 / +1.91` |

观察：
- Run B 的 fixing 问题偏向 **runtime failure**，尤其 Iter 0-2。
- 到 Iter 3，compile failure 增加，但整体还能把 3 个测试修成并带来增长。

### Run C（前 0-3 轮）

| Iter | LLM calls | Attempts | Passed | Compile | Runtime | Coverage delta |
|------|-----------|----------|--------|---------|---------|----------------|
| 0 | 4 | 9 | 3 | 2 | 4 | `+1.28 / +1.91` |
| 1 | 3 | 7 | 3 | 4 | 0 | `+2.05 / +2.68` |
| 2 | 4 | 7 | 3 | 4 | 0 | `+5.13 / +4.98` |
| 3 | 4 | 16 | 2 | 9 | 5 | `+0.00 / +0.00` |

观察：
- Run C 的 Iter 0-2 其实不差：
  - 成功数都达到 3
  - Iter 2 还是本次最强的一轮
- 但 Iter 3 明显崩了：
  - `16` 次 attempts
  - `9` 个 compilation error
  - `5` 个 runtime error
  - 最终只修成 2 个测试，而且没带来 coverage increase

### 为什么会觉得“现在的 fixing 更差”

因为 Run C 的失败集中出现在最后一轮，而且很刺眼：

1. **compile failure 数量非常高**
   - 相比 Run B 前 0-3 轮，Run C Iter 3 的 compile fail 是最高的。

2. **这一轮 advice 本身更复杂**
   - 不只是 `equals(...)`
   - 还混入了 `print(...)` 这类需要更多上下文和 imports 的路径
   - 生成难度和修复难度都更高

3. **大部分失败没有被 fixing 真正消掉**
   - 最终只有 nullString/commentMarker 两条被修成
   - quoteMode/print 相关路径还是没站稳

### 更准确的判断

如果只看“前几轮 fixing 的总体表现”：
- **Run C 不比 Run B 差**
- 它甚至更有效率地把一些新方向（record separator / ignore empty lines / header comments）转成了通过测试

如果看“最后一轮 fixing 的稳定性”：
- **Run C 明显更差**
- 说明随着 design 更分散、路径更复杂，当前 fixing 模块开始扛不住了

也就是说，问题不是“新的 light-advice 改法整体更差”，而是：
- **探索宽度上去了**
- **但 fixing 对更复杂 design 的承接能力不够**

这和我们前面的判断是一致的：
- 当前 fixing 更擅长修简单 `equals/hashCode` 型测试
- 一旦进入 `print/parse/config-combination` 这类稍复杂路径，compile/runtime 错误会明显增多

---

## 10. Updated Takeaways

1. 从 `55.90 / 42.15` 出发，`CSVFormat` 还有进一步上涨空间。
2. light-advice 放宽 design 数量后，探索面确实变宽了。
3. 当前模板改动的收益主要体现在：
   - 更容易碰到新的 public behavior cluster
   - 不再完全被 `equals(...)` 单线锁死
4. 但 fixing 现在成了更明显的瓶颈：
   - 对简单 equality-style 测试还行
   - 对更复杂的 printing/parsing/config-combination 测试承接能力不足
5. 因此下一阶段如果继续优化，优先级应当是：
   - 先提升 fixing 的结构化修复能力
   - 再继续放宽 light-advice 的探索面

---

## 11. Template / Temperature Comparison From The Same 55.90% Baseline

这一段专门记录从同一个 baseline

- Start coverage: `55.90% line / 42.15% branch`
- Baseline file: `smoke_test_log/2026-03-31_23:13:11_csvformat-6iter_r1_CSVFormat/backup/CSVFormatTest_after.java`

出发做的对比试验。

### C run: cluster-breadth light-advice variant

Run:
- `2026-04-01_00:42:38_csvformat-continue-first-after-4iter_r1_CSVFormat`

Prompt/config changes relative to the original threshold100 run:
- `light-advice` 模板改动位于
  - `src/panta/prompt_templates/java_templates/test_generation_llm_light_advice_selection_prompt.toml`
- 规则文本的关键变化是：

Original:
```text
2. Prefer returning 2 or 3 candidate `test_designs` when the source file offers multiple plausible uncovered behaviors.
```

Variant:
```text
2. Return a small set of candidate `test_designs` that covers different plausible uncovered behavior clusters. For larger files with multiple plausible clusters, you may return more than 3 designs.
3. Avoid near-duplicate designs that exercise the same behavior cluster with only minor input variations. Prefer breadth across different public entry paths, target methods, or observable behaviors.
```

- 也就是从“数量偏好”改成了“行为簇覆盖 + 去重约束”
- 其余关键参数保持：
  - `llm_advice_activation_line_coverage = 100`
  - `llm_light_advice_temperature = 0.2`
  - `maximum_iterations = 4`

Result:
- End coverage: `64.36% / 51.72%`
- Delta: `+8.46 line / +9.57 branch`

Observed behavior:
- 每轮稳定产出 `4` 个 designs
- advice 开始更频繁地扩到：
  - `getHeaderComments()`
  - `withRecordSeparator(...)`
  - `withIgnoreEmptyLines(...)`
  - `print(...)`
- fixing 压力明显上升，但探索宽度也明显更大

### D run: original light-advice template + higher temperature

Run:
- `2026-04-01_01:21:46_csvformat-continue-first-after-4iter-temp04_r1_CSVFormat`

Prompt/config changes relative to the original threshold100 run:
- `light-advice` 模板恢复为原版：

```text
2. Prefer returning 2 or 3 candidate `test_designs` when the source file offers multiple plausible uncovered behaviors.
```

- 不再包含下列 variant 规则：

```text
2. Return a small set of candidate `test_designs` that covers different plausible uncovered behavior clusters. For larger files with multiple plausible clusters, you may return more than 3 designs.
3. Avoid near-duplicate designs that exercise the same behavior cluster with only minor input variations. Prefer breadth across different public entry paths, target methods, or observable behaviors.
```

- 温度提高：
  - `llm_light_advice_temperature = 0.4`
- 其余关键参数保持：
  - `llm_advice_activation_line_coverage = 100`
  - `maximum_iterations = 4`

Result:
- End coverage: `58.97% / 46.74%`
- Delta: `+3.07 line / +4.59 branch`

Observed behavior:
- 每轮大多仍是 `3` 个 designs
- focus 仍然主要围绕：
  - `equals(...)`
  - `getHeaderComments()`
  - `isNullStringSet()` / `isCommentMarkerSet()`
- 0.4 温度带来一些表述扰动，但没有稳定扩大探索宽度

### Side-by-side comparison

| Run | Template | Temp | Typical design count | End coverage | Delta |
|-----|----------|------|----------------------|--------------|-------|
| C | cluster-breadth variant | 0.2 | 4 | `64.36 / 51.72` | `+8.46 / +9.57` |
| D | original template | 0.4 | 3 | `58.97 / 46.74` | `+3.07 / +4.59` |

### What this comparison suggests

1. 这组结果**不支持**“只要把原模板 temperature 调高一点，就能达到和 C run 类似的效果”。
2. 原模板的主要限制更像是**候选 design 集过窄**，而不是单纯 temperature 太低。
3. 提高 temperature 让输出更松，但没有自动解决“行为簇重复”问题。
4. `cluster-breadth` 版本虽然 fixing 成本更高，但明显更能打开新 coverage 区域。

### Temporary artifact storage

为了保留这次 prompt A/B 对比里的临时修改，当前变体模板已存放在：

- `test_analysis/temp/2026-04-01_test_generation_llm_light_advice_selection_prompt_cluster-breadth_variant.toml`

`test_analysis/temp/` 目录用于存放：
- 临时 prompt/template 变体
- 短期 A/B 实验输入
- 不适合直接放进最终分析正文、但后续可能要回看的一次性文件
