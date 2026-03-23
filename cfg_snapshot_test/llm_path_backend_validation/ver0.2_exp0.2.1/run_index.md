# ver0.2_exp0.2.1 运行索引

## 固定条件

- subject：`Csv-16f`
- focal file：`defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java`
- 基线：no-tests（运行前删除 `CSVParserTest.java`）
- `path_cfg_backend = llm`
- `maximum_iterations = 1`
- `enable_fixing = 0`
- `prompt_type = control`
- `pick_two_paths = true`
- `dump_cfg_intermediate = true`
- `cfg_dump_level = full`
- `cfg_dump_prompt_mode = summary`

## 本次 smoke run

- backend：`llm`
- 日志：`cfg_snapshot_test/validation_runs/wp2_smoke_llm.log`
- snapshot root：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260323_212946_f37558c0`
- prompt builder snapshot：
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260323_212946_f37558c0/003_prompt_builder_cfg`
- 最终 coverage：`2.97% / 0.0%`

## 代码基线

- 提交锚点：`260de5b`
- 实验对应代码版本：`ver0.2` 的 `WP1 + WP2` 首次提交实现

## 运行说明

- 本次不是 A/B 对照，而是 `ver0.2` 的单后端 smoke 验证
- 目的在于确认 `WP1` 和 `WP2` 的最小实现已经可运行，并观察快照与生成质量
