# 2026-03-30_04:06:28_llm-direct-threshold-smoke_CSVParser

AI note.

## What this smoke run was for

Sanity-check the new `llm-direct -> llm advice` switching logic with the normal two-iteration configuration.

## Result

- The run completed normally.
- Starting coverage was already line `77.23%`, branch `67.86%`.
- Final coverage stayed at line `77.23%`, branch `67.86%`.
- One generated test passed, but no further coverage increase was achieved.

## Notes

- Because the run started above the configured advice activation threshold (`50%` line coverage), it used the advice-guided path immediately.
- This run validates that the new switching code does not break the existing high-coverage path.
- It does not yet validate the new direct-annotated path, because the threshold was never crossed from below.
