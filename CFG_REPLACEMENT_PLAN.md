# CFG 混合改造计划

## 文档目的

本文档用于记录围绕 `llm_cfg_analyzer` 与原有 `cfg/comex` 的混合式改造计划，并作为后续跟进与验收的总入口。

这里有两个相关但职责不同的部分：

- `cfg_snapshot_test/`：验证与参照工具
- `src/panta/llm_cfg_analyzer/`：功能改造实现区域

这两者相关，但不能混为一谈。

## 模块边界

### `cfg_snapshot_test`

定位：

- 作为本次迁移的验证工具
- 保存中间结果参照样本
- 提供 A/B 对照、验证流程与运行记录

它主要回答的是：

- 新实现能否产出与旧 CFG 可对照的中间结果
- 新实现是否在运行行为上足够接近旧实现

因此，`cfg_snapshot_test` 应持续作为独立验证层存在，而不是承载替换实现本身。

### `src/panta/llm_cfg_analyzer`

定位：

- 实现新的 CFG 兼容分析器
- 为 Panta 主流程提供与现有 CFG 接口兼容的数据结构
- 逐步替代对 `cfg/comex` 的直接依赖

它主要回答的是：

- 不依赖原后端时，如何生成 CFG 兼容信息
- 如何在迁移过程中保持现有调用方继续工作

## 改造目标

本次改造的首要目标，不是立即彻底删除 `cfg/comex`，而是让 `llm_cfg_analyzer` 在最有价值的部分先承担起职责，尤其是与路径理解、路径表达、路径选择相关的能力。

整体目标是构建一个混合式 CFG 架构：

- 保留 `cfg/comex` 负责稳定、确定性的结构分析任务
- 让 `llm_cfg_analyzer` 优先承担路径相关的高价值分析任务
- 在必要时由上层协调层整合两类结果供 Panta 主流程使用

在这一前提下，Panta 仍需继续获得以下关键能力：

- `file_obj`
- `node_id_to_line_number`
- `line_number_to_node_id`
- `preprocessed_src_code`
- `testable_methods_statistics`

同时让 `cfg_snapshot_test` 继续作为中间结果参照与迁移验证依据。

## 非目标

- 不把验证逻辑并入 `llm_cfg_analyzer`
- 不一次性重写所有调用点
- 不以“完全删除 `cfg/comex`”作为当前阶段的前提目标
- 不为了统一而强行把所有确定性静态分析职责迁入 `llm_cfg_analyzer`

## 当前状态

### 已有基础

- 快照能力已经独立到 `src/panta/cfg_snapshot.py`
- 当前生产流程仍然在使用 `cfg/comex`
- `src/panta/llm_cfg_analyzer/llm_analyzer.py` 已存在早期原型
- `cfg_snapshot_test/intermediate/` 已经保存了旧流程的中间结果参照样本

### 主要缺口

当前 `LLMCFGAnalyzer` 还没有暴露出足以支撑路径选择相关主流程的稳定接口与结果结构。

## 设计原则

- 严格区分实现模块与验证模块
- 优先解决高价值路径能力，而不是追求表面上的全量替换
- 优先保留确定性结构分析的稳定来源
- 按调用点逐步替换，不做一次性硬切
- 把快照结果作为验证参照，而不是把验证目录当实现目录
- 在达到可接受一致性之前，保留旧后端作为回退路径

## 默认迁移策略

采用“混合后端 + 协调层”方案。

这意味着：

- 确定性结构分析继续优先使用 `cfg/comex`
- 路径相关分析能力优先由 `llm_cfg_analyzer` 补强或接管
- 上层通过协调层或适配层组合两类结果，而不是要求单一后端包办一切
- 切换通过配置项或内部开关控制，而不是直接大面积改业务逻辑

该方案的优势在于：

- 可以持续对照 `cfg_snapshot_test` 做 A/B 验证
- 可以降低一次性替换的风险
- 可以让新模块聚焦在最能体现 LLM 价值的部分
- 可以在未完全达标前保留旧实现兜底

