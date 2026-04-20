#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"
PROMPT_DIR="$ROOT_DIR/src/panta/prompt_templates/java_templates"
BACKUP_DIR="$ROOT_DIR/test_analysis/fixmode_compare"

COMPILATION_PROMPT="$PROMPT_DIR/failed_test_compilation_feedback_prompt.toml"
RUNTIME_PROMPT="$PROMPT_DIR/failed_test_runtime_feedback_prompt.toml"

VER4_COMPILATION="$BACKUP_DIR/failed_test_compilation_feedback_prompt_ver4_backup.toml"
VER4_RUNTIME="$BACKUP_DIR/failed_test_runtime_feedback_prompt_ver4_backup.toml"
VER6_COMPILATION="$BACKUP_DIR/failed_test_compilation_feedback_prompt_ver6_backup.toml"
VER6_RUNTIME="$BACKUP_DIR/failed_test_runtime_feedback_prompt_ver6_backup.toml"

ENABLE_FIXING="3"
NO_COVERAGE_INCREASE_ITERATIONS="3"
FIXING_MODE="separate"

restore_ver6_prompts() {
  cp "$VER6_COMPILATION" "$COMPILATION_PROMPT"
  cp "$VER6_RUNTIME" "$RUNTIME_PROMPT"
}

install_ver4_prompts() {
  cp "$VER4_COMPILATION" "$COMPILATION_PROMPT"
  cp "$VER4_RUNTIME" "$RUNTIME_PROMPT"
}

install_ver6_prompts() {
  restore_ver6_prompts
}

trap restore_ver6_prompts EXIT

run_class() {
  local batch_name="$1"
  local rounds="$2"
  local project_dir="$3"
  local source_file="$4"
  local test_file="$5"
  local cov_report="$6"
  local cmd_dir="$7"
  local test_cmd="$8"
  local max_iter="$9"

  echo "===== Starting: $batch_name (rounds=$rounds, fixing_mode=$FIXING_MODE, max_iter=$max_iter) ====="
  bash "$RUN_BATCH" \
    --batch-name "$batch_name" \
    --rounds "$rounds" \
    --set junit_version=4 \
    --set prompt_type=control \
    --set selection_mode=llm \
    --set enable_fixing="$ENABLE_FIXING" \
    --set no_coverage_increase_iterations="$NO_COVERAGE_INCREASE_ITERATIONS" \
    --set llm_advice_activation_line_coverage=100 \
    --set enable_advice_feedback=true \
    --set fixing_mode="$FIXING_MODE" \
    --set "project_directory=$project_dir" \
    --set "source_code_file=$source_file" \
    --set "test_code_file=$test_file" \
    --set "code_coverage_report_path=$cov_report" \
    --set "test_code_command_dir=$cmd_dir" \
    --set "test_execution_command=$test_cmd" \
    --set "maximum_iterations=$max_iter"
  echo "===== Finished: $batch_name ====="
  echo
}

echo "===== Installing ver6 prompts ====="
install_ver6_prompts

run_class "fixmode_compare_ver6_round2_csvformat_separate" "2" \
  "defects4j-subjects-notests/Csv-16f" \
  "defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java" \
  "defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java" \
  "defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Csv-16f" \
  "mvn clean package -Dtest=CSVFormatTest" \
  "25"

run_class "fixmode_compare_ver6_round2_jsonstringencoder_separate" "2" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "defects4j-subjects-notests/JacksonCore-26f/src/main/java/com/fasterxml/jackson/core/io/JsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/src/test/java/com/fasterxml/jackson/core/io/TestJsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "mvn clean package -Dtest=TestJsonStringEncoder" \
  "18"

run_class "fixmode_compare_ver6_round2_gson_separate" "2" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "defects4j-subjects-notests/Gson-16f/gson/src/main/java/com/google/gson/Gson.java" \
  "defects4j-subjects-notests/Gson-16f/gson/src/test/java/com/google/gson/GsonTest.java" \
  "defects4j-subjects-notests/Gson-16f/gson/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "mvn clean package -Dtest=GsonTest" \
  "22"

echo "===== Installing ver4 prompts ====="
install_ver4_prompts

run_class "fixmode_compare_ver4_round1_csvformat_separate" "1" \
  "defects4j-subjects-notests/Csv-16f" \
  "defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java" \
  "defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java" \
  "defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Csv-16f" \
  "mvn clean package -Dtest=CSVFormatTest" \
  "25"

run_class "fixmode_compare_ver4_round1_jsonstringencoder_separate" "1" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "defects4j-subjects-notests/JacksonCore-26f/src/main/java/com/fasterxml/jackson/core/io/JsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/src/test/java/com/fasterxml/jackson/core/io/TestJsonStringEncoder.java" \
  "defects4j-subjects-notests/JacksonCore-26f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/JacksonCore-26f" \
  "mvn clean package -Dtest=TestJsonStringEncoder" \
  "18"

run_class "fixmode_compare_ver4_round1_gson_separate" "1" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "defects4j-subjects-notests/Gson-16f/gson/src/main/java/com/google/gson/Gson.java" \
  "defects4j-subjects-notests/Gson-16f/gson/src/test/java/com/google/gson/GsonTest.java" \
  "defects4j-subjects-notests/Gson-16f/gson/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "mvn clean package -Dtest=GsonTest" \
  "22"

echo "===== All runs complete; prompts restored to ver6 ====="
