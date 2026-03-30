# Test Analysis

This directory stores analysis summaries and comparison reports for smoke test runs and test groups.

## What goes here

- Per-run or cross-run analysis markdown files
- Comparison tables, statistical observations, pattern summaries
- General conclusions drawn from experiments

## What does NOT go here

- Runtime logs, backup test files, config snapshots → `smoke_test_log/<run_dir>/`
- CFG intermediate data → `cfg_snapshot_test/intermediate/`
- Source code or config changes → project source tree

Each analysis document should reference its source data by path so that findings can be traced back to the original runs.
