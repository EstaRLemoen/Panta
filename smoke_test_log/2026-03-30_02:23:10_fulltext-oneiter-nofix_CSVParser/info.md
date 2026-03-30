# 2026-03-30_02:23:10_fulltext-oneiter-nofix_CSVParser

AI note.

## What this smoke run was for

Run one `llm` iteration with fixing disabled and CFG prompt snapshots in `full_text` mode, so we can inspect the actual final test-generation prompt.

## Config overrides for this run

- `maximum_iterations = 1`
- `enable_fixing = 0`
- `cfg_dump_prompt_mode = full_text`

## Result

- The run completed normally.
- The generated test `testTrailingDelimiterHandling` passed.
- Coverage increased from line `75.25%`, branch `62.5%` to line `77.23%`, branch `67.86%`.

## Notes

- This run is useful as a prompt-inspection baseline because it stores full prompt text in the matching CFG snapshot directory.
- The advice still contains some shaky branch narration, but the final generated test was materially better than the previous failing trailing-delimiter attempts.
