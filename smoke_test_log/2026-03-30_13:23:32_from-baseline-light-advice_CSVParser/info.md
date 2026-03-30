# 2026-03-30_13:23:32_from-baseline-light-advice_CSVParser

AI note.

## What this smoke run was for

Run a smoke from the saved baseline-only CSVParser test file after switching low-coverage routing from direct generation to lightweight advice.

## Result

- The run completed normally.
- Starting coverage was line `2.97%`, branch `0.0%`.
- Final coverage reached line `68.32%`, branch `57.14%`.
- Iteration 0 did not improve coverage.
- Iteration 1 improved coverage by line `65.35%`, branch `57.14%`.

## Notes

- The first lightweight-advice iteration still produced only failing tests.
- The second iteration produced two passing higher-value tests:
  - `testCSVParserTrailingDelimiterEmptyLastRecord`
  - `testCSVParserDuplicateHeaders`
- This is the first clear evidence that the lightweight-advice low-coverage stage can lift the suite materially from the saved baseline fixture.
