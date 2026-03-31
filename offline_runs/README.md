# Offline Runs

This directory stores reusable shell scripts for long-running local experiments.

## Purpose

- Re-run the same smoke configuration multiple times from the same baseline
- Compare parameter settings across several long runs
- Keep offline experiment helpers separate from source code and analysis docs

## Conventions

- Each batch run restores `src/panta/config.ini` after it finishes.
- Each run restores the baseline test file before execution. If `--baseline` is omitted, the original file at the effective `test_code_file` path is used; if that file did not exist, the run starts without a test file.
- `report_filepath` is always auto-generated uniquely per run to avoid overwriting reports.
- Each run saves the effective `config.ini` in `smoke_test_log/<run_dir>/backup/config.ini.snapshot`.
- Each run saves `<TestName>_before.java` / `<TestName>_after.java` when the test file exists, or an `.absent` marker when it does not, plus `runtime.log` and `info.md`.

## Scripts

- `run_batch.sh`
  - Generic parameterized batch runner
  - Supports shared config overrides via `--set KEY=VALUE`
  - Supports per-run config overrides via `--set-per-round KEY=V1,V2,...,VN`
  - Auto-generates unique `report_filepath` values
- `repeat_baseline_3iter.sh`
  - Thin wrapper around `run_batch.sh`
  - Equivalent to a 3-run baseline repeat with `maximum_iterations = 3`, `no_coverage_increase_iterations = 3`, and `llm_advice_activation_line_coverage = 50`
- `repeat_baseline_5iter_fix2_threshold70_vs_100.sh`
  - Thin wrapper around `run_batch.sh`
  - Runs 2 rounds from the same baseline with `maximum_iterations = 5` and `enable_fixing = 2`
  - Uses `llm_advice_activation_line_coverage = 70` for round 1 and `100` for round 2

## `run_batch.sh` Usage

```bash
bash offline_runs/run_batch.sh \
  --batch-name NAME \
  --rounds N \
  [--baseline PATH] \
  [--set KEY=VALUE ...] \
  [--set-per-round KEY=V1,V2,...,VN ...]
```

## Arguments

- `--batch-name NAME`
  - Batch name prefix used in run directory names and auto-generated report names.
- `--rounds N`
  - Number of runs in the batch.
- `--baseline PATH`
  - Baseline test file path relative to repo root.
  - Default: the original file at the effective `test_code_file` path.
  - If that file does not exist, each round starts without a test file.
- `--set KEY=VALUE`
  - Apply one config override to every run.
  - Can be provided multiple times.
- `--set-per-round KEY=V1,V2,...,VN`
  - Apply a different value for each run.
  - The number of comma-separated values must equal `--rounds`.
  - Can be provided multiple times.

## Examples

Repeat the same 3-iteration baseline experiment 3 times:

```bash
nohup bash offline_runs/run_batch.sh \
  --batch-name repeat-baseline-3iter \
  --rounds 3 \
  --baseline smoke_test_log/baselines/CSVParser_baseline_only_test.java \
  --set maximum_iterations=3 \
  --set no_coverage_increase_iterations=3 \
  --set llm_advice_activation_line_coverage=50 \
  > repeat_baseline_3iter.log 2>&1 &
```

Run 3 rounds with different `maximum_iterations` values:

```bash
nohup bash offline_runs/run_batch.sh \
  --batch-name vary-max-iter \
  --rounds 3 \
  --baseline smoke_test_log/baselines/CSVParser_baseline_only_test.java \
  --set no_coverage_increase_iterations=3 \
  --set llm_advice_activation_line_coverage=50 \
  --set-per-round maximum_iterations=3,4,5 \
  > vary_max_iter.log 2>&1 &
```

Use the compatibility wrapper:

```bash
nohup bash offline_runs/repeat_baseline_3iter.sh > repeat_baseline_3iter.log 2>&1 &
```

Use the 2-round threshold comparison wrapper:

```bash
nohup bash offline_runs/repeat_baseline_5iter_fix2_threshold70_vs_100.sh > repeat_baseline_5iter_fix2_threshold70_vs_100.log 2>&1 &
```
