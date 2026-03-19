from .cfg.src.comex.codeviews.CFG.CFG_driver import CFGDriver
from .cfg.src.comex.codeviews.combined_graph.combined_driver import (
    CombinedDriver,
    line_number_to_node_id_mapping,
)


def has_line_mappings(cfg_obj):
    return hasattr(cfg_obj, "line_number_to_node_id") and hasattr(
        cfg_obj, "node_id_to_line_number"
    )


def ensure_line_mappings(src_code, cfg_obj):
    if has_line_mappings(cfg_obj):
        return cfg_obj.line_number_to_node_id, cfg_obj.node_id_to_line_number

    if not hasattr(cfg_obj, "CFG_nodes"):
        raise TypeError("CFG object does not expose CFG_nodes for line mapping")

    line_number_to_node_id, node_id_to_line_number = line_number_to_node_id_mapping(
        src_code, cfg_obj.CFG_nodes
    )
    cfg_obj.line_number_to_node_id = line_number_to_node_id
    cfg_obj.node_id_to_line_number = node_id_to_line_number
    return line_number_to_node_id, node_id_to_line_number


def get_structural_cfg(language, src_code, properties=None):
    cfg_obj = CFGDriver(language, src_code, properties or {})
    ensure_line_mappings(src_code, cfg_obj)
    return cfg_obj


def get_path_cfg(language, src_code, properties=None):
    cfg_obj = CombinedDriver(src_language=language, src_code=src_code)
    ensure_line_mappings(src_code, cfg_obj)
    return cfg_obj
