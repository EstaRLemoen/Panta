import json
import logging
import re

from ..model_invocation.llm_invocation import LLMInvocation
from .line_mapping import remap_line_to_original
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

    def generate_llm_prompt(self, method_skeletons=None) -> dict:
        source_for_prompt = (
            self.original_src_code
            if self.llm_line_mode == "original"
            else self.preprocessed_src_code
        )
        numbered_code = self.add_line_number_with_prefix(source_for_prompt)
        method_skeletons = (
            self._method_skeletons_for_prompt()
            if method_skeletons is None
            else method_skeletons
        )
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
          "summary": "when x > 0, return x",
          "direct_path_text": "11: int y = x + 1 | 12: if (x > 0) [true] -> 13: return x | 13: return x",
          "segments": [
            {{
              "segment_type": "sequential",
              "statements": [
                {{"text": "int y = x + 1", "line": 11}}
              ]
            }},
            {{
              "segment_type": "transition",
              "source_text": "if (x > 0)",
              "source_line": 12,
              "condition_value": true,
              "branch_label": null,
              "target_text": "return x",
              "target_line": 13
            }},
            {{
              "segment_type": "sequential",
              "statements": [
                {{"text": "return x", "line": 13}}
              ]
            }}
          ]
        }}
      ]
    }}
  ]
}}