## 职责划分

### 继续由 `cfg/comex` 承担的部分

以下能力以确定性结构提取为主，收益明确、波动应尽量低，当前阶段继续保留在 `cfg/comex`：

- 测试类骨架生成所需的 import / class 基础结构提取
- 测试文件中的 import 插入点分析
- 测试文件中的测试方法插入点分析
- 缩进、已有测试方法位置等偏 AST/结构定位能力

对应位置主要包括：

- `src/panta/panta.py`
- `src/panta/unit_test_generator.py`

### 优先由 `llm_cfg_analyzer` 承担的部分

以下能力更能体现 LLM 的潜力，应作为当前改造重点：

- 方法级复杂度判断或补强
- 路径候选生成
- 路径语义描述
- 路径与 missed lines / missed branches 的关联表达
- 路径优先级选择与提示词构造支撑

对应位置主要包括：

- `src/panta/prompt_builder.py`
- 后续视情况扩展到 `src/panta/symprompt.py`

## 需要重点改造的集成点

当前与 CFG 相关的关键位置包括：

- `src/panta/panta.py`
- `src/panta/unit_test_generator.py`
- `src/panta/prompt_builder.py`
- `src/panta/symprompt.py`
- `evaluation/compute_statistics.py`

其中当前阶段的优先级并不相同：

- 高优先级：`src/panta/prompt_builder.py`
- 中优先级：`src/panta/symprompt.py`
- 低优先级且可暂时保留旧实现：`src/panta/panta.py`、`src/panta/unit_test_generator.py`

## 各集成点当前如何使用 CFG

这一节用于明确“当前与 CFG 相关的关键位置”分别在做什么，以及它们到底依赖 CFG 的哪一类能力。

整体上，这些使用方式可以分为两类：

- 确定性结构用途：依赖 import、class、method、节点到行号映射，用于骨架生成、插入点分析、统计
- 路径语义用途：依赖复杂度、路径、路径节点与源码行号的对应关系，用于路径选择和 prompt 构造

### `src/panta/panta.py`

用途：生成初始测试骨架。

当前做法：

- 使用 `CFGDriver(language, src_code)` 分析源码文件
- 读取 `cfg_driver.file_obj["imports"]`
- 通过 `line_number_to_node_id_mapping(..., cfg_driver.CFG_nodes)` 找到最后一个 import 对应的源码行号
- 将源码中的 package/import 相关部分写入测试文件，再拼接 junit 测试类模板

它依赖的 CFG 能力主要是：

- import 列表
- import 节点到源码行号的映射

结论：

- 这是确定性结构分析用途
- 当前阶段继续保留在 `cfg/comex` 更合理

### `src/panta/unit_test_generator.py`

用途：分析现有测试文件中，新 import 和新测试方法应插入的位置。

当前做法：

- 在 `initial_test_suite_analysis_AST()` 中使用 `CFGDriver(self.language, test_code, {"test_code": True})`
- 通过 `line_number_to_node_id_mapping` 建立节点与源码行号的映射关系
- 读取最后一个 import 的 id，得到 `relevant_line_number_to_insert_imports_after`
- 读取最后一个测试方法声明的 id，得到 `relevant_line_number_to_insert_tests_before`
- 再根据目标方法起始行计算缩进，用于插入新测试代码

它依赖的 CFG 能力主要是：

- 测试文件中的 import
- 测试类与测试方法声明
- 节点到源码行号的映射

结论：

- 这也是确定性结构分析用途
- 当前阶段继续保留在 `cfg/comex` 更合理

### `src/panta/prompt_builder.py`

用途：执行 coverage-guided 的路径筛选，并把选中的路径信息组织进 prompt。

当前做法：

