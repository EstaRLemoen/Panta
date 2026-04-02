# CSVFormat Repeat 25-Iteration Two-Round Analysis

Date: `2026-04-02`

## Experiment Config And Source Data

Subject under test:

- Source file: `defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java`
- Test file: `defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java`
- Test command: `mvn clean package -Dtest=CSVFormatTest`

Batch log:

- `/home/esta/Panta/Panta/offline_runs/repeat_csvformat_25iter_2rounds.log`

Common config from `info.md` and the batch log:

| Setting | Value |
|---|---|
| `maximum_iterations` | `25` |
| `enable_fixing` | `3` |
| `no_coverage_increase_iterations` | `3` |
| `llm_advice_activation_line_coverage` | `100` |
| `enable_advice_feedback` | `false` |
| `junit_version` | `4` |

Round artifacts used:

| Round | Run dir | CFG snapshot dir | Snapshot evidence used |
|---|---|---|---|
| R1 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-02_02:13:37_repeat-csvformat-25iter_r1_CSVFormat` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_021352_22667006` | snapshot root listing, `003/004` metadata, `rendered_prompt_context.json` advice summaries |
| R2 | `/home/esta/Panta/Panta/smoke_test_log/2026-04-02_03:06:16_repeat-csvformat-25iter_r2_CSVFormat` | `/home/esta/Panta/Panta/cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_030637_a4987b86` | snapshot root listing, `003/004` and `033/034` metadata, `rendered_prompt_context.json` advice summaries |

Method actually used for both rounds:

- line of investigation 1: `runtime.log` for generated designs, pass/fail status, and per-iteration coverage changes
- line of investigation 2: matching CFG snapshot directories for per-iteration prompt-builder stages and persisted `advice_summary`

## Final Coverage And Stop Condition

| Round | Final line | Final branch | Last logged iteration | Stop message |
|---|---:|---:|---:|---|
| R1 | `42.05%` | `27.2%` | `12` | `Reached maximum iteration limit without improving coverage. Current Coverage: (42.05%, 27.2%)` |
| R2 | `50.0%` | `41.38%` | `16` | `Reached maximum iteration limit without improving coverage. Current Coverage: (50.0%, 41.38%)` |

## R1 Per-Iteration Table

Snapshot pairing for R1:

- `003/004` -> iteration 1
- `005/006` -> iteration 2
- ...
- `025/026` -> iteration 12

All inspected R1 snapshot pairs stay centered on the same advice family: null-header handling in `CSVFormat`, with minor wording shifts such as `creation`, `initialization`, and `handling of null header`.

| Iteration | Snapshot stages | Snapshot advice summary | Successful generated tests visible before coverage check | Coverage result |
|---|---|---|---|---|
| 0 | none before first coverage gain | baseline prompt only | `testNewFormatWithLineBreakDelimiter`, `testWithDelimiterLineBreak`, `testWithCommentMarkerLineBreak`, `testWithEscapeLineBreak`, `testWithQuoteLineBreak`, `testWithNullString` | `27.95%` / `13.79%` |
| 1 | `003/004` | `Test behavior when header is null during CSVFormat creation.` | `testWithHeaderNull`, `testEqualsDifferentDelimiters`, `testWithCommentMarker` | `30.77%` / `17.24%` |
| 2 | `005/006` | `Test handling of null header in CSVFormat.` | `testPrintAndEscape`, `testEqualsDifferentDelimitersUpdated`, `testWithCommentMarkerUpdated` | `40.77%` / `24.9%` |
| 3 | `007/008` | `Test behavior when header is null during CSVFormat initialization.` | `testWithQuoteNull`, `testWithHeaderNullUpdated`, `testWithEscapeLineBreakUpdated`, `testEqualsDifferentDelimitersUpdatedFixed` | `40.77%` / `25.29%` |
| 4 | `009/010` | `Test behavior when header is null during CSVFormat creation.` | no passing test logged before the coverage check | cannot increase |
| 5 | `011/012` | `Test behavior when header is null during CSVFormat creation.` | `testWithHeaderNullBehavior`, `testEqualsDifferentCSVFormats`, `testWithEscapeNull`, `testWithQuoteNullFixed` | `41.03%` / `25.67%` |
| 6 | `013/014` | `Test behavior when header is null during CSVFormat creation.` | `testEqualsWithNullObject`, `testEqualsWithDifferentClassType`, `testWithHeaderNullBehaviorFixed`, `testWithEscapeNullFixed` | `41.54%` / `26.44%` |
| 7 | `015/016` | `Test behavior when header is null during CSVFormat creation.` | no passing test logged before the coverage check | cannot increase |
| 8 | `017/018` | `Test behavior when header is null during CSVFormat initialization.` | `testWithHeaderNullInitialization`, `testWithQuoteSameAsDelimiter`, `testEqualsDifferentCSVFormatsUpdated` | `41.79%` / `26.82%` |
| 9 | `019/020` | `Test behavior when header is null during CSVFormat initialization.` | `testWithEscapeSameAsDelimiter`, `testWithHeaderNullInitializationFixed`, `testEqualsDifferentDelimitersFixedUpdated`, `testWithQuoteNullFixedUpdated` | `42.05%` / `27.2%` |
| 10 | `021/022` | `Test behavior when header is null during CSVFormat initialization.` | no passing test logged before the coverage check | cannot increase |
| 11 | `023/024` | `Test behavior when header is null during CSVFormat initialization.` | no passing test logged before the coverage check | cannot increase |
| 12 | `025/026` | `Test behavior when header is null during CSVFormat creation.` | `testWithQuoteNullUpdated`, `testWithEscapeLineBreakFixed` | cannot increase; run stops |

