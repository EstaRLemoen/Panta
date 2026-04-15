#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage:
  bash offline_runs/run_batch.sh \
    --batch-name NAME \
    --rounds N \
    [--baseline PATH] \
    [--set KEY=VALUE ...] \
    [--set-per-round KEY=V1,V2,...,VN ...]

Arguments:
  --batch-name NAME            Batch name prefix used in run directories and report names.
  --rounds N                   Number of runs.
  --baseline PATH              Baseline test file path relative to repo root.
                               Default: the original file at the effective `test_code_file` path.
                               If that file does not exist, each round starts without a test file.
  --set KEY=VALUE              Config entry applied to every run. Can be repeated.
  --set-per-round KEY=...      Config entry with one value per run, comma-separated.
                               Value count must equal --rounds. Can be repeated.
  --help                       Show this message.

Example:
  bash offline_runs/run_batch.sh \
    --batch-name repeat-baseline-3iter \
    --rounds 3 \
    --set maximum_iterations=3 \
    --set no_coverage_increase_iterations=3 \
    --set llm_advice_activation_line_coverage=50
EOF
}

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG="$REPO_ROOT/src/panta/config.ini"
LOG_DIR="$REPO_ROOT/smoke_test_log"
SECRETS="$REPO_ROOT/cfg_snapshot_test/local.secrets.env"

BATCH_NAME=""
ROUND_COUNT=""
BASELINE_REL=""

declare -A COMMON_SETTINGS=()
declare -A PER_ROUND_SETTINGS=()
declare -A EFFECTIVE_SETTINGS=()
declare -A ORIGINAL_TEST_CACHE=()
declare -A ORIGINAL_TEST_EXISTS=()
COMMON_KEYS=()
PER_ROUND_KEYS=()
SUMMARY_LINES=()
ORIG_CONFIG="$REPO_ROOT/offline_runs/run_batch.config.bak"
ACTIVE_TEST_FILE=""
ACTIVE_TEST_BASENAME=""
ACTIVE_SUBJECT_LABEL="subject"

require_value() {
    local option="$1"
    local value="${2:-}"
    if [[ -z "$value" ]]; then
        printf 'Missing value for %s\n' "$option" >&2
        usage >&2
        exit 1
    fi
}

parse_assignment() {
    local raw="$1"
    if [[ "$raw" != *=* ]]; then
        printf 'Expected KEY=VALUE format, got: %s\n' "$raw" >&2
        exit 1
    fi
    local key="${raw%%=*}"
    local value="${raw#*=}"
    if [[ -z "$key" ]]; then
        printf 'Config key cannot be empty: %s\n' "$raw" >&2
        exit 1
    fi
    printf '%s\n%s\n' "$key" "$value"
}

contains_key() {
    local needle="$1"
    shift || true
    local item
    for item in "$@"; do
        if [[ "$item" == "$needle" ]]; then
            return 0
        fi
    done
    return 1
}

config_line_exists() {
    local key="$1"
    grep -q "^${key} = " "$CONFIG"
}

set_config_value() {
    local key="$1"
    local value="$2"
    if ! config_line_exists "$key"; then
        printf 'Config key not found in %s: %s\n' "$CONFIG" "$key" >&2
        exit 1
    fi
    sed -i "s|^${key} = .*|${key} = ${value}|" "$CONFIG"
}

get_config_value() {
    local key="$1"
    sed -n "s|^${key} = ||p" "$CONFIG"
}