- 使用 `CombinedDriver(src_language=self.language, src_code=self.source_file)` 分析被测源码
- 读取：
  - `preprocessed_src_code`
  - `file_obj`
  - `node_id_to_line_number`
  - `line_number_to_node_id`
- 遍历 `file_obj["class_objects"][0]["methods_under_test"]`
- 对每个方法读取：
  - `method_declaration["name"]`
  - `method_declaration["complexity"]`
  - `method_declaration["nodes"]`
  - `paths`
- 将 coverage missed lines / missed branches 映射回方法与路径
- 从候选路径中筛出最值得优先测试的路径，再把路径文本拼入 prompt

它依赖的 CFG 能力主要是：

- 方法集合
- 方法复杂度
- 路径集合
- 路径节点与源码行号的对应关系
- 路径节点上的 `statement` / `conditional` 信息

结论：

- 这是当前最关键的路径语义用途
- 也是 `llm_cfg_analyzer` 应优先切入的核心位置

### `src/panta/symprompt.py`

用途：基于路径信息和类内调用关系，构造更丰富的 SymPrompt 上下文。

当前做法：

- 同样使用 `CombinedDriver(...)` 获取 CFG 信息
- 通过 `method_declaration["nodes"]` 与 `node_id_to_line_number` 反推出 focal method 的源码片段
- 遍历每条 path 的 `method_calls_within_class`
- 通过这些调用记录中的起止节点 id，再反查对应源码行，抽出类内相关方法上下文
- 同时把路径转换成自然语言描述，用于构造 prompt

它依赖的 CFG 能力主要是：

- 方法列表
- `method_declaration["value"]`
- `method_declaration["nodes"]`
- `paths`
- `path["method_calls_within_class"]`
- 节点到源码行号映射

结论：

- 这同样属于路径语义用途
- 但它比 `prompt_builder` 对 schema 的要求更高，因此应晚于 `prompt_builder` 迁移

### `evaluation/compute_statistics.py`

用途：离线统计代码文件中的可测试方法及复杂度分布。

当前做法：

- 使用 `CFGDriver(language, src_code, {"statistics": code_file})`
- 读取 `cfg_driver.testable_methods`
- 读取 `cfg_driver.file_obj["class_objects"][0]["class_declaration"]["value"]`
- 汇总各复杂度区间的方法数量

它依赖的 CFG 能力主要是：

- 类声明
- 可测试方法统计结构
- 圈复杂度数据

结论：

- 这是统计用途
- 不属于当前最核心的路径选择链路，可后置处理

### `src/panta/cfg_snapshot.py`

用途：将 CFG 中间状态落盘，作为验证与参照材料。

当前做法：

- 不负责生成 CFG
- 只负责读取传入 driver/analyzer 上已有的属性并写成快照文件

它依赖的输入主要是：

- `file_obj`
- `node_id_to_line_number`
- `line_number_to_node_id`
- `testable_methods_statistics`

结论：

- 它属于验证/记录层，而不是 CFG 语义提供层
- 它的意义在于为本次混合改造提供中间结果参照与验证依据

## 新模块当前应优先提供的能力

### 路径相关核心属性

- `file_obj`
- `node_id_to_line_number`
- `line_number_to_node_id`
- `preprocessed_src_code`
- `testable_methods_statistics`

### `file_obj` 的基本结构

- 顶层 `imports`
- 顶层 `class_objects`
- 类级别 `class_declaration`
- 类级别 `fields`
- 类级别 `constructors`
- 类级别 `methods_under_test`

### 方法与路径层面必须具备的字段

- `method_declaration.id`
- `method_declaration.name`
- `method_declaration.value`
- `method_declaration.complexity`
- `method_declaration.nodes`
- `paths`
- `path[].path`
- `path[].true_branches`
- `path[].catch_exception_at`
- `path[].method_calls_within_class`
- `path[].method_calls_outside_class`

说明：