R1 narrative:

- The baseline added six line-break/null-string tests and produced the largest single gain of the run.
- After that, the snapshot advice remained narrowly focused on null-header-related framing, while the runtime-generated passing tests broadened into delimiter equality, comment marker, print/escape, null quote/escape, and basic `equals` guard cases.
- The run accumulated many compilation-error attempts between successful tests and stopped at iteration 12 with `42.05%` line coverage.

## R2 Per-Iteration Table

Snapshot pairing for R2:

- `003/004` -> iteration 1
- `005/006` -> iteration 2
- ...
- `033/034` -> iteration 16

R2 snapshot advice changed materially over time. Verified `advice_summary` sequence from `rendered_prompt_context.json`:

- iter 1: `Test behavior when header is null during CSVFormat creation.`
- iter 2: same
- iter 3-4: `Test equality check for CSVFormat instances.`
- iter 5: `Test equality check with identical CSVFormat instances.`
- iter 6: `Test equality check for CSVFormat instances with different quote characters.`
- iter 7-9: `Test equality check for CSVFormat instances with different configurations.`
- iter 10: `Test equality comparison for CSVFormat instances.`
- iter 11: `Test equality check with null object.`
- iter 12: `Test behavior when quote character is set.`
- iter 13: `Test equality comparison with different quote characters.`
- iter 14: `Test behavior when quote character is set.`
- iter 15: `Test handling of null string in CSV format.`
- iter 16: `Test behavior when quote character is set.`

