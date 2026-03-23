# ver0.2_exp0.2.2 运行索引

## 固定条件

- subject：`Csv-16f`
- focal file：`defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java`
- 基线：no-tests（运行前删除 `CSVParserTest.java`）
- `path_cfg_backend = llm`
- `llm_line_mode = preprocessed`
- `maximum_iterations = 1`
- `enable_fixing = 0`
- `prompt_type = control`
- `pick_two_paths = true`
- `dump_cfg_intermediate = true`
- `cfg_dump_level = full`
- `cfg_dump_prompt_mode = summary`

## 本次 smoke run

- backend：`llm`
- line mode：`preprocessed`
- 日志：`cfg_snapshot_test/validation_runs/post_denoise_smoke_llm.log`
- snapshot root：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_005051_6185a7b2`
- prompt builder snapshot：
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_005051_6185a7b2/003_prompt_builder_cfg`
- 最终 coverage：`47.52% / 30.36%`

## 代码基线

- 提交锚点：`d4f6fd5`
- 实验对应代码版本：`ver0.2` 的行号映射、`llm_line_mode` 与快照增强首次提交实现

## 运行说明

- 本次不是 A/B 对照，而是 `ver0.2` 行号映射能力的 smoke 验证
- 目的在于确认：
  - 行号映射层已生效
  - 对外暴露的 path anchors 已回到原始源码行号
  - 快照已能记录 line mode 及映射文件