- 对 `prompt_builder` 而言，最关键的是方法、路径、复杂度、路径节点与行号映射
- 对测试骨架生成和插入点分析而言，并不要求 `llm_cfg_analyzer` 立即接管
- 对 `symprompt` 而言，`method_calls_within_class` 等字段后续仍需补齐，但不是当前第一优先级

## `PromptBuilder` 最小路径 schema 拆解结论

这一节用于记录当前对 `src/panta/prompt_builder.py` 的最小依赖拆解结果，为后续路径后端接入提供明确目标。

### 当前真正读取的 driver 属性

`PromptBuilder` 初始化时会从路径分析对象上读取：

- `file_obj`
- `node_id_to_line_number`
- `line_number_to_node_id`
- `preprocessed_src_code`

但从实际使用看，优先级并不相同：

- 核心依赖：`file_obj`、`node_id_to_line_number`
- 当前保留兼容即可：`line_number_to_node_id`、`preprocessed_src_code`

### 当前最小可用 `file_obj` 结构

如果目标仅是先支撑 `PromptBuilder` 的路径选择能力，则当前最小可用结构可收缩为：

- `class_objects[0]`
- `class_objects[0].methods_under_test`
- `method_declaration.id`
- `method_declaration.name`
- `method_declaration.complexity`
- `method_declaration.nodes`
- `paths`
- `path[].path`
- `path[].path[].id`
- `path[].path[].statement`
- `path[].path[].conditional`

在这一阶段，对 `PromptBuilder` 而言并非硬性必需的有：

- `imports`
- `class_declaration`
- `fields`
- `constructors`
- `true_branches`
- `catch_exception_at`
- `method_calls_within_class`
- `method_calls_outside_class`

### 必须语义准确的部分

以下内容会直接影响路径选择结果，不能只“占位”：

- `methods_under_test` 的方法集合
- `method_declaration.nodes` 对应的方法行范围
- `method_declaration.complexity`，至少 `> 1` 的判定要可靠
- 路径节点 id 对应的 `node_id_to_line_number`
- `method_declaration.id` 与 path 顺序的稳定性

### 可以先保证存在、后续再提升质量的部分

以下内容若语义不够精细，主要影响 prompt 文本质量，而不是立即阻断流程：

- `method_declaration.name`
- `path[].path[].statement`
- `path[].path[].conditional`

### 当前 `PromptBuilder` 推导出的候选路径结构

`PromptBuilder` 会基于 CFG 数据生成如下候选路径元组：

- `missed_value`
- `path_lines`
- `path_nodes`
- `path_conditions_str`
- `path_label`

其中：

- `path_lines` 依赖路径节点 id 到源码行号的映射
- `path_conditions_str` 是最终送入 prompt 的路径文本
- `path_label` 是 `path_history` 中用于记录访问次数的稳定键

### 当前拆解得到的实现约束

- 方法级和路径级 node id 必须能映射回原始源码行号
- 若仍沿用当前 `PromptBuilder` 的路径文本拼接方式，则路径节点的行号列表与 `statement` 多行文本最好保持基本对齐
- 如果后续允许对 `PromptBuilder` 做小改动，则可以减少对 `comex` 风格 path node 拼装细节的依赖

## `PromptBuilder` 小改动决策表

这一节用于明确：为降低 `llm_cfg_analyzer` 第一版接入成本，哪些 `PromptBuilder` 改动值得做，哪些不应轻易动。

### 建议保持不动的部分

- 方法级 missed lines / missed branches 的归属逻辑
- 基于路径覆盖行号计算 `missed_value` 的逻辑
- `path_history` 机制及其对稳定 `path_label` 的依赖
- 复杂度驱动的“复杂方法优先”基本排序逻辑

原因：

- 这些部分直接影响路径选择行为本身
- 如果这里改动过大，就很难判断后续收益来自新路径后端，还是来自上层策略变化

### 建议允许小幅简化的部分

