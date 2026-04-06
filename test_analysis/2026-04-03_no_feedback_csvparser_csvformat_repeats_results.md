# No-Feedback Repeat Results: CSVParser And CSVFormat

Date: 2026-04-03

Scope:
- Batch log: `offline_runs/run_no_feedback_csvparser_csvformat_repeats.log`
- Feedback setting: `enable_advice_feedback=false`
- Baseline: both targets started without an initial test file

## Run Directories

### CSVParser

- `smoke_test_log/2026-04-03_05:45:09_no-feedback-csvparser-11iter_r1_CSVParser`
- `smoke_test_log/2026-04-03_07:01:03_no-feedback-csvparser-11iter_r2_CSVParser`
- `smoke_test_log/2026-04-03_07:58:34_no-feedback-csvparser-11iter_r3_CSVParser`

### CSVFormat

- `smoke_test_log/2026-04-03_09:02:17_no-feedback-csvformat-25iter_r1_CSVFormat`
- `smoke_test_log/2026-04-03_11:09:07_no-feedback-csvformat-25iter_r2_CSVFormat`

## Effective Settings

- `enable_advice_feedback=false`
- `enable_fixing=3`
- `no_coverage_increase_iterations=3`
- `llm_advice_activation_line_coverage=100`
- `CSVParser maximum_iterations=11`
- `CSVFormat maximum_iterations=25`

## CSVParser Final Results

| Round | Final line coverage | Final branch coverage | End condition |
|---|---:|---:|---|
| R1 | `77.23%` | `71.43%` | Reached max iter 11 |
| R2 | `75.25%` | `58.93%` | Reached no-improvement stop |
| R3 | `68.32%` | `60.71%` | Reached max iter 11 |

### CSVParser Coverage Trajectories

| Iter | R1 line | R1 branch | R2 line | R2 branch | R3 line | R3 branch |
|---|---:|---:|---:|---:|---:|---:|
| baseline | `0.0%` | `0.0%` | `0.0%` | `0.0%` | `0.0%` | `0.0%` |
| 0 | `35.64%` | `10.71%` | `34.65%` | `10.71%` | `2.97%` | `0.0%` |
| 1 | `54.46%` | `33.93%` | `34.65%` | `10.71%` | `23.76%` | `3.57%` |
| 2 | `55.45%` | `39.29%` | `39.6%` | `12.5%` | `50.5%` | `32.14%` |
| 3 | `76.24%` | `64.29%` | `39.6%` | `12.5%` | `50.5%` | `37.5%` |
| 4 | `76.24%` | `64.29%` | `75.25%` | `58.93%` | `52.48%` | `39.29%` |
| 5 | `76.24%` | `66.07%` | `75.25%` | `58.93%` | `68.32%` | `58.93%` |
| 6 | `76.24%` | `67.86%` | `75.25%` | `58.93%` | `68.32%` | `58.93%` |
| 7 | `76.24%` | `67.86%` | `75.25%` | `58.93%` | `68.32%` | `58.93%` |
| 8 | `76.24%` | `67.86%` | — | — | `68.32%` | `60.71%` |
| 9 | `77.23%` | `69.64%` | — | — | `68.32%` | `60.71%` |
| 10 | `77.23%` | `71.43%` | — | — | `68.32%` | `60.71%` |

## CSVFormat Final Results

| Round | Final line coverage | Final branch coverage | End condition |
|---|---:|---:|---|
| R1 | `68.21%` | `57.47%` | Reached max iter 25 |
| R2 | `65.9%` | `52.49%` | Reached no-improvement stop |

### CSVFormat Coverage Trajectories

| Iter | R1 line | R1 branch | R2 line | R2 branch |
|---|---:|---:|---:|---:|
| baseline | `0.0%` | `0.0%` | `0.0%` | `0.0%` |
| 0 | `27.95%` | `13.79%` | `29.49%` | `16.48%` |
| 1 | `30.0%` | `14.56%` | `35.9%` | `22.22%` |
| 2 | `35.64%` | `22.22%` | `36.15%` | `22.22%` |
| 3 | `36.41%` | `23.37%` | `40.77%` | `29.12%` |
| 4 | `55.13%` | `39.85%` | `41.54%` | `30.65%` |
| 5 | `55.13%` | `40.23%` | `41.54%` | `30.65%` |
| 6 | `56.67%` | `42.15%` | `44.1%` | `32.95%` |
| 7 | `56.67%` | `42.15%` | `57.95%` | `44.44%` |
| 8 | `56.67%` | `42.15%` | `58.46%` | `45.98%` |
| 9 | `56.92%` | `42.53%` | `59.23%` | `46.74%` |
| 10 | `56.92%` | `42.53%` | `59.49%` | `46.74%` |
| 11 | `57.18%` | `42.53%` | `65.13%` | `50.96%` |
| 12 | `58.72%` | `44.83%` | `65.38%` | `51.34%` |
| 13 | `59.49%` | `45.98%` | `65.64%` | `51.72%` |
| 14 | `61.03%` | `48.28%` | `65.64%` | `52.11%` |
| 15 | `61.03%` | `48.66%` | `65.64%` | `52.11%` |
| 16 | `61.28%` | `48.66%` | `65.9%` | `52.49%` |
| 17 | `61.54%` | `49.04%` | `65.9%` | `52.49%` |
| 18 | `62.82%` | `50.96%` | `65.9%` | `52.49%` |
| 19 | `63.08%` | `51.34%` | `65.9%` | `52.49%` |
| 20 | `63.08%` | `51.34%` | — | — |
| 21 | `63.33%` | `51.72%` | — | — |
| 22 | `67.95%` | `56.32%` | — | — |
| 23 | `68.21%` | `56.7%` | — | — |
| 24 | `68.21%` | `57.47%` | — | — |

## Minimal Factual Notes

1. This batch is the current no-feedback baseline for later feedback comparisons.
2. `CSVParser` still shows substantial round-to-round variance: `77.23%`, `75.25%`, `68.32%` line coverage.
3. `CSVFormat` is more stable in this batch: `68.21%` and `65.9%` line coverage.
4. The batch completed fully. The log ends with `All runs complete` and `Config restored to original state.`
