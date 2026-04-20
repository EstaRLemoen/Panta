import csv
import re
from collections import Counter
from pathlib import Path


REPORT_ROOT = Path("/home/esta/Panta/Panta/fixmode_compare_html")
OUTPUT_DIR = Path("/home/esta/Panta/Panta/test_analysis/fixmode_compare")
PER_REPORT_OUTPUT_DIR = OUTPUT_DIR / "per_report_fixing_summary"
REPORT_GLOB = "*fixmode_compare_ver*.html"

ROW_RE = re.compile(
    r'<tr>\s*<td class="status-([A-Z]+)">([A-Z]+)</td>'
    r"\s*<td>([^<]+)</td>"
    r"\s*<td>([^<]*)</td>"
    r"\s*<td>([^<]*)</td>"
    r"\s*<td>([^<]*)</td>"
    r"\s*<td>([^<]*)</td>",
    re.S,
)


def parse_report_metadata(report_path: Path) -> dict:
    stem = report_path.stem
    tail_match = re.search(r"_r(?P<round>\d+)_(?P<timestamp>\d{8}_\d{6})$", stem)
    if not tail_match:
        raise ValueError(f"Unexpected report filename: {report_path.name}")

    prefix = stem[: tail_match.start()]
    mode_match = re.search(
        r"_(?P<mode>combined|separate)(?:_(?P<mode_suffix>[^_]+))?$", prefix
    )
    if not mode_match:
        raise ValueError(f"Unexpected report filename: {report_path.name}")

    mode = mode_match.group("mode")
    mode_suffix = mode_match.group("mode_suffix")
    variant_prefix = prefix[: mode_match.start()]

    subject_marker = "_control_fixmode_compare_"
    if subject_marker not in variant_prefix:
        raise ValueError(f"Unexpected report filename: {report_path.name}")

    subject, variant = variant_prefix.split(subject_marker, 1)
    if mode_suffix:
        variant = f"{variant}_{mode_suffix}"
    version_match = re.match(r"^(ver\d+)", variant)
    return {
        "subject": subject,
        "variant": variant,
        "version": version_match.group(1) if version_match else variant,
        "mode": mode,
        "round": tail_match.group("round"),
        "timestamp": tail_match.group("timestamp"),
    }


def parse_rows(report_path: Path) -> list[dict]:
    text = report_path.read_text(encoding="utf-8", errors="ignore")
    rows = []
    for _, status, label, reason, exit_code, line_cov, branch_cov in ROW_RE.findall(
        text
    ):
        rows.append(
            {
                "status": status.strip(),
                "label": label.strip(),
                "reason": reason.strip(),
                "exit_code": exit_code.strip(),
                "line_coverage": line_cov.strip(),
                "branch_coverage": branch_cov.strip(),
            }
        )
    return rows


def build_report_stem(metadata: dict) -> str:
    return (
        f"{metadata['subject']}_{metadata['version']}_{metadata['mode']}_"
        f"r{metadata['round']}_{metadata['timestamp']}"
    )


