#!/usr/bin/env python3
import json
import sys
from pathlib import Path


def main() -> int:
    if len(sys.argv) != 3:
        print(
            "Usage: python offline_runs/query_class_params.py <project> <class>",
            file=sys.stderr,
        )
        return 1

    project, class_name = sys.argv[1], sys.argv[2]
    json_path = Path(__file__).with_name("class_list_path_params.json")
    data = json.loads(json_path.read_text(encoding="utf-8"))

    for entry in data["entries"]:
        if entry["project"] == project and entry["class"] == class_name:
            for key in (
                "project_directory",
                "source_code_file",
                "test_code_file",
                "code_coverage_report_path",
                "test_execution_command",
                "test_code_command_dir",
                "junit_version",
                "complexity",
            ):
                print(f"{key}={entry[key]}")
            return 0

    print(f"Entry not found: {project} / {class_name}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
