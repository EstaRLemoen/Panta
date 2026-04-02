# 结果记录：CSVParser 三次重复实验（11 iter）

**日期**: 2026-04-02
**作者**: OpenCode (gpt-5.4)
**对象**: `CSVParser` (`defects4j-subjects-notests/Csv-16f`)
**性质**: 结果整理，仅记录事实，不做原因推断

## 实验脚本

- 脚本: `offline_runs/repeat_csvparser_11iter_3rounds.sh`
- 总日志: `offline_runs/repeat_csvparser_11iter_3rounds.log`

脚本参数如下：

| 参数 | 值 |
|---|---|
| `batch-name` | `repeat-csvparser-11iter` |
| `rounds` | `3` |
| `project_directory` | `defects4j-subjects-notests/Csv-16f` |
| `source_code_file` | `defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java` |
| `test_code_file` | `defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVParserTest.java` |
| `test_execution_command` | `mvn clean package -Dtest=CSVParserTest` |
| `maximum_iterations` | `11` |
| `enable_fixing` | `3` |
| `no_coverage_increase_iterations` | `3` |
| `llm_advice_activation_line_coverage` | `100` |
| `enable_advice_feedback` | `false` |

## 数据来源

| Round | 运行目录 | CFG 快照目录 |
|---|---|---|
| R1 | `smoke_test_log/2026-04-01_20:14:55_repeat-csvparser-11iter_r1_CSVParser/` | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260401_201517_b47fbeda/` |
| R2 | `smoke_test_log/2026-04-01_21:12:44_repeat-csvparser-11iter_r2_CSVParser/` | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260401_211303_324e4927/` |
| R3 | `smoke_test_log/2026-04-01_22:09:10_repeat-csvparser-11iter_r3_CSVParser/` | `cfg_snapshot_test/intermediate/cfg/CSVParser_20260401_220925_681e8e0f/` |

## 最终覆盖率

| Round | 最终行覆盖率 | 最终分支覆盖率 | 结束方式 |
|---|---|---|---|
| R1 | `80.2%` | `76.79%` | 达到 `maximum_iterations=11` 后结束 |
| R2 | `76.24%` | `66.07%` | 达到 `maximum_iterations=11` 后结束 |
| R3 | `72.28%` | `69.64%` | 达到 `maximum_iterations=11` 后结束 |

对应日志中的结束记录：

- R1: `Reached maximum iteration limit without achieving desired coverage. Current Coverage: (80.2%, 76.79%)`
- R2: `Reached maximum iteration limit without achieving desired coverage. Current Coverage: (76.24%, 66.07%)`
- R3: `Reached maximum iteration limit without achieving desired coverage. Current Coverage: (72.28%, 69.64%)`

## 覆盖率轨迹

### R1

| 迭代 | 行覆盖累计 | 分支覆盖累计 | 日志状态 |
|---|---|---|---|
| baseline | `0.0%` | `0.0%` | baseline |
| 0 | `34.65%` | `10.71%` | increased |
| 1 | `53.47%` | `33.93%` | increased |
| 2 | `53.47%` | `33.93%` | cannot increase |
| 3 | `53.47%` | `33.93%` | cannot increase |
| 4 | `67.33%` | `57.14%` | increased |
| 5 | `74.26%` | `69.64%` | increased |
| 6 | `74.26%` | `69.64%` | cannot increase |
| 7 | `80.2%` | `76.79%` | increased |
| 8 | `80.2%` | `76.79%` | cannot increase |
| 9 | `80.2%` | `76.79%` | cannot increase |
| 10 | `80.2%` | `76.79%` | cannot increase |

### R2

| 迭代 | 行覆盖累计 | 分支覆盖累计 | 日志状态 |
|---|---|---|---|
| baseline | `0.0%` | `0.0%` | baseline |
| 0 | `47.52%` | `30.36%` | increased |
| 1 | `54.46%` | `37.5%` | increased |
| 2 | `59.41%` | `39.29%` | increased |
| 3 | `59.41%` | `39.29%` | cannot increase |
| 4 | `59.41%` | `39.29%` | cannot increase |
| 5 | `76.24%` | `62.5%` | increased |
| 6 | `76.24%` | `64.29%` | increased |
| 7 | `76.24%` | `64.29%` | cannot increase |
| 8 | `76.24%` | `66.07%` | increased |
| 9 | `76.24%` | `66.07%` | cannot increase |
| 10 | `76.24%` | `66.07%` | cannot increase |

