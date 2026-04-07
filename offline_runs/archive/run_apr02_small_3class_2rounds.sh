#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec python3 "$SCRIPT_DIR/run_class_list_batch.py" \
  --project Csv-16f \
  --project Cli-40f \
  --class CSVParser \
  --class HelpFormatter \
  --class PosixParser \
  --rounds 2 \
  --batch-prefix apr02-small \
  "$@"
