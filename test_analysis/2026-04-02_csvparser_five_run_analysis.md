# CSVParser Five-Run Analysis

Date: `2026-04-02`

## Scope

This document combines:

- the previously reconstructed facts in `/home/esta/Panta/Panta/test_analysis/2026-04-02_repeat_csvparser_11iter_3rounds_results.md`
- two additional Apr 2 runs:
  - `/home/esta/Panta/Panta/smoke_test_log/2026-04-02_04:47:09_apr02-small-Csv-16f-CSVParser_r1_CSVParser`
  - `/home/esta/Panta/Panta/smoke_test_log/2026-04-02_05:40:07_apr02-small-Csv-16f-CSVParser_r2_CSVParser`

The tables below were verified against `runtime.log`, `info.md`, `backup/CSVParserTest_after.java`, and the matching CFG snapshot roots.

## Experiment Config And Sources

Subject under test:

- Source file: `defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java`
- Test file: `defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVParserTest.java`
- Test command: `mvn clean package -Dtest=CSVParserTest`

Common config in all five runs:

| Setting | Value |
|---|---|
| `enable_fixing` | `3` |
| `llm_advice_activation_line_coverage` | `100` |
| `enable_advice_feedback` | `false` |
| `junit_version` | `4` |

Run artifacts used:

| Run | Run dir | CFG snapshot dir | Max iter | Final coverage |
|---|---|---|---:|---|
| Repeat R1 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-01_20:14:55_repeat-csvparser-11iter_r1_CSVParser` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVParser_20260401_201517_b47fbeda` | `11` | `80.2%` line / `76.79%` branch |
| Repeat R2 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-01_21:12:44_repeat-csvparser-11iter_r2_CSVParser` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVParser_20260401_211303_324e4927` | `11` | `76.24%` line / `66.07%` branch |
| Repeat R3 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-01_22:09:10_repeat-csvparser-11iter_r3_CSVParser` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVParser_20260401_220925_681e8e0f` | `11` | `72.28%` line / `69.64%` branch |
| Small R1 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-02_04:47:09_apr02-small-Csv-16f-CSVParser_r1_CSVParser` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVParser_20260402_044732_7c116200` | `11` | `69.31%` line / `60.71%` branch |
| Small R2 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-02_05:40:07_apr02-small-Csv-16f-CSVParser_r2_CSVParser` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVParser_20260402_054027_2b5e7631` | `11` | `58.42%` line / `37.5%` branch |

## Per-Run Iteration Tables

### Repeat R1

| Iteration | Line | Branch | Log status |
|---|---:|---:|---|
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

Narrative:

- Baseline and early iterations established null-argument and empty-record coverage with `testParseFormatNull`, `testParseInputStreamNull`, `testParseUrlCharsetNull`, and `testGetRecordsEmpty`.
- Mid-run gains came from trailing-delimiter, closed-parser, duplicate-header, spacing, skipped-header, and comment-handling tests.
- The run ended at `Iteration 10 cannot increase coverage` with `Reached maximum iteration limit without achieving desired coverage. Current Coverage: (80.2%, 76.79%)`.

### Repeat R2

| Iteration | Line | Branch | Log status |
|---|---:|---:|---|
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

Narrative:

- Early progress came from `testParseFileFormatNull`, `testParseFileIOException`, `testGetRecordsEmpty`, `testGetRecordsSingleRecord`, and `testGetRecordsWithComments`.
- Later increases came from trailing-delimiter and duplicate-header tests, then branch-only gains from closed-iterator and skip-header variants.
- The run ended at `Iteration 10 cannot increase coverage` with `Reached maximum iteration limit without achieving desired coverage. Current Coverage: (76.24%, 66.07%)`.

### Repeat R3

| Iteration | Line | Branch | Log status |
|---|---:|---:|---|
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

Narrative:

- This run started with null-input parse guards, then moved into closed-parser iteration checks.
- The largest gains came from trailing-delimiter handling, header-map case-sensitivity, header-skipping, invalid-token handling, duplicate-header handling, and whitespace-related tests.
- The run ended at `Iteration 10 cannot increase coverage` with `Reached maximum iteration limit without achieving desired coverage. Current Coverage: (72.28%, 69.64%)`.