resolve_active_paths() {
    local test_file_rel
    local source_file_rel

    test_file_rel="$(get_config_value test_code_file)"
    source_file_rel="$(get_config_value source_code_file)"

    if [[ -z "$test_file_rel" ]]; then
        printf 'Config value test_code_file is empty in %s\n' "$CONFIG" >&2
        exit 1
    fi

    ACTIVE_TEST_FILE="$REPO_ROOT/$test_file_rel"
    ACTIVE_TEST_BASENAME="$(basename "$ACTIVE_TEST_FILE" .java)"

    if [[ -n "$source_file_rel" ]]; then
        ACTIVE_SUBJECT_LABEL="$(basename "$source_file_rel" .java)"
    else
        ACTIVE_SUBJECT_LABEL="$ACTIVE_TEST_BASENAME"
        ACTIVE_SUBJECT_LABEL="${ACTIVE_SUBJECT_LABEL%Test}"
    fi
}

ensure_original_test_state_cached() {
    local test_file_abs="$1"
    local baseline_cache="${ORIGINAL_TEST_CACHE[$test_file_abs]:-}"

    if [[ -n "$baseline_cache" || "${ORIGINAL_TEST_EXISTS[$test_file_abs]:-unset}" != "unset" ]]; then
        return
    fi

    if [[ -f "$test_file_abs" ]]; then
        baseline_cache="$(mktemp "$REPO_ROOT/offline_runs/run_batch.test_baseline.XXXXXX")"
        cp "$test_file_abs" "$baseline_cache"
        ORIGINAL_TEST_CACHE["$test_file_abs"]="$baseline_cache"
        ORIGINAL_TEST_EXISTS["$test_file_abs"]="1"
    else
        ORIGINAL_TEST_CACHE["$test_file_abs"]=""
        ORIGINAL_TEST_EXISTS["$test_file_abs"]="0"
    fi
}

restore_baseline() {
    local baseline_abs="$1"
    if [[ -n "$baseline_abs" ]]; then
        cp "$baseline_abs" "$ACTIVE_TEST_FILE"
        return
    fi

    ensure_original_test_state_cached "$ACTIVE_TEST_FILE"
    if [[ "${ORIGINAL_TEST_EXISTS[$ACTIVE_TEST_FILE]}" == "1" ]]; then
        cp "${ORIGINAL_TEST_CACHE[$ACTIVE_TEST_FILE]}" "$ACTIVE_TEST_FILE"
    else
        rm -f "$ACTIVE_TEST_FILE"
    fi
}

save_test_snapshot() {
    local run_dir="$1"
    local suffix="$2"
    mkdir -p "$run_dir/backup"
    if [[ -f "$ACTIVE_TEST_FILE" ]]; then
        cp "$ACTIVE_TEST_FILE" "$run_dir/backup/${ACTIVE_TEST_BASENAME}_${suffix}.java"
    else
        : > "$run_dir/backup/${ACTIVE_TEST_BASENAME}_${suffix}.absent"
    fi
}

save_config_snapshot() {
    local run_dir="$1"
    cp "$CONFIG" "$run_dir/backup/config.ini.snapshot"
}

write_info() {
    local run_dir="$1"
    local run_idx="$2"
    local report_name="$3"
    local timestamp="$4"
    local run_status="${5:-unknown}"
    local exit_code="${6:-N/A}"
    {
        printf '# Batch Run R%s\n\n' "$run_idx"
        printf -- '- Batch name: %s\n' "$BATCH_NAME"
        printf -- '- Timestamp: %s\n' "$timestamp"
        printf -- '- Round: %s / %s\n' "$run_idx" "$ROUND_COUNT"
        if [[ -n "$BASELINE_REL" ]]; then
            printf -- '- Baseline test file: %s\n' "$BASELINE_REL"
        else
            printf -- '- Baseline test file: original `%s` contents\n' "$(get_config_value test_code_file)"
        fi
        printf -- '- report_filepath: %s\n' "$report_name"
        printf -- '- Run status: %s\n' "$run_status"
        printf -- '- Process exit code: %s\n' "$exit_code"
        printf -- '- Effective config overrides:\n'
        local key
        for key in "${!EFFECTIVE_SETTINGS[@]}"; do
            printf '  - %s = %s\n' "$key" "${EFFECTIVE_SETTINGS[$key]}"
        done | sort
    } > "$run_dir/info.md"
}

