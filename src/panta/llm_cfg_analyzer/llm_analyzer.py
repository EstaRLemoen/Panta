import json
import logging
import re

from ..model_invocation.llm_invocation import LLMInvocation
from .line_mapping import remap_line_to_original, remap_line_to_preprocessed
from .method_skeleton_extractor import MethodSkeletonExtractor


class LLMCFGAnalyzer:
    def __init__(
        self,
        src_language: str,
        src_code: str,
        llm_model: str,
        llm_line_mode: str = "preprocessed",
    ):
        self.src_language = src_language
        self.original_src_code = src_code
        self.src_code = src_code
        self.llm_model = llm_model
        self.llm_line_mode = llm_line_mode
        self.logger = logging.getLogger(__name__)

        self.preprocessed_src_code = src_code
        self.testable_methods_statistics = {}
        self.file_obj = {"imports": [], "class_objects": []}
        self.node_id_to_line_number = {}
        self.line_number_to_node_id = {}
        self._current_dummy_id = 1000
        self.method_skeleton = {"classes": []}
        self.method_key_to_id = {}
        self.preprocessed_to_original_line = {}
        self.original_to_preprocessed_line = {}

    def _method_complexity_hint(self, method_source: str) -> int:
        control_patterns = [
            r"\bif\b",
            r"\bwhile\b",
            r"\bfor\b",
            r"\bcatch\b",
            r"\bcase\b",
            r"\bswitch\b",
            r"\bthrow\b",
            r"\?",
            r"&&",
            r"\|\|",
        ]
        complexity = 1
        for pattern in control_patterns:
            complexity += len(re.findall(pattern, method_source))
        return max(complexity, 1)

    def _method_skeletons_for_prompt(self):
        methods = []
        for class_info in self.method_skeleton["classes"]:
            for method in class_info["methods"]:
                if self.llm_line_mode == "original":
                    start_line = method["start_line"]
                    end_line = method["end_line"]
                    declaration_text = method["declaration_text"]
                    method_source = method["method_source_original"]
                else:
                    start_line = method["start_line_preprocessed"]
                    end_line = method["end_line_preprocessed"]
                    declaration_text = method["declaration_text"]
                    method_source = method["method_source"]
                methods.append(
                    {
                        "qualified_method_key": method["qualified_method_key"],
                        "class_name": method["class_name"],
                        "method_name": method["method_name"],
                        "signature": method["signature"],
                        "start_line": start_line,
                        "end_line": end_line,
                        "declaration_text": declaration_text,
                        "complexity_hint": self._method_complexity_hint(
                            method["method_source"]
                        ),
                        "method_source": method_source,
                    }
                )
        return methods

    def get_next_id(self) -> int:
        self._current_dummy_id += 1
        return self._current_dummy_id

    def add_line_number_with_prefix(self, src_code=None) -> str:
        numbered_lines = []
        src_code = self.src_code if src_code is None else src_code
        for i, line in enumerate(src_code.split("\n")):
            numbered_lines.append(f"{i + 1}: {line}")
        return "\n".join(numbered_lines)

    def extract_deterministic_info(self):
        extractor = MethodSkeletonExtractor(self.src_language, self.src_code)
        self.preprocessed_src_code = extractor.src_code
        self.src_code = extractor.src_code
        self.method_skeleton = extractor.extract()
        self.preprocessed_to_original_line = extractor.preprocessed_to_original_line
        self.original_to_preprocessed_line = extractor.original_to_preprocessed_line
        lines = self.original_src_code.split("\n")

        for i, line in enumerate(lines):
            line_num = i + 1
            stripped_line = line.strip()

            if stripped_line.startswith("import ") and stripped_line.endswith(";"):
                import_id = self.get_next_id()
                self.file_obj["imports"].append(
                    {"id": import_id, "value": stripped_line}
                )
                self.node_id_to_line_number[import_id] = [line_num]
                self.line_number_to_node_id[line_num] = (line_num, import_id)

        self.file_obj["class_objects"] = []
        for class_info in self.method_skeleton["classes"]:
            class_id = self.get_next_id()
            self.file_obj["class_objects"].append(
                {
                    "class_declaration": {
                        "id": class_id,
                        "name": class_info["class_name"],
                        "value": class_info["declaration_text"],
                    },
                    "fields": [],
                    "constructors": [],
                    "methods_under_test": [],
                }
            )
            self.node_id_to_line_number[class_id] = [class_info["declaration_line"]]
            self.line_number_to_node_id[class_info["declaration_line"]] = (
                class_info["declaration_line"],
                class_id,
            )

        if not self.file_obj["class_objects"]:
            class_id = self.get_next_id()
            self.file_obj["class_objects"].append(
                {
                    "class_declaration": {
                        "id": class_id,
                        "name": "UnknownClass",
                        "value": "",
                    },
                    "fields": [],
                    "constructors": [],
                    "methods_under_test": [],
                }
            )

    def generate_llm_prompt(self) -> dict:
        source_for_prompt = (
            self.original_src_code
            if self.llm_line_mode == "original"
            else self.preprocessed_src_code
        )
        numbered_code = self.add_line_number_with_prefix(source_for_prompt)
        prompt_user = f"""
You are an expert static code analyzer. Analyze the provided Java source code and extract control-flow information for the listed methods.

Return strict JSON with this schema:
{{
  "methods": [
    {{
      "qualified_method_key": "CSVParser.parse(File, Charset, CSVFormat)",
      "complexity": 3,
      "path_count": 2,
      "paths": [
        {{
          "path_id": "p1",
          "kind": "branch",
          "summary": "when x > 0, return x",
          "conditions": [{{"text": "x > 0", "line": 12}}],
          "effects": [{{"text": "return x", "line": 13}}],
          "coverage_anchors": [12, 13],
          "line_hints": [12, 13]
        }}
      ]
    }}
  ]
}}

Rules:
- Analyze only the methods listed below. Do not invent methods, classes, or line ranges.
- Keep `complexity` reasonably aligned with `complexity_hint`, unless the source strongly indicates otherwise.
- `path_count` must match the number of items in `paths`.
- If `complexity <= 1`, return `path_count = 0` and `paths = []`.
- Each path should represent a major control-flow alternative, not every statement.
- Each path should include concise `conditions` and `effects`, and each item should include a concrete line number.
- `coverage_anchors` should identify the key lines that distinguish this path for coverage guidance.
- `line_hints` should be the most relevant source lines for the path, and should stay within the method range.
- Respond with JSON only.

Method skeletons:
{json.dumps(self._method_skeletons_for_prompt(), indent=2)}

```java
{numbered_code}
```
"""
        return {
            "system": "You are a precise static code analyzer.",
            "user": prompt_user,
        }

    def _normalize_line_hints(self, line_hints, method_lines, line_mode="original"):
        normalized = []
        method_line_set = set(method_lines)
        for line in line_hints or []:
            if line_mode == "preprocessed":
                line = remap_line_to_original(line, self.preprocessed_to_original_line)
            if (
                isinstance(line, int)
                and line in method_line_set
                and line not in normalized
            ):
                normalized.append(line)
        return normalized or method_lines

    def _build_path_entry(self, path_data, method_lines):
        dummy_path_node_id = self.get_next_id()
        coverage_anchors = self._normalize_line_hints(
            path_data.get("coverage_anchors"),
            method_lines,
            path_data.get("_line_mode", "original"),
        )
        path_lines = self._normalize_line_hints(
            path_data.get("line_hints"),
            method_lines,
            path_data.get("_line_mode", "original"),
        )
        for line in coverage_anchors:
            if line not in path_lines:
                path_lines.append(line)
        path_lines = sorted(path_lines)
        self.node_id_to_line_number[dummy_path_node_id] = path_lines
        for line in path_lines:
            self.line_number_to_node_id[line] = (line, dummy_path_node_id)

        summary_str = (path_data.get("summary") or "").strip()
        conditions = path_data.get("conditions", [])
        effects = path_data.get("effects", [])
        kind = (path_data.get("kind") or "").strip()
        condition_texts = [item["text"] for item in conditions if item.get("text")]
        effect_texts = [item["text"] for item in effects if item.get("text")]
        precomputed_parts = []
        if kind:
            precomputed_parts.append(f"[{kind}]")
        if summary_str:
            precomputed_parts.append(summary_str)
        if condition_texts:
            precomputed_parts.append("conditions: " + "; ".join(condition_texts))
        if effect_texts:
            precomputed_parts.append("effects: " + "; ".join(effect_texts))
        precomputed = " | ".join(precomputed_parts)
        statement = summary_str or (
            "; ".join(condition_texts) if condition_texts else precomputed
        )
        conditional = "; ".join(effect_texts) if effect_texts else None

        return {
            "path": [
                {
                    "id": dummy_path_node_id,
                    "statement": statement,
                    "conditional": conditional,
                }
            ],
            "true_branches": [],
            "catch_exception_at": None,
            "method_calls_within_class": [],
            "method_calls_outside_class": [],
            "_llm_precomputed_path_str": precomputed,
            "_llm_path_kind": kind,
            "_llm_path_id": path_data.get("path_id", ""),
            "_llm_conditions": conditions,
            "_llm_effects": effects,
            "_llm_coverage_anchors": coverage_anchors,
        }

    def _normalize_anchored_items(
        self, items, method_start, method_end, line_mode="original"
    ):
        normalized = []
        for item in items or []:
            if not isinstance(item, dict):
                continue
            text = str(item.get("text", "")).strip()
            line = item.get("line")
            if not text:
                continue
            try:
                line = int(line)
            except (TypeError, ValueError):
                continue
            if line_mode == "preprocessed":
                line = remap_line_to_original(line, self.preprocessed_to_original_line)
            if method_start <= line <= method_end:
                normalized.append({"text": text, "line": line})
        return normalized

    def _validate_method_analysis(self, skeleton, method_data):
        complexity_hint = self._method_complexity_hint(skeleton["method_source"])
        raw_complexity = method_data.get("complexity", complexity_hint)
        try:
            complexity = int(raw_complexity or complexity_hint)
        except (TypeError, ValueError):
            complexity = complexity_hint
        complexity = max(complexity, 1)

        paths = (
            method_data.get("paths", [])
            if isinstance(method_data.get("paths", []), list)
            else []
        )
        declared_path_count = method_data.get("path_count", len(paths))
        try:
            declared_path_count = int(declared_path_count)
        except (TypeError, ValueError):
            declared_path_count = len(paths)

        if complexity <= 1:
            return (
                complexity_hint if complexity_hint > 1 else 1,
                [] if complexity_hint <= 1 else paths[:1],
            )

        valid_paths = []
        seen_signatures = set()
        method_start = skeleton["start_line"]
        method_end = skeleton["end_line"]
        line_mode = method_data.get("_line_mode", self.llm_line_mode)
        for index, path in enumerate(paths):
            conditions = self._normalize_anchored_items(
                path.get("conditions", []), method_start, method_end, line_mode
            )
            effects = self._normalize_anchored_items(
                path.get("effects", []), method_start, method_end, line_mode
            )
            coverage_anchors = [item["line"] for item in conditions + effects]
            extra_anchors = []
            for line in path.get("coverage_anchors", []):
                if not isinstance(line, int):
                    continue
                mapped_line = (
                    remap_line_to_original(line, self.preprocessed_to_original_line)
                    if line_mode == "preprocessed"
                    else line
                )
                if method_start <= mapped_line <= method_end:
                    extra_anchors.append(mapped_line)
            for line in extra_anchors:
                if line not in coverage_anchors:
                    coverage_anchors.append(line)
            line_hints = [
                remap_line_to_original(line, self.preprocessed_to_original_line)
                if line_mode == "preprocessed"
                else line
                for line in path.get("line_hints", [])
                if isinstance(line, int)
            ]
            line_hints = [
                line for line in line_hints if method_start <= line <= method_end
            ]
            for line in coverage_anchors:
                if line not in line_hints:
                    line_hints.append(line)
            line_hints = sorted(line_hints)
            summary = (path.get("summary") or "").strip()
            condition_texts = [item["text"] for item in conditions]
            effect_texts = [item["text"] for item in effects]
            signature = (
                summary,
                tuple((item["text"], item["line"]) for item in conditions),
                tuple((item["text"], item["line"]) for item in effects),
                tuple(coverage_anchors),
            )
            if not coverage_anchors or signature in seen_signatures:
                continue
            seen_signatures.add(signature)
            valid_paths.append(
                {
                    "path_id": path.get("path_id") or f"p{index + 1}",
                    "kind": path.get("kind")
                    or self._infer_path_kind(summary, condition_texts, effect_texts),
                    "summary": summary,
                    "conditions": conditions,
                    "effects": effects,
                    "coverage_anchors": coverage_anchors,
                    "line_hints": line_hints,
                    "_line_mode": line_mode,
                }
            )

        if declared_path_count and len(valid_paths) > declared_path_count:
            valid_paths = valid_paths[:declared_path_count]

        if complexity_hint > 1 and complexity == 1:
            complexity = complexity_hint
        if complexity > 1 and not valid_paths:
            complexity = complexity_hint if complexity_hint > 1 else complexity

        return complexity, valid_paths

    def _infer_path_kind(self, summary, conditions, effects):
        text = " ".join([summary] + conditions + effects).lower()
        if "throw" in text or "exception" in text:
            return "exception"
        if "while" in text or "for" in text or "loop" in text:
            return "loop"
        if "return" in text and not conditions:
            return "fallthrough"
        return "branch"

    def parse_llm_response(self, response_text: str):
        clean_text = response_text.strip()
        if clean_text.startswith("```json"):
            clean_text = clean_text[len("```json") :]
        if clean_text.startswith("```"):
            clean_text = clean_text[len("```") :]
        if clean_text.endswith("```"):
            clean_text = clean_text[:-3]
        data = json.loads(clean_text.strip())

        methods_by_class = {
            class_obj["class_declaration"]["name"]: []
            for class_obj in self.file_obj["class_objects"]
        }
        method_stats = {}
        skeleton_by_key = {}
        for class_info in self.method_skeleton["classes"]:
            for method in class_info["methods"]:
                skeleton_by_key[method["qualified_method_key"]] = method

        llm_methods = {
            method.get("qualified_method_key", ""): method
            for method in data.get("methods", [])
            if method.get("qualified_method_key")
        }

        for method_key, skeleton in skeleton_by_key.items():
            method_data = llm_methods.get(method_key, {})
            method_id = self.get_next_id()
            self.method_key_to_id[method_key] = method_id
            start_line = skeleton["start_line"]
            end_line = skeleton["end_line"]

            method_lines = list(range(start_line, end_line + 1))
            self.node_id_to_line_number[method_id] = method_lines
            for line in method_lines:
                self.line_number_to_node_id[line] = (line, method_id)

            complexity, validated_paths = self._validate_method_analysis(
                skeleton, method_data
            )

            paths = []
            for path_data in validated_paths:
                paths.append(self._build_path_entry(path_data, method_lines))

            method_name = skeleton["method_name"]
            method_obj = {
                "method_declaration": {
                    "id": method_id,
                    "name": method_name,
                    "value": skeleton["declaration_text"],
                    "complexity": complexity,
                    "nodes": [method_id],
                },
                "paths": paths,
            }
            methods_by_class.setdefault(skeleton["class_name"], []).append(method_obj)
            method_stats[method_key] = complexity

        for class_obj in self.file_obj["class_objects"]:
            class_name = class_obj["class_declaration"]["name"]
            class_obj["methods_under_test"] = methods_by_class.get(class_name, [])
        self.testable_methods_statistics = method_stats

    def analyze_with_llm(self):
        llm = LLMInvocation(model=self.llm_model)
        prompt = self.generate_llm_prompt()
        response_text, _, _ = llm.call_model(
            prompt=prompt, max_tokens=2048, temperature=0.1
        )
        self.parse_llm_response(response_text)

    def run(self, mock_response_text=None):
        self.extract_deterministic_info()
        if mock_response_text is not None:
            self.parse_llm_response(mock_response_text)
        else:
            self.analyze_with_llm()
        return self
