# Prompt Template Design Review

Date: 2026-04-01
Scope:
- `src/panta/prompt_templates/java_templates/test_generation_llm_light_advice_selection_prompt.toml`
- `src/panta/prompt_templates/java_templates/test_generation_llm_guided_prompt.toml`
- `src/panta/prompt_templates/java_templates/test_generation_llm_advice_selection_prompt.toml`
- `src/panta/prompt_templates/java_templates/failed_test_feedback_prompt.toml`
- `src/panta/llm_path_advice_prompt_builder.py`

---

## 1. Purpose

这份文档记录当前 prompt 模板体系的一次设计回顾，目标不是复盘某一次单独实验，而是总结：

1. 各模板当前分别承担什么角色。
2. 最近几轮改动后，哪些设计已经明显变好。
3. 还有哪些结构性问题会继续影响 advice 质量、生成稳定性和 fixing 承接效果。

当前前提：

- `light-advice` 是主力路径。
- `full-advice` 目前默认通过 `llm_advice_activation_line_coverage = 100` 基本禁用。
- `guided generation` 仍然是实际产测的关键模板。
- `failed_test_feedback` 仍主要用于 fixing，但尚未做最近一轮系统化重构。

---

## 2. Current Template Roles

### `test_generation_llm_light_advice_selection_prompt.toml`

当前主力 advice 模板。

职责：
- 基于带 `PANTA:` 标注的 source code 识别值得继续探索的 uncovered behavior。
- 返回多个轻量 `test_designs`，供后续 guided generation 消费。
- 强调探索宽度，而不是精细 runtime 推演。

当前定位：
- 这是现在最重要的模板。
- 最近一轮已明确要求优先返回 4 个 design，并显式避免多个 design 聚焦同一行为簇。

### `test_generation_llm_guided_prompt.toml`

当前测试生成主模板。

职责：
- 消费 advice YAML。
- 结合 annotated source code、existing test file、依赖信息与失败反馈，生成可追加到测试文件中的新测试。

当前定位：
- advice 是否有效，最终还要看这个模板能否把 design 转成能通过且能涨 coverage 的测试。
- 它目前已经承担了“不要盲目模仿 existing tests”这一关键纠偏职责。

### `test_generation_llm_advice_selection_prompt.toml`

旧的 full-advice 模板。

职责：
- 要求模型选择高价值 uncovered branch。
- 用很重的 `execution_flow` 写法描述精确路径、条件值、分支方向。

当前定位：
- 暂不推荐继续作为默认路径。
- 如果未来要做 re-planning，应该重新设计，而不是直接回到这个版本。

### `failed_test_feedback_prompt.toml`

fixing 阶段模板。

职责：
- 根据 failed test、error message、source/test file 生成修复后的替代测试。

当前定位：
- 仍能工作，但模板指导力度偏弱。
- fixing 的成败目前更多依赖模型本身，而不是 prompt 结构设计。

---

## 3. What Improved Recently

### 3.1 Light-advice now has a proper schema example

这是最近最重要的一次 prompt 改善。

现在的 `light-advice`：
- 提供了完整的自创代码 YAML example。
- example 明确展示了 `method_context.entry_hint` 的正确嵌套位置。
- `observable_behavior` 也从“猜精确断言”改成了“开放式、可观察结果”。

这对降低 schema compliance 问题是有帮助的，尤其是此前已经观察到：
- runtime log 里的原始 advice 往往有 `entry_hint`
- 但 snapshot 中归一化后的 `generation_outcome.json` 里，`entry_hint` 有时为空
- 根因不是代码把 `entry_hint` 删除了，而是模型有时把它写到了 `method_context` 外层

### 3.2 Guided prompt now limits over-imitation of existing tests

`test_generation_llm_guided_prompt.toml` 现在已明确要求：

- existing test file 仅用于 style / imports / helpers / integration
- 不要让 existing tests 决定要测什么
- 不要盲目模仿已有测试的情景形状，导致重复已覆盖行为

这条约束很重要，因为当前系统确实会把整个 existing test file 喂给 LLM。这个输入既有价值，也有明显锚定风险。

### 3.3 Light-advice now explicitly pushes breadth

当前模板已经写明：

- 优先返回 4 个 `test_designs`
- 避免多个 design collapse 到同一行为簇
- 变换 public entry path、target method 或 observable behavior

从近期 CSVFormat 对照实验结论看，这一方向是对的。问题不主要是 temperature 太低，而是原来的候选 design 集过窄。

---

## 4. Main Design Issues

### 4.1 `execution_flow` and `input_focus` are still not cleanly separated

