# 2026-04-05 目标类与 control_gpt-4o-mini 对比

## 范围

对比这 8 个类在两组结果中的表现：

- 当前运行：`offline_runs/run_no_feedback_selected_subjects_5iter.sh`
- 参考报告：`result-files/control_gpt-4o-mini/*_control_test_results.html`

当前运行配置：

- `rounds=1`
- `maximum_iterations=5`
- `enable_advice_feedback=false`

## 总表

| 类 | 复杂度 | 当前运行覆盖率（line / branch） | 当前运行轮次结果 | control 覆盖率（line / branch） | control 可观察到的迭代深度 | 对比结论 |
|---|---:|---:|---|---:|---|---|
| `MapUtils` | 11 | `15.69 / 17.55` | 跑满 `5` iter 后停止 | `38.56 / 43.09` | 看到 `f_10` | 当前明显更差 |
| `PrototypeFactory` | 16 | `100.0 / 100.0` | `2` iter 提前达到目标 | `0.0 / 0.0` | 看到 `f_2` | 当前明显更好 |
| `HelpFormatter` | 11 | `61.32 / 40.16` | 跑满 `5` iter 后停止 | `64.62 / 43.44` | 看到 `g_7 / f_7` | control 略好 |
| `PosixParser` | 11 | `82.61 / 73.08` | 跑满 `5` iter 后停止 | `86.96 / 65.38` | 看到 `g_6 / f_6` | 混合：当前 line 略低，branch 更高 |
| `UTF8JsonGenerator` | 11 | `0.0 / 0.0` | 跑满 `5` iter 后停止 | `0.0 / 0.0` | 看到 `f_2` | 基本打平，两边都没跑起来 |
| `UTF32Reader` | 14 | `53.47 / 48.33` | 跑满 `5` iter 后停止 | `37.62 / 31.67` | 看到 `f_6` | 当前更好 |
| `ValueUtils` | 12 | `22.36 / 25.0` | 跑满 `5` iter 后停止 | `32.11 / 36.49` | 看到 `f_6` | 当前更差 |
| `PackageFunctions` | 18 | `76.67 / 57.5` | 跑满 `5` iter 后停止 | `30.0 / 20.0` | 看到 `f_3` | 当前明显更好 |

## 轮次说明

- 当前运行是统一配置：每个类都是 `1 round`，`maximum_iterations=5`。
- 只有 `PrototypeFactory` 在第 `2` 轮就达到 `100/100`，提前结束。
- 其余 7 个类都是跑满 `5` iter 后停止。
- control 报告不是统一的 `5 iter` 配置，不同类能看到的最大标签不同，因此它更适合作为非严格同配置 baseline，而不是完全 apples-to-apples 的直接对照。

从报告尾部可观察到的 control 迭代深度如下：

- `MapUtils`：到 `f_10`
- `PrototypeFactory`：到 `f_2`
- `HelpFormatter`：到 `g_7 / f_7`
- `PosixParser`：到 `g_6 / f_6`
- `UTF8JsonGenerator`：到 `f_2`
- `UTF32Reader`：到 `f_6`
- `ValueUtils`：到 `f_6`
- `PackageFunctions`：到 `f_3`

## 分类观察

### `MapUtils`

- 当前结果明显落后。
- 当前这轮早期设计明显过度集中在 `null map` 和一批 getter 行为上，覆盖面偏窄。
- control 虽然后期也出现很多重复测试名和编译失败，但前面已经把覆盖率推到了更高的位置。
- 同时 control 跑得也比当前更深，到了 `f_10`。

### `PrototypeFactory`

- 当前结果显著更好。
- control 一直停在 `0/0`，主要原因是反复生成依赖不存在辅助类的测试，例如：
  - `TestPrototype`
  - `TestPrototypeWithConstructor`
  - `SerializablePrototype`
- 当前这次没有掉进这个坑，且很快就到 `100/100`。

### `HelpFormatter`

- 两边差距不大，control 略优。
- control 最终覆盖到更多 `renderOptions`、`findWrapPos`、`printUsage` 相关路径。
- 这类结果更像是迭代预算差异，而不是策略完全失效。

### `PosixParser`

- 这是一个比较混合的结果。
- control 的 line coverage 更高。
- 当前这次的 branch coverage 更高。
- 如果更看重 branch，这次并不吃亏。

### `UTF8JsonGenerator`

- 两边都基本失败。
- control 里反复出现 API 假设错误，例如：
  - `IOContext` 构造参数不对
  - `writeFieldName(null)` 触发重载歧义
- 这个类大概率需要更强的 API grounding，或者专门的 prompt 约束。

### `UTF32Reader`

- 当前这次明显更好。
- control 也有不少 API 使用错误，例如：
  - `IOContext` 构造方式错误
  - byte 字面量不合法
  - 异常类型假设不对
- 当前至少避开了一部分基础 API 错误，所以覆盖率抬得更高。

### `ValueUtils`

- 当前这次明显更差。
- control 后期虽然也被 `IndexedPropertyDescriptor` / introspection 相关问题拖住，但在那之前已经覆盖了更多行为面。

### `PackageFunctions`

- 当前这次明显更好。
- control 主要卡在类型和接口理解错误上，例如：
  - 把 `Function` 错放到 `org.apache.commons.jxpath.functions`
  - 生成了不合法的 `Pointer` 匿名实现
- 当前这次后期虽然也有编译失败和 runtime error，但前面已经把 coverage 拉得很高。

## 总结

- 当前明显更好的类：`PrototypeFactory`、`UTF32Reader`、`PackageFunctions`
- 当前明显更差的类：`MapUtils`、`ValueUtils`
- 接近或各有胜负的类：`HelpFormatter`、`PosixParser`
- 两边都失败的类：`UTF8JsonGenerator`

综合来看：

- 当前这套 `1 round / 5 iter / no feedback` 配置，在一些 API 敏感类上表现不错。
- 但在 `MapUtils`、`ValueUtils` 这类更依赖设计多样性的 utility 类上，前几轮 design 容易收得太窄，导致明显落后于 control。
