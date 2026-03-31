# Advice Feedback A/B Test Analysis

**Date**: 2026-03-31
**Subject**: CSVParser (Csv-16f), baseline 2.97% / 0.0%
**Config**: fix=3, threshold=70, max_iterations=5, no_growth_limit=3
**Variable**: `enable_advice_feedback` ON vs OFF

---

## 1. Coverage Results

### Feedback ON (thr=70, R1 only — R2 used thr=100)

| Run | Iter 0 | Iter 1 | Iter 2 | Iter 3 | Iter 4 | Final |
|-----|--------|--------|--------|--------|--------|-------|
| R1 (thr=70) | 47.52% ✓ | 47.52% ❌ | 47.52% ❌ | 47.52% ❌ | — | **47.52%** |
| R2 (thr=100) | 46.53% ✓ | 51.49% ✓ | 51.49% ❌ | 53.47% ✓ | 53.47% ❌ | **53.47%** |

### Feedback OFF (thr=70)

| Run | Iter 0 | Iter 1 | Iter 2 | Iter 3 | Iter 4 | Final |
|-----|--------|--------|--------|--------|--------|-------|
| R1 | 2.97% ❌ | 52.48% ✓ | 52.48% ❌ | 57.43% ✓ | **74.26%** ✓ | **74.26%** |
| R2 | 2.97% ❌ | 2.97% ❌ | 48.51% ✓ | 64.36% ✓ | 64.36% ❌ | **64.36%** |

### Summary

| Group | R1 Line | R2 Line | Avg Line | R1 Branch | R2 Branch | Avg Branch |
|-------|---------|---------|----------|-----------|-----------|------------|
| Feedback ON (thr=70 R1) | 47.52% | — | — | 33.93% | — | — |
| Feedback ON (thr=100 R2) | — | 53.47% | — | — | 39.29% | — |
| **Feedback OFF** | **74.26%** | **64.36%** | **69.3%** | **62.5%** | **42.86%** | **52.7%** |

Feedback OFF 的 R1 单轮达到了 **74.26% / 62.5%**，这是所有 CSVParser 实验中的最高纪录。

---

## 2. Advice Design Diversity Comparison

### 2.1 Target Method Diversity

| Experiment | Total Designs | Unique Target Methods | Methods Hit |
|------------|--------------|----------------------|-------------|
| Feedback R1 | 12 | 3 | `addRecordValue`, `initializeHeader` |
| Feedback R2 | 15 | 4 | `addRecordValue`, `initializeHeader`, `isClosed`(×1), `nextRecord`(×1) |
| **No-Feedback R1** | **15** | **5** | `addRecordValue`, `initializeHeader`, `nextRecord`(×2), `isClosed`(×1) |
| **No-Feedback R2** | **15** | **5** | `addRecordValue`, `initializeHeader`, `nextRecord`(×1), `hasNext`(×1) |

No-Feedback 实验探索了 **5 种不同的 target method**，Feedback 实验几乎只聚焦在 **2 种** (`addRecordValue` + `initializeHeader`)。

### 2.2 逐 Iteration Advice 设计对比

#### Feedback R1 (thr=70) — Advice 锚定到同一个主题

| Iter | Design 1 | Design 2 | Design 3 |
|------|----------|----------|----------|
| 0 (light) | trailing delimiter → `addRecordValue` | case sensitivity → `initializeHeader` | empty headers → `initializeHeader` |
| 1 (light) | empty records w/ trailing delim | case sensitivity | duplicate headers |
| 2 (light) | empty records w/ trailing delim | case sensitivity | duplicate headers |
| 3 (light) | empty records w/ trailing delim | case sensitivity | duplicate headers |

**Iter 1→2→3 的 advice 完全相同**，即使 feedback 明确告知"上一轮全部 runtime failure"。

#### Feedback R2 (thr=100) — 同样的问题

| Iter | Design 1 | Design 2 | Design 3 |
|------|----------|----------|----------|
| 0 (light) | trailing delimiter → `addRecordValue` | duplicate headers → `initializeHeader` | empty header → `initializeHeader` |
| 1 (light) | trailing delimiter | header handling | **closed state → `isClosed()`** ← 唯一突破 |
| 2 (light) | trailing delimiter | empty header | duplicate headers |
| 3 (light) | trailing delimiter | case-insensitive | duplicate headers w/ flag |
| 4 (light) | trailing delimiter | duplicate names | **comment handling → `nextRecord()`** ← 唯一突破 |

R2 在 Iter 1 和 Iter 4 偶尔探索了新方法，但这 2 次都没带来覆盖增长。

#### No-Feedback R1 — 后期探索了新方向，直接带来覆盖增长

| Iter | Design 1 | Design 2 | Design 3 | 覆盖率变化 |
|------|----------|----------|----------|-----------|
| 0 (light) | trailing delimiter → `addRecordValue` | case sensitivity → `initializeHeader` | **comments → `nextRecord`** | ❌ 2.97%→2.97% |
| 1 (light) | trailing delimiter → `addRecordValue` | case sensitivity → `initializeHeader` | **comments → `nextRecord`** | ✓ 2.97%→52.48% (+49.51%) |
| 2 (light) | trailing delimiter → `addRecordValue` | case sensitivity → `initializeHeader` | **comments → `nextRecord`** | ❌ 52.48%→52.48% |
| 3 (light) | trailing delimiter → `addRecordValue` | **duplicate headers** → `initializeHeader` | **empty header** → `initializeHeader` | ✓ 52.48%→57.43% (+4.95%) |
| 4 (light) | trailing delimiter → `addRecordValue` | **duplicate headers** → `initializeHeader` | **closed state → `isClosed()`** | ✓ 57.43%→74.26% (+16.83%) |

