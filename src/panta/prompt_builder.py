import logging

from jinja2 import Environment, StrictUndefined

from .comex_path_prompt_builder import ComexPathPromptBuilder
from .config_loader import get_settings
from .llm_path_advice_prompt_builder import LLMPathAdvicePromptBuilder
from .panta_logger import pantaLogger
from .templates import (
    ADDITIONAL_INCLUDES_TEXT,
    ADDITIONAL_INSTRUCTIONS_TEXT,
    FAILED_TESTS_TEXT,
)
from .utils import read_file

MAX_TESTS_PER_RUN = 4


class PromptBuilder:
    def __init__(
        self,
        project_dir: str,
        source_code_file: str,
        test_code_file: str,
        code_coverage_report: str = "",
        included_files: str = "",
        additional_instructions: str = "",
        failed_test_runs: str = "",
        coverage_invalid_tests: str = "",
        language: str = "python",
        lines_missed=None,
        branch_missed=None,
        path_history=None,
        test_dependencies="",
        llm_model="",
        llm_path_advice_model="",
        llm_path_advice_temperature=0.1,
        llm_light_advice_temperature=0.1,
        selection_mode="comex",
        current_coverage=None,
        no_coverage_increase_count=0,
        llm_advice_activation_line_coverage=50.0,
        llm_advice_activation_no_growth=1,
        snapshotter=None,
        source_line_map=None,
        selected_branch_lines=None,
    ):
        if lines_missed is None:
            lines_missed = []
        if branch_missed is None:
            branch_missed = []
        if path_history is None:
            path_history = {}

        self.project_dir = project_dir
        self.source_code_file_path = source_code_file
        self.source_file_name = source_code_file.split("/")[-1]
        self.test_file_name = test_code_file.split("/")[-1]
        self.source_file = read_file(source_code_file)
        self.test_file = read_file(test_code_file)
        self.code_coverage_report = code_coverage_report
        self.language = language
        self.llm_model = llm_model
        self.llm_path_advice_model = llm_path_advice_model or llm_model
        self.llm_path_advice_temperature = llm_path_advice_temperature
        self.llm_light_advice_temperature = llm_light_advice_temperature
        self.selection_mode = selection_mode
        self.current_coverage = current_coverage or (0.0, 0.0)
        self.no_coverage_increase_count = no_coverage_increase_count
        self.llm_advice_activation_line_coverage = llm_advice_activation_line_coverage
        self.llm_advice_activation_no_growth = llm_advice_activation_no_growth
        self.snapshotter = snapshotter
        self.source_line_map = source_line_map
        self.selected_branch_lines = selected_branch_lines or []
        self.lines_missed = lines_missed
        self.branch_missed = branch_missed
        self.path_history = path_history
        self.test_dependencies = test_dependencies

        self.logger = pantaLogger.initialize_logger(__name__)

        self.source_file_numbered = "\n".join(
            [f"{i + 1} {line}" for i, line in enumerate(self.source_file.split("\n"))]
        )
        self.test_file_numbered = "\n".join(
            [f"{i + 1} {line}" for i, line in enumerate(self.test_file.split("\n"))]
        )

        self.included_files = (
            ADDITIONAL_INCLUDES_TEXT.format(included_files=included_files)
            if included_files
            else ""
        )
        self.additional_instructions = (
            ADDITIONAL_INSTRUCTIONS_TEXT.format(
                additional_instructions=additional_instructions
            )
            if additional_instructions
            else ""
        )
        self.failed_test_runs_feedback = (
            FAILED_TESTS_TEXT.format(failed_test_runs=failed_test_runs)
            if failed_test_runs
            else ""
        )
        self.coverage_invalid_tests = coverage_invalid_tests
        self.failed_test_runs = failed_test_runs

    def _build_comex_path_prompt_builder(self):
        return ComexPathPromptBuilder(
            source_code_file=self.source_code_file_path,
            source_file_name=self.source_file_name,
            test_file_name=self.test_file_name,
            source_file=self.source_file,
            test_file=self.test_file,
            code_coverage_report=self.code_coverage_report,
            included_files=self.included_files,
            additional_instructions=self.additional_instructions,
            failed_test_runs_feedback=self.failed_test_runs_feedback,
            coverage_invalid_tests=self.coverage_invalid_tests,
            language=self.language,
            lines_missed=self.lines_missed,
            branch_missed=self.branch_missed,
            path_history=self.path_history,
            test_dependencies=self.test_dependencies,
            snapshotter=self.snapshotter,
        )

    def build_prompt_cfa_guided(self, pick_two_paths=True) -> dict:
        comex_prompt_builder = self._build_comex_path_prompt_builder()
        prompt = comex_prompt_builder.build_prompt_cfa_guided(pick_two_paths)
        self.path_history = comex_prompt_builder.get_current_path_history()
        return prompt

    def build_prompt_llm_guided(self) -> dict:
        llm_prompt_builder = LLMPathAdvicePromptBuilder(
            source_code_file=self.source_code_file_path,
            source_file_name=self.source_file_name,
            test_file_name=self.test_file_name,
            source_file=self.source_file,
            test_file=self.test_file,
            code_coverage_report=self.code_coverage_report,
            included_files=self.included_files,
            additional_instructions=self.additional_instructions,
            failed_test_runs_feedback=self.failed_test_runs_feedback,
            coverage_invalid_tests=self.coverage_invalid_tests,
            language=self.language,
            lines_missed=self.lines_missed,
            branch_missed=self.branch_missed,
            test_dependencies=self.test_dependencies,
            llm_model=self.llm_path_advice_model,
            llm_path_advice_temperature=self.llm_path_advice_temperature,
            llm_light_advice_temperature=self.llm_light_advice_temperature,
            current_coverage=self.current_coverage,
            no_coverage_increase_count=self.no_coverage_increase_count,
            llm_advice_activation_line_coverage=self.llm_advice_activation_line_coverage,
            llm_advice_activation_no_growth=self.llm_advice_activation_no_growth,
            snapshotter=self.snapshotter,
            source_line_map=self.source_line_map,
            selected_branch_lines=self.selected_branch_lines,
        )
        return llm_prompt_builder.build_prompt_guided()

    def get_current_path_history(self):
        return self.path_history

    def build_prompt(self, coverage_enabled=False) -> dict:
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
        }
        environment = Environment(undefined=StrictUndefined)
        try:
            if coverage_enabled:
                system_prompt = environment.from_string(
                    get_settings().test_generation_prompt_with_code_coverage_report.system
                ).render(variables)
                user_prompt = environment.from_string(
                    get_settings().test_generation_prompt_with_code_coverage_report.user
                ).render(variables)
            else:
                system_prompt = environment.from_string(
                    get_settings().test_generation_prompt.system
                ).render(variables)
                user_prompt = environment.from_string(
                    get_settings().test_generation_prompt.user
                ).render(variables)

            self.logger.debug(f"system_prompt: {system_prompt}")
            self.logger.debug(f"user_prompt: {user_prompt}")
        except Exception as e:
            logging.error(f"Error rendering prompt: {e}")
            return {"system": "", "user": ""}

        return {"system": system_prompt, "user": user_prompt}

    def build_prompt_custom(self, file) -> dict:
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
            "additional_includes_section": self.included_files,
            "failed_tests_section": self.failed_test_runs_feedback,
            "additional_instructions_text": self.additional_instructions,
            "language": self.language,
            "max_tests": MAX_TESTS_PER_RUN,
        }
        environment = Environment(undefined=StrictUndefined)
        try:
            system_prompt = environment.from_string(
                get_settings().get(file).system
            ).render(variables)
            user_prompt = environment.from_string(get_settings().get(file).user).render(
                variables
            )
        except Exception as e:
            logging.error(f"Error rendering prompt: {e}")
            return {"system": "", "user": ""}

        return {"system": system_prompt, "user": user_prompt}

    def build_prompt_for_fixing(self) -> dict:
        variables = {
            "source_file_name": self.source_file_name,
            "test_file_name": self.test_file_name,
            "source_file": self.source_file,
            "test_file": self.test_file,
            "test_dependencies": self.test_dependencies,
            "failed_test_runs": self.failed_test_runs,
            "language": self.language,
        }
        environment = Environment(undefined=StrictUndefined)
        try:
            system_prompt = environment.from_string(
                get_settings().failed_test_prompt.system
            ).render(variables)
            user_prompt = environment.from_string(
                get_settings().failed_test_prompt.user
            ).render(variables)
            self.logger.debug(f"system_prompt: {system_prompt}")
            self.logger.debug(f"user_prompt: {user_prompt}")
        except Exception as e:
            logging.error(f"Error rendering prompt: {e}")
            return {"system": "", "user": ""}

        return {"system": system_prompt, "user": user_prompt}
