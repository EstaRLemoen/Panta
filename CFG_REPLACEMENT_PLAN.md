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

## 分阶段改造计划

### 阶段 1：建立混合协调入口

新增共享的协调层/适配层，用于明确区分“结构分析走旧后端”“路径分析走新后端”的职责。

目标：

- 后续改造围绕统一入口进行
- 两套后端可以长期并存，而不是只作为短期过渡
- 避免每推进一步就重复改一遍业务接口

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

- [ ] 建立混合后端协调层/适配层
- [ ] 明确结构分析与路径分析的职责边界
- [ ] 补齐 `LLMCFGAnalyzer` 的路径兼容属性
- [ ] 支持方法复杂度与路径结构表达
- [ ] 支持路径节点到行号的可用映射
- [ ] 接入 `src/panta/prompt_builder.py`
- [ ] 验证路径相关快照产物兼容性
- [ ] 评估并决定是否接入 `src/panta/symprompt.py`
- [ ] 视需要补齐 `evaluation/compute_statistics.py`
- [ ] 保持 `src/panta/panta.py` 的结构分析稳定性
- [ ] 保持 `src/panta/unit_test_generator.py` 的插入点分析稳定性

## 备注

本文档是总改造计划与跟进文档。

- 实现细节文档应放在 `src/panta/llm_cfg_analyzer/` 附近
- 验证流程与运行记录应继续放在 `cfg_snapshot_test/` 中
- 当前阶段的重点不是“替换一切”，而是“优先让新模块在路径选择链路中发挥价值”