这是 `light-advice` 里最明显的边界问题。

当前规则一方面要求：
- `execution_flow` 保持轻量，只描述 reachability skeleton

另一方面又要求：
- `input_focus` 负责具体参数值、flag、状态设置

但模板 example 里两者仍有重叠。例如：
- `execution_flow` 已经写了 premium order / above threshold
- `input_focus` 又再写一次 premium + threshold

结果是：
- 模型不容易稳定区分“路径骨架”和“输入配置”
- 生成时也可能重复消费同一信息，造成 advice 内容表面丰富、实际冗余

建议方向：
- `execution_flow` 只写“从哪个 public entry 出发，经过哪些关键方法/判断，到达哪类 target”
- `input_focus` 只写“为了让该路径成立，需要哪些具体输入或配置”

### 4.2 “Avoid same cluster” is still a soft rule, not a structural constraint

当前模板已经要求不要让多个 design 聚焦同一行为簇，但这仍然只是自然语言约束。

实际风险仍在：
- 模型可能给出 4 个 design
- 但 4 个都围绕同一个 public entry、同一个 method、同一类 behavior，只是换了几个措辞

这意味着当前“增大 design 数”的收益，仍然部分依赖模型自觉，而不是系统强约束。

更现实的短期方向不是立刻做复杂 cluster，而是：
- 在后处理里观察 `entry_hint` / `method_signature_hint` 是否高度重复
- 若高度重复，至少打日志或在分析里标注 advice breadth 不足

### 4.3 The example count still anchors toward fewer than 4 designs

当前 `light-advice` 的规则说“优先返回 4 个 design”，但 example 只展示了 2 个。

这会形成一个很直接的锚：
- 文字说 4 个
- example 实际示范 2 个

模型往往会更相信 example 的形态而不是规则语句。

建议方向：
- 要么把 example 扩成 3 到 4 个 design
- 要么在 example 后明确写一句：这里仅展示 2 个 design 用于说明嵌套格式，实际回答应优先返回 4 个

### 4.4 Light mode validation is too permissive in normalization

`src/panta/llm_path_advice_prompt_builder.py` 中 `_normalize_advice(...)` 当前对 `light_mode=True` 的校验非常松。

当前行为：
- 只要 `design_name` 非空，design 就可能被保留
- 即使 `entry_hint` / `execution_flow` / `input_focus` 缺失，也不会像 full-advice 那样被过滤掉

这和模板本身的结构要求不完全一致。

实际影响：
- light-advice 的 schema 看起来完整
- 但一旦模型漏掉核心字段，后处理仍可能把半残 design 传给 guided generation
- 最终不是 advice 阶段报错，而是 generation 阶段开始“猜”

这会降低问题可观察性，也会让下游质量波动更大。

### 4.5 `entry_hint` still lacks defensive fallback during normalization

目前 `_normalize_advice(...)` 读取 `entry_hint` 的方式是：

- 只看 `method_context.entry_hint`

如果模型把 `entry_hint` 错写到 design 顶层：
- 它不会被识别
- snapshot 里的归一化 advice 会显示为空

虽然 prompt example 已经改善了这个问题，但代码侧仍然没有兜底。

建议方向：
- 如果 `method_context.entry_hint` 为空，尝试读取 design 顶层的 `entry_hint`
- 这样至少可以提升鲁棒性，避免因为 YAML 层级小偏差直接丢失关键字段

### 4.6 Guided prompt may encourage over-branching when uncertain

`test_generation_llm_guided_prompt.toml` 当前有一条规则：

- 如果不确定精确 expected result，可以为同一场景生成多个不同可能结果的测试

这条规则在理论上提高了“至少一条猜中”的概率，但也带来明显副作用：

- 同一 design 可能分裂成多个近似测试
- 编译失败、运行失败、语义重复的概率上升
- 与 advice 层面追求 breadth 的目标部分冲突

换句话说：
- advice 想把预算花在不同方向
- generation 却可能把预算重新花回同一场景的多次猜测

这条规则值得重新审视。

### 4.7 Failed-test fixing prompt is structurally thin

`failed_test_feedback_prompt.toml` 目前格式上是够用的：
- 有明确 YAML response example
- 有 failed test 和 error message 输入

但行为指导偏弱：
- 只说要“fix the failed tests”
- 没有明确区分 compile error / runtime exception / assertion mismatch 的不同修法
- 没有告诉模型优先保留什么、可以牺牲什么

结果是：
- fixing 成果往往靠模型通用能力
- prompt 本身没有显式帮它做收缩和裁剪

