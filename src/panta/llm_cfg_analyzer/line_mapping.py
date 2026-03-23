from ..cfg.src.comex.codeviews.combined_graph.combined_driver import (
    preprocessed_to_original_line_number_mapping,
)


def build_line_mappings(src_code: str, language: str):
    preprocessed_to_original, original_to_preprocessed = (
        preprocessed_to_original_line_number_mapping(src_code, language)
    )
    return preprocessed_to_original, original_to_preprocessed


def remap_line_to_original(line_number, preprocessed_to_original):
    return preprocessed_to_original.get(line_number, line_number)


def remap_line_to_preprocessed(line_number, original_to_preprocessed):
    return original_to_preprocessed.get(line_number, line_number)


def slice_source_by_lines(src_code: str, start_line: int, end_line: int):
    lines = src_code.split("\n")
    if start_line <= 0 or end_line < start_line:
        return ""
    return "\n".join(lines[start_line - 1 : end_line])
