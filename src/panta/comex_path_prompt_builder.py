import random

from jinja2 import Environment, StrictUndefined

from .cfg_access import get_comex_path_cfg
from .config_loader import get_settings
from .panta_logger import pantaLogger


MAX_TESTS_PER_RUN = 4


class ComexPathPromptBuilder:
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
        path_history,
        test_dependencies: str,
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
        self.lines_missed = lines_missed
        self.branch_missed = branch_missed
        self.path_history = path_history
        self.test_dependencies = test_dependencies
        self.snapshotter = snapshotter

        cfg_driver = get_comex_path_cfg(self.language, self.source_file)
        if self.snapshotter:
            self.snapshotter.capture(
                stage="prompt_builder_cfg",
                source_code_file=source_code_file,
                language=self.language,
                cfg_driver=cfg_driver,
            )
        self.processed_source_code = cfg_driver.preprocessed_src_code
        self.cfg_obj = cfg_driver.file_obj
        self.cfg_node_to_line = cfg_driver.node_id_to_line_number
        self.line_to_cfg_node = cfg_driver.line_number_to_node_id

        self.cfa_guided_methods_under_test = self.extract_cfa_info_for_each_method_under_test()
        self.logger = pantaLogger.initialize_logger(__name__)

        self.source_file_numbered = "\n".join(
            [f"{i + 1} {line}" for i, line in enumerate(self.source_file.split("\n"))]
        )
        self.test_file_numbered = "\n".join(
            [f"{i + 1} {line}" for i, line in enumerate(self.test_file.split("\n"))]
        )

    def identify_method_under_tests_with_missed_lines(self, method):
        lines = [line for node_id in method["method_declaration"]["nodes"] for line in self.cfg_node_to_line[node_id]]
        method_name = method["method_declaration"]["name"]
        cyc_complexity = method["method_declaration"]["complexity"]
        method_missed_lines = []
        method_missed_branches = []
        for line in self.lines_missed:
            if line in lines:
                method_missed_lines.append(line)
        for branch_line in self.branch_missed:
            if branch_line in lines:
                method_missed_branches.append(branch_line)
        return method_name, cyc_complexity, lines, method_missed_lines, method_missed_branches

    def generate_paths_to_be_covered(self, method, missed_lines, missed_branches):
        paths = method["paths"]
        candidate_paths = []
        method_label = f"{method['method_declaration']['name']}_{method['method_declaration']['id']}"
        for index, path in enumerate(paths):
            path_label = f"{method_label}_{index}"
            path_node_ids = [node['id'] for node in path["path"]]
            path_lines = [line for node_id in path_node_ids for line in self.cfg_node_to_line[node_id]]
            path_covered_missed_lines = [value for value in missed_lines if value in path_lines]
            path_covered_missed_branches = [value for value in missed_branches if value in path_lines]
            path_nodes = [(self.cfg_node_to_line[node['id']], node['statement'], node['conditional']) for node in
                          path["path"]]
            if len(path_covered_missed_lines) or len(path_covered_missed_branches):
                path_conditions_str = ""
                for node in path_nodes:
                    node_lines = node[1].split("\n")
                    for i, line in enumerate(node[0]):
                        if i >= len(node_lines):
                            break
                        path_conditions_str += f"\n{line}: {node_lines[i]}"
                    if node[2] is not None:
                        path_conditions_str += f" is {node[2]}"
                missed_value = len(path_covered_missed_lines) + len(path_covered_missed_branches)
                candidate_paths.append((missed_value, path_lines, path_nodes, path_conditions_str, path_label))
                random.shuffle(candidate_paths)
        return candidate_paths

    def extract_cfa_info_for_each_method_under_test(self):
        clz_obj = self.cfg_obj["class_objects"][0]
        methods_under_test = clz_obj["methods_under_test"]
        cfa_guided_methods = []
        for method in methods_under_test:
            name, complexity, lines, missed_lines, missed_branches = self.identify_method_under_tests_with_missed_lines(
                method)
            if complexity > 1:
                candidate_paths = self.generate_paths_to_be_covered(method, missed_lines, missed_branches)
            else:
                candidate_paths = []
            cfa_guided_methods.append((name, complexity, lines, missed_lines, candidate_paths))

        return sorted(cfa_guided_methods, key=lambda x: x[1], reverse=True)

    def pick_two_paths(self, candidate_paths, path_history, max_visit=10):

        if not candidate_paths:
            return None, None

        paths_with_visits = [
            (path, path_history.get(path[4], 0))
            for path in candidate_paths
        ]

        filtered_paths = [path for path in paths_with_visits if path[1] < max_visit]

        if not filtered_paths:
            return None, None

        highest_missed_path = max(filtered_paths, key=lambda x: x[0][0])[0]
        least_visited_path = max(filtered_paths, key=lambda x: -x[1])[0]

        return highest_missed_path, least_visited_path

    def pick_path(self, candidate_paths, path_history, alpha=0.7):
        if not candidate_paths:
            return None

        paths_with_visits = [
            (path, path_history.get(path[4], 0))
            for path in candidate_paths
        ]
        max_missed_value = max((p[0] for p in candidate_paths), default=1)

        prioritized_path = max(
            paths_with_visits,
            key=lambda x: (
                alpha * (x[0][0] / max_missed_value) + (1 - alpha) / (x[1] + 1),
                x[0][0]
            )
        )[0]

        return prioritized_path

    def build_prompt_cfa_guided(self, pick_two_paths=True) -> dict:
        variables = {
            "source_file_name": self.source_file_name,
            "test_file_name": self.test_file_name,
            "source_file_numbered": self.source_file_numbered,
            "test_file_numbered": self.test_file_numbered,
            "source_file": self.source_file,
            "test_file": self.test_file,
            "test_dependencies": self.test_dependencies,
            "code_coverage_report": self.code_coverage_report,
            "coverage_invalid_tests_section": self.coverage_invalid_tests,
            "failed_tests_section": self.failed_test_runs_feedback,
            "additional_includes_section": self.included_files,
            "additional_instructions_text": self.additional_instructions,
            "language": self.language,
            "max_tests": MAX_TESTS_PER_RUN,
            "processed_source_code": self.processed_source_code,
        }

        environment = Environment(undefined=StrictUndefined)
        system_prompt = environment.from_string(
            get_settings().test_generation_cfg_guided_prompt.system
        ).render(variables)

        rendered_templates = ""
        for method in self.cfa_guided_methods_under_test:
            method_name = method[0]
            method_complexity = method[1]
            missed_lines = method[3]
            candidate_paths = method[4]
            rendered_template = ""
            if method_complexity > 1:
                template_str = "\n=========\nPlease generate test case for method `{{ method_name }}` " \
                               "to cover the path: {{ candidate_path }}"
                if pick_two_paths:
                    highest_missed_path, least_visited_path = self.pick_two_paths(candidate_paths, self.path_history)
                    if highest_missed_path and least_visited_path:
                        self.logger.info(
                            f"select the candidate path that covers the most missed lines for method {method_name}")
                        highest_path_label = highest_missed_path[4]
                        least_path_label = least_visited_path[4]
                        self.path_history[highest_path_label] = self.path_history.get(highest_path_label, 0) + 1
                        path_str = highest_missed_path[3]
                        rendered_template = environment.from_string(template_str).render(
                            method_name=method_name,
                            candidate_path=path_str,
                        )
                        if least_path_label != highest_path_label:
                            self.logger.info(
                                f"select another candidate path with the least time of visits for method {method_name}")
                            self.path_history[least_path_label] = self.path_history.get(least_path_label, 0) + 1
                            path_str = least_visited_path[3]
                            rendered_template += environment.from_string(template_str).render(
                                method_name=method_name,
                                candidate_path=path_str,
                            )
                else:
                    prioritized_path = self.pick_path(candidate_paths, self.path_history)
                    if prioritized_path:
                        self.logger.info(
                            f"select the path that has highest priority score for method {method_name}")
                        path_label = prioritized_path[4]
                        self.path_history[path_label] = self.path_history.get(path_label, 0) + 1
                        path_str = prioritized_path[3]
                        rendered_template = environment.from_string(template_str).render(
                            method_name=method_name,
                            candidate_path=path_str,
                        )
            else:
                if missed_lines:
                    template_str_missed_lines = "\n=========\nPlease generate test case for method `{{ method_name }}` " \
                                                "to cover missed lines: {{ missed_lines }}"

                    rendered_template = environment.from_string(template_str_missed_lines).render(
                        method_name=method_name, missed_lines=missed_lines)
            rendered_templates += rendered_template

        user_prompt = environment.from_string(
            get_settings().test_generation_cfg_guided_prompt.user
        ).render(variables, method_under_test=rendered_templates)

        self.logger.debug(f"system_prompt: {system_prompt}")
        self.logger.debug(f"user_prompt: {user_prompt}")
        if self.snapshotter:
            self.snapshotter.capture(
                stage="prompt_builder_selection",
                source_code_file=self.source_code_file_path,
                language=self.language,
                context={
                    "path_history": self.path_history,
                    "prompt_context": {
                        "prompt": user_prompt,
                    },
                },
            )

        return {"system": system_prompt, "user": user_prompt}

    def get_current_path_history(self):
        return self.path_history
