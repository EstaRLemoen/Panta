#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
RUN_BATCH="$SCRIPT_DIR/../run_batch.sh"
PROMPT_DIR="$ROOT_DIR/src/panta/prompt_templates/java_templates"
BACKUP_DIR="$ROOT_DIR/test_analysis/fixmode_compare"

COMPILATION_PROMPT="$PROMPT_DIR/failed_test_compilation_feedback_prompt.toml"
RUNTIME_PROMPT="$PROMPT_DIR/failed_test_runtime_feedback_prompt.toml"

VER3_COMPILATION="$BACKUP_DIR/failed_test_compilation_feedback_prompt_ver3_backup.toml"
VER3_RUNTIME="$BACKUP_DIR/failed_test_runtime_feedback_prompt_ver3_backup.toml"
VER6_COMPILATION="$BACKUP_DIR/failed_test_compilation_feedback_prompt_ver6_backup.toml"
VER6_RUNTIME="$BACKUP_DIR/failed_test_runtime_feedback_prompt_ver6_backup.toml"

restore_ver6_prompts() {
  cp "$VER6_COMPILATION" "$COMPILATION_PROMPT"
  cp "$VER6_RUNTIME" "$RUNTIME_PROMPT"
}

install_ver3_prompts() {
  cp "$VER3_COMPILATION" "$COMPILATION_PROMPT"
  cp "$VER3_RUNTIME" "$RUNTIME_PROMPT"
}

trap restore_ver6_prompts EXIT

echo "===== Installing ver3 prompts ====="
install_ver3_prompts

bash "$RUN_BATCH" \
  --batch-name "fixmode_compare_ver3prompt_round2_jsonstringencoder_separate" \
  --rounds "2" \
  --set junit_version=4 \
  --set prompt_type=control \
  --set selection_mode=llm \
  --set enable_fixing="3" \
  --set no_coverage_increase_iterations="3" \
  --set llm_advice_activation_line_coverage=100 \
  --set enable_advice_feedback=true \
  --set fixing_mode="separate" \
  --set project_directory="defects4j-subjects-notests/JacksonCore-26f" \
  --set source_code_file="defects4j-subjects-notests/JacksonCore-26f/src/main/java/com/fasterxml/jackson/core/io/JsonStringEncoder.java" \
  --set test_code_file="defects4j-subjects-notests/JacksonCore-26f/src/test/java/com/fasterxml/jackson/core/io/TestJsonStringEncoder.java" \
  --set code_coverage_report_path="defects4j-subjects-notests/JacksonCore-26f/target/jacoco/jacoco.csv" \
  --set test_code_command_dir="defects4j-subjects-notests/JacksonCore-26f" \
  --set test_execution_command="mvn clean package -Dtest=TestJsonStringEncoder" \
  --set maximum_iterations="18"

echo "===== Run complete; prompts restored to ver6 ====="
