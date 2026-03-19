import json
import logging
import re

from ..model_invocation.llm_invocation import LLMInvocation


class LLMCFGAnalyzer:
    def __init__(self, src_language: str, src_code: str, llm_model: str):
        self.src_language = src_language
        self.src_code = src_code
        self.llm_model = llm_model
        self.logger = logging.getLogger(__name__)

        self.preprocessed_src_code = src_code
        self.testable_methods_statistics = {}
        self.file_obj = {"imports": [], "class_objects": []}
        self.node_id_to_line_number = {}
        self.line_number_to_node_id = {}
        self._current_dummy_id = 1000

    def get_next_id(self) -> int:
        self._current_dummy_id += 1
        return self._current_dummy_id

    def add_line_number_with_prefix(self) -> str:
        numbered_lines = []
        for i, line in enumerate(self.src_code.split("\n")):
            numbered_lines.append(f"{i + 1}: {line}")
        return "\n".join(numbered_lines)

    def extract_deterministic_info(self):
        lines = self.src_code.split("\n")

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

            class_match = re.search(
                r"(?:public\s+)?(?:abstract\s+)?class\s+(\w+)", stripped_line
            )
            if class_match and not self.file_obj["class_objects"]:
                class_name = class_match.group(1)
                class_id = self.get_next_id()
                self.file_obj["class_objects"].append(
                    {
                        "class_declaration": {
                            "id": class_id,
                            "name": class_name,
                            "value": stripped_line,
                        },
                        "fields": [],
                        "constructors": [],
                        "methods_under_test": [],
                    }
                )
                self.node_id_to_line_number[class_id] = [line_num]
                self.line_number_to_node_id[line_num] = (line_num, class_id)

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
        numbered_code = self.add_line_number_with_prefix()
        prompt_user = f"""
You are an expert static code analyzer. Analyze the provided Java source code and extract control-flow information for each public or protected method.

Return strict JSON with this schema:
{{
  "methods": [
    {{
      "name": "methodName",
      "start_line": 10,
      "end_line": 20,
      "complexity": 3,
      "paths": [
        {{
          "path_conditions_str": "when x > 0 is true",
          "returns": "return x;",
          "line_hints": [12, 13]
        }}
      ]
    }}
  ]
}}

Rules:
- Include all public and protected methods in the outer class.
- `line_hints` should be the most relevant source lines for the path, and should stay within the method range.
- If complexity <= 1, return an empty `paths` list.
- Respond with JSON only.

```java
{numbered_code}
```
"""
        return {
            "system": "You are a precise static code analyzer.",
            "user": prompt_user,
        }

    def _normalize_line_hints(self, line_hints, method_lines):
        normalized = []
        method_line_set = set(method_lines)
        for line in line_hints or []:
            if (
                isinstance(line, int)
                and line in method_line_set
                and line not in normalized
            ):
                normalized.append(line)
        return normalized or method_lines

    def _build_path_entry(self, path_data, method_lines):
        dummy_path_node_id = self.get_next_id()
        path_lines = self._normalize_line_hints(
            path_data.get("line_hints"), method_lines
        )
        self.node_id_to_line_number[dummy_path_node_id] = path_lines
        for line in path_lines:
            self.line_number_to_node_id[line] = (line, dummy_path_node_id)

        path_conditions_str = (path_data.get("path_conditions_str") or "").strip()
        returns_str = (path_data.get("returns") or "").strip()
        precomputed = path_conditions_str
        if returns_str:
            precomputed = (
                f"{precomputed} returns: {returns_str}"
                if precomputed
                else f"returns: {returns_str}"
            )

        return {
            "path": [
                {
                    "id": dummy_path_node_id,
                    "statement": path_conditions_str or precomputed,
                    "conditional": returns_str or None,
                }
            ],
            "true_branches": [],
            "catch_exception_at": None,
            "method_calls_within_class": [],
            "method_calls_outside_class": [],
            "_llm_precomputed_path_str": precomputed,
        }

    def parse_llm_response(self, response_text: str):
        clean_text = response_text.strip()
        if clean_text.startswith("```json"):
            clean_text = clean_text[len("```json") :]
        if clean_text.startswith("```"):
            clean_text = clean_text[len("```") :]
        if clean_text.endswith("```"):
            clean_text = clean_text[:-3]
        data = json.loads(clean_text.strip())

        class_obj = self.file_obj["class_objects"][0]
        methods_under_test = []
        method_stats = {}

        for method in data.get("methods", []):
            method_id = self.get_next_id()
            start_line = int(method.get("start_line", 0) or 0)
            end_line = int(method.get("end_line", 0) or 0)
            if start_line <= 0 or end_line < start_line:
                continue

            method_lines = list(range(start_line, end_line + 1))
            self.node_id_to_line_number[method_id] = method_lines
            for line in method_lines:
                self.line_number_to_node_id[line] = (line, method_id)

            paths = []
            for path_data in method.get("paths", []):
                paths.append(self._build_path_entry(path_data, method_lines))

            complexity = int(method.get("complexity", 1) or 1)
            method_name = method.get("name", "unknown")
            methods_under_test.append(
                {
                    "method_declaration": {
                        "id": method_id,
                        "name": method_name,
                        "value": self.src_code.split("\n")[start_line - 1].strip()
                        if start_line <= len(self.src_code.split("\n"))
                        else "",
                        "complexity": complexity,
                        "nodes": [method_id],
                    },
                    "paths": paths,
                }
            )
            method_stats[method_name] = complexity

        class_obj["methods_under_test"] = methods_under_test
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
