# 2026-03-30_05:00:00_from-baseline_CSVParser

AI note.

## What this smoke run was for

Run a normal smoke from the saved baseline-only CSVParser test file, to verify the new low-coverage routing:

- iteration 0 should use `llm-direct`
- after one no-growth iteration, iteration 1 should switch to advice mode

## Result

- The run completed normally.
- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage stayed at line `2.97%`, branch `0.0%`.
- No passing higher-value tests were added.

## Routing Observed

- Snapshot `002_prompt_builder_llm_direct_selection` confirms iteration 0 used the new direct-annotated prompt.
- Snapshots `003_prompt_builder_llm_advice` and `004_prompt_builder_llm_selection` confirm iteration 1 switched to advice-guided mode after no growth.

## Notes

- The new routing logic is behaving as intended.
- However, neither the direct low-coverage prompt nor the follow-up advice-guided prompt was able to lift coverage above the baseline-only start point.
- Iteration 0 drifted toward broad "valid input" tests; iteration 1 switched back into the more structured trailing-delimiter style, but still failed to produce a passing test.