### R3

| 迭代 | 行覆盖累计 | 分支覆盖累计 | 日志状态 |
|---|---|---|---|
| baseline | `0.0%` | `0.0%` | baseline |
| 0 | `2.97%` | `0.0%` | increased |
| 1 | `23.76%` | `3.57%` | increased |
| 2 | `23.76%` | `3.57%` | cannot increase |
| 3 | `50.5%` | `33.93%` | increased |
| 4 | `70.3%` | `60.71%` | increased |
| 5 | `70.3%` | `60.71%` | cannot increase |
| 6 | `70.3%` | `62.5%` | increased |
| 7 | `72.28%` | `67.86%` | increased |
| 8 | `72.28%` | `69.64%` | increased |
| 9 | `72.28%` | `69.64%` | cannot increase |
| 10 | `72.28%` | `69.64%` | cannot increase |

## 覆盖率汇总表

| Iter | R1 Line | R2 Line | R3 Line | R1 Branch | R2 Branch | R3 Branch |
|---|---|---|---|---|---|---|
| baseline | `0.0%` | `0.0%` | `0.0%` | `0.0%` | `0.0%` | `0.0%` |
| 0 | `34.65%` | `47.52%` | `2.97%` | `10.71%` | `30.36%` | `0.0%` |
| 1 | `53.47%` | `54.46%` | `23.76%` | `33.93%` | `37.5%` | `3.57%` |
| 2 | `53.47%` | `59.41%` | `23.76%` | `33.93%` | `39.29%` | `3.57%` |
| 3 | `53.47%` | `59.41%` | `50.5%` | `33.93%` | `39.29%` | `33.93%` |
| 4 | `67.33%` | `59.41%` | `70.3%` | `57.14%` | `39.29%` | `60.71%` |
| 5 | `74.26%` | `76.24%` | `70.3%` | `69.64%` | `62.5%` | `60.71%` |
| 6 | `74.26%` | `76.24%` | `70.3%` | `69.64%` | `64.29%` | `62.5%` |
| 7 | `80.2%` | `76.24%` | `72.28%` | `76.79%` | `64.29%` | `67.86%` |
| 8 | `80.2%` | `76.24%` | `72.28%` | `76.79%` | `66.07%` | `69.64%` |
| 9 | `80.2%` | `76.24%` | `72.28%` | `76.79%` | `66.07%` | `69.64%` |
| 10 | `80.2%` | `76.24%` | `72.28%` | `76.79%` | `66.07%` | `69.64%` |

## 运行时结果计数

以下计数来自各轮 `runtime.log` 中的日志文本匹配：

| Round | `Test generated failed due to runtime error.` 次数 | `compile error` 相关匹配次数 | `test_designs:` 次数 |
|---|---|---|---|
| R1 | `48` | `0` | `10` |
| R2 | `77` | `0` | `10` |
| R3 | `69` | `0` | `10` |

## 直接可见的原始观察

1. 三轮都从 `0.0% / 0.0%` baseline 开始。
2. 三轮都执行到了 `Iteration 10`，然后因达到 `maximum_iterations=11` 结束。
3. 三轮最终行覆盖率分别为 `80.2%`、`76.24%`、`72.28%`。
4. 三轮最终分支覆盖率分别为 `76.79%`、`66.07%`、`69.64%`。
5. 三轮 `runtime.log` 中都出现了 `10` 次 `test_designs:` 区块。
6. 当前统计中未匹配到 compile error 相关日志文本；匹配到的失败日志均为 runtime error 文本。
7. 总批量日志末尾记录了 `Config restored to original state.`。

## 备注

- 本文档只整理结果，不对差异原因、prompt 质量、fixing 能力或 advice 结构做解释。
- 若后续需要做分析，应以本文档中的运行目录、快照目录和总日志为溯源入口。
