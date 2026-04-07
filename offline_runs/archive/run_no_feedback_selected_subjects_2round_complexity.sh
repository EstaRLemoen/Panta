#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BATCH="$SCRIPT_DIR/run_batch.sh"
EXTRA_ARGS=("$@")

COMMON_ARGS=(
  --rounds 2
  --set no_coverage_increase_iterations=3
  --set llm_advice_activation_line_coverage=100
  --set llm_advice_activation_no_growth=3
  --set enable_fixing=3
  --set junit_version=4
  --set enable_advice_feedback=false
)

run_class() {
  local batch_name="$1"
  local max_iters="$2"
  local project_dir="$3"
  local source_file="$4"
  local test_file="$5"
  local cov_report="$6"
  local cmd_dir="$7"
  local test_cmd="$8"

  mkdir -p "$(dirname "$test_file")"

  echo "===== Starting: $batch_name ====="
  bash "$BATCH" \
    --batch-name "$batch_name" \
    --set "maximum_iterations=$max_iters" \
    --set "project_directory=$project_dir" \
    --set "source_code_file=$source_file" \
    --set "test_code_file=$test_file" \
    --set "code_coverage_report_path=$cov_report" \
    --set "test_code_command_dir=$cmd_dir" \
    --set "test_execution_command=$test_cmd" \
    "${COMMON_ARGS[@]}" \
    "${EXTRA_ARGS[@]}"
  echo "===== Finished: $batch_name ====="
  echo
}

run_class collections28f-prototypefactory-2round-complexity 16 \
  defects4j-subjects-notests/Collections-28f \
  defects4j-subjects-notests/Collections-28f/src/main/java/org/apache/commons/collections4/functors/PrototypeFactory.java \
  defects4j-subjects-notests/Collections-28f/src/test/java/org/apache/commons/collections4/functors/PrototypeFactoryTest.java \
  defects4j-subjects-notests/Collections-28f/target/jacoco/jacoco.csv \
  defects4j-subjects-notests/Collections-28f \
  'mvn clean package -Dtest=PrototypeFactoryTest'

run_class cli40f-helpformatter-2round-complexity 11 \
  defects4j-subjects-notests/Cli-40f \
  defects4j-subjects-notests/Cli-40f/src/main/java/org/apache/commons/cli/HelpFormatter.java \
  defects4j-subjects-notests/Cli-40f/src/test/java/org/apache/commons/cli/HelpFormatterTest.java \
  defects4j-subjects-notests/Cli-40f/target/jacoco/jacoco.csv \
  defects4j-subjects-notests/Cli-40f \
  'mvn clean package -Dtest=HelpFormatterTest'

run_class cli40f-posixparser-2round-complexity 11 \
  defects4j-subjects-notests/Cli-40f \
  defects4j-subjects-notests/Cli-40f/src/main/java/org/apache/commons/cli/PosixParser.java \
  defects4j-subjects-notests/Cli-40f/src/test/java/org/apache/commons/cli/PosixParserTest.java \
  defects4j-subjects-notests/Cli-40f/target/jacoco/jacoco.csv \
  defects4j-subjects-notests/Cli-40f \
  'mvn clean package -Dtest=PosixParserTest'

run_class jacksoncore26f-utf32reader-2round-complexity 14 \
  defects4j-subjects-notests/JacksonCore-26f \
  defects4j-subjects-notests/JacksonCore-26f/src/main/java/com/fasterxml/jackson/core/io/UTF32Reader.java \
  defects4j-subjects-notests/JacksonCore-26f/src/test/java/com/fasterxml/jackson/core/io/UTF32ReaderTest.java \
  defects4j-subjects-notests/JacksonCore-26f/target/jacoco/jacoco.csv \
  defects4j-subjects-notests/JacksonCore-26f \
  'mvn clean package -Dtest=UTF32ReaderTest'

run_class jxpath22f-packagefunctions-2round-complexity 18 \
  defects4j-subjects-notests/JxPath-22f \
  defects4j-subjects-notests/JxPath-22f/src/java/org/apache/commons/jxpath/PackageFunctions.java \
  defects4j-subjects-notests/JxPath-22f/src/test/org/apache/commons/jxpath/PackageFunctionsTest.java \
  defects4j-subjects-notests/JxPath-22f/target/jacoco/jacoco.csv \
  defects4j-subjects-notests/JxPath-22f \
  'mvn clean package -Dtest=PackageFunctionsTest'

echo "===== All 5 classes complete ====="