restore_config_on_exit() {
    local test_file_abs
    local cache_path
    if [[ -f "$ORIG_CONFIG" ]]; then
        cp "$ORIG_CONFIG" "$CONFIG"
        rm -f "$ORIG_CONFIG"
    fi

    for test_file_abs in "${!ORIGINAL_TEST_EXISTS[@]}"; do
        if [[ "${ORIGINAL_TEST_EXISTS[$test_file_abs]}" == "1" ]]; then
            cp "${ORIGINAL_TEST_CACHE[$test_file_abs]}" "$test_file_abs"
        else
            rm -f "$test_file_abs"
        fi
    done

    for cache_path in "${ORIGINAL_TEST_CACHE[@]}"; do
        if [[ -n "$cache_path" && -f "$cache_path" ]]; then
            rm -f "$cache_path"
        fi
    done
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --batch-name)
            require_value "$1" "${2:-}"
            BATCH_NAME="$2"
            shift 2
            ;;
        --rounds)
            require_value "$1" "${2:-}"
            ROUND_COUNT="$2"
            shift 2
            ;;
        --baseline)
            require_value "$1" "${2:-}"
            BASELINE_REL="$2"
            shift 2
            ;;
        --set)
            require_value "$1" "${2:-}"
            mapfile -t kv < <(parse_assignment "$2")
            COMMON_SETTINGS["${kv[0]}"]="${kv[1]}"
            if ! contains_key "${kv[0]}" "${COMMON_KEYS[@]}"; then
                COMMON_KEYS+=("${kv[0]}")
            fi
            shift 2
            ;;
        --set-per-round)
            require_value "$1" "${2:-}"
            mapfile -t kv < <(parse_assignment "$2")
            PER_ROUND_SETTINGS["${kv[0]}"]="${kv[1]}"
            if ! contains_key "${kv[0]}" "${PER_ROUND_KEYS[@]}"; then
                PER_ROUND_KEYS+=("${kv[0]}")
            fi
            shift 2
            ;;
        --help)
            usage
            exit 0
            ;;
        *)
            printf 'Unknown argument: %s\n' "$1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

if [[ -z "$BATCH_NAME" || -z "$ROUND_COUNT" ]]; then
    printf 'Both --batch-name and --rounds are required.\n' >&2
    usage >&2
    exit 1
fi

if ! [[ "$ROUND_COUNT" =~ ^[1-9][0-9]*$ ]]; then
    printf '--rounds must be a positive integer, got: %s\n' "$ROUND_COUNT" >&2
    exit 1
fi

BASELINE_ABS=""
if [[ -n "$BASELINE_REL" ]]; then
    BASELINE_ABS="$REPO_ROOT/$BASELINE_REL"
    if [[ ! -f "$BASELINE_ABS" ]]; then
        printf 'Baseline file not found: %s\n' "$BASELINE_ABS" >&2
        exit 1
    fi
fi

if [[ ! -f "$CONFIG" ]]; then
    printf 'Config file not found: %s\n' "$CONFIG" >&2
    exit 1
fi

cp "$CONFIG" "$ORIG_CONFIG"
trap restore_config_on_exit EXIT

for key in "${COMMON_KEYS[@]}"; do
    if ! config_line_exists "$key"; then
        printf 'Config key not found for --set: %s\n' "$key" >&2
        exit 1
    fi
done

for key in "${PER_ROUND_KEYS[@]}"; do
    if ! config_line_exists "$key"; then
        printf 'Config key not found for --set-per-round: %s\n' "$key" >&2
        exit 1
    fi
    IFS=',' read -r -a values <<< "${PER_ROUND_SETTINGS[$key]}"
    if [[ "${#values[@]}" -ne "$ROUND_COUNT" ]]; then
        printf 'Value count mismatch for %s: expected %s, got %s\n' "$key" "$ROUND_COUNT" "${#values[@]}" >&2
        exit 1
    fi