Rules:
- Analyze only the methods listed below. Do not invent methods, classes, or line ranges.
- Return exactly one `methods` entry for every listed method skeleton. Do not omit listed methods, even if a method is complex or you believe it has no useful paths.
- Keep `complexity` reasonably aligned with `complexity_hint`, unless the source strongly indicates otherwise.
- `path_count` must match the number of items in `paths`.
- If `complexity <= 1`, return `path_count = 0` and `paths = []`.
- Each path should represent a major control-flow alternative, not every statement in the method.
- `segments` is the primary path structure and must describe the path in execution order.
- `direct_path_text` is optional, but when present it should be a single long-form path string in execution order that resembles the kind of path text ultimately consumed by PromptBuilder.
- `kind` is optional and only a coarse summary label if you include it.
- A path means one actual execution sequence of the method in time order, not a merged summary of several possible outcomes.
- If two executions would end in different major outcomes, they must be separate paths. For example, `return result`, `throw IOException(...)`, and `return null` belong to different paths.
- If a `switch` can go to different case labels, each chosen case label should normally become a different path unless later control flow truly rejoins and the same remaining execution sequence is shared.
- If a non-loop `if` can go to different outcomes, its `true` and `false` branches should normally become different paths.
- A single path must choose one concrete outcome at each decision occurrence.
- For one `switch` statement occurrence, do not include multiple case labels in the same path. Pick the single case label actually taken by that occurrence.
- A loop may revisit the same boolean condition multiple times across one path. In that case, repeated boolean transitions are allowed if they follow execution order, for example `true` on loop iterations and `false` on loop exit.
- The only normal reason to show both outcomes for the same source line in one path is a loop revisit across time, such as `while (...) [true]` on one iteration and `while (...) [false]` on exit.
- Do not compress a long method into one "mega path" that mixes TOKEN, EORECORD, EOF, COMMENT, INVALID, throw, and return outcomes together.
- Every time execution reaches a decision point and takes a branch, add a separate `transition` segment for that occurrence.
- Do not output a separate top-level `transitions` field unless it exactly mirrors the transition segments already present in `segments`.
- Use `segment_type = "sequential"` for straight-line execution between jumps, and list the executed statements as completely as possible.
- Use `segment_type = "transition"` for a control-flow jump, with `source_text`, `source_line`, `condition_value`, `branch_label`, `target_text`, and `target_line`.
- For `if`/`while`/`for` style boolean branches, use `condition_value=true/false` and keep `branch_label=null`.
- For `switch`/`case` style branches, use `condition_value=null` and put the chosen case label in `branch_label` such as `TOKEN`, `EORECORD`, `EOF`, `DEFAULT`.
- Never use `true`/`false` to describe a `switch` branch choice.
- When execution continues downward until the next jump point, include the ordinary executed statements in a `sequential` segment.
- For a loop back-edge, set `target_text` and `target_line` to the first real statement executed in the next iteration, not to structural lines like `do {{`, `while (...) {{`, or `for (...) {{`.
- For a `switch` branch, set `target_text` and `target_line` to the first real statement executed in the selected case body, not to `case LABEL:`.
- Do not put the branch-evaluation statement itself into `sequential` if that same line is already represented as a `transition` source.
- In `sequential`, prefer ordinary executed statements such as assignments, calls, returns, throws, and object creation; avoid using bare `if`, `while`, `for`, `switch`, `case`, `catch`, `else`, or `do` lines as filler.
- If a control-flow line appears in `sequential`, only keep it when it is also an actually executed body statement rather than a jump-point marker.
- If a path enters a `switch` arm, keep the immediately preceding real executed statements before the `switch`, such as token reset/read calls, instead of jumping directly from setup lines to the case body.
- For statements and transitions, prefer full statement text plus line number. Do not output line numbers alone.
- Do not invent lines or statements. Keep every line within the method range.
- You may omit blank lines and brace-only lines, but do not omit real executed statements.
- In `direct_path_text`, write the path in execution order with line-numbered items.
- In `direct_path_text`, format boolean branch outcomes inline, for example `140: while ((rec = this.nextRecord()) != null) [true] -> 141: records.add(rec);`.
- In `direct_path_text`, format switch choices with explicit case labels inline, for example `320: switch (token) [case TOKEN] -> 321: this.reusableToken.reset();`.
- In `direct_path_text`, if a loop condition is revisited, include each revisit occurrence in order rather than collapsing them.
- In `direct_path_text`, for a normal `if` decision occurrence, do not include both `[true]` and `[false]` in the same path.
- In `direct_path_text`, for a single `switch` decision occurrence, do not include multiple case labels in the same path.
- In `direct_path_text`, repeated `true`/`false` outcomes for the same source line are only acceptable for loop conditions such as `while`/`for`/`do` revisits.
- In `direct_path_text`, keep it to one path only. Do not include debug labels, JSON fragments, headings, or metadata dumps like `segments:` or `transitions:`.
- Bad loop-back example: `while (token == TOKEN) -> do {{`
- Good loop-back example: `while (token == TOKEN) -> this.reusableToken.reset();`
- Bad switch-target example: `switch (token) -> case EORECORD:`
- Good switch-target example: `switch (token) -> this.addRecordValue(true);`
- Good `direct_path_text` example: `138: CSVRecord rec; | 139: final List<CSVRecord> records = new ArrayList<>(); | 140: while ((rec = this.nextRecord()) != null) [true] -> 141: records.add(rec); | 141: records.add(rec); | 140: while ((rec = this.nextRecord()) != null) [false] -> 143: return records; | 143: return records;`
- Good `direct_path_text` switch example: `318: this.reusableToken.reset(); | 319: token = this.lexer.nextToken(); | 320: switch (token) [case EORECORD] -> 333: this.addRecordValue(true); | 333: this.addRecordValue(true);`
- Bad conflicting `if` example: `206: if (CSVParser.this.isClosed()) [false] -> 207: return false; | 206: if (CSVParser.this.isClosed()) [true] -> 209: if (this.current == null)`
- Bad conflicting `switch` example: `320: switch (token) [case TOKEN] -> 321: ... | 320: switch (token) [case EOF] -> 333: ...`
- Bad mega-path example: `switch(token) [case TOKEN] -> addRecordValue(false) | switch(token) [case EORECORD] -> addRecordValue(true) | switch(token) [case INVALID] -> throw IOException(...) | return result;`
- Good split-path example A: `... | switch(token) [case TOKEN] -> addRecordValue(false) | ... | return result;`
- Good split-path example B: `... | switch(token) [case INVALID] -> throw IOException(...);`
- Strong split example for a loop + switch method:
  - Path A (TOKEN then EORECORD): `240: result = null; | 245: do {{ | 246: reset(); | 247: nextToken(); | 248: switch(token) [case TOKEN] -> 250: addRecordValue(false); | 250: addRecordValue(false); | 245: do {{ | 246: reset(); | 247: nextToken(); | 248: switch(token) [case EORECORD] -> 253: addRecordValue(true); | 253: addRecordValue(true); | 282: return result;`
  - Path B (INVALID): `240: result = null; | 245: do {{ | 246: reset(); | 247: nextToken(); | 248: switch(token) [case INVALID] -> 261: throw IOException(...);`
  - Path C (EOF not ready): `240: result = null; | 245: do {{ | 246: reset(); | 247: nextToken(); | 248: switch(token) [case EOF] -> 256: if (isReady) [false] -> 282: return result;`
  - These are different paths. Do not merge Path A, Path B, and Path C into one output path.
