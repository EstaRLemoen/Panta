# Smoke Test: temp02-3iter R2 (repeat verification)

- **Date**: 2026-03-31 01:43
- **Author**: AI (glm-5.1)
- **Subject**: CSVParser (Csv-16f)
- **Baseline**: `baselines/CSVParser_baseline_only_test.java`
- **Repeat of**: `2026-03-31_01:12:52_from-baseline-temp02-3iter_CSVParser`

## Config

Same as R1: gpt-4o-mini, selection_mode=llm, threshold=30, temp=0.2, max_iter=3, fix=3

## Purpose

Verify R1 results are reproducible.

## Timeline

| Time | Event |
|---|---|
| 01:44:10 | Start, baseline 2.97%/0% |
| 01:50:31 | Iter 0: +22.77% line, +3.57% branch → **25.74%/3.57%** (light advice: 3 designs, passed: `testClosedParserIterator`) |
| 01:55:42 | Iter 1: +38.62% line, +39.29% branch → **64.36%/42.86%** (light advice still, <30%: passed `testCSVParserWithEmptyHeader`) |
| 02:00:46 | Iter 2: no growth (full advice, trailing delimiter locked, all runtime errors) |
| 02:00:46 | End, max iterations reached |

## Results

**Final coverage: 64.36% line / 42.86% branch** (from baseline 2.97%/0%)

### Passed tests (new, 2 total)
- `testClosedParserIterator` (Iter 0, light advice) — iterator/close behavior
- `testCSVParserWithEmptyHeader` (Iter 1, light advice) — header initialization

### Comparison with R1

| Metric | R1 | R2 |
|---|---|---|
| Final line | 65.35% | 64.36% |
| Final branch | 53.57% | 42.86% |
| Passed tests | 3 | 2 |
| Iter 0 coverage | 64.36% | 25.74% |
| Feedback helped? | Yes (Iter 2 switched) | Iter 2 still locked on trailing delimiter |

### Observations
- Iter 0 in R2 was much weaker than R1 (25% vs 64%). Only `testClosedParserIterator` passed — a different type of test (exception-based).
- Iter 1 (still light advice, <30%) delivered a big jump to 64% via `testCSVParserWithEmptyHeader`.
- Iter 2 (full advice) locked onto trailing delimiter again, all runtime errors, no growth.
- Feedback mechanism did NOT cause a switch in R2 — only 1 no-growth iteration before max was reached.
- CFG snapshot: `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_014410_69a2cad7`
