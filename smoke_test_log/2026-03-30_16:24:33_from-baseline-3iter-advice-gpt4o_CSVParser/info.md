# 2026-03-30_16:24:33_from-baseline-3iter-advice-gpt4o_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with `maximum_iterations = 3` and `llm_path_advice_model = gpt-4o`.

## Result

- The run completed normally.
- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `25.74%`, branch `3.57%`.

## Iteration Summary

- Iteration 0 did not increase coverage.
- Iteration 1 did not increase coverage.
- Iteration 2 increased coverage to line `25.74%`, branch `3.57%`.

## Passing Tests Observed

- `testCSVParserWithClosedLexer`

## Notes

- Snapshot stages show all three iterations used the lightweight-advice path followed by final guided generation.
- This run slightly outperformed the `gpt-4o-mini` advice run on final line coverage, but both remained far below the earlier successful baseline-lift run.
