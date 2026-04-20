#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "===== Starting ver3 CSVFormat+Gson round2 ====="
bash "$SCRIPT_DIR/run_fixmode_compare_ver3_prompt_round2_csvformat_gson_separate.sh"

echo "===== Starting ver8 JsonStringEncoder round1 ====="
bash "$SCRIPT_DIR/run_fixmode_compare_ver8_prompt_round1_jsonstringencoder_separate.sh"

echo "===== Sleep batch complete ====="
