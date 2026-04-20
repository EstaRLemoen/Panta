import argparse
import configparser
import os
from .panta import Panta


def load_config():
    config_path = os.path.join(os.path.dirname(__file__), "config.ini")
    confparser = configparser.ConfigParser()
    confparser.read(config_path)
    return confparser["default"]


def config_to_namespace(config):
    return argparse.Namespace(
        project_directory=config.get("project_directory"),
        source_code_file=config.get("source_code_file"),
        test_code_file=config.get("test_code_file"),
        test_file_output_path=config.get("test_file_output_path"),
        code_coverage_report_path=config.get("code_coverage_report_path"),
        test_execution_command=config.get("test_execution_command"),
        test_dependency_command=config.get("test_dependency_command"),
        test_code_command_dir=config.get("test_code_command_dir"),
        included_files=config.get("included_files"),
        junit_version=config.getint("junit_version"),
        model=config.get("model"),
        llm_path_advice_model=config.get(
            "llm_path_advice_model", fallback=config.get("model")
        ),
        coverage_type=config.get("coverage_type"),
        report_filepath=config.get("report_filepath"),
        target_coverage=config.getint("target_coverage"),
        maximum_iterations=config.getint("maximum_iterations"),
        no_coverage_increase_iterations=config.getint(
            "no_coverage_increase_iterations"
        ),
        enable_fixing=config.getint("enable_fixing"),
        fixing_mode=config.get("fixing_mode", fallback="combined"),
        enable_advice_feedback=config.getboolean(
            "enable_advice_feedback", fallback=False
        ),
        run_symprompt=config.getboolean("run_symprompt"),
        prompt_type=config.get("prompt_type"),
        pick_two_paths=config.getboolean("pick_two_paths"),
        selection_mode=config.get("selection_mode", fallback="comex"),
        llm_path_advice_temperature=config.getfloat(
            "llm_path_advice_temperature", fallback=0.1
        ),
        llm_light_advice_temperature=config.getfloat(
            "llm_light_advice_temperature", fallback=0.1
        ),
        llm_advice_activation_line_coverage=config.getfloat(
            "llm_advice_activation_line_coverage", fallback=50.0
        ),
        llm_advice_activation_no_growth=config.getint(
            "llm_advice_activation_no_growth", fallback=1
        ),
        additional_instructions=config.get("additional_instructions"),
        # Semantic change: optional CFG snapshot controls.
        dump_cfg_intermediate=config.getboolean(
            "dump_cfg_intermediate", fallback=False
        ),
        cfg_dump_dir=config.get(
            "cfg_dump_dir", fallback="cfg_snapshot_test/intermediate/cfg"
        ),
        cfg_dump_level=config.get("cfg_dump_level", fallback="full"),
        cfg_dump_prompt_mode=config.get("cfg_dump_prompt_mode", fallback="summary"),
        artifact_snapshot_enabled=config.getboolean(
            "artifact_snapshot_enabled", fallback=False
        ),
        artifact_snapshot_dir=config.get(
            "artifact_snapshot_dir", fallback="artifact_snapshot"
        ),
    )


def main():
    config_parser = load_config()
    args = config_to_namespace(config_parser)
    panta = Panta(args)
    if args.run_symprompt:
        panta.run_symprompt()
    else:
        panta.run()


if __name__ == "__main__":
    main()
