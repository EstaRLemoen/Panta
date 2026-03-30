# 2026-03-30_17:23:15_from-baseline-temp03_CSVParser

AI note.

## What this smoke run was for

Run from the saved CSVParser baseline fixture with `llm_path_advice_model = gpt-4o-mini`, `maximum_iterations = 3`, and `llm_light_advice_temperature = 0.3`.

## Result

- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `39.6%`, branch `10.71%`.

## Iteration Summary

- Iteration 0 increased coverage to line `39.6%`, branch `10.71%`.
- Iteration 1 did not increase coverage.
- Iteration 2 did not increase coverage.

## Passing Tests Observed

- `testParseNullFormatWithFile`
- `testParseNullFormatWithPath`
- `testParseNullFormatWithReader`
- `testParseNullFormatWithString`
- `testParseNullFormatWithURL`
- `testGetRecordsFromEmptyInput`
- `testParseNullFormatWithInputStream`
- `testParseNullStringDuplicate`
- `testParseNullReaderDuplicate`
- `testParseNullURLDuplicate`

## Notes

- The first lightweight advice fell back to default advice.
- This run mostly drifted into null-format and null-input tests, so it improved coverage but in a lower-value way than the stronger baseline-lift runs.
- `backup/test_before.java` is the true run start file and was replaced with the saved baseline fixture.
- `backup/original_test_before.java` preserves the pre-run working-tree test file that existed before the baseline fixture was copied in.
