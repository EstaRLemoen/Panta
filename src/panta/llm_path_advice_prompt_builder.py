import logging

import yaml
from jinja2 import Environment, StrictUndefined

from .config_loader import get_settings
from .model_invocation.llm_invocation import LLMInvocation
from .panta_logger import pantaLogger
from .yaml_parser_utils import load_yaml


COMMENT_SUFFIX_BY_LANGUAGE = {
    "java": "//",
}


class CoverageAnnotatedSourceBuilder:
    def __init__(self, language: str):
        self.language = (language or "").lower()
        self.annotated_source = None
        self.uncovered_branches = None
        self.uncovered_branches_text = None
        self.uncovered_lines = None

    def build(self, source_code: str, lines_missed, branch_missed):
        comment_suffix = COMMENT_SUFFIX_BY_LANGUAGE.get(self.language)
        if not comment_suffix:
            raise NotImplementedError(
                f"Annotated source is not implemented for language '{self.language}'"
            )

        missed_lines = set(lines_missed or [])
        missed_branches = set(branch_missed or [])
        annotated_lines = []
        uncovered_branches = []
        uncovered_lines = []
        for index, line in enumerate(source_code.splitlines(), start=1):
            tags = []
            if index in missed_lines:
                tags.append("uncovered-line")
                uncovered_lines.append([index, line])
            if index in missed_branches:
                tags.append("uncovered-branch")
                uncovered_branches.append(f"{index}: {line}")

            if tags:
                annotation = f" {comment_suffix} PANTA: {', '.join(tags)}"
                annotated_lines.append(f"{line}{annotation}")
            else:
                annotated_lines.append(line)
        self.annotated_source = "\n".join(annotated_lines)
        self.uncovered_branches = uncovered_branches
        self.uncovered_branches_text = "\n".join(uncovered_branches)
        self.uncovered_lines = uncovered_lines

    def get_annotated_source(self) -> str:
        return self.annotated_source

    def get_uncovered_branches(self) -> list:
        return self.uncovered_branches

    def get_uncovered_branches_text(self) -> str:
        return self.uncovered_branches_text


