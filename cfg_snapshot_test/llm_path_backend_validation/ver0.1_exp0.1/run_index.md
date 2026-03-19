# ver0.1_exp0.1 运行索引

## 固定条件

- subject：`Csv-16f`
- focal file：`defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java`
- 基线：no-tests（运行前删除 `CSVParserTest.java`）
- `maximum_iterations = 2`
- `enable_fixing = 0`
- `prompt_type = control`
- `pick_two_paths = true`
- `dump_cfg_intermediate = true`
- `cfg_dump_level = full`
- `cfg_dump_prompt_mode = summary`

## A 组：comex

- backend：`comex`
- 日志：`cfg_snapshot_test/validation_runs/ab_comex.log`
- snapshot root：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260320_011936_1166d569`
- prompt builder snapshots：
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260320_011936_1166d569/003_prompt_builder_cfg`
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260320_011936_1166d569/004_prompt_builder_cfg`
- 最终 coverage：`51.49% / 30.36%`

## B 组：llm

- backend：`llm`
- 日志：`cfg_snapshot_test/validation_runs/ab_llm.log`
- snapshot root：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260320_012946_c3cf324e`
- prompt builder snapshots：
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260320_012946_c3cf324e/003_prompt_builder_cfg`
  - `cfg_snapshot_test/intermediate/cfg/CSVParser_20260320_012946_c3cf324e/004_prompt_builder_cfg`
- 最终 coverage：`59.41% / 33.93%`

## 代码版本

- 实验对应代码提交：`ca318ac`

## 运行说明

- 两组都从相同 no-tests 基线启动
- 唯一实验变量为 `path_cfg_backend`
- `src/panta/config.ini` 在实验过程中被临时改写，不应作为版本基线依据
