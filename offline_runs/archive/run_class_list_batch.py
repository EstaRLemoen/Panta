#!/usr/bin/env python3
import argparse
import csv
import json
import subprocess
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
CLASS_LIST_PATH = REPO_ROOT / "evaluation" / "data" / "class_list.csv"
CODEFILES_DIR = REPO_ROOT / "evaluation" / "defects4j-codefiles"
RUN_BATCH_PATH = REPO_ROOT / "offline_runs" / "run_batch.sh"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Select classes from evaluation/data/class_list.csv and run them via offline_runs/run_batch.sh.",
    )
    parser.add_argument(
        "--project",
        action="append",
        dest="projects",
        required=True,
        help="Project name from class_list.csv. Can be repeated.",
    )
    parser.add_argument(
        "--class",
        action="append",
        dest="classes",
        default=[],
        help="Simple class name from class_list.csv. Can be repeated. If omitted, all matching classes in the selected projects are considered.",
    )
    parser.add_argument(
        "--min-complexity",
        type=int,
        default=None,
        help="Only include classes whose complexity is at least this value.",
    )
    parser.add_argument(
        "--max-classes",
        type=int,
        default=None,
        help="Stop after selecting this many classes.",
    )
    parser.add_argument(
        "--rounds",
        type=int,
        default=2,
        help="Number of rounds passed to run_batch.sh for each selected class.",
    )
    parser.add_argument(
        "--batch-prefix",
        default="class-list",
        help="Prefix added to each per-class batch name.",
    )
    parser.add_argument(
        "--set",
        action="append",
        dest="extra_sets",
        default=[],
        help="Extra KEY=VALUE config override forwarded to run_batch.sh. Can be repeated.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print selected classes and commands without executing them.",
    )
    return parser.parse_args()


def read_class_list() -> list[dict[str, object]]:
    rows: list[dict[str, object]] = []
    with CLASS_LIST_PATH.open("r", encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            rows.append(
                {
                    "project": row["project"],
                    "class": row["class"],
                    "complexity": int(row["complexity"]),
                }
            )
    return rows


def load_codefiles(project: str) -> list[dict[str, object]]:
    codefiles_path = CODEFILES_DIR / f"{project}-codefiles.json"
    if not codefiles_path.exists():
        raise FileNotFoundError(
            f"Missing codefiles JSON for project {project}: {codefiles_path}"
        )
    with codefiles_path.open("r", encoding="utf-8") as handle:
        data = json.load(handle)
    return (
        data["src_test_exact_match"]
        + data["src_test_fuzz_match"]
        + data["src_without_tests"]
    )


def derive_project_dir(project: str) -> str:
    if project == "Gson-16f":
        return f"defects4j-subjects-notests/{project}/gson"
    return f"defects4j-subjects-notests/{project}"


def derive_test_path(project: str, src_file: dict[str, object]) -> str:
    explicit_test_path = str(src_file.get("test_path", "")).strip()
    if explicit_test_path:
        test_path = explicit_test_path.replace(
            "defects4j-subjects", "defects4j-subjects-notests"
        )
        return test_path.lstrip("../")

    src_path = str(src_file["src_path"]).replace(
        "defects4j-subjects", "defects4j-subjects-notests"
    )
    src_path = src_path.lstrip("../")
    src_name = str(src_file["src_name"])
    src_file_path = Path(src_path)

    if project == "JxPath-22f":
        test_dir = str(src_file_path.parent).replace("src/java", "src/test")
    else:
        test_dir = str(src_file_path.parent).replace("src/main/java", "src/test/java")

    return f"{test_dir}/{src_name}Test.java"


def build_selection(args: argparse.Namespace) -> list[dict[str, object]]:
    wanted_projects = set(args.projects)
    wanted_classes = set(args.classes)
    rows = read_class_list()
    codefiles_cache: dict[str, list[dict[str, object]]] = {}
    selections: list[dict[str, object]] = []

    for row in rows:
        project = str(row["project"])
        class_name = str(row["class"])
        complexity = int(row["complexity"])

        if project not in wanted_projects:
            continue
        if wanted_classes and class_name not in wanted_classes:
            continue
        if args.min_complexity is not None and complexity < args.min_complexity:
            continue

        if project not in codefiles_cache:
            codefiles_cache[project] = load_codefiles(project)

        matched_src_file = None
        for src_file in codefiles_cache[project]:
            if src_file["src_name"] == class_name:
                matched_src_file = src_file
                break

        if matched_src_file is None:
            raise ValueError(
                f"Could not find src_name={class_name} in {project}-codefiles.json"
            )

        src_path = str(matched_src_file["src_path"]).replace(
            "defects4j-subjects", "defects4j-subjects-notests"
        )
        src_path = src_path.lstrip("../")
        project_dir = derive_project_dir(project)
        test_path = derive_test_path(project, matched_src_file)

        selections.append(
            {
                "project": project,
                "class": class_name,
                "complexity": complexity,
                "project_directory": project_dir,
                "source_code_file": src_path,
                "test_code_file": test_path,
                "code_coverage_report_path": f"{project_dir}/target/jacoco/jacoco.csv",
                "test_execution_command": f"mvn clean package -Dtest={class_name}Test",
                "test_code_command_dir": f"{project_dir}/",
                "junit_version": "4",
            }
        )

        if args.max_classes is not None and len(selections) >= args.max_classes:
            break

    if wanted_classes:
        selected_classes = {str(item["class"]) for item in selections}
        missing = [
            class_name
            for class_name in wanted_classes
            if class_name not in selected_classes
        ]
        if missing:
            raise ValueError(
                "Requested classes were not selected from the chosen projects: "
                + ", ".join(sorted(missing))
            )

    return selections


def build_command(selection: dict[str, object], args: argparse.Namespace) -> list[str]:
    batch_name = f"{args.batch_prefix}-{selection['project']}-{selection['class']}"
    command = [
        "bash",
        str(RUN_BATCH_PATH),
        "--batch-name",
        batch_name,
        "--rounds",
        str(args.rounds),
        "--set",
        f"project_directory={selection['project_directory']}",
        "--set",
        f"source_code_file={selection['source_code_file']}",
        "--set",
        f"test_code_file={selection['test_code_file']}",
        "--set",
        f"code_coverage_report_path={selection['code_coverage_report_path']}",
        "--set",
        f"test_execution_command={selection['test_execution_command']}",
        "--set",
        f"test_code_command_dir={selection['test_code_command_dir']}",
        "--set",
        f"junit_version={selection['junit_version']}",
        "--set",
        f"maximum_iterations={selection['complexity']}",
        "--set",
        "enable_fixing=3",
        "--set",
        "no_coverage_increase_iterations=3",
        "--set",
        "llm_advice_activation_line_coverage=100",
        "--set",
        "enable_advice_feedback=false",
    ]
    for assignment in args.extra_sets:
        command.extend(["--set", assignment])
    return command


def main() -> int:
    args = parse_args()
    selections = build_selection(args)

    if not selections:
        print("No classes matched the current filters.", file=sys.stderr)
        return 1

    print("Selected classes:")
    for item in selections:
        print(
            f"- {item['project']}:{item['class']} "
            f"(complexity={item['complexity']}, rounds={args.rounds}, batch_prefix={args.batch_prefix})"
        )

    for item in selections:
        command = build_command(item, args)
        print("\nCommand:")
        print(" ".join(subprocess.list2cmdline([part]) for part in command))
        if args.dry_run:
            continue

        completed = subprocess.run(command, cwd=REPO_ROOT)
        if completed.returncode != 0:
            return completed.returncode

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
