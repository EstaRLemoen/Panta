#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec bash "$SCRIPT_DIR/run_batch.sh" \
  --batch-name feedback-csvparser-from-repeat-r1-after-3rounds-3iter \
  --rounds 1 \
  --baseline smoke_test_log/2026-04-01_20:14:55_repeat-csvparser-11iter_r1_CSVParser/backup/CSVParserTest_after.java \
  --set project_directory=defects4j-subjects-notests/Csv-16f \
  --set source_code_file=defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVParser.java \
  --set test_code_file=defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVParserTest.java \
  --set code_coverage_report_path=defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv \
  --set test_execution_command='mvn clean package -Dtest=CSVParserTest' \
  --set test_code_command_dir=defects4j-subjects-notests/Csv-16f/ \
  --set junit_version=4 \
  --set maximum_iterations=3 \
  --set enable_fixing=3 \
  --set no_coverage_increase_iterations=3 \
  --set llm_advice_activation_line_coverage=100 \
  --set enable_advice_feedback=true \
  "$@"