- 允许直接消费预渲染路径文本，而不是强依赖逐节点拼接 `path_conditions_str`
- 允许逐步降低对完整 `comex` 风格 path node 富结构的依赖
- 允许将 `line_number_to_node_id` 从 `PromptBuilder` 的核心依赖降为兼容字段
- 允许路径对象未来向“路径文本 + 覆盖行号”形式收缩

原因：

- 这些部分更多是旧消费方式的适配成本
- 它们不是路径选择价值本身，而是历史接口形态

### 哪些工作量是真正可以省掉的

若允许上述小改动，可以真实减少的工作主要包括：

- 为兼容旧路径文本拼接方式而构造细粒度假 path node 的成本
- 为对齐多行 `statement` 与多行行号列表而做的额外兼容成本
- 对 `PromptBuilder` 无直接价值的 `comex` 路径细节模拟工作

这部分工作量在当前阶段通常不会在别处原样补回来，因为它主要服务于旧接口消费习惯。

### 哪些工作量只是后移而不是消失

以下能力即使当前阶段在 `PromptBuilder` 上放宽要求，后续仍可能在别处需要：

- `symprompt` 依赖的 `method_calls_within_class`
- 更完整的 `file_obj` 结构
- 更严格的统计模式兼容
- 更精细的方法筛选与复杂度口径对齐

因此，对 `PromptBuilder` 的小改动应被视为“聚焦当前高价值链路”，而不是“永久取消所有兼容工作”。

## 分阶段改造计划

### 阶段 1：建立混合协调入口

新增共享的协调层/适配层，用于明确区分“结构分析走旧后端”“路径分析走新后端”的职责。

目标：

- 后续改造围绕统一入口进行
- 两套后端可以长期并存，而不是只作为短期过渡
- 避免每推进一步就重复改一遍业务接口

当前阶段的实施边界：

- 收口 `src/panta/panta.py`
- 收口 `src/panta/unit_test_generator.py`
- 收口 `src/panta/prompt_builder.py`
- 暂不收口 `src/panta/symprompt.py`
- 当前行为保持不变，底层实现仍然走 `cfg/comex`

阶段 1 的推荐协调层职责：

- 提供结构分析入口，例如 `get_structural_cfg(...)`
- 提供路径分析入口，例如 `get_path_cfg(...)`
- 提供行号映射补齐入口，例如 `ensure_line_mappings(...)`
- 将补齐后的映射暴露为公开兼容属性，而不是私有缓存

阶段 1 需要约定的公开兼容属性：

- 结构分析对象最低需要：`file_obj`、`CFG_nodes`
- 若结构调用方需要行号映射，则还应可获得：
  - `line_number_to_node_id`
  - `node_id_to_line_number`
- 路径分析对象最低需要：
  - `file_obj`
  - `preprocessed_src_code`
  - `line_number_to_node_id`
  - `node_id_to_line_number`
  - `testable_methods_statistics`

阶段 1 的验收标准：

- `src/panta/panta.py` 不再直接创建 `CFGDriver`
- `src/panta/unit_test_generator.py` 不再直接创建 `CFGDriver`
- `src/panta/prompt_builder.py` 不再直接创建 `CombinedDriver`
- 上述三处行为保持一致
- 快照行为保持一致

阶段 1 当前进展：

- 已新增协调层：`src/panta/cfg_access.py`
- 已收口 `src/panta/panta.py`
- 已收口 `src/panta/unit_test_generator.py`
- 已收口 `src/panta/prompt_builder.py`
- `src/panta/symprompt.py` 仍按计划暂不收口

### 阶段 2：补齐 `LLMCFGAnalyzer` 的路径能力兼容性

扩展 `LLMCFGAnalyzer`，优先使其具备支撑路径选择链路的最低兼容属性和 schema。

优先项：

- `preprocessed_src_code`
- `testable_methods_statistics`
- 更完整的 `file_obj`
- 方法复杂度与路径表示结构
- 路径节点到行号的可用映射

### 阶段 3：先接入 `PromptBuilder` 的路径链路

