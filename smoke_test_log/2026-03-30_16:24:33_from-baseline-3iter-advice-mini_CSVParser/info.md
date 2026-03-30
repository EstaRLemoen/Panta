# 2026-03-30_16:24:33_from-baseline-3iter-advice-mini_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with `maximum_iterations = 3` and `llm_path_advice_model = gpt-4o-mini`.

## Result

- The run completed normally.
- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `23.76%`, branch `3.57%`.

## Iteration Summary

- Iteration 0 increased coverage to line `23.76%`, branch `3.57%`.
- Iteration 1 did not increase coverage.
- Iteration 2 did not increase coverage.

## Passing Tests Observed

- `testClosedParserHasNext`
- `testCsvParserClosedDuringIteration`

## Notes

- Snapshot stages show all three iterations used the lightweight-advice path followed by final guided generation.
- This run improved over the baseline fixture, but it did not recover the much stronger lift seen in the earlier successful light-advice smoke.
