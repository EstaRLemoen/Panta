# WP3.5 执行段与跳转计划

## 目标

把当前偏摘要型的 path 输出升级成“执行段 + 跳转证据”结构，为后续 `WP4` 的 path membership 判断提供更完整的依据。

当前不改 `PromptBuilder` 的高层选路/打分公式，只增强 path 证据的表达与透传。

## 当前问题

`WP3` 之前的 path 输出主要包含：

- `conditions`
- `effects`
- `coverage_anchors`
- `line_hints`

这些字段能表达关键点，但不能稳定表达：

- 分支点究竟跳向哪里
- 两个跳转点之间顺序执行经过了哪些普通语句

结果就是：

- missed line 如果不是关键 anchor，可能无法影响 path 选择
- `line_hints` 往往仍然过稀，无法支撑“沿途普通行”的 membership 判断

## 修正后的核心思路

`WP3.5` 不再只做“加一个 `transitions` 字段”。

它要把每条 path 升级成：

- `transitions`：关键控制流跳转证据
- `segments`：按执行顺序排列的执行段

其中：

- `transition` 段表示控制流跳转
- `sequential` 段表示两个跳转点之间顺序执行经过的语句

这样 path 不再只是“几条关键线索”，而是“简化版执行过程”。

## schema 设计

每条 path 保留现有兼容字段，并新增：

```json
{
  "path_id": "p1",
  "kind": "branch",
  "summary": "when next is null, fetch next record and continue",
  "conditions": [{"text": "next == null", "line": 221}],
  "effects": [{"text": "next = this.getNextRecord()", "line": 225}],
  "coverage_anchors": [221, 225],
  "line_hints": [218, 219, 221, 225, 226, 227],
  "transitions": [
    {
      "source_text": "if (next == null)",
      "source_line": 221,
      "condition_value": true,
      "branch_label": null,
      "target_text": "next = this.getNextRecord()",
      "target_line": 225
    }
  ],
  "segments": [
    {
      "segment_type": "sequential",
      "statements": [
        {"text": "CSVRecord next = this.current;", "line": 218},
        {"text": "this.current = null;", "line": 219}
      ]
    },
    {
      "segment_type": "transition",
      "source_text": "if (next == null)",
      "source_line": 221,
      "condition_value": true,
      "branch_label": null,
      "target_text": "next = this.getNextRecord()",
      "target_line": 225
    },
    {
      "segment_type": "sequential",
      "statements": [
        {"text": "next = this.getNextRecord();", "line": 225},
        {"text": "return next;", "line": 227}
      ]
    }
  ]
}
```

## 字段语义

### `transitions`

- 记录该 path 中所有关键控制流跳转
- 不再限制“1-2 个”，有几个关键跳转就写几个
- 普通顺序执行不写进 `transitions`

### `segments`

- 按执行顺序排列
- `segment_type = "sequential"`
  - 表示顺序执行段
  - `statements` 中尽量列出沿途真实执行语句
- `segment_type = "transition"`
  - 表示一个控制流跳转
  - 字段结构与 `transitions` 对齐

### `condition_value`

- 仅用于布尔分支
- 允许值：`true` / `false` / `null`

### `branch_label`

- 用于 `switch/case` 等非布尔分支标签
- 允许值：字符串或 `null`

## 行号策略

系统内部继续保留两套坐标空间：

- preprocessed line numbers
- original source line numbers

规则：

- LLM 只看一种坐标空间，由 `llm_line_mode` 控制
- LLM 输出的 `transitions` / `segments` 行号先在输入空间解释
- 对外暴露给 `PromptBuilder` 和 snapshot 的标准字段统一转换回原始源码行号

## Prompt 约束

Prompt 应明确要求模型：

- 只分析列出的 methods
- path 表达的是执行过程，而不是摘要
- `transitions` 应覆盖该 path 的关键跳转，不要人为截断数量
- `segments` 应按执行顺序排列
- 在两个跳转点之间，尽量列出顺序执行经过的真实语句
- 每条 statement 尽量输出 `text + line`
- 不要只输出少量“最重要行”
- 不要虚构源码中不存在的 statement / line
- 所有 lines 必须在 method range 内

## 校验规则

### statement 校验

- `text` 非空
- `line` 可解析为 int
- `line` 落在 method range 内
- `text` 能在 method source 中近似 grounding

### transition 校验

- 必填字段存在
- `condition_value` 属于 `true` / `false` / `null`
- `branch_label` 为 string 或 `null`
- `source_line` / `target_line` 落在 method range 内
- `source_text` / `target_text` 能在 method source 中近似 grounding

### segment 校验

- 只接受 `sequential` / `transition`
- `sequential.statements` 去空、去重
- `segments` 整体顺序基本单调

### 降级策略

- statement 非法：丢 statement
- transition 非法：丢 transition
- segment 清空：丢 segment
- path 若仍有有效证据则保留

## PromptBuilder 集成

当前阶段不改 `PromptBuilder` 的高层选路逻辑。

先透传以下 metadata：

- `conditions`
- `effects`
- `coverage_anchors`
- `transitions`
- `segments`

同时让旧兼容字段受益：

- `line_hints` 变成 path 中更完整的执行行集合
- `_llm_precomputed_path_str` 能表达 transition 和 sequential path 文本

## 快照预期

full snapshot 应能观察到：

- `path_cfg_backend`
- `llm_line_mode`
- 每条 path 的 `_llm_transitions`
- 每条 path 的 `_llm_segments`
- `line_hints` 是否明显变密

## 实施顺序

1. 扩 path schema：加入 `transitions` 与 `segments`
2. 改 prompt 规则，明确“执行段”表达
3. 解析并校验 `statement` / `transition` / `segments`
4. 行号统一映射回 original source 坐标
5. 在 path object 上保留 `_llm_transitions` / `_llm_segments`
6. 在 `PromptBuilder` candidate metadata 中透传新字段
7. 做 smoke validation，检查快照与 coverage 行为

## 当前 exit criteria

- 复杂 path 在适用时能保留至少一个有效 transition
- path 输出不再只依赖 sparse anchors
- snapshot 中可直接观察 `_llm_segments` / `_llm_transitions`
- `line_hints` 比旧版更密，可作为 `WP4` membership 的过渡输入
- 在真实 smoke 中能参与 `PromptBuilder` 路径链路，而不是只停留在 schema 设计层
