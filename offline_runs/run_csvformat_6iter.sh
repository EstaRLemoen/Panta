#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec bash "$SCRIPT_DIR/run_batch.sh" \
  --batch-name csvformat-6iter \
  --rounds 1 \
  --set project_directory=defects4j-subjects-notests/Csv-16f \
  --set source_code_file=defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java \
  --set test_code_file=defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java \
  --set code_coverage_report_path=defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv \
  --set test_execution_command='mvn clean package -Dtest=CSVFormatTest' \
  --set test_code_command_dir=defects4j-subjects-notests/Csv-16f/ \
  --set junit_version=4 \
  --set maximum_iterations=6 \
  --set no_coverage_increase_iterations=3 \
  "$@"
