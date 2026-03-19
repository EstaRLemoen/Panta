# ver0.1_exp0.1 对照结论

## 一句话结论

`llm` 路径后端提供的路径信息与 `comex` 明显不同，而且这种差异已经会影响 `PromptBuilder` 的方法/路径选择；当前 `llm` 原型更激进，coverage 更高，但稳定性更弱。

## 主要观察

- `comex` 的路径表示更完整、更细粒度，保留更多 CFG 节点与调用关系
- `llm` 的路径表示更稀疏、更语义化，偏摘要路径而不是节点路径
- `comex` 在 `prompt_builder_cfg` 中保留了更多方法、更多类和更多路径
- `llm` 当前主要聚焦外层类，方法覆盖面和路径数量都更少

## 快照层面的关键差异

### 第一轮 `prompt_builder_cfg`

- `comex`
  - `num_classes = 2`
  - `num_methods = 13`
  - `total_paths = 30`
- `llm`
  - `num_classes = 1`
  - `num_methods = 8`
  - `total_paths = 6`

### 第二轮 `prompt_builder_cfg`

- `comex`
  - `num_methods = 13`
  - `total_paths = 30`
- `llm`
  - `num_methods = 13`
  - `total_paths = 1`

## 方法与路径表现差异

- `comex` 能识别内部类 `CSVRecordIterator` 及其方法
- `llm` 当前主要覆盖外层 `CSVParser`
- 对 `getRecords`：
  - `comex` 给出更完整的 while 路径和类内调用信息
  - `llm` 给出更压缩的语义摘要路径
- 对 `nextRecord`：
  - `comex` 复杂度更高、路径更多、更接近真实 CFG
  - `llm` 给出的是若干 switch/case 条件摘要路径

## 对 `PromptBuilder` 的影响

- `comex` 第二轮选中了多个方法：`nextRecord`、`close`、`getRecords`
- `llm` 第二轮日志中主要聚焦在 `getRecords`
- 说明两种后端不仅中间表示不同，而且已经影响了路径优先级和 prompt 导向

## 生成结果与 coverage

- `comex`
  - 最终 coverage：`51.49% / 30.36%`
  - 整体更稳，失败较少
- `llm`
  - 最终 coverage：`59.41% / 33.93%`
  - 更激进，失败更多，但覆盖率更高

## 当前判断

- `llm_cfg_analyzer ver0.1` 已经不是“只能接上”的状态，而是能实际改变路径链路行为
- 当前收益来自更激进、更语义化的测试导向
- 当前主要问题是：
  - 方法覆盖面还不够稳定
  - 路径粒度仍较粗
  - 编译错误与运行时错误偏多

## 对后续版本的启示

- `ver0.2` 应优先改进：
  - 方法覆盖范围
  - 路径行号/`line_hints` 质量
  - 对复杂方法（尤其 `nextRecord`）的路径稳定性
  - Java 代码生成约束下的可编译性和语义准确性
