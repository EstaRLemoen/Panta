"""
CFG Snapshot Module

Provides functionality to capture and persist CFG intermediate states
for analysis, debugging, and paper reproducibility.
"""

import json
import os
import hashlib
from datetime import datetime
from typing import Optional, Dict, Any


class CFGSnapshotter:
    """Manages CFG snapshot capture and persistence."""

    def __init__(
        self,
        enabled: bool,
        dump_dir: str,
        dump_level: str,
        prompt_mode: str,
        run_id: str,
        logger,
    ):
        """
        Initialize the snapshotter.

        Args:
            enabled: Whether snapshot capture is enabled
            dump_dir: Base directory for snapshots
            dump_level: "core" or "full" - controls what gets captured
            prompt_mode: "summary" or "full_text" - controls prompt storage
            run_id: Unique identifier for this run
            logger: Logger instance for warnings/errors
        """
        self.enabled = enabled
        self.dump_dir = dump_dir
        self.dump_level = dump_level
        self.prompt_mode = prompt_mode
        self.run_id = run_id
        self.logger = logger
        self.sequence_counter = 0

        # Semantic change: snapshoting is explicitly opt-in and defaults to disabled.
        if self.enabled:
            self._ensure_run_directory()

    def _ensure_run_directory(self):
        """Create the run directory if it doesn't exist."""
        try:
            run_path = os.path.join(self.dump_dir, self.run_id)
            os.makedirs(run_path, exist_ok=True)
            self.logger.info(f"CFG snapshot directory created: {run_path}")
        except Exception as e:
            self.logger.warning(f"Failed to create snapshot directory: {e}")
            self.enabled = False

    def capture(
        self,
        stage: str,
        source_code_file: str,
        language: str,
        cfg_driver=None,
        context: Optional[Dict[str, Any]] = None,
    ) -> Optional[str]:
        """
        Capture a CFG snapshot at a specific stage.

        Args:
            stage: Stage identifier (e.g., "prompt_builder_cfg_init")
            source_code_file: Path to source file being analyzed
            language: Programming language
            cfg_driver: CombinedDriver or CFGDriver instance
            context: Additional context (path_history, selected_paths, etc.)

        Returns:
            Path to snapshot directory if successful, None otherwise
        """
        # Semantic change: snapshot capture is non-intrusive and never blocks runtime flow.
        if not self.enabled:
            return None

        try:
            # Create stage directory
            self.sequence_counter += 1
            stage_dir_name = f"{self.sequence_counter:03d}_{stage}"
            stage_path = os.path.join(self.dump_dir, self.run_id, stage_dir_name)
            os.makedirs(stage_path, exist_ok=True)

            # Capture metadata
            self._write_meta(stage_path, stage, source_code_file, language, context)

            # Capture CFG core outputs
            if cfg_driver:
                self._write_cfg_outputs(stage_path, cfg_driver)

            # Capture additional context if dump_level is "full"
            if self.dump_level == "full" and context:
                self._write_context(stage_path, context)

            self.logger.debug(f"CFG snapshot captured: {stage_path}")
            return stage_path

        except Exception as e:
            # Semantic change: write failures are downgraded to warnings by design.
            self.logger.warning(
                f"Failed to capture CFG snapshot for stage '{stage}': {e}"
            )
            return None

    def _write_meta(
        self,
        stage_path: str,
        stage: str,
        source_code_file: str,
        language: str,
        context: Optional[Dict[str, Any]],
    ):
        """Write metadata file."""
        try:
            source_basename = os.path.basename(source_code_file)

            # Compute source file hash if it exists
            source_sha256 = None
            source_size = None
            source_line_count = None
            if os.path.exists(source_code_file):
                try:
                    with open(source_code_file, "rb") as f:
                        content = f.read()
                        source_sha256 = hashlib.sha256(content).hexdigest()
                        source_size = len(content)
                        source_line_count = (
                            content.decode("utf-8", errors="ignore").count("\n") + 1
                        )
                except Exception as e:
                    self.logger.warning(f"Failed to compute source file hash: {e}")

            meta = {
                "run_id": self.run_id,
                "stage": stage,
                "sequence": self.sequence_counter,
                "timestamp": datetime.now().isoformat(),
                "source_code_file": source_code_file,
                "source_basename": source_basename,
                "source_sha256": source_sha256,
                "source_size_bytes": source_size,
                "source_line_count": source_line_count,
                "language": language,
                "dump_level": self.dump_level,
                "prompt_mode": self.prompt_mode,
            }

            # Add context metadata if available
            if context:
                if "iteration" in context:
                    meta["iteration"] = context["iteration"]
                if "method_label" in context:
                    meta["method_label"] = context["method_label"]
                if "path_index" in context:
                    meta["path_index"] = context["path_index"]

            self._write_json(stage_path, "meta.json", meta)

        except Exception as e:
            self.logger.warning(f"Failed to write meta file: {e}")

    def _write_cfg_outputs(self, stage_path: str, cfg_driver):
        """Write CFG core outputs."""
        try:
            # Write file_obj
            if hasattr(cfg_driver, "file_obj") and cfg_driver.file_obj:
                self._write_json(stage_path, "cfg_file_obj.json", cfg_driver.file_obj)

            # Write node mappings
            if (
                hasattr(cfg_driver, "node_id_to_line_number")
                and cfg_driver.node_id_to_line_number
            ):
                self._write_json(
                    stage_path,
                    "cfg_node_id_to_line_number.json",
                    cfg_driver.node_id_to_line_number,
                )

            if (
                hasattr(cfg_driver, "line_number_to_node_id")
                and cfg_driver.line_number_to_node_id
            ):
                # Convert tuple values to lists for JSON serialization
                serializable = {
                    k: list(v) if isinstance(v, tuple) else v
                    for k, v in cfg_driver.line_number_to_node_id.items()
                }
                self._write_json(
                    stage_path, "cfg_line_number_to_node_id.json", serializable
                )

            if (
                hasattr(cfg_driver, "preprocessed_to_original_line")
                and cfg_driver.preprocessed_to_original_line
            ):
                self._write_json(
                    stage_path,
                    "cfg_preprocessed_to_original_line.json",
                    cfg_driver.preprocessed_to_original_line,
                )

            if (
                hasattr(cfg_driver, "original_to_preprocessed_line")
                and cfg_driver.original_to_preprocessed_line
            ):
                self._write_json(
                    stage_path,
                    "cfg_original_to_preprocessed_line.json",
                    cfg_driver.original_to_preprocessed_line,
                )

            # Write testable methods statistics if available
            if (
                hasattr(cfg_driver, "testable_methods_statistics")
                and cfg_driver.testable_methods_statistics
            ):
                self._write_json(
                    stage_path,
                    "cfg_testable_methods_statistics.json",
                    cfg_driver.testable_methods_statistics,
                )

            # Write CFG summary
            self._write_cfg_summary(stage_path, cfg_driver)

        except Exception as e:
            self.logger.warning(f"Failed to write CFG outputs: {e}")

    def _write_cfg_summary(self, stage_path: str, cfg_driver):
        """Write a summary of CFG structure."""
        try:
            summary = {}

            if hasattr(cfg_driver, "file_obj") and cfg_driver.file_obj:
                file_obj = cfg_driver.file_obj

                # Count classes and methods
                if "class_objects" in file_obj:
                    summary["num_classes"] = len(file_obj["class_objects"])

                    total_methods = 0
                    complexity_distribution = {"=1": 0, "2-10": 0, "11-20": 0, ">20": 0}
                    total_paths = 0

                    for cls in file_obj["class_objects"]:
                        if "methods_under_test" in cls:
                            methods = cls["methods_under_test"]
                            total_methods += len(methods)

                            for method in methods:
                                if "method_declaration" in method:
                                    complexity = method["method_declaration"].get(
                                        "complexity", 0
                                    )
                                    if complexity == 1:
                                        complexity_distribution["=1"] += 1
                                    elif 2 <= complexity <= 10:
                                        complexity_distribution["2-10"] += 1
                                    elif 11 <= complexity <= 20:
                                        complexity_distribution["11-20"] += 1
                                    elif complexity > 20:
                                        complexity_distribution[">20"] += 1

                                if "paths" in method:
                                    total_paths += len(method["paths"])

                    summary["num_methods"] = total_methods
                    summary["complexity_distribution"] = complexity_distribution
                    summary["total_paths"] = total_paths

                # Count imports
                if "imports" in file_obj:
                    summary["num_imports"] = len(file_obj["imports"])

            if summary:
                self._write_json(stage_path, "cfg_summary.json", summary)

        except Exception as e:
            self.logger.warning(f"Failed to write CFG summary: {e}")

    def _write_context(self, stage_path: str, context: Dict[str, Any]):
        """Write additional context for full dump level."""
        try:
            # Write path_history if present
            if "path_history" in context and context["path_history"]:
                self._write_json(
                    stage_path, "path_history.json", context["path_history"]
                )

            # Write selected_paths if present
            if "selected_paths" in context and context["selected_paths"]:
                self._write_json(
                    stage_path, "selected_paths.json", context["selected_paths"]
                )

            # Write rendered_prompt_context
            if "prompt_context" in context:
                prompt_ctx = context["prompt_context"]

                if self.prompt_mode == "summary":
                    # Only write structured summary
                    summary = {
                        "method_name": prompt_ctx.get("method_name"),
                        "target_methods": prompt_ctx.get("target_methods"),
                        "target_lines": prompt_ctx.get("target_lines"),
                        "target_branches": prompt_ctx.get("target_branches"),
                        "advice_summary": prompt_ctx.get("advice_summary"),
                        "annotated_line_count": prompt_ctx.get("annotated_line_count"),
                        "selected_path_labels": prompt_ctx.get("selected_path_labels"),
                        "num_candidate_paths": prompt_ctx.get("num_candidate_paths"),
                        "token_count": prompt_ctx.get("token_count"),
                        "prompt_length_chars": len(str(prompt_ctx.get("prompt", ""))),
                    }
                    self._write_json(
                        stage_path, "rendered_prompt_context.json", summary
                    )
                else:
                    # Write full prompt text
                    self._write_json(
                        stage_path, "rendered_prompt_context.json", prompt_ctx
                    )
            # Write generation_outcome if present
            if "generation_outcome" in context:
                self._write_json(
                    stage_path, "generation_outcome.json", context["generation_outcome"]
                )

        except Exception as e:
            self.logger.warning(f"Failed to write context: {e}")

    def _write_json(self, stage_path: str, filename: str, data: Any):
        """Write JSON file with error handling."""
        try:
            filepath = os.path.join(stage_path, filename)
            with open(filepath, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
        except Exception as e:
            self.logger.warning(f"Failed to write {filename}: {e}")


def generate_run_id(source_code_file: str) -> str:
    """
    Generate a unique run ID based on source file and timestamp.

    Args:
        source_code_file: Path to source file

    Returns:
        Run ID string in format: {basename}_{timestamp}_{hash}
    """
    basename = os.path.splitext(os.path.basename(source_code_file))[0]
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    # Generate short hash from full path and timestamp
    hash_input = f"{source_code_file}_{timestamp}".encode("utf-8")
    short_hash = hashlib.sha256(hash_input).hexdigest()[:8]

    return f"{basename}_{timestamp}_{short_hash}"
