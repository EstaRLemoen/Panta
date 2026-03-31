#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec bash "$SCRIPT_DIR/run_batch.sh" \
  --batch-name repeat-baseline-3iter \
  --rounds 3 \
  --baseline smoke_test_log/baselines/CSVParser_baseline_only_test.java \
  --set maximum_iterations=3 \
  --set no_coverage_increase_iterations=3 \
  --set llm_advice_activation_line_coverage=50 \
  "$@"