这和最近 CSVFormat C 组后期某轮 fixing 爆炸的观察是相符的：探索宽度起来后，fixing 承接能力没有同步加强。

---

## 5. Full-Advice Retrospective

旧 `full-advice` 模板的主要问题，不只是“复杂”，而是它把复杂度放在了最容易幻觉的位置：

- 要求模型写精确 runtime path
- 要求给出条件表达式、变量运行值、分支方向
- 还要保证这些值与目标 uncovered branch 真正一致

这类要求在静态阅读源码时很容易出现“写得很像真的，但并不可靠”的路径叙述。

因此现在把 full-advice 默认压到 `threshold=100` 是合理的。

如果未来真的要恢复更强的 re-planning：
- 不应回退到旧 full-advice 的“重 execution_flow”思路
- 更可能需要的是轻量、结构清晰、偏重新选入口/新选 observable path 的后期 replanning advice

---

## 6. Cross-Template Tension

当前模板体系内部存在几组典型张力：

### 6.1 Breadth vs fixability

`light-advice` 现在更强调 breadth，这个方向是对的。

但 breadth 上去以后：
- design 更异质
- setup 更复杂
- generated tests 更容易带来 compile/runtime failure

也就是说，模板层已经在推动更广探索，但 fixing 模板还没有同步升级。

### 6.2 Open-ended observable behavior vs assertion precision

`light-advice` 现在要求 `observable_behavior` 保持开放，不要预测精确值。

这是对的，因为 advice 层本来就不该硬猜断言。

但 generation 阶段仍需要把 open-ended observable behavior 转成可执行断言。若 guided prompt 处理不好，就会出现：
- advice 太抽象
- generation 被迫自己猜
- fixing 再来兜底

因此真正关键的是：
- advice 不要过度精确
- 但也不能抽象到只剩“look for behavior changes”这种空话

### 6.3 Existing test integration vs thematic anchoring

当前系统把整个 existing test file 喂给模型，这本身是双刃剑：

- 好处：风格、imports、helpers、fixture 复用更稳
- 风险：模型容易沿着旧测试主题继续写，而不是去追新 uncovered behavior

这也是为什么 guided prompt 现在必须显式写：
- existing tests 只用于 integration，不用于决定测什么

---

## 7. Practical Near-Term Recommendations

以下建议按“投入小、收益高”排序。

### 7.1 Keep the current light-advice direction

当前方向应继续保持：
- 保留 `light-advice` 作为主路径
- 保留“优先 4 个 design”
- 保留“避免同簇集中”
- 保留自创代码的完整 YAML example

### 7.2 Tighten field separation in light-advice wording

下一步最值得改的是把 `execution_flow` 和 `input_focus` 的边界写得更硬。

目标：
- 减少冗余
- 提高 advice 字段职责稳定性
- 让 generation 更容易知道“该从哪里拿什么信息”

### 7.3 Add a defensive fallback for misplaced `entry_hint`

这属于小改动、高收益：
- prompt 继续修 schema compliance
- 代码侧也加兜底

这样即使模型偶尔层级写错，snapshot 和后续 generation 也不至于直接丢字段。

### 7.4 Revisit guided prompt uncertainty handling

建议重新审查“同一场景多猜几个测试”的规则。

如果保留，也应更克制；否则它会抵消 advice 层的 breadth 设计。

### 7.5 Treat fixing prompt as the next major bottleneck

如果后续实验继续显示：
- breadth 增加后 early/mid iterations 更好
- 但 late iterations fixing 爆炸

那么下一阶段最该投入的不是再继续加 advice 细节，而是重写 fixing prompt 的行为策略。

---

## 8. Summary

当前模板体系里，最健康的部分是：

- `light-advice` 的整体方向已经从“窄而重复”转向“更宽、更结构化”
- `guided prompt` 已经开始抑制 existing test anchoring
- `full-advice` 被默认禁用是合理决策

当前最主要的残留问题是：

1. `light-advice` 仍有字段职责边界不清的问题。
2. breadth 仍主要依赖模型自觉，没有强结构约束。
3. normalization 对 light-advice 太宽松，对 `entry_hint` 也缺少代码级兜底。
4. `guided prompt` 的不确定性策略可能重新把预算花回同一场景的多次猜测。
5. fixing prompt 还没有跟上更宽探索带来的复杂度。

整体判断：

- 目前最该保留的是 `light-advice` 的广度方向。
- 最该小步修的，是字段边界和 schema 鲁棒性。
- 最可能成为下一轮瓶颈的，是 fixing，而不是 advice 本身。
