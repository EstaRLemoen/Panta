#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec bash "$SCRIPT_DIR/run_batch.sh" \
  --batch-name csvformat-5iter-fix1-branch-feedback-smoke \
  --rounds 1 \
  --set project_directory=defects4j-subjects-notests/Csv-16f \
  --set source_code_file=defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java \
  --set test_code_file=defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java \
  --set code_coverage_report_path=defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv \
  --set test_execution_command='mvn clean package -Dtest=CSVFormatTest' \
  --set test_code_command_dir=defects4j-subjects-notests/Csv-16f/ \
  --set junit_version=4 \
  --set prompt_type=control \
  --set selection_mode=llm \
  --set maximum_iterations=5 \
  --set enable_fixing=1 \
  --set no_coverage_increase_iterations=3 \
  --set llm_advice_activation_line_coverage=100 \
  --set enable_advice_feedback=true \
  "$@"