done

printf '==========================================\n'
printf ' Batch Runs: %s — %s\n' "$BATCH_NAME" "$(date)"
printf '==========================================\n'

for ((i=1; i<=ROUND_COUNT; i++)); do
    TIMESTAMP=$(date '+%Y-%m-%d_%H:%M:%S')
    REPORT_STAMP=$(date '+%Y%m%d_%H%M%S')
    REPORT_NAME="${BATCH_NAME//-/_}_r${i}_${REPORT_STAMP}.html"

    cp "$ORIG_CONFIG" "$CONFIG"
    EFFECTIVE_SETTINGS=()

    for key in "${COMMON_KEYS[@]}"; do
        set_config_value "$key" "${COMMON_SETTINGS[$key]}"
        EFFECTIVE_SETTINGS["$key"]="${COMMON_SETTINGS[$key]}"
    done

    for key in "${PER_ROUND_KEYS[@]}"; do
        IFS=',' read -r -a values <<< "${PER_ROUND_SETTINGS[$key]}"
        value="${values[$((i-1))]}"
        set_config_value "$key" "$value"
        EFFECTIVE_SETTINGS["$key"]="$value"
    done

    set_config_value report_filepath "$REPORT_NAME"
    EFFECTIVE_SETTINGS["report_filepath"]="$REPORT_NAME"

    resolve_active_paths
    ensure_original_test_state_cached "$ACTIVE_TEST_FILE"

    restore_baseline "$BASELINE_ABS"
    RUN_DIR="$LOG_DIR/${TIMESTAMP}_${BATCH_NAME}_r${i}_${ACTIVE_SUBJECT_LABEL}"

    mkdir -p "$RUN_DIR"
    save_test_snapshot "$RUN_DIR" before
    save_config_snapshot "$RUN_DIR"
    write_info "$RUN_DIR" "$i" "$REPORT_NAME" "$TIMESTAMP"

    printf '\n==========================================\n'
    printf ' [R%s/%s] Batch: %s\n' "$i" "$ROUND_COUNT" "$BATCH_NAME"
    printf ' Dir: %s\n' "$RUN_DIR"
    printf ' Start: %s\n' "$(date)"
    printf '==========================================\n'
    for key in "${!EFFECTIVE_SETTINGS[@]}"; do
        printf '  %s = %s\n' "$key" "${EFFECTIVE_SETTINGS[$key]}"
    done | sort

    set -a
    source "$SECRETS"
    set +a
    if conda run --no-capture-output -n panta-env python -m panta.main 2>&1 | tee "$RUN_DIR/runtime.log"; then
        RUN_EXIT_CODE=0
    else
        RUN_EXIT_CODE=$?
    fi

    save_test_snapshot "$RUN_DIR" after

    if [[ "$RUN_EXIT_CODE" -eq 0 ]]; then
        RUN_STATUS="completed"
    else
        RUN_STATUS="failed"
    fi
    write_info "$RUN_DIR" "$i" "$REPORT_NAME" "$TIMESTAMP" "$RUN_STATUS" "$RUN_EXIT_CODE"

    printf '[R%s] Done at %s\n' "$i" "$(date)"
    printf '[R%s] Status: %s (exit=%s)\n' "$i" "$RUN_STATUS" "$RUN_EXIT_CODE"
    printf '[R%s] Inspect: %s/runtime.log\n' "$i" "$RUN_DIR"

    SUMMARY_LINES+=("R${i}: status=${RUN_STATUS} exit=${RUN_EXIT_CODE} — ${RUN_DIR}")
    sleep 1
done

printf '\n==========================================\n'
printf ' All runs complete — %s\n' "$(date)"
printf '==========================================\n'
for line in "${SUMMARY_LINES[@]}"; do
    printf '  %s\n' "$line"
done
printf '\nConfig restored to original state.\n'
printf '==========================================\n'
