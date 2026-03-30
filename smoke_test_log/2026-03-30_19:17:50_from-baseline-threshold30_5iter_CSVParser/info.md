# 2026-03-30_19:17:50_from-baseline-threshold30_5iter_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with:

- `llm_path_advice_model = gpt-4o-mini`
- `llm_light_advice_temperature = 0.2`
- `maximum_iterations = 5`
- `llm_advice_activation_line_coverage = 30`

## Result

- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `62.38%`, branch `50.0%`.

## Iteration Summary

- Iteration 0: no increase.
- Iteration 1: no increase.
- Iteration 2: increased coverage to line `62.38%`, branch `50.0%`.
- Iteration 3: no increase.
- Iteration 4: no increase.

## Passing Tests Observed

- `testHeaderInitializationWithEmptyHeader`

## Notes

- Snapshot stages show two light-advice iterations first, then advice-guided iterations after the threshold/routing switch.
- This run outperformed the threshold-50 comparison and gave the best result in this threshold experiment.
- `backup/test_before.java` is the true run start file and was replaced with the saved baseline fixture.
- `backup/original_test_before.java` preserves the pre-run working-tree test file that existed before the baseline fixture was copied in.
- `backup/config.ini.snapshot` is the reconstructed effective config used for this run.
- `backup/original_config.ini.backup` preserves the pre-run working-tree config that existed before the smoke-specific edits.
