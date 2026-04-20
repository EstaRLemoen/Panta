#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"

BATCH_NAME="fixmode_compare_ver5_small_gson_separate_repeat1"
ROUND_COUNT="1"
ENABLE_FIXING="3"
NO_COVERAGE_INCREASE_ITERATIONS="3"
FIXING_MODE="separate"

bash "$RUN_BATCH" \
  --batch-name "$BATCH_NAME" \
  --rounds "$ROUND_COUNT" \
  --set junit_version=4 \
  --set prompt_type=control \
  --set selection_mode=llm \
  --set enable_fixing="$ENABLE_FIXING" \
  --set no_coverage_increase_iterations="$NO_COVERAGE_INCREASE_ITERATIONS" \
  --set llm_advice_activation_line_coverage=100 \
  --set enable_advice_feedback=true \
  --set fixing_mode="$FIXING_MODE" \
  --set project_directory="defects4j-subjects-notests/Gson-16f/gson" \
  --set source_code_file="defects4j-subjects-notests/Gson-16f/gson/src/main/java/com/google/gson/Gson.java" \
  --set test_code_file="defects4j-subjects-notests/Gson-16f/gson/src/test/java/com/google/gson/GsonTest.java" \
  --set code_coverage_report_path="defects4j-subjects-notests/Gson-16f/gson/target/jacoco/jacoco.csv" \
  --set test_code_command_dir="defects4j-subjects-notests/Gson-16f/gson" \
  --set test_execution_command="mvn clean package -Dtest=GsonTest" \
  --set maximum_iterations="22"