Iter 4 的 `isClosed()` 探索带来了 **+16.83% 的巨大覆盖率增长**——这正是 Feedback 实验从未到达的方向。

#### No-Feedback R2 — 前期挣扎但后期找到突破口

| Iter | Design 1 | Design 2 | Design 3 | 覆盖率变化 |
|------|----------|----------|----------|-----------|
| 0 (light) | trailing delimiter | duplicate headers | **comments → `nextRecord`** | ❌ 2.97%→2.97% |
| 1 (light) | trailing delimiter | case-insensitive | **comments → `nextRecord`** | ❌ 2.97%→2.97% |
| 2 (light) | trailing delimiter | case-insensitive | empty header | ✓ 2.97%→48.51% (+45.54%) |
| 3 (light) | trailing delimiter | case-insensitive | **closed state → `isClosed()`** ← 突破 | ✓ 48.51%→64.36% (+15.85%) |
| 4 (light) | trailing delimiter | empty header | duplicate headers | ❌ 64.36%→64.36% |

Iter 3 探索 `isClosed()`，直接带来 **+15.85%** 增长。

---

## 3. Feedback 锚定效应的具体证据

### 3.1 Feedback R1 Iter 1→2: Advice 文本完全重复

Feedback R1 Iter 1 的 feedback 内容：

```
## Previous Advice Feedback
The previous iteration's advice did not lead to any coverage increase.
### Previous Advice Designs
- "Test handling of empty records with trailing delimiter" via `List<CSVRecord> getRecords()`
- "Test case sensitivity in header mapping" via `CSVParser parse(Reader reader, CSVFormat format)`
- "Test handling of duplicate headers" via `CSVParser parse(Reader reader, CSVFormat format)`
### Generated Tests And Fixing Summary
- `testEmptyRecordWithTrailingDelimiter`: generation runtime failure; fixing still failed (runtime failure, runtime failure, ...)
- `testHeaderMappingCaseSensitivity`: generation runtime failure; fixing still failed (runtime failure, runtime failure, ...)
- `testDuplicateHeaders`: generation runtime failure; fixing still failed (runtime failure, runtime failure, ...)
### Notes
- Most unresolved tests reached execution but failed assertions or setup; reconsider the expected behavior and path setup.
```

LLM 看到这段 feedback 后，Iter 2 生成的 advice：

```
Design 1: "Test handling of empty records with trailing delimiter"  → addRecordValue  ← 一样
Design 2: "Test case sensitivity in header mapping"                 → initializeHeader ← 一样
Design 3: "Test handling of duplicate headers"                      → initializeHeader ← 一样
```

**Feedback 列出的 3 个失败 design 被完整"复述"回了下一轮 advice，没有探索任何新方向。**

### 3.2 Feedback 的锚定 vs 无 feedback 的漂移

| 行为 | Feedback ON | Feedback OFF |
|------|-------------|--------------|
| Design 1 主题 | 始终是 trailing delimiter | 始终是 trailing delimiter |
| Design 2-3 主题 | 被锁定为 case sensitivity / duplicate headers | 会漂移到 comments、isClosed、hasNext |
| 新方法探索率 | 2/27 stages (7%) | 6/30 stages (20%) |
| 新方法成功带来覆盖增长 | 0 次 | 2 次 (isClosed 带来 +16.83% 和 +15.85%) |

### 3.3 关键观察：无 feedback 时"comments → nextRecord"反复出现

无 feedback 实验中，`nextRecord()` (comment handling) 在多个 iteration 反复被提及：
- No-Feedback R1 Iter 0/1/2 的 Design 3 都是 comment handling
- No-Feedback R2 Iter 0/1 的 Design 3 也是 comment handling

这个方向虽然本身没能带来覆盖增长（生成的测试总是 runtime failure），但它说明 **没有 feedback 时 LLM 自然会探索更广泛的未覆盖区域**，而 feedback 把 LLM 的注意力拉回到了它已经反复失败的旧主题。

---

## 4. 结论

### Feedback 确实起了反作用

1. **锚定效应**：Feedback 把失败的 design 名称和主题复述给 LLM，LLM 将其解读为"需要继续深耕的方向"而非"应该避免的方向"。
2. **多样性收窄**：Feedback ON 时 unique target methods = 3-4，OFF 时 = 5。
3. **覆盖率差距显著**：Feedback OFF 平均 69.3% line / 52.7% branch，Feedback ON 平均约 50.5% / 36.6%。

### 根本原因

不是 feedback 这个"概念"有问题，而是**反馈的内容形式导致了锚定**：
- 列出 design name 等于给 LLM 一个 prompt 暗示
- "runtime failure" 太泛，LLM 无法从中学习如何修正
- "reconsider expected behavior" 太模糊，没有给出可操作的改方向指令

### 后续建议

如果要重新启用 feedback，需要：
1. **反转语义**：不说"these designs were tried"，而说"AVOID these approaches, explore DIFFERENT target methods"
2. **提供具体错误信息**：不只是"runtime failure"，给出具体的 exception type 和 message
3. **强制 diversification**：在 prompt 中加入"your designs must target methods NOT listed in the feedback"
4. 或者更激进地：**不在 advice 阶段使用 feedback**，而是在 **fixing 阶段传递修复历史**（fixing 的结构性问题更紧迫）
