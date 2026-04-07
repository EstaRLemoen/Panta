#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BATCH="$SCRIPT_DIR/run_batch.sh"

CSV_PROJ="defects4j-subjects-notests/Csv-16f"
CSV_SRC="defects4j-subjects-notests/Csv-16f/src/main/java/org/apache/commons/csv"
CSV_TEST="defects4j-subjects-notests/Csv-16f/src/test/java/org/apache/commons/csv"
CSV_CMD="defects4j-subjects-notests/Csv-16f"
CSV_COV="defects4j-subjects-notests/Csv-16f/target/jacoco/jacoco.csv"

COLL_PROJ="defects4j-subjects-notests/Collections-28f"
COLL_SRC="defects4j-subjects-notests/Collections-28f/src/main/java/org/apache/commons/collections4"
COLL_TEST="defects4j-subjects-notests/Collections-28f/src/test/java/org/apache/commons/collections4"
COLL_CMD="defects4j-subjects-notests/Collections-28f"
COLL_COV="defects4j-subjects-notests/Collections-28f/target/jacoco/jacoco.csv"

COMMON_ARGS=(
  --rounds 1
  --set enable_fixing=3
  --set no_coverage_increase_iterations=3
  --set llm_advice_activation_line_coverage=100
  --set junit_version=4
  --set enable_advice_feedback=false
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

  echo "===== Starting: $batch_name (max_iter=$max_iter) ====="
  bash "$BATCH" \
    --batch-name "$batch_name" \
    --rounds 1 \
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

run_class csvformat-25iter \
  "$CSV_PROJ" "$CSV_SRC/CSVFormat.java" "$CSV_TEST/CSVFormatTest.java" \
  "$CSV_COV" "$CSV_CMD" "mvn clean package -Dtest=CSVFormatTest" 25

run_class csvparser-11iter \
  "$CSV_PROJ" "$CSV_SRC/CSVParser.java" "$CSV_TEST/CSVParserTest.java" \
  "$CSV_COV" "$CSV_CMD" "mvn clean package -Dtest=CSVParserTest" 11

run_class lexer-14iter \
  "$CSV_PROJ" "$CSV_SRC/Lexer.java" "$CSV_TEST/LexerTest.java" \
  "$CSV_COV" "$CSV_CMD" "mvn clean package -Dtest=LexerTest" 14

run_class iterableutils-11iter \
  "$COLL_PROJ" "$COLL_SRC/IterableUtils.java" "$COLL_TEST/IterableUtilsTest.java" \
  "$COLL_COV" "$COLL_CMD" "mvn clean package -Dtest=IterableUtilsTest" 11

run_class maputils-11iter \
  "$COLL_PROJ" "$COLL_SRC/MapUtils.java" "$COLL_TEST/MapUtilsTest.java" \
  "$COLL_COV" "$COLL_CMD" "mvn clean package -Dtest=MapUtilsTest" 11

echo "===== All 5 classes complete ====="
