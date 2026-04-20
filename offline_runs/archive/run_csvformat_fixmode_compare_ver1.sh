#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"

BATCH_BASE="fixmode_compare_ver1"
ROUND_COUNT="1"
MAX_ITERATIONS="10"
ENABLE_FIXING="3"
NO_COVERAGE_INCREASE_ITERATIONS="3"

run_mode() {
  local mode="$1"
  bash "$RUN_BATCH" \
    --batch-name "${BATCH_BASE}_${mode}" \
    --rounds "$ROUND_COUNT" \
    --set project_directory=defects4j-subjects-notests/Csv-16f \
    --set source_code_file=defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java \
    --set test_code_file=defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java \
    --set code_coverage_report_path=defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv \
    --set test_execution_command='mvn clean package -Dtest=CSVFormatTest' \
    --set test_code_command_dir=defects4j-subjects-notests/Csv-16f \
    --set junit_version=4 \
    --set prompt_type=control \
    --set selection_mode=llm \
    --set maximum_iterations="$MAX_ITERATIONS" \
    --set enable_fixing="$ENABLE_FIXING" \
    --set no_coverage_increase_iterations="$NO_COVERAGE_INCREASE_ITERATIONS" \
    --set llm_advice_activation_line_coverage=100 \
    --set enable_advice_feedback=true \
    --set fixing_mode="$mode"
}

run_mode combined
run_mode separate
