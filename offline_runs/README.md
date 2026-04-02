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
- `run_class_list_batch.py`
  - Thin selector wrapper around `run_batch.sh`
  - Reads `evaluation/data/class_list.csv`
  - Resolves source/test paths from `evaluation/defects4j-codefiles/<project>-codefiles.json`
  - Uses CSV `complexity` as `maximum_iterations`
  - Supports selecting a subset of classes and adding a custom batch name prefix
- `repeat_baseline_3iter.sh`
  - Thin wrapper around `run_batch.sh`
  - Equivalent to a 3-run baseline repeat with `maximum_iterations = 3`, `no_coverage_increase_iterations = 3`, and `llm_advice_activation_line_coverage = 50`
- `repeat_baseline_5iter_fix2_threshold70_vs_100.sh`
  - Thin wrapper around `run_batch.sh`
  - Runs 2 rounds from the same baseline with `maximum_iterations = 5` and `enable_fixing = 2`
  - Uses `llm_advice_activation_line_coverage = 70` for round 1 and `100` for round 2
- `repeat_baseline_5iter_fix3_threshold70_vs_100.sh`
  - Thin wrapper around `run_batch.sh`
  - Runs 2 rounds from the same baseline with `maximum_iterations = 5` and `enable_fixing = 3`
  - Uses `llm_advice_activation_line_coverage = 70` for round 1 and `100` for round 2
- `no_feedback_5iter_fix3_threshold70.sh`
  - Runs 2 rounds from the same CSVParser baseline with `maximum_iterations = 5`, `enable_fixing = 3`, and `llm_advice_activation_line_coverage = 70`
  - Forces `enable_advice_feedback = false` for the advice-feedback A/B check
- `run_csvformat_6iter.sh`
  - Runs `CSVFormat.java` for 1 round with `maximum_iterations = 6`
  - Uses the current project state as the default baseline for `CSVFormatTest.java`; if the file is absent, the run starts without an initial test file
- `run_csvformat_continue_from_after_6iter.sh`
  - Continues `CSVFormat.java` from the `CSVFormatTest_after.java` produced by the earlier 6-iteration run
  - Runs 1 round with `maximum_iterations = 6`
- `run_csvformat_continue_from_first_after_4iter.sh`
  - Continues `CSVFormat.java` from the first `threshold=100` 6-iteration run's `CSVFormatTest_after.java`
  - Runs 1 round with `maximum_iterations = 4`
  - Used for the cluster-breadth light-advice comparison run
- `run_csvformat_continue_from_first_after_4iter_temp04.sh`
  - Continues `CSVFormat.java` from the same `55.90% / 42.15%` baseline as `run_csvformat_continue_from_first_after_4iter.sh`
  - Runs 1 round with `maximum_iterations = 4`
  - Uses the original light-advice template with `llm_light_advice_temperature = 0.4`
  - Used for the D-run comparison against the cluster-breadth variant

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

Run a selected subset from `class_list.csv` with a custom prefix:

```bash
python3 offline_runs/run_class_list_batch.py \
  --project Csv-16f \
  --class CSVParser \
  --class CSVFormat \
  --rounds 2 \
  --batch-prefix apr02-sample
```

Preview selection without running:

```bash
python3 offline_runs/run_class_list_batch.py \
  --project Csv-16f \
  --min-complexity 14 \
  --max-classes 3 \
  --batch-prefix apr02-sample \
  --dry-run
```

Use the 2-round threshold comparison wrapper:

```bash
nohup bash offline_runs/repeat_baseline_5iter_fix2_threshold70_vs_100.sh > repeat_baseline_5iter_fix2_threshold70_vs_100.log 2>&1 &
```