- If a method loops until one of several terminal outcomes happens, create one path per terminal outcome, with only the necessary preceding loop iterations included.
- Respond with JSON only.

Method skeletons:
{json.dumps(method_skeletons, indent=2)}

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

    def _normalize_text(self, text):
        return re.sub(r"\s+", " ", str(text or "")).strip()

    def _is_control_flow_marker(self, text):
        normalized = self._normalize_text(text).rstrip("{").rstrip()
        return bool(
            re.match(
                r"^(if|else\s+if|else|while|for|switch|case|default|catch|finally|do)\b",
                normalized,
            )
        )

    def _is_switch_transition(self, text):
        normalized = self._normalize_text(text).rstrip("{").rstrip()
        return normalized.startswith("switch ") or normalized.startswith("switch(")

    def _is_boolean_transition(self, text):
        normalized = self._normalize_text(text).rstrip("{").rstrip()
        return bool(re.match(r"^(if|else\s+if|while|for|do)\b", normalized))

    def _ground_text_in_method(self, text, method_source):
        normalized_text = self._normalize_text(text)
        if not normalized_text:
            return False
        return normalized_text in self._normalize_text(method_source)

    def _coerce_line_in_method_range(
        self, line, method_start, method_end, line_mode="original"
    ):
        try:
            line = int(line)
        except (TypeError, ValueError):
            return None
        if line_mode == "preprocessed":
            line = remap_line_to_original(line, self.preprocessed_to_original_line)
        if method_start <= line <= method_end:
            return line
        return None

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

    def _normalize_statement_items(
        self, items, method_start, method_end, method_source, line_mode="original"
    ):
        normalized = []
        seen = set()
        for item in items or []:
            if not isinstance(item, dict):
                continue
            text = str(item.get("text", "")).strip()
            line = self._coerce_line_in_method_range(
                item.get("line"), method_start, method_end, line_mode
            )
            if not text or line is None:
                continue
            if not self._ground_text_in_method(text, method_source):
                continue
            signature = (text, line)
            if signature in seen:
                continue
            seen.add(signature)
            normalized.append({"text": text, "line": line})
        return sorted(normalized, key=lambda item: item["line"])

    def _normalize_transition_items(
        self, items, method_start, method_end, method_source, line_mode="original"
    ):
        normalized = []
        seen = set()
        for item in items or []:
            if not isinstance(item, dict):
                continue
            source_text = str(item.get("source_text", "")).strip()
            target_text = str(item.get("target_text", "")).strip()
            source_line = self._coerce_line_in_method_range(
                item.get("source_line"), method_start, method_end, line_mode
            )
            target_line = self._coerce_line_in_method_range(
                item.get("target_line"), method_start, method_end, line_mode
            )
            condition_value = item.get("condition_value")
            branch_label = item.get("branch_label")
            if condition_value not in (True, False, None):
                continue
            if branch_label is not None and not isinstance(branch_label, str):
                continue
            if not source_text or not target_text:
                continue
            if source_line is None or target_line is None:
                continue
            if not self._ground_text_in_method(source_text, method_source):
                continue
            if not self._ground_text_in_method(target_text, method_source):
                continue
            if self._is_switch_transition(source_text):
                if condition_value is not None:
                    condition_value = None
                if isinstance(branch_label, str):
                    branch_label = branch_label.strip() or None
                if branch_label is None:
                    continue
            elif self._is_boolean_transition(source_text):
                if branch_label is not None:
                    branch_label = None
                if condition_value not in (True, False):
                    continue
            signature = (
                source_text,
                source_line,
                condition_value,
                branch_label,
                target_text,
                target_line,
            )
            if signature in seen:
                continue
            seen.add(signature)
            normalized.append(
                {
                    "source_text": source_text,
                    "source_line": source_line,
                    "condition_value": condition_value,
                    "branch_label": branch_label,
                    "target_text": target_text,
                    "target_line": target_line,
                }
            )
        return normalized

    def _normalize_segments(
        self, segments, method_start, method_end, method_source, line_mode="original"
    ):
        raw_segments = []
        last_line = None
        for segment in segments or []:
            if not isinstance(segment, dict):
                continue
            segment_type = str(segment.get("segment_type", "")).strip()
            if segment_type == "sequential":
                statements = self._normalize_statement_items(
                    segment.get("statements", []),
                    method_start,
                    method_end,
                    method_source,
                    line_mode,
                )
                if not statements:
                    continue
                if last_line is not None:
                    statements = [
                        item for item in statements if item["line"] >= last_line
                    ]
                if not statements:
                    continue
                raw_segments.append(
                    {"segment_type": "sequential", "statements": statements}
                )
                last_line = statements[-1]["line"]
            elif segment_type == "transition":
                transitions = self._normalize_transition_items(
                    [segment], method_start, method_end, method_source, line_mode
                )
                if not transitions:
                    continue
                transition = transitions[0]
                if last_line is not None and transition["source_line"] < last_line:
                    continue
                raw_segments.append({"segment_type": "transition", **transition})
                last_line = max(transition["source_line"], transition["target_line"])
        return self._prune_segments(raw_segments)

    def _prune_segments(self, segments):
        if not segments:
            return []
        transition_source_lines = {
            segment["source_line"]
            for segment in segments
            if segment.get("segment_type") == "transition"
            and isinstance(segment.get("source_line"), int)
        }
        pruned = []
        previous_key = None
        for segment in segments:
            if segment.get("segment_type") == "sequential":
                statements = []
                for item in segment.get("statements", []):
                    line = item.get("line")
                    text = item.get("text", "")
                    if line in transition_source_lines and self._is_control_flow_marker(
                        text
                    ):
                        continue
                    if self._normalize_text(text) in {"{", "}"}:
                        continue
                    statements.append(item)
                if not statements:
                    continue
                key = (
                    "sequential",
                    tuple(
                        (item["line"], self._normalize_text(item["text"]))
                        for item in statements
                    ),
                )
                if key == previous_key:
                    continue
                pruned.append({"segment_type": "sequential", "statements": statements})
                previous_key = key
                continue
            key = (
                "transition",
                segment.get("source_line"),
                segment.get("target_line"),
                self._normalize_text(segment.get("source_text", "")),
                self._normalize_text(segment.get("target_text", "")),
                segment.get("condition_value"),
                segment.get("branch_label"),
            )
            if key == previous_key:
                continue
            pruned.append(segment)
            previous_key = key
        return pruned

    def _collect_segment_lines(self, segments):
        lines = []
        for segment in segments or []:
            if segment.get("segment_type") == "sequential":
                for item in segment.get("statements", []):
                    line = item.get("line")
                    if isinstance(line, int) and line not in lines:
                        lines.append(line)
            elif segment.get("segment_type") == "transition":
                for key in ("source_line", "target_line"):
                    line = segment.get(key)
                    if isinstance(line, int) and line not in lines:
                        lines.append(line)
        return sorted(lines)

    def _derive_transitions_from_segments(self, segments):
        return [
            {
                "source_text": segment["source_text"],
                "source_line": segment["source_line"],
                "condition_value": segment["condition_value"],
                "branch_label": segment["branch_label"],
                "target_text": segment["target_text"],
                "target_line": segment["target_line"],
            }
            for segment in segments or []
            if segment.get("segment_type") == "transition"
        ]

    def _derive_coverage_anchors(self, segments, conditions, effects, extra_anchors):
        anchors = [item["line"] for item in conditions + effects]
        for transition in self._derive_transitions_from_segments(segments):
            for key in ("source_line", "target_line"):
                line = transition[key]
                if line not in anchors:
                    anchors.append(line)
        for line in extra_anchors:
            if line not in anchors:
                anchors.append(line)
        return sorted(anchors)

    def _derive_line_hints(self, segments, coverage_anchors, extra_line_hints):
        line_hints = []
        for line in extra_line_hints:
            if line not in line_hints:
                line_hints.append(line)
        for line in self._collect_segment_lines(segments):
            if line not in line_hints:
                line_hints.append(line)
        for line in coverage_anchors:
            if line not in line_hints:
                line_hints.append(line)
        return sorted(line_hints)

    def _derive_membership_lines_from_segments(self, segments, coverage_anchors):
        membership_lines = []
        for line in self._collect_segment_lines(segments):
            if line not in membership_lines:
                membership_lines.append(line)
        for line in coverage_anchors:
            if line not in membership_lines:
                membership_lines.append(line)
        return sorted(membership_lines)

    def _build_llm_path_prompt_text(
        self, summary_str, conditions, effects, transitions, segments
    ):
        parts = []
        if summary_str:
            parts.append(summary_str)

        if transitions:
            transition_parts = []
            for transition in transitions:
                branch = transition["branch_label"]
                if branch is None:
                    branch = transition["condition_value"]
                branch_text = f" ({branch})" if branch is not None else ""
                transition_parts.append(
                    f"{transition['source_line']}: {transition['source_text']} -> {transition['target_line']}: {transition['target_text']}{branch_text}"
                )
            if transition_parts:
                parts.append("transitions: " + " ; ".join(transition_parts))

        sequential_lines = []
        for segment in segments:
            if segment.get("segment_type") != "sequential":
                continue
            for statement in segment.get("statements", []):
                sequential_lines.append(f"{statement['line']}: {statement['text']}")
        if sequential_lines:
            parts.append("path: " + " | ".join(sequential_lines))

        if not parts:
            effect_texts = [item["text"] for item in effects if item.get("text")]
            condition_texts = [item["text"] for item in conditions if item.get("text")]
            fallback = effect_texts or condition_texts
            if fallback:
                return "; ".join(fallback)
        return " | ".join(parts)

    def _remap_direct_path_text_lines(self, direct_path_text, line_mode):
        if not isinstance(direct_path_text, str):
            return ""

        def replace_line(match):
            line = int(match.group(1))
            if line_mode == "preprocessed":
                line = remap_line_to_original(line, self.preprocessed_to_original_line)
            return f"{line}:"

        return re.sub(r"\b(\d+)\s*:", replace_line, direct_path_text)

    def _sanitize_direct_path_text(
        self, direct_path_text, method_start, method_end, line_mode="original"
    ):
        if not isinstance(direct_path_text, str):
            return ""
        remapped_text = self._remap_direct_path_text_lines(direct_path_text, line_mode)
        sanitized = " ".join(remapped_text.strip().split())
        if not sanitized:
            return ""
        lowered = sanitized.lower()
        if any(
            token in lowered
            for token in ('"segment_type"', "segments:", "transitions:", "{", "}")
        ):
            return ""
        line_numbers = [int(value) for value in re.findall(r"\b(\d+)\s*:", sanitized)]
        if not line_numbers:
            return ""
        if any(line < method_start or line > method_end for line in line_numbers):
            return ""
        return sanitized

    def _get_direct_path_text_debug_info(
        self, direct_path_text, method_start, method_end, line_mode="original"
    ):
        raw_text = direct_path_text if isinstance(direct_path_text, str) else ""
        remapped_raw_text = self._remap_direct_path_text_lines(raw_text, line_mode)
        sanitized_text = self._sanitize_direct_path_text(
            direct_path_text, method_start, method_end, line_mode
        )
        if not raw_text.strip():
            reject_reason = "empty"
        elif sanitized_text:
            reject_reason = ""
        else:
            lowered = " ".join(remapped_raw_text.strip().split()).lower()
            if any(
                token in lowered
                for token in ('"segment_type"', "segments:", "transitions:", "{", "}")
            ):
                reject_reason = "contains_debug_or_json_tokens"
            else:
                line_numbers = [
                    int(value)
                    for value in re.findall(r"\b(\d+)\s*:", remapped_raw_text)
                ]
                if not line_numbers:
                    reject_reason = "missing_line_numbers"
                elif any(
                    line < method_start or line > method_end for line in line_numbers
                ):
                    reject_reason = "line_out_of_method_range"
                else:
                    reject_reason = "format_not_accepted"
        consistency_issue = self._detect_direct_path_text_consistency_issue(
            remapped_raw_text
        )
        return (
            remapped_raw_text.strip(),
            sanitized_text,
            reject_reason,
            consistency_issue,
        )

    def _detect_direct_path_text_consistency_issue(self, direct_path_text):
        if not isinstance(direct_path_text, str) or not direct_path_text.strip():
            return ""
        boolean_outcomes = {}
        switch_outcomes = {}
        clauses = [part.strip() for part in direct_path_text.split("|") if part.strip()]
        for clause in clauses:
            match = re.match(r"^(\d+):\s*(.*?)\s*\[(.*?)\]\s*->", clause)
            if not match:
                continue
            source_line = int(match.group(1))
            source_text = match.group(2).strip()
            outcome = match.group(3).strip()
            normalized = self._normalize_text(source_text).rstrip("{").rstrip()
            if normalized.startswith("if ") or normalized.startswith("if("):
                if outcome.lower() in ("true", "false"):
                    key = (source_line, normalized)
                    seen = boolean_outcomes.setdefault(key, set())
                    seen.add(outcome.lower())
                    if len(seen) > 1:
                        return f"conflicting_if_outcomes@{source_line}"
            if normalized.startswith("switch ") or normalized.startswith("switch("):
                key = (source_line, normalized)
                seen = switch_outcomes.setdefault(key, set())
                seen.add(outcome)
                if len(seen) > 1:
                    return f"conflicting_switch_cases@{source_line}"
        return ""

    def _build_precomputed_path_str(
        self, kind, summary_str, conditions, effects, transitions, segments
    ):
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
        if transitions:
            transition_parts = []
            for transition in transitions:
                branch = transition["branch_label"]
                if branch is None:
                    branch = transition["condition_value"]
                transition_parts.append(
                    f"{transition['source_line']}: {transition['source_text']} -> {transition['target_line']}: {transition['target_text']} ({branch})"
                )
            precomputed_parts.append("transitions: " + " ; ".join(transition_parts))
        sequential_lines = []
        for segment in segments:
            if segment.get("segment_type") != "sequential":
                continue
            for statement in segment.get("statements", []):
                sequential_lines.append(f"{statement['line']}: {statement['text']}")
        if sequential_lines:
            precomputed_parts.append("path: " + " | ".join(sequential_lines))
        return " | ".join(precomputed_parts)

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
        transitions = path_data.get("transitions", [])
        segments = path_data.get("segments", [])
        membership_lines = path_data.get("membership_lines", [])
        kind = (path_data.get("kind") or "").strip()
        direct_path_text = (path_data.get("direct_path_text") or "").strip()
        direct_path_text_raw = (path_data.get("direct_path_text_raw") or "").strip()
        direct_path_text_sanitized = (
            path_data.get("direct_path_text_sanitized") or ""
        ).strip()
        direct_path_text_reject_reason = path_data.get(
            "direct_path_text_reject_reason", ""
        )
        direct_path_text_consistency_issue = path_data.get(
            "direct_path_text_consistency_issue", ""
        )
        condition_texts = [item["text"] for item in conditions if item.get("text")]
        effect_texts = [item["text"] for item in effects if item.get("text")]
        precomputed = self._build_precomputed_path_str(
            kind,
            summary_str,
            conditions,
            effects,
            transitions,
            segments,
        )
        prompt_text = self._build_llm_path_prompt_text(
            summary_str,
            conditions,
            effects,
            transitions,
            segments,
        )
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
            "_llm_membership_lines": membership_lines,
            "_llm_path_prompt_text": prompt_text,
            "_llm_direct_path_text": direct_path_text,
            "_llm_direct_path_text_raw": direct_path_text_raw,
            "_llm_direct_path_text_sanitized": direct_path_text_sanitized,
            "_llm_direct_path_text_reject_reason": direct_path_text_reject_reason,
            "_llm_direct_path_text_consistency_issue": direct_path_text_consistency_issue,
            "_llm_transitions": transitions,
            "_llm_segments": segments,
        }

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
        method_source = skeleton["method_source_original"]
        for index, path in enumerate(paths):
            segments = self._normalize_segments(
                path.get("segments", []),
                method_start,
                method_end,
                method_source,
                line_mode,
            )
            if not segments:
                continue

            conditions = self._normalize_anchored_items(
                path.get("conditions", []), method_start, method_end, line_mode
            )
            effects = self._normalize_anchored_items(
                path.get("effects", []), method_start, method_end, line_mode
            )
            transitions = self._derive_transitions_from_segments(segments)

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
            coverage_anchors = self._derive_coverage_anchors(
                segments, conditions, effects, extra_anchors
            )

            extra_line_hints = [
                remap_line_to_original(line, self.preprocessed_to_original_line)
                if line_mode == "preprocessed"
                else line
                for line in path.get("line_hints", [])
                if isinstance(line, int)
            ]
            extra_line_hints = [
                line for line in extra_line_hints if method_start <= line <= method_end
            ]
            line_hints = self._derive_line_hints(
                segments, coverage_anchors, extra_line_hints
            )
            membership_lines = self._derive_membership_lines_from_segments(
                segments, coverage_anchors
            )
            summary = (path.get("summary") or "").strip()
            prompt_text = self._build_llm_path_prompt_text(
                summary,
                conditions,
                effects,
                transitions,
                segments,
            )
            (
                raw_direct_path_text,
                sanitized_direct_path_text,
                reject_reason,
                consistency_issue,
            ) = self._get_direct_path_text_debug_info(
                path.get("direct_path_text"),
                method_start,
                method_end,
                line_mode,
            )
            direct_path_text = raw_direct_path_text

            condition_texts = [item["text"] for item in conditions]
            effect_texts = [item["text"] for item in effects]
            signature = (
                summary,
                tuple((item["text"], item["line"]) for item in conditions),
                tuple((item["text"], item["line"]) for item in effects),
                tuple(
                    (
                        item["source_text"],
                        item["source_line"],
                        item["condition_value"],
                        item["branch_label"],
                        item["target_text"],
                        item["target_line"],
                    )
                    for item in transitions
                ),
                tuple(line_hints),
            )
            if not line_hints or signature in seen_signatures:
                continue
            seen_signatures.add(signature)
            valid_paths.append(
                {
                    "path_id": path.get("path_id") or f"p{index + 1}",
                    "kind": (path.get("kind") or "").strip(),
                    "summary": summary,
                    "conditions": conditions,
                    "effects": effects,
                    "coverage_anchors": sorted(coverage_anchors),
                    "line_hints": line_hints,
                    "membership_lines": membership_lines,
                    "direct_path_text": direct_path_text,
                    "direct_path_text_raw": raw_direct_path_text,
                    "direct_path_text_sanitized": sanitized_direct_path_text,
                    "direct_path_text_reject_reason": reject_reason,
                    "direct_path_text_consistency_issue": consistency_issue,
                    "transitions": transitions,
                    "segments": segments,
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
        method_skeletons = self._method_skeletons_for_prompt()
        method_entries = []
        for method_skeleton in method_skeletons:
            prompt = self.generate_llm_prompt([method_skeleton])
            response_text, _, _ = llm.call_model(
                prompt=prompt, max_tokens=8192, temperature=0.1
            )
            clean_text = response_text.strip()
            if clean_text.startswith("```json"):
                clean_text = clean_text[len("```json") :]
            if clean_text.startswith("```"):
                clean_text = clean_text[len("```") :]
            if clean_text.endswith("```"):
                clean_text = clean_text[:-3]
            data = json.loads(clean_text.strip())
            returned_methods = data.get("methods", [])
            if returned_methods:
                method_entries.extend(returned_methods)
            else:
                method_entries.append(
                    {
                        "qualified_method_key": method_skeleton["qualified_method_key"],
                        "complexity": max(method_skeleton.get("complexity_hint", 1), 1),
                        "path_count": 0,
                        "paths": [],
                    }
                )
        self.parse_llm_response(json.dumps({"methods": method_entries}))

    def run(self, mock_response_text=None):
        self.extract_deterministic_info()
        if mock_response_text is not None:
            self.parse_llm_response(mock_response_text)
        else:
            self.analyze_with_llm()
        return self
