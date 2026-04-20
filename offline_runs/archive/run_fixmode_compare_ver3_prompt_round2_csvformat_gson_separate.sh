#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"
PROMPT_DIR="$ROOT_DIR/src/panta/prompt_templates/java_templates"
BACKUP_DIR="$ROOT_DIR/test_analysis/fixmode_compare"
TMP_BACKUP_DIR="$ROOT_DIR/offline_runs/runtime_tmp_backup"

COMPILATION_PROMPT="$PROMPT_DIR/failed_test_compilation_feedback_prompt.toml"
RUNTIME_PROMPT="$PROMPT_DIR/failed_test_runtime_feedback_prompt.toml"

TMP_COMPILATION_BACKUP="$TMP_BACKUP_DIR/failed_test_compilation_feedback_prompt.before_run.toml"
TMP_RUNTIME_BACKUP="$TMP_BACKUP_DIR/failed_test_runtime_feedback_prompt.before_run.toml"

VER3_COMPILATION="$BACKUP_DIR/failed_test_compilation_feedback_prompt_ver3_backup.toml"
VER3_RUNTIME="$BACKUP_DIR/failed_test_runtime_feedback_prompt_ver3_backup.toml"
ENABLE_FIXING="3"
NO_COVERAGE_INCREASE_ITERATIONS="3"
FIXING_MODE="separate"

restore_original_prompts() {
  if [[ -f "$TMP_COMPILATION_BACKUP" ]]; then
    cp "$TMP_COMPILATION_BACKUP" "$COMPILATION_PROMPT"
  fi
  if [[ -f "$TMP_RUNTIME_BACKUP" ]]; then
    cp "$TMP_RUNTIME_BACKUP" "$RUNTIME_PROMPT"
  fi
}

install_ver3_prompts() {
  cp "$VER3_COMPILATION" "$COMPILATION_PROMPT"
  cp "$VER3_RUNTIME" "$RUNTIME_PROMPT"
}

backup_current_prompts() {
  mkdir -p "$TMP_BACKUP_DIR"
  cp "$COMPILATION_PROMPT" "$TMP_COMPILATION_BACKUP"
  cp "$RUNTIME_PROMPT" "$TMP_RUNTIME_BACKUP"
}

trap restore_original_prompts EXIT

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

echo "===== Installing ver3 prompts ====="
backup_current_prompts
install_ver3_prompts

run_class "fixmode_compare_ver3prompt_round2_csvformat_separate" "2" \
  "defects4j-subjects-notests/Csv-16f" \
  "defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv/CSVFormat.java" \
  "defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv/CSVFormatTest.java" \
  "defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Csv-16f" \
  "mvn clean package -Dtest=CSVFormatTest" \
  "25"

run_class "fixmode_compare_ver3prompt_round2_gson_separate" "2" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "defects4j-subjects-notests/Gson-16f/gson/src/main/java/com/google/gson/Gson.java" \
  "defects4j-subjects-notests/Gson-16f/gson/src/test/java/com/google/gson/GsonTest.java" \
  "defects4j-subjects-notests/Gson-16f/gson/target/jacoco/jacoco.csv" \
  "defects4j-subjects-notests/Gson-16f/gson" \
  "mvn clean package -Dtest=GsonTest" \
  "22"

echo "===== Run complete; prompts restored from runtime backup ====="
