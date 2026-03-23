# ver0.2_exp0.2.2 结果摘要

## 一句话结论

`ver0.2` 的 llm 路径后端已经建立起 `preprocessed <-> original` 的双向映射，并且在 `llm_line_mode = preprocessed` 下，快照和 `PromptBuilder` 对外看到的 path anchors 已经统一回原始源码行号；当前 smoke run 仍可稳定跑通。

## 本次验证确认的能力

- `LLMCFGAnalyzer` 已支持 `llm_line_mode`
- llm 后端已暴露：
  - `preprocessed_to_original_line`
  - `original_to_preprocessed_line`
- skeleton 已同时保留原始/预处理两套行号基础
- path anchors 已可在内部按模式解释，并对外投影到原始源码行号

## 快照侧关键证据

- `meta.json` 中已记录：
  - `path_cfg_backend = llm`
  - `llm_line_mode = preprocessed`
- `prompt_builder_cfg` 阶段已额外生成：
  - `cfg_preprocessed_to_original_line.json`
  - `cfg_original_to_preprocessed_line.json`

关键文件：

- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_005051_6185a7b2/003_prompt_builder_cfg/meta.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_005051_6185a7b2/003_prompt_builder_cfg/cfg_preprocessed_to_original_line.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_005051_6185a7b2/003_prompt_builder_cfg/cfg_original_to_preprocessed_line.json`

## 行号映射观察

- `cfg_file_obj.json` 中 `_llm_conditions`、`_llm_effects`、`_llm_coverage_anchors` 当前已体现为原始源码行号
- 例如：
  - `getRecords()` 的 anchors 落在 `451/452`
  - `nextRecord()` 的 anchors 落在 `592/594` 等高位原始源码行
- 这说明：
  - LLM 可继续在预处理行号空间工作
  - 但下游消费与快照观察已回到原始源码口径

## smoke run 行为

- 主流程完整跑通
- baseline generation 通过 5 个测试：
  - `testParseFileNull`
  - `testParseFormatNull`
  - `testParseFileIOException`
  - `testGetRecordsEmpty`
  - `testGetRecordsSingleRecord`
- 最终 coverage：`47.52% / 30.36%`

## 当前仍存在的问题

- 运行日志中仍混有一段原始 JSON/YAML 输出，日志通道不够干净
- 当前只验证了 `llm_line_mode = preprocessed`
- 尚未做 `original vs preprocessed` 的正式对照实验
- 还未把 path score 明细写入快照，因此仍只能验证“可打分性”，不能直接验证“打分准确性”

## 对后续的启示

- 现在已经具备进入 `WP4` 的基础数据契约：
  - 条件锚点
  - 效果锚点
  - 覆盖锚点
  - 原始/预处理双向映射
- 下一步应优先：
  - 设计并实现 path score breakdown
  - 将 score 明细写入快照
  - 再做 `llm_line_mode = original` 的对照实验
