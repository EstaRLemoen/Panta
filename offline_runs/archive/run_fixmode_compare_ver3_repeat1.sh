#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"

BATCH_BASE="fixmode_compare_ver3_repeat1"
ROUND_COUNT="1"
ENABLE_FIXING="3"
NO_COVERAGE_INCREASE_ITERATIONS="3"

COMMON_ARGS=(
  --rounds "$ROUND_COUNT"
  --set junit_version=4
  --set prompt_type=control
  --set selection_mode=llm
  --set enable_fixing="$ENABLE_FIXING"
  --set no_coverage_increase_iterations="$NO_COVERAGE_INCREASE_ITERATIONS"
  --set llm_advice_activation_line_coverage=100
  --set enable_advice_feedback=true
)

run_class_mode() {
  local class_key="$1"
  local mode="$2"
  local project_dir="$3"
  local source_file="$4"
  local test_file="$5"
  local cov_report="$6"
  local cmd_dir="$7"
  local test_cmd="$8"
  local max_iter="$9"

  local batch_name="${BATCH_BASE}_${class_key}_${mode}"

  echo "===== Starting: $batch_name (max_iter=$max_iter) ====="
  bash "$RUN_BATCH" \
    --batch-name "$batch_name" \
    --rounds "$ROUND_COUNT" \
    --set "project_directory=$project_dir" \
    --set "source_code_file=$source_file" \
    --set "test_code_file=$test_file" \
    --set "code_coverage_report_path=$cov_report" \
    --set "test_code_command_dir=$cmd_dir" \
    --set "test_execution_command=$test_cmd" \
    --set "maximum_iterations=$max_iter" \
    --set "fixing_mode=$mode" \
    "${COMMON_ARGS[@]}"
  echo "===== Finished: $batch_name ====="
  echo
}

run_both_modes() {
  local class_key="$1"
  local project_dir="$2"
  local source_file="$3"
  local test_file="$4"
  local cov_report="$5"
  local cmd_dir="$6"
  local test_cmd="$7"
  local max_iter="$8"

  run_class_mode "$class_key" combined "$project_dir" "$source_file" "$test_file" "$cov_report" "$cmd_dir" "$test_cmd" "$max_iter"
  run_class_mode "$class_key" separate "$project_dir" "$source_file" "$test_file" "$cov_report" "$cmd_dir" "$test_cmd" "$max_iter"
}

# JacksonCore-26f / JsonStringEncoder: complexity 18.
run_both_modes \
  "jsonstringencoder" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "defects4j-subjects-notests/JacksonCore-26f/src/main/java/com/fasterxml/jackson/core/io/JsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/src/test/java/com/fasterxml/jackson/core/io/TestJsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "mvn clean package -Dtest=TestJsonStringEncoder" \
  "18"

# Gson-16f / Gson: complexity 22.
run_both_modes \
  "gson" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "defects4j-subjects-notests/Gson-16f/gson/src/main/java/com/google/gson/Gson.java" \
  "defects4j-subjects-notests/Gson-16f/gson/src/test/java/com/google/gson/GsonTest.java" \
  "defects4j-subjects-notests/Gson-16f/gson/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "mvn clean package -Dtest=GsonTest" \
  "22"

echo "===== All ver3 repeat1 runs complete ====="
