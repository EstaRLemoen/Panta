#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec bash "$SCRIPT_DIR/run_batch.sh" \
  --batch-name repeat-baseline-5iter-fix3-threshold70-vs-100 \
  --rounds 2 \
  --baseline smoke_test_log/baselines/CSVParser_baseline_only_test.java \
  --set maximum_iterations=5 \
  --set enable_fixing=3 \
  --set no_coverage_increase_iterations=3 \
  --set-per-round llm_advice_activation_line_coverage=70,100 \
  "$@"
