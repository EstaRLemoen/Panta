import json
import os
import re
from typing import Any, Dict, List


class ArtifactSnapshotter:
    """Stores generated/fixed test artifacts and per-round summaries."""

    def __init__(self, enabled: bool, dump_dir: str, run_id: str, logger):
        self.enabled = enabled
        self.dump_dir = dump_dir
        self.run_id = run_id
        self.logger = logger
        self._bucket_counters: Dict[str, int] = {}

        if self.enabled:
            self._ensure_run_directory()

    def _ensure_run_directory(self):
        try:
            os.makedirs(self._run_path(), exist_ok=True)
        except Exception as e:
            self.logger.warning(f"Failed to create artifact snapshot directory: {e}")
            self.enabled = False

    def _run_path(self) -> str:
        return os.path.join(self.dump_dir, self.run_id)

    @staticmethod
    def _sanitize_path_component(value: str) -> str:
        sanitized = re.sub(r"[^A-Za-z0-9_.-]+", "_", value.strip())
        return sanitized.strip("_") or "unnamed"

    def _write_json(self, filepath: str, data: Any):
        try:
            os.makedirs(os.path.dirname(filepath), exist_ok=True)
            with open(filepath, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
        except Exception as e:
            self.logger.warning(f"Failed to write artifact snapshot {filepath}: {e}")

    def capture_result(self, phase: str, parent_label: str, result: Dict[str, Any]):
        if not self.enabled:
            return None

        bucket_key = f"{phase}/{parent_label}"
        next_index = self._bucket_counters.get(bucket_key, 0) + 1
        self._bucket_counters[bucket_key] = next_index

        test_name = self._sanitize_path_component(
            str((result.get("test") or {}).get("test_name") or "unnamed_test")
        )
        status = self._sanitize_path_component(str(result.get("status") or "UNKNOWN"))
        filename = f"{next_index:03d}_{status}_{test_name}.json"
        filepath = os.path.join(self._run_path(), phase, parent_label, filename)

        payload = {
            "phase": phase,
            "parent_label": parent_label,
            **result,
        }
        self._write_json(filepath, payload)
        return filepath

    def capture_summary(
        self,
        phase: str,
        label: str,
        results: List[Dict[str, Any]],
        line_coverage: float,
        branch_coverage: float,
        token_count: int = None,
        duration_seconds: float = None,
    ):
        if not self.enabled:
            return None

        summary_results = []
        for result in results:
            test = result.get("test") or {}
            summary_results.append(
                {
                    "label": result.get("label"),
                    "status": result.get("status"),
                    "reason": result.get("reason"),
                    "test_name": test.get("test_name"),
                    "test_behavior": test.get("test_behavior"),
                    "line_coverage": result.get("line_coverage"),
                    "branch_coverage": result.get("branch_coverage"),
                }
            )

        summary = {
            "phase": phase,
            "label": label,
            "line_coverage": line_coverage,
            "branch_coverage": branch_coverage,
            "token_count": token_count,
            "duration_seconds": duration_seconds,
            "result_count": len(results),
            "results": summary_results,
        }
        filepath = os.path.join(self._run_path(), "summaries", f"{label}.json")
        self._write_json(filepath, summary)
        return filepath
