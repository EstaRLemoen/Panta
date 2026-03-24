# ver0.2_exp0.2.4 运行索引

## 固定条件

- subject：`Csv-16f`
- focal file：`defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java`
- 基线：用户手动清理旧测试残留后重新运行
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
- 日志：`cfg_snapshot_test/validation_runs/ver0.3_exp0.3.2_smoke3_rerun.log`
- snapshot root：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d`
- prompt builder snapshots：
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/003_prompt_builder_cfg`
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/004_prompt_builder_cfg`
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_154335_23bb189d/005_prompt_builder_cfg`
- 最终 coverage：`57.43% / 23.21%`

## 运行说明

- 这是 `WP3.5` 在清理旧测试残留后的 rerun
- 目的在于区分：
  - coverage 差是因为 path 接口没接上
  - 还是因为 path / test 质量本身仍不稳定