class LLMPathAdvicePromptBuilder:
    def __init__(
        self,
        source_code_file: str,
        source_file_name: str,
        test_file_name: str,
        source_file: str,
        test_file: str,
        code_coverage_report: str,
        included_files: str,
        additional_instructions: str,
        failed_test_runs_feedback: str,
        coverage_invalid_tests: str,
        language: str,
        lines_missed,
        branch_missed,
        test_dependencies: str,
        llm_model: str,
        current_coverage=None,
        no_coverage_increase_count: int = 0,
        llm_advice_activation_line_coverage: float = 50.0,
        llm_advice_activation_no_growth: int = 1,
        snapshotter=None,
    ):
        self.source_code_file_path = source_code_file
        self.source_file_name = source_file_name
        self.test_file_name = test_file_name
        self.source_file = source_file
        self.test_file = test_file
        self.code_coverage_report = code_coverage_report
        self.included_files = included_files
        self.additional_instructions = additional_instructions
        self.failed_test_runs_feedback = failed_test_runs_feedback
        self.coverage_invalid_tests = coverage_invalid_tests
        self.language = language
        self.lines_missed = lines_missed or []
        self.branch_missed = branch_missed or []
        self.test_dependencies = test_dependencies
        self.current_coverage = current_coverage or (0.0, 0.0)
        self.no_coverage_increase_count = no_coverage_increase_count
        self.llm_advice_activation_line_coverage = llm_advice_activation_line_coverage
        self.llm_advice_activation_no_growth = llm_advice_activation_no_growth
        self.snapshotter = snapshotter
        self.llm_model = llm_model
        self.llm_invoker = LLMInvocation(model=llm_model)
        self.logger = pantaLogger.initialize_logger(__name__)
        self.selection_state = {"mode": "llm", "advice_history": []}
        self.builder = None
        self.last_advice_fallback_reason = None

    def build_prompt_guided(self) -> dict:
        self._build_annotated_source()
        annotated_source_code = self.builder.get_annotated_source()
        uncovered_branches = self.builder.get_uncovered_branches()
        if self._should_use_advice():
            advice = self._generate_advice(
                annotated_source_code,
                uncovered_branches,
                light_mode=False,
            )
            prompt = self._build_final_generation_prompt(annotated_source_code, advice)
            self.selection_state = self._build_selection_state(advice, mode="llm")
        else:
            advice = self._generate_advice(
                annotated_source_code,
                uncovered_branches,
                light_mode=True,
            )
            prompt = self._build_final_generation_prompt(annotated_source_code, advice)
            self.selection_state = self._build_selection_state(
                advice,
                mode="llm-light-advice",
            )
        return prompt

    def get_current_selection_state(self):
        return self.selection_state

    def _should_use_advice(self) -> bool:
        current_line_coverage = (self.current_coverage[0] or 0.0) * 100
        if current_line_coverage >= self.llm_advice_activation_line_coverage:
            return True
        return self.no_coverage_increase_count >= self.llm_advice_activation_no_growth

    def _build_annotated_source(self):
        self.builder = CoverageAnnotatedSourceBuilder(self.language)
        self.builder.build(self.source_file, self.lines_missed, self.branch_missed)

    def _build_advice_prompt(
        self, annotated_source_code: str, uncovered_branches, light_mode: bool = False
    ) -> dict:
        variables = {
            "source_file_name": self.source_file_name,
            "annotated_source_code": annotated_source_code,
            "uncovered_branches": uncovered_branches,
            "uncovered_branches_text": self.builder.get_uncovered_branches_text(),
            "language": self.language,
        }
        environment = Environment(undefined=StrictUndefined)
        settings = get_settings()
        prompt_setting = (
            settings.test_generation_llm_light_advice_selection_prompt
            if light_mode
            else settings.test_generation_llm_advice_selection_prompt
        )
        return {
            "system": environment.from_string(prompt_setting.system).render(variables),
            "user": environment.from_string(prompt_setting.user).render(variables),
        }

    def _generate_advice(
        self,
        annotated_source_code: str,
        uncovered_branches,
        light_mode: bool = False,
    ) -> dict:
        advice_prompt = self._build_advice_prompt(
            annotated_source_code, uncovered_branches, light_mode=light_mode
        )
        response, prompt_tokens, response_tokens = self.llm_invoker.call_model(
            prompt=advice_prompt, max_tokens=2048, temperature=0.1
        )
        advice = load_yaml(response) or {}
        advice = self._normalize_advice(advice)
        if self.last_advice_fallback_reason:
            self.logger.warning(
                "LLM advice invalid; fell back to default advice. reason: %s",
                self.last_advice_fallback_reason,
            )
        self._capture_advice_snapshot(
            annotated_source_code,
            advice_prompt,
            advice,
            prompt_tokens + response_tokens,
            light_mode=light_mode,
        )
        return advice

    def _normalize_advice(self, advice: dict) -> dict:
        self.last_advice_fallback_reason = None
        if not isinstance(advice, dict):
            self.last_advice_fallback_reason = "parsed response is not a mapping"
            return self._fallback_advice()

        normalized = {
            "focus_summary": str(advice.get("focus_summary", "")).strip(),
            "target_methods": [],
            "test_designs": [],
            "rationale": str(advice.get("rationale", "")).strip(),
            "test_intent": str(advice.get("test_intent", "")).strip(),
        }

        derived_target_methods = []

        raw_test_designs = advice.get("test_designs") or []
        if not raw_test_designs and advice.get("target_regions"):
            raw_test_designs = self._convert_legacy_regions_to_designs(advice)

        for design in raw_test_designs:
            if not isinstance(design, dict):
                continue
            design_name = str(design.get("design_name", "")).strip()
            method_context = design.get("method_context") or {}
            method_signature_hint = str(
                method_context.get("method_signature_hint", "")
            ).strip()
            entry_kind = str(method_context.get("entry_kind", "public-entry")).strip()
            entry_hint = str(method_context.get("entry_hint", "")).strip()
            execution_flow = str(design.get("execution_flow", "")).strip()
            input_focus = str(design.get("input_focus", "")).strip()
            observable_behavior = str(design.get("observable_behavior", "")).strip()
            observable_intent = str(design.get("observable_intent", "")).strip()
            rationale = str(design.get("rationale", "")).strip()

            normalized_targets = []
            for target in design.get("uncovered_targets") or []:
                if not isinstance(target, dict):
                    continue
                anchor_text = str(target.get("anchor_text", "")).strip()
                uncovered_kind = [
                    str(item).strip()
                    for item in (target.get("uncovered_kind") or [])
                    if str(item).strip() in {"line", "branch"}
                ]
                if not anchor_text:
                    continue
                normalized_targets.append(
                    {
                        "anchor_text": anchor_text,
                        "uncovered_kind": uncovered_kind,
                    }
                )

            if not observable_behavior:
                observable_behavior = observable_intent

            if not observable_intent:
                observable_intent = (
                    observable_behavior or design_name or input_focus or execution_flow
                )

            if not observable_behavior:
                observable_behavior = observable_intent

            if not normalized_targets:
                fallback_anchor = method_signature_hint or design_name
                if fallback_anchor:
                    normalized_targets.append(
                        {
                            "anchor_text": fallback_anchor,
                            "uncovered_kind": [],
                        }
                    )

            if (
                not design_name
                or not method_signature_hint
                or not entry_hint
                or not execution_flow
                or not input_focus
            ):
                continue
            normalized["test_designs"].append(
                {
                    "design_name": design_name,
                    "uncovered_targets": normalized_targets,
                    "method_context": {
                        "method_signature_hint": method_signature_hint,
                        "entry_kind": entry_kind or "public-entry",
                        "entry_hint": entry_hint,
                    },
                    "execution_flow": execution_flow,
                    "input_focus": input_focus,
                    "observable_behavior": observable_behavior,
                    "observable_intent": observable_intent,
                    "rationale": rationale,
                }
            )
            derived_target_methods.append(method_signature_hint)

        explicit_target_methods = [
            str(item).strip()
            for item in (advice.get("target_methods") or [])
            if str(item).strip()
        ]
        normalized["target_methods"] = explicit_target_methods or derived_target_methods

        if not normalized["focus_summary"] and normalized["test_designs"]:
            first_design = normalized["test_designs"][0]
            normalized["focus_summary"] = (
                first_design.get("design_name")
                or first_design.get("observable_intent")
                or first_design.get("input_focus", "")
            )

        if not normalized["test_intent"] and normalized["test_designs"]:
            first_design = normalized["test_designs"][0]
            normalized["test_intent"] = (
                first_design.get("execution_flow")
                or first_design.get("observable_intent")
                or first_design.get("input_focus", "")
            )

        if not normalized["focus_summary"] or not normalized["test_intent"]:
            self.last_advice_fallback_reason = (
                "normalized advice missing focus_summary or test_intent"
            )
            fallback = self._fallback_advice()
            for key, value in fallback.items():
                if not normalized.get(key):
                    normalized[key] = value

        if not normalized["test_designs"]:
            if not self.last_advice_fallback_reason:
                self.last_advice_fallback_reason = (
                    "normalized advice contains no valid test_designs"
                )

        return normalized

    def _convert_legacy_regions_to_designs(self, advice: dict):
        converted_designs = []
        for index, region in enumerate(advice.get("target_regions") or [], start=1):
            if not isinstance(region, dict):
                continue
            method_signature_hint = str(
                region.get("method_signature_hint", region.get("method", ""))
            ).strip()
            anchor_text = str(region.get("anchor_text", "")).strip()
            input_focus = str(region.get("input_focus", "")).strip()
            observable_behavior = str(region.get("observable_behavior", "")).strip()
            observable_intent = str(region.get("intent", "")).strip()
            uncovered_kind = [
                str(item).strip()
                for item in (region.get("uncovered_kind") or [])
                if str(item).strip() in {"line", "branch"}
            ]
            if not method_signature_hint or not anchor_text or not observable_intent:
                continue
            converted_designs.append(
                {
                    "design_name": f"legacy_design_{index}",
                    "uncovered_targets": [
                        {
                            "anchor_text": anchor_text,
                            "uncovered_kind": uncovered_kind,
                        }
                    ],
                    "method_context": {
                        "method_signature_hint": method_signature_hint,
                        "entry_kind": "public-entry",
                        "entry_hint": f"Reach this region through a public entrypoint that exercises `{method_signature_hint}`.",
                    },
                    "execution_flow": f"Drive execution from a public entrypoint into the code around `{anchor_text}`.",
                    "input_focus": input_focus
                    or "Construct inputs that trigger the uncovered behavior.",
                    "observable_behavior": observable_behavior or observable_intent,
                    "observable_intent": observable_intent,
                    "rationale": str(
                        region.get("rationale", advice.get("rationale", ""))
                    ).strip(),
                }
            )
        return converted_designs

    def _fallback_advice(self) -> dict:
        return {
            "focus_summary": "Focus on behaviors marked with PANTA uncovered annotations.",
            "target_methods": [],
            "test_designs": [],
            "rationale": "The current coverage report still contains uncovered lines or branches in the annotated source.",
            "test_intent": "Generate tests that exercise the uncovered behavior highlighted by the PANTA annotations.",
        }

    def _build_final_generation_prompt(
        self, annotated_source_code: str, advice: dict
    ) -> dict:
        advice_yaml = yaml.safe_dump(
            advice, sort_keys=False, allow_unicode=False, default_flow_style=False
        ).strip()
        variables = {
            "source_file_name": self.source_file_name,
            "test_file_name": self.test_file_name,
            "source_file": self.source_file,
            "annotated_source_code": annotated_source_code,
            "test_file": self.test_file,
            "test_dependencies": self.test_dependencies,
            "failed_tests_section": self.failed_test_runs_feedback,
            "additional_instructions_text": self.additional_instructions,
            "code_coverage_report": self.code_coverage_report,
            "advice_yaml": advice_yaml,
            "language": self.language,
        }
        environment = Environment(undefined=StrictUndefined)
        prompt = {
            "system": environment.from_string(
                get_settings().test_generation_llm_guided_prompt.system
            ).render(variables),
            "user": environment.from_string(
                get_settings().test_generation_llm_guided_prompt.user
            ).render(variables),
        }
        self._capture_final_prompt_snapshot(annotated_source_code, advice, prompt)
        return prompt

    def _build_selection_state(self, advice: dict, mode: str = "llm") -> dict:
        return {
            "mode": mode,
            "last_advice": advice,
            "advice_history": [advice],
        }

    def _capture_advice_snapshot(
        self,
        annotated_source_code: str,
        advice_prompt: dict,
        advice: dict,
        token_count: int,
        light_mode: bool = False,
    ):
        if not self.snapshotter:
            return
        self.snapshotter.capture(
            stage=(
                "prompt_builder_llm_light_advice"
                if light_mode
                else "prompt_builder_llm_advice"
            ),
            source_code_file=self.source_code_file_path,
            language=self.language,
            context={
                "prompt_context": {
                    "prompt": advice_prompt.get("user", ""),
                    "target_methods": advice.get("target_methods", []),
                    "advice_summary": advice.get("focus_summary", ""),
                    "annotated_line_count": len(annotated_source_code.splitlines()),
                    "token_count": token_count,
                },
                "generation_outcome": advice,
            },
        )

    def _capture_final_prompt_snapshot(
        self, annotated_source_code: str, advice: dict, prompt: dict
    ):
        if not self.snapshotter:
            return
        self.snapshotter.capture(
            stage="prompt_builder_llm_selection",
            source_code_file=self.source_code_file_path,
            language=self.language,
            context={
                "prompt_context": {
                    "prompt": prompt.get("user", ""),
                    "target_methods": advice.get("target_methods", []),
                    "advice_summary": advice.get("focus_summary", ""),
                    "annotated_line_count": len(annotated_source_code.splitlines()),
                },
                "generation_outcome": advice,
            },
        )