### Small R1

| Iteration | Line | Branch | Log status |
|---|---:|---:|---|
| baseline | `0.0%` | `0.0%` | baseline |
| 0 | `2.97%` | `0.0%` | increased |
| 1 | `2.97%` | `0.0%` | cannot increase |
| 2 | `23.76%` | `3.57%` | increased |
| 3 | `23.76%` | `3.57%` | cannot increase |
| 4 | `68.32%` | `55.36%` | increased |
| 5 | `68.32%` | `55.36%` | cannot increase |
| 6 | `69.31%` | `60.71%` | increased |
| 7 | `69.31%` | `60.71%` | cannot increase |
| 8 | `69.31%` | `60.71%` | cannot increase |
| 9 | `69.31%` | `60.71%` | cannot increase |

Narrative:

- Baseline tests were six null-input parse guards: `testParseNullFile`, `testParseNullInputStream`, `testParseNullPath`, `testParseNullReader`, `testParseNullString`, `testParseNullURL`.
- Later successful additions were `testIteratorThrowsExceptionWhenClosed`, `testTrimmedCSVRecords`, `testDuplicateHeaders`, `testEmptyHeaderHandling`, and `testNoHeaderDefined`.
- `backup/CSVParserTest_after.java` contains exactly those ten generated tests plus the placeholder. The run stopped at `Iteration 9 cannot increase coverage` with `Current Coverage: (69.31%, 60.71%)`.

### Small R2

| Iteration | Line | Branch | Log status |
|---|---:|---:|---|
| baseline | `0.0%` | `0.0%` | baseline |
| 0 | `3.96%` | `0.0%` | increased |
| 1 | `53.47%` | `35.71%` | increased |
| 2 | `53.47%` | `35.71%` | cannot increase |
| 3 | `58.42%` | `37.5%` | increased |
| 4 | `58.42%` | `37.5%` | cannot increase |
| 5 | `58.42%` | `37.5%` | cannot increase |
| 6 | `58.42%` | `37.5%` | cannot increase |

Narrative:

- Baseline tests were six null-input guards, but this round used `testParseNullFormat` instead of the path-null variant.
- Subsequent successful additions were `testCommentHandling`, `testClosedParserIteration`, and `testClosedParserDuringIteration`.
- `backup/CSVParserTest_after.java` contains those nine generated tests plus the placeholder. The run stopped at `Iteration 6 cannot increase coverage` with `Current Coverage: (58.42%, 37.5%)`.

## Comparison

| Run | Final line | Final branch | End iteration | Notes from surviving generated tests |
|---|---:|---:|---:|---|
| Repeat R1 | `80.2%` | `76.79%` | `10` | Broadest mix: null handling, delimiter cases, comments, whitespace, headers, duplicate headers, parser-close behavior |
| Repeat R2 | `76.24%` | `66.07%` | `10` | Strong early gains from file/error handling and record-shape cases |
| Repeat R3 | `72.28%` | `69.64%` | `10` | Similar later-stage breadth, but lower final line coverage than Repeat R1/R2 |
| Small R1 | `69.31%` | `60.71%` | `9` | Concentrated on null parse guards, closed iteration, trimming, duplicate/empty/no-header cases |
| Small R2 | `58.42%` | `37.5%` | `6` | Concentrated on null guards, comment handling, and closed-parser iteration |

Main factual differences:

- Among the five runs, `Repeat R1` reached the highest final coverage.
- The two Apr 2 small runs terminated earlier than the three repeat-11iter runs.
- `Small R1` and `Repeat R3` both began with the same six null-input guard tests, but their later successful additions diverged.
- `Small R2` differed immediately at baseline by generating `testParseNullFormat` instead of a null-path case.

## Data Caveats

- For the three repeat runs, the prior facts were taken from `/home/esta/Panta/Panta/test_analysis/2026-04-02_repeat_csvparser_11iter_3rounds_results.md` and rechecked against the corresponding `runtime.log` endings and `backup/CSVParserTest_after.java` files.
- This document only records what is directly visible in the artifacts. It does not infer why one run outperformed another.
