# 2026-03-30_17:23:15_from-baseline-temp02_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with `llm_path_advice_model = gpt-4o-mini`, `maximum_iterations = 3`, and `llm_light_advice_temperature = 0.2`.

## Result

- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `54.46%`, branch `35.71%`.

## Iteration Summary

- Iteration 0 increased coverage to line `52.48%`, branch `32.14%`.
- Iteration 1 did not increase coverage.
- Iteration 2 increased coverage to line `54.46%`, branch `35.71%`.

## Passing Tests Observed

- `testIteratorAfterClose`
- `testRecordWithNullString`

## Notes

- This run gave the best result among the three temperature sweeps.
- It started with lightweight advice, then moved into the heavier advice-guided stages after crossing the coverage threshold.
- `backup/test_before.java` is the true run start file and was replaced with the saved baseline fixture.
- `backup/original_test_before.java` preserves the pre-run working-tree test file that existed before the baseline fixture was copied in.
- `backup/test_after.java` was not preserved for this run.
