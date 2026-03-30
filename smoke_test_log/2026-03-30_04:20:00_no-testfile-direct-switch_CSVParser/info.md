# 2026-03-30_04:20:00_no-testfile-direct-switch_CSVParser

AI note.

## What this smoke run was for

Re-run the no-existing-test-file smoke after adding the new `llm-direct` path, to see whether low-start generation can recover better before advice mode is activated.

## Result

- The run completed normally.
- It started from line `0.0%`, branch `0.0%`.
- It finished at line `2.97%`, branch `0.0%`.
- The baseline iteration added only null-argument tests.
- The second iteration did use the new `llm-direct` path, but it still failed to add any passing higher-value tests.

## Notes

- Snapshot confirmed the new direct mode was used: `prompt_builder_llm_direct_selection`.
- The direct prompt did not get stuck on the old trailing-delimiter advice schema, but it still drifted into broad "valid input" tests that mostly failed at runtime.
- This means the new low-coverage routing is wired correctly, but it is not yet sufficient to lift the suite meaningfully from a near-empty starting point.
