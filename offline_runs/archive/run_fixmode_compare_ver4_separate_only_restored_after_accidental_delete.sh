#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"

BATCH_BASE="fixmode_compare_ver4"
ROUND_COUNT="1"
ENABLE_FIXING="3"
NO_COVERAGE_INCREASE_ITERATIONS="3"
FIXING_MODE="separate"

COMMON_ARGS=(
  --rounds "$ROUND_COUNT"
  --set junit_version=4
  --set prompt_type=control
  --set selection_mode=llm
  --set enable_fixing="$ENABLE_FIXING"
  --set no_coverage_increase_iterations="$NO_COVERAGE_INCREASE_ITERATIONS"
  --set llm_advice_activation_line_coverage=100
  --set enable_advice_feedback=true
  --set fixing_mode="$FIXING_MODE"
)

run_class() {
  local batch_name="$1"
  local project_dir="$2"
  local source_file="$3"
  local test_file="$4"
  local cov_report="$5"
  local cmd_dir="$6"
  local test_cmd="$7"
  local max_iter="$8"

  echo "===== Starting: $batch_name (fixing_mode=$FIXING_MODE, max_iter=$max_iter) ====="
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
    "${COMMON_ARGS[@]}"
  echo "===== Finished: $batch_name ====="
  echo
}

# Csv-16f / CSVFormat: complexity 25.
run_class "${BATCH_BASE}_csvformat_separate" \
  "defects4j-subjects-notests/Csv-16f" \
  "defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java" \
  "defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java" \
  "defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Csv-16f" \
  "mvn clean package -Dtest=CSVFormatTest" \
  "25"

# JacksonCore-26f / JsonStringEncoder: complexity 18.
run_class "${BATCH_BASE}_jsonstringencoder_separate" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "defects4j-subjects-notests/JacksonCore-26f/src/main/java/com/fasterxml/jackson/core/io/JsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/src/test/java/com/fasterxml/jackson/core/io/TestJsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "mvn clean package -Dtest=TestJsonStringEncoder" \
  "18"

# Gson-16f / Gson: complexity 22.
run_class "${BATCH_BASE}_gson_separate" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "defects4j-subjects-notests/Gson-16f/gson/src/main/java/com/google/gson/Gson.java" \
  "defects4j-subjects-notests/Gson-16f/gson/src/test/java/com/google/gson/GsonTest.java" \
  "defects4j-subjects-notests/Gson-16f/gson/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "mvn clean package -Dtest=GsonTest" \
  "22"

echo "===== All ver4 separate-only runs complete ====="