| Iteration | Snapshot stages | Successful generated tests visible before coverage check | Coverage result |
|---|---|---|---|
| 0 | none before first coverage gain | `testNewFormatWithLineBreakDelimiter`, `testWithLineBreakDelimiter`, `testWithLineBreakCommentMarker`, `testWithLineBreakEscapeCharacter`, `testWithLineBreakQuoteCharacter` | `27.69%` / `13.79%` |
| 1 | `003/004` | `testNewFormatWithNullHeader`, `testEqualsWithDifferentFormats`, `testIsCommentMarkerSetWithUnusedMarker` | `30.51%` / `17.24%` |
| 2 | `005/006` | `testWithHeaderNull`, `testEqualsDifferentDelimiters`, `testWithEscapeSameAsDelimiter`, `testWithQuoteSameAsDelimiter` | `31.28%` / `18.01%` |
| 3 | `007/008` | `testHeaderCloningBehavior`, `testEqualsDifferentDelimitersFixed`, `testNullStringHandlingInPrintMethod` | `35.38%` / `22.99%` |
| 4 | `009/010` | `testEqualsWithIdenticalFormats`, `testGetNullString`, `testIsEscapeCharacterSetFalse`, `testIsCommentMarkerSetTrue` | `40.0%` / `29.5%` |
| 5 | `011/012` | `testEqualsWithIdenticalCSVFormats`, `testPrintWithNullValue`, `testGetHeaderCommentsWithEmptyComments`, `testEqualsWithDifferentQuoteModes` | `41.28%` / `31.03%` |
| 6 | `013/014` | `testEqualsWithDifferentQuoteCharacters`, `testEqualsWithNullAndNonNullQuoteCharacters`, `testEqualsWithDifferentEscapeCharacters`, `testEqualsWithDifferentRecordSeparators` | `43.08%` / `33.33%` |
| 7 | `015/016` | `testHashCodeConsistency`, `testPrintWithConfiguredNullString`, `testEqualsWithDifferentQuoteCharactersFixed` | `46.92%` / `37.16%` |
| 8 | `017/018` | `testEqualsWithDifferentDelimiters`, `testIsNullStringSetWhenNullStringIsNotSet`, `testIsQuoteCharacterSetWhenQuoteCharacterIsSet`, `testIsEscapeCharacterSetWhenEscapeCharacterIsSet`, `testWithSkipHeaderRecord` | `47.44%` / `38.31%` |
| 9 | `019/020` | `testEqualsWithDifferentDelimitersFixed`, `testHashCodeConsistencyFixed`, `testPrintHandlesNullValueFixed` | `47.95%` / `38.31%` |
| 10 | `021/022` | `testWithTrimEnabled`, `testEqualsIdenticalCSVFormats`, `testEqualsDifferentCSVFormats`, `testPrintWithNullValueFixed` | `48.21%` / `38.7%` |
| 11 | `023/024` | `testEqualsWithNullObject`, `testEqualsWithDifferentClassTypes`, `testEqualsWithMatchingQuoteCharacters`, `testEqualsWithDifferentNullStrings` | `49.23%` / `40.23%` |
| 12 | `025/026` | `testEqualsWithQuoteCharacterSet`, `testEqualsWithCommentMarkerSet`, `testEqualsWithEscapeCharacterSet`, `testEqualsWithNullStringSet` | `49.74%` / `41.0%` |
| 13 | `027/028` | `testEqualsWithNullCommentMarker`, `testEqualsWithDifferentEscapeCharactersFixed`, `testEqualsWithDifferentNullStringsFixed` | `50.0%` / `41.38%` |
| 14 | `029/030` | `testEqualsWithQuoteCharacterSetFixed`, `testEqualsWithCommentMarkerSetFixed`, `testEqualsWithEscapeCharacterSetFixed`, `testEqualsWithNullStringSetFixed` | cannot increase |
| 15 | `031/032` | `testEqualsWithNullStringSetToNull`, `testEqualsWithIgnoreEmptyLines`, `testEqualsWithDifferentRecordSeparatorsFixed` | cannot increase |
| 16 | `033/034` | `testEqualsWithDifferentQuoteCharactersSet`, `testEqualsWithDifferentEscapeCharactersSet`, `testEqualsWithDifferentNullStringsSet`, `testEqualsWithDifferentCommentMarkersSet` | cannot increase; run stops |

R2 narrative:

- R2 widened much more than R1. Snapshot advice moved from null-header checks into repeated `equals`-oriented themes, then into quote/comment/escape/null-string comparisons.
- The run logged many compilation-error attempts, but it also kept accumulating successful passing tests across 17 coverage checkpoints, reaching `50.0%` line and `41.38%` branch coverage.
- `backup/CSVFormatTest_after.java` confirms that the final test file retained the later equality/configuration tests listed in the table.

## R1 Vs R2

| Item | R1 | R2 |
|---|---|---|
| Final line coverage | `42.05%` | `50.0%` |
| Final branch coverage | `27.2%` | `41.38%` |
| Last logged iteration | `12` | `16` |
| Snapshot prompt-builder pairs present | `12` pairs (`003-026`) | `16` pairs (`003-034`) |
| Snapshot advice drift | narrow, mostly null-header wording | broad, mostly `equals` and configuration comparison wording |

Main factual takeaways:

- R1 and R2 started from similar line-break guard tests, but they diverged immediately after iteration 0.
- R1's snapshot advice stayed concentrated on null-header framing even when later passing tests touched other behaviors.
- R2's snapshot advice evolved through several equality/configuration themes, and its runtime logs show a much larger surviving test surface.
- R2 finished `7.95` line-coverage points and `14.18` branch-coverage points above R1.

## Data Caveats

- Snapshot directories preserve prompt-builder metadata and rendered context, not the entire execution history of every failing candidate. The exact list of failed candidates was taken from `runtime.log`.
- The snapshot `advice_summary` is only one field of the prompt context. The per-iteration tables therefore use both snapshot advice and the runtime-visible passing test names together.
- This document records artifact-backed facts only and avoids causal claims.
