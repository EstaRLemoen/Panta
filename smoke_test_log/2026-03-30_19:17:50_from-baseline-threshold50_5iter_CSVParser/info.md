# 2026-03-30_19:17:50_from-baseline-threshold50_5iter_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with:

- `llm_path_advice_model = gpt-4o-mini`
- `llm_light_advice_temperature = 0.2`
- `maximum_iterations = 5`
- `llm_advice_activation_line_coverage = 50`

## Result

- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `51.49%`, branch `39.29%`.

## Iteration Summary

- Iteration 0: no increase.
- Iteration 1: increased coverage to line `51.49%`, branch `39.29%`.
- Iteration 2: no increase.
- Iteration 3: no increase.
- Iteration 4: no increase.

## Passing Tests Observed

- `testDuplicateHeaderHandling`

## Notes

- Snapshot stages show two light-advice iterations first, then advice-guided iterations after the threshold/routing switch.
- This run was clearly weaker than the threshold-30 comparison on both line and branch coverage.
- `backup/test_before.java` is the true run start file and was replaced with the saved baseline fixture.
- `backup/original_test_before.java` preserves the pre-run working-tree test file that existed before the baseline fixture was copied in.
- `backup/config.ini.snapshot` is the reconstructed effective config used for this run.
- `backup/original_config.ini.backup` preserves the pre-run working-tree config that existed before the smoke-specific edits.
