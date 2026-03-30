# 2026-03-30_17:23:15_from-baseline-temp05_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with `llm_path_advice_model = gpt-4o-mini`, `maximum_iterations = 3`, and `llm_light_advice_temperature = 0.5`.

## Result

- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `51.49%`, branch `39.29%`.

## Iteration Summary

- Iteration 0 did not increase coverage.
- Iteration 1 did not increase coverage.
- Iteration 2 increased coverage to line `51.49%`, branch `39.29%`.

## Passing Tests Observed

- `testCSVParserHeaderInitializationWithDuplicates`

## Notes

- The early iterations spent a lot of effort on failing or compiling-bad tests.
- This run recovered late and ended close to the `0.2` run, but still slightly worse overall.
- The first successful lift happened only in the final iteration.
- `backup/test_before.java` is the true run start file and was replaced with the saved baseline fixture.
- `backup/original_test_before.java` preserves the pre-run working-tree test file that existed before the baseline fixture was copied in.
