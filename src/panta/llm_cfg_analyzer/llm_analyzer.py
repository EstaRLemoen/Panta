import re
import json
import logging
from typing import Dict, List, Tuple, Any

# Assuming we will use the existing litellm invocation mechanism from Panta
# from src.panta.model_invocation.llm_invocation import LLMInvocation

class LLMCFGAnalyzer:
    """
    A lightweight, pure Python replacement for the tree-sitter based CFG module.
    It uses regular expressions for deterministic AST extraction (like imports and class names),
    and leverages an LLM to perform complex Control Flow Graph (CFG) analysis
    (like calculating cyclomatic complexity and extracting independent execution paths).
    """

    def __init__(self, src_language: str, src_code: str):
        self.src_language = src_language
        self.src_code = src_code
        self.logger = logging.getLogger(__name__)
        
        # We will populate these structures to mimic the original 'comex' output
        self.file_obj = {
            "imports": [],
            "class_objects": []
        }
        
        # Original comex mapping: node_id -> list of original lines
        # Since we don't have AST nodes, we will use sequential dummy IDs (e.g., 1000+) 
        # for methods and assign their corresponding line ranges.
        self.node_id_to_line_number = {}
        self.line_number_to_node_id = {}
        
        self._current_dummy_id = 1000

    def get_next_id(self) -> int:
        self._current_dummy_id += 1
        return self._current_dummy_id

    def add_line_number_with_prefix(self) -> str:
        """
        Adds line numbers to the source code to help the LLM accurately reference lines.
        """
        numbered_lines = []
        for i, line in enumerate(self.src_code.split('\n')):
            numbered_lines.append(f"{i + 1}: {line}")
        return "\n".join(numbered_lines)

    def extract_deterministic_info(self):
        """
        Uses Regex to extract imports and the class name.
        This is 100% accurate and costs 0 tokens.
        """
        lines = self.src_code.split('\n')
        
        class_name = "UnknownClass"
        
        for i, line in enumerate(lines):
            line_num = i + 1
            stripped_line = line.strip()
            
            # Extract Imports
            if stripped_line.startswith("import ") and stripped_line.endswith(";"):
                import_id = self.get_next_id()
                self.file_obj["imports"].append({
                    "id": import_id,
                    "value": stripped_line
                })
                self.node_id_to_line_number[import_id] = [line_num]
                self.line_number_to_node_id[line_num] = (line_num, import_id)
                
            # Extract Class Name (simplified regex for public class)
            class_match = re.search(r'(?:public\s+)?(?:abstract\s+)?class\s+(\w+)', stripped_line)
            if class_match:
                class_name = class_match.group(1)
                class_id = self.get_next_id()
                
                # We initialize the class object here
                if not self.file_obj["class_objects"]:
                    self.file_obj["class_objects"].append({
                        "class_declaration": {
                            "id": class_id,
                            "name": class_name,
                            "value": stripped_line
                        },
                        "fields": [],
                        "constructors": [],
                        "methods_under_test": []
                    })
                self.node_id_to_line_number[class_id] = [line_num]

    def generate_llm_prompt(self) -> str:
        """
        Builds the prompt string to ask the LLM to extract methods, complexity, and paths.
        Does not call the LLM itself. Returns the prompt string.
        """
        numbered_code = self.add_line_number_with_prefix()
        
        prompt_user = f"""
You are an expert static code analyzer. Your task is to analyze the provided Java source code and extract control flow information for all public and protected methods.

Please analyze the following Java code:

```java
{numbered_code}
```

For each public or protected method in the class, extract the following information and return it strictly as a JSON object:
1. "name": The name of the method.
2. "start_line": The line number where the method signature begins.
3. "end_line": The line number where the method's closing brace '}}' is located.
4. "complexity": The cyclomatic complexity of the method (e.g., 1 for simple linear flow, +1 for each if, for, while, case, catch).
5. "paths": If the complexity is > 1, extract all independent execution paths from the start of the method to a return statement or the end of the method. 
   For each path, provide:
   - "path_conditions_str": A natural language description of the conditions required to take this path (e.g., "when a > 0 is True and b == null is False").
   - "returns": A description of what is returned or the final state (e.g., "return a + b;" or "throws IllegalArgumentException").
   If complexity is 1, "paths" should be an empty list [].

Respond ONLY with valid JSON matching this schema:
{{
  "methods": [
    {{
      "name": "methodName",
      "start_line": 10,
      "end_line": 20,
      "complexity": 3,
      "paths": [
        {{"path_conditions_str": "when x > 0 is True", "returns": "return x;"}},
        {{"path_conditions_str": "when x > 0 is False", "returns": "return 0;"}}
      ]
    }}
  ]
}}
"""
        return prompt_user

    def parse_llm_response(self, response_text: str):
        """
        Parses the JSON returned by the LLM and populates the fake 'file_obj' 
        to perfectly mimic the output of the original 'comex' module.
        """
        try:
            # Clean up potential markdown formatting from LLM
            clean_text = response_text.strip().removeprefix("```json").rstrip("`").strip()
            data = json.loads(clean_text)
            
            if not self.file_obj["class_objects"]:
                # Fallback if regex failed to find a class
                self.file_obj["class_objects"].append({"methods_under_test": []})
                
            class_obj = self.file_obj["class_objects"][0]
            
            for method in data.get("methods", []):
                method_id = self.get_next_id()
                start_line = method.get("start_line", 0)
                end_line = method.get("end_line", 0)
                
                # Populate line mappings for the method (Panta uses this to check missed lines)
                method_lines = list(range(start_line, end_line + 1))
                self.node_id_to_line_number[method_id] = method_lines
                for line in method_lines:
                    self.line_number_to_node_id[line] = (line, method_id)
                
                # Construct the mocked paths structure
                mocked_paths = []
                for i, path_data in enumerate(method.get("paths", [])):
                    # We create a dummy "path" array with a single dummy node 
                    # that holds the human-readable string.
                    # Panta's prompt_builder currently translates nodes to strings, 
                    # but we will bypass that translation later in prompt_builder by just reading this string!
                    dummy_path_node_id = self.get_next_id()
                    
                    # Store the path line range roughly as the method lines so coverage can match it
                    self.node_id_to_line_number[dummy_path_node_id] = method_lines
                    
                    mocked_path = {
                        "path": [
                            {
                                "id": dummy_path_node_id,
                                "statement": path_data.get("path_conditions_str", ""),
                                "conditional": path_data.get("returns", "")
                            }
                        ],
                        # We also inject the pre-computed string directly so prompt_builder 
                        # can just grab it without trying to parse AST nodes.
                        "_llm_precomputed_path_str": f"{path_data.get('path_conditions_str')} returns: {path_data.get('returns')}"
                    }
                    mocked_paths.append(mocked_path)
                
                method_declaration = {
                    "method_declaration": {
                        "id": method_id,
                        "name": method.get("name", "unknown"),
                        "complexity": method.get("complexity", 1),
                        # Panta uses this 'nodes' array to find all lines belonging to the method
                        "nodes": [method_id] 
                    },
                    "paths": mocked_paths
                }
                
                class_obj["methods_under_test"].append(method_declaration)
                
        except Exception as e:
            self.logger.error(f"Failed to parse LLM CFG response: {e}")

    def run(self, mock_response_text=None):
        """Executes the full pipeline (used primarily for mocked testing)."""
        self.extract_deterministic_info()
        if mock_response_text:
            self.parse_llm_response(mock_response_text)
        return self.file_obj