def summarize_stage_rows(report_path: Path) -> tuple[list[dict], dict]:
    metadata = parse_report_metadata(report_path)
    rows = parse_rows(report_path)

    ordered_labels = []
    grouped_rows: dict[str, list[dict]] = {}
    for row in rows:
        label = row["label"]
        if not label.startswith(("g_", "f_")):
            continue
        if row["status"] == "INFO":
            continue
        if label not in grouped_rows:
            grouped_rows[label] = []
            ordered_labels.append(label)
        grouped_rows[label].append(row)

    stage_summary_rows = []
    for label in ordered_labels:
        label_rows = grouped_rows[label]
        status_counts = Counter(row["status"] for row in label_rows)
        reason_counts = Counter(row["reason"] for row in label_rows if row["reason"])
        stage_summary_rows.append(
            {
                "label": label,
                "generated_test_count": status_counts.get("PASS", 0)
                + status_counts.get("FAIL", 0),
                "skipped_test_count": status_counts.get("SKIP", 0),
                "pass_test_count": status_counts.get("PASS", 0),
                "compilation_failure_test_count": reason_counts.get(
                    "Compilation failure", 0
                ),
                "runtime_failure_test_count": reason_counts.get("Test failures", 0),
            }
        )

    final_cov_row = None
    for row in rows:
        if row["label"].startswith(("g_", "f_")):
            final_cov_row = row

    fix_rows = [row for row in rows if row["label"].startswith("f_")]
    fix_status_counts = Counter(row["status"] for row in fix_rows)
    fix_reason_counts = Counter(row["reason"] for row in fix_rows if row["reason"])
    comp_fix_rows = [row for row in fix_rows if row["label"].endswith("_comp")]
    rt_fix_rows = [row for row in fix_rows if row["label"].endswith("_rt")]
    comp_fix_status_counts = Counter(row["status"] for row in comp_fix_rows)
    rt_fix_status_counts = Counter(row["status"] for row in rt_fix_rows)
    g_with_fix = set()
    g_rescued = set()
    for row in fix_rows:
        match = re.match(r"f_(\d+)", row["label"])
        if not match:
            continue
        g_label = f"g_{match.group(1)}"
        g_with_fix.add(g_label)
        if row["status"] == "PASS":
            g_rescued.add(g_label)

    attempt_denominator = fix_status_counts.get("PASS", 0) + fix_status_counts.get(
        "FAIL", 0
    )
    attempt_success_rate = (
        round((fix_status_counts.get("PASS", 0) / attempt_denominator) * 100, 1)
        if attempt_denominator
        else ""
    )
    comp_attempt_denominator = comp_fix_status_counts.get(
        "PASS", 0
    ) + comp_fix_status_counts.get("FAIL", 0)
    comp_attempt_success_rate = (
        round(
            (comp_fix_status_counts.get("PASS", 0) / comp_attempt_denominator) * 100,
            1,
        )
        if comp_attempt_denominator
        else ""
    )
    rt_attempt_denominator = rt_fix_status_counts.get(
        "PASS", 0
    ) + rt_fix_status_counts.get("FAIL", 0)
    rt_attempt_success_rate = (
        round((rt_fix_status_counts.get("PASS", 0) / rt_attempt_denominator) * 100, 1)
        if rt_attempt_denominator
        else ""
    )
    comp_skip_rate = (
        round((comp_fix_status_counts.get("SKIP", 0) / len(comp_fix_rows)) * 100, 1)
        if comp_fix_rows
        else ""
    )
    rescue_rate = (
        round((len(g_rescued) / len(g_with_fix)) * 100, 1) if g_with_fix else ""
    )

    report_summary_row = {
        **metadata,
        "report_file": report_path.name,
        "final_line_coverage": final_cov_row["line_coverage"] if final_cov_row else "",
        "final_branch_coverage": final_cov_row["branch_coverage"]
        if final_cov_row
        else "",
        "comp_fix_attempt_success_rate": comp_attempt_success_rate,
        "rt_fix_attempt_success_rate": rt_attempt_success_rate,
        "comp_skip_rate": comp_skip_rate,
        "fix_label_count": len(
            {row["label"] for row in fix_rows if row["status"] != "INFO"}
        ),
        "fix_attempt_total": attempt_denominator,
        "fix_pass_total": fix_status_counts.get("PASS", 0),
        "fix_fail_total": fix_status_counts.get("FAIL", 0),
        "fix_skip_total": fix_status_counts.get("SKIP", 0),
        "comp_fix_total": len(comp_fix_rows),
        "comp_fix_pass_total": comp_fix_status_counts.get("PASS", 0),
        "comp_fix_fail_total": comp_fix_status_counts.get("FAIL", 0),
        "comp_fix_skip_total": comp_fix_status_counts.get("SKIP", 0),
        "rt_fix_total": len(rt_fix_rows),
        "rt_fix_pass_total": rt_fix_status_counts.get("PASS", 0),
        "rt_fix_fail_total": rt_fix_status_counts.get("FAIL", 0),
        "rt_fix_skip_total": rt_fix_status_counts.get("SKIP", 0),
        "compilation_failure_total": fix_reason_counts.get("Compilation failure", 0),
        "runtime_failure_total": fix_reason_counts.get("Test failures", 0),
        "timeout_total": fix_reason_counts.get("Timeout", 0),
        "is_obviously_repeated_total": fix_reason_counts.get(
            "is_obviously_repeated", 0
        ),
        "g_with_fix_count": len(g_with_fix),
        "g_rescued_count": len(g_rescued),
        "attempt_success_rate": attempt_success_rate,
        "rescue_rate": rescue_rate,
    }
    return stage_summary_rows, report_summary_row


def write_csv(output_path: Path, rows: list[dict], fieldnames: list[str]) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def project_rows(rows: list[dict], fieldnames: list[str]) -> list[dict]:
    return [{field: row.get(field, "") for field in fieldnames} for row in rows]


def main() -> None:
    report_paths = sorted(REPORT_ROOT.glob(REPORT_GLOB))
    if not report_paths:
        raise SystemExit(f"No reports found under {REPORT_ROOT} matching {REPORT_GLOB}")

    per_report_fieldnames = [
        "label",
        "generated_test_count",
        "skipped_test_count",
        "pass_test_count",
        "compilation_failure_test_count",
        "runtime_failure_test_count",
    ]
    summary_fieldnames = [
        "report_file",
        "subject",
        "round",
        "variant",
        "version",
        "mode",
        "attempt_success_rate",
        "rescue_rate",
        "comp_fix_attempt_success_rate",
        "rt_fix_attempt_success_rate",
        "comp_skip_rate",
        "g_with_fix_count",
        "g_rescued_count",
        "final_line_coverage",
        "final_branch_coverage",
        "fix_label_count",
        "fix_attempt_total",
        "fix_pass_total",
        "fix_fail_total",
        "fix_skip_total",
        "comp_fix_total",
        "comp_fix_pass_total",
        "comp_fix_fail_total",
        "comp_fix_skip_total",
        "rt_fix_total",
        "rt_fix_pass_total",
        "rt_fix_fail_total",
        "rt_fix_skip_total",
        "compilation_failure_total",
        "runtime_failure_total",
        "timeout_total",
        "is_obviously_repeated_total",
        "timestamp",
    ]
    summary_rows = []
    for report_path in report_paths:
        per_report_rows, report_summary_row = summarize_stage_rows(report_path)
        summary_rows.append(report_summary_row)
        report_stem = build_report_stem(parse_report_metadata(report_path))
        write_csv(
            PER_REPORT_OUTPUT_DIR / f"{report_stem}_fixing_summary.csv",
            per_report_rows,
            per_report_fieldnames,
        )

    write_csv(
        OUTPUT_DIR / "fixmode_compare_report_summary.csv",
        project_rows(summary_rows, summary_fieldnames),
        summary_fieldnames,
    )


if __name__ == "__main__":
    main()