优先让 `llm_cfg_analyzer` 进入最能体现价值的路径选择链路。

重点位置：

- `src/panta/prompt_builder.py`

重点目标：

- 能提供可供路径筛选使用的方法与路径信息
- 能支持 missed lines / missed branches 对应的路径候选选择
- 能产出可对照的中间结果供快照验证

### 阶段 4：验证快照产物兼容性

通过 `src/panta/cfg_snapshot.py` 验证新后端是否能产出结构兼容的中间结果。

重点对照文件：

- `meta.json`
- `cfg_file_obj.json`
- `cfg_node_id_to_line_number.json`
- `cfg_line_number_to_node_id.json`
- `cfg_testable_methods_statistics.json`
- `cfg_summary.json`

参照来源：

- `cfg_snapshot_test/intermediate/`

### 阶段 5：评估是否扩展到 `SymPrompt`

在 `PromptBuilder` 路径链路达到可用状态后，再评估是否扩展到 `SymPrompt`。

关键要求：

- 保持 `method_calls_within_class` 等路径元数据兼容

这一步不应早于 `PromptBuilder`，因为其对字段完整性的要求更高。

### 阶段 6：保留并明确旧后端的稳定职责

对于下列能力，当前阶段明确继续使用 `cfg/comex`：

- 测试类骨架生成
- 测试文件 import 插入点分析
- 测试方法插入点分析
- 缩进和结构定位相关逻辑

只有在后续出现明确收益时，才考虑是否将这些确定性能力迁出旧后端。

### 阶段 7：补齐统计能力并视情况扩展

迁移评估/统计相关使用点，并在新模块价值已经被验证后，再决定是否扩大替换范围。

## 验证策略

验证工作持续由 `cfg_snapshot_test` 承担，并作为独立参照体系存在。

验证重点包括：

- 中间产物是否完整
- schema 是否兼容
- 各阶段快照是否按预期生成
- 主流程是否稳定
- 在适用场景下，coverage 表现是否保持同量级稳定
- 路径候选与路径描述是否足以支持 prompt 构建

## 主要风险

- 路径到行号的映射过粗，导致 prompt 引导效果偏离旧实现
- 方法筛选逻辑与旧 CFG 漂移，导致方法集合不一致
- `symprompt` 强依赖的字段在原型中尚未补齐
- LLM 输出存在波动，可能影响可复现性
- 统计口径若不显式兼容，可能难以做到完全一致
- 若过早追求全替换，可能分散掉对高价值路径能力的投入

## 迁移期间的工作约束

在达到可接受一致性之前：

- `cfg_snapshot_test` 持续作为独立验证与参照工具
- `llm_cfg_analyzer` 持续作为功能改造实现区域
- 旧 `cfg/comex` 后端不仅保留为回退方案，也继续承担确定性结构分析职责

## 跟进清单

- [x] 建立混合后端协调层/适配层
- [x] 明确结构分析与路径分析的职责边界
- [ ] 补齐 `LLMCFGAnalyzer` 的路径兼容属性
- [ ] 支持方法复杂度与路径结构表达
- [ ] 支持路径节点到行号的可用映射
- [ ] 接入 `src/panta/prompt_builder.py`
- [ ] 验证路径相关快照产物兼容性
- [ ] 评估并决定是否接入 `src/panta/symprompt.py`
- [ ] 视需要补齐 `evaluation/compute_statistics.py`
- [x] 保持 `src/panta/panta.py` 的结构分析稳定性
- [x] 保持 `src/panta/unit_test_generator.py` 的插入点分析稳定性

## 备注

本文档是总改造计划与跟进文档。

- 实现细节文档应放在 `src/panta/llm_cfg_analyzer/` 附近
- 验证流程与运行记录应继续放在 `cfg_snapshot_test/` 中
- 当前阶段的重点不是“替换一切”，而是“优先让新模块在路径选择链路中发挥价值”
