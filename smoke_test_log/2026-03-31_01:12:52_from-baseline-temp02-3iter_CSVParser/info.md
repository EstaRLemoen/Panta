# Smoke Test: temp02-3iter with 4 new changes

- **Date**: 2026-03-31 01:12
- **Author**: AI (glm-5.1)
- **Subject**: CSVParser (Csv-16f)
- **Baseline**: `baselines/CSVParser_baseline_only_test.java`

## Config

| Parameter | Value |
|---|---|
| model | gpt-4o-mini |
| selection_mode | llm |
| llm_advice_activation_line_coverage | 30 |
| llm_advice_activation_no_growth | 3 |
| llm_path_advice_temperature | 0.2 |
| llm_light_advice_temperature | 0.2 |
| maximum_iterations | 3 |
| enable_fixing | 3 |

## Purpose

Validate the 4 code changes made in this session:
1. Weakened light-advice `observable_behavior` (open-ended, not precise prediction)
2. Behavioral hedging instruction in test-gen prompt
3. Advice failure feedback mechanism (error summary fed back to next advice prompt)
4. Relaxed `_normalize_advice` validation in light mode

## Timeline

| Time | Event |
|---|---|
| 01:14:00 | Start, baseline coverage 2.97%/0% |
| 01:19:54 | Iter 0: +61.39% line, +50% branch → **64.36%/50%** (light advice: 3 designs, passed: `testHeaderInitializationWithEmptyHeader`) |
| 01:24:53 | Iter 1: no growth (full advice, trailing delimiter locked, all runtime errors) |
| 01:31:31 | Iter 2: +0.99% line, +3.57% branch → **65.35%/53.57%** (full advice, passed: `testGetHeaderMapReturnsNullForNoHeaderDefined`, `testGetHeaderMapReturnsNullForEmptyHeader`) |
| 01:31:31 | End, max iterations reached |

## Results

**Final coverage: 65.35% line / 53.57% branch** (from baseline 2.97%/0%)

### Passed tests (new, 3 total)
- `testHeaderInitializationWithEmptyHeader` (Iter 0, light advice)
- `testGetHeaderMapReturnsNullForNoHeaderDefined` (Iter 2, full advice)
- `testGetHeaderMapReturnsNullForEmptyHeader` (Iter 2, full advice)

### Observations
- Iter 0 (light advice, <30% coverage): 3 diverse designs generated. `testHeaderInitializationWithEmptyHeader` passed. Trailing delimiter + comment tests failed (compilation errors).
- Iter 1 (full advice, ≥30%): Full advice locked onto trailing delimiter again. All 3 fix rounds → runtime errors. Feedback mechanism triggered for next iteration.
- Iter 2 (full advice, with feedback): Switched to header-related designs. 2 tests passed. Still some trailing delimiter attempts failed.
- **Feedback mechanism appears to have worked**: Iter 2 switched away from trailing delimiter after receiving error feedback from Iter 1.
- CFG snapshot: `cfg_snapshot_test/intermediate/cfg/CSVParser_20260331_011400_565b01c9`
