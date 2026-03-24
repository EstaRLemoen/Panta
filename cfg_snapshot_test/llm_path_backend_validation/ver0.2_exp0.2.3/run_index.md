# ver0.2_exp0.2.3 运行索引

## 固定条件

- subject：`Csv-16f`
- focal file：`defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java`
- baseline：当时测试文件中仍保留了先前实验残留内容，不是干净 no-tests 基线
- `path_cfg_backend = llm`
- `llm_line_mode = preprocessed`
- `maximum_iterations = 3`
- `enable_fixing = 0`
- `prompt_type = control`
- `pick_two_paths = true`
- `dump_cfg_intermediate = true`
- `cfg_dump_level = full`
- `cfg_dump_prompt_mode = summary`

## 本次 smoke run

- backend：`llm`
- line mode：`preprocessed`
- 日志：`cfg_snapshot_test/validation_runs/ver0.3_exp0.3.1_smoke3.log`
- snapshot root：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_152129_9692f8e9`
- prompt builder snapshots：
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_152129_9692f8e9/002_prompt_builder_cfg`
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_152129_9692f8e9/004_prompt_builder_cfg`
- 最终 coverage：`0.99% / 0.0%`

## 运行说明

- 这是 `WP3.5` 第一轮 smoke，不是公平对照，也不是最终基线结果
- 该轮主要目标是确认：
  - 新 schema 已进入真实主流程
  - `segments` / `transitions` 已落到 snapshot
  - 主流程在输出变长后是否还能维持基本可运行
- 该轮结果受“旧测试残留”影响较大，因此更适合视为技术连通性验证，而不是 coverage 表现结论
