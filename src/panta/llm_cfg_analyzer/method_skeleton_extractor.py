from dataclasses import dataclass, field

from ..cfg.src.comex.tree_parser.parser_driver import ParserDriver
from ..cfg.src.comex.utils.java_nodes import get_signature


@dataclass
class MethodSkeleton:
    qualified_method_key: str
    class_name: str
    method_name: str
    signature: list[str]
    visibility: str
    is_constructor: bool
    is_static: bool
    is_abstract: bool
    start_line: int
    end_line: int
    declaration_line: int
    declaration_text: str
    method_source: str


@dataclass
class ClassSkeleton:
    class_name: str
    class_kind: str
    parent_class_name: str | None
    declaration_line: int
    declaration_text: str
    methods: list[MethodSkeleton] = field(default_factory=list)


class MethodSkeletonExtractor:
    def __init__(self, src_language: str, src_code: str):
        self.src_language = src_language
        self.parser_driver = ParserDriver(src_language, src_code)
        self.src_code = self.parser_driver.src_code
        self.root_node = self.parser_driver.root_node

    def extract(self):
        if self.src_language != "java":
            raise NotImplementedError(
                "MethodSkeletonExtractor currently supports Java only"
            )

        classes = []
        self._collect_classes(self.root_node, classes, None)
        return {
            "classes": [
                self._class_to_dict(class_skeleton) for class_skeleton in classes
            ]
        }

    def _collect_classes(self, node, classes, parent_class_name):
        if node.type == "class_declaration":
            class_skeleton = self._build_class_skeleton(node, parent_class_name)
            classes.append(class_skeleton)

            body = node.child_by_field_name("body")
            if body is not None:
                for child in body.named_children:
                    if child.type in {"method_declaration", "constructor_declaration"}:
                        method = self._build_method_skeleton(
                            child, class_skeleton.class_name
                        )
                        if method is not None:
                            class_skeleton.methods.append(method)
                    elif child.type == "class_declaration":
                        self._collect_classes(child, classes, class_skeleton.class_name)
            return

        for child in node.named_children:
            self._collect_classes(child, classes, parent_class_name)

    def _build_class_skeleton(self, node, parent_class_name):
        class_name = self._node_text(node.child_by_field_name("name"))
        return ClassSkeleton(
            class_name=class_name,
            class_kind="inner" if parent_class_name else "outer",
            parent_class_name=parent_class_name,
            declaration_line=node.start_point[0] + 1,
            declaration_text=self._declaration_text(node, {"class_body"}),
        )

    def _build_method_skeleton(self, node, class_name):
        method_name = self._method_name(node)
        signature = list(get_signature(node))
        visibility = self._visibility(node)
        declaration_text = self._declaration_text(node, {"block", "constructor_body"})
        body = node.child_by_field_name("body")

        method = MethodSkeleton(
            qualified_method_key=self._qualified_method_key(
                class_name, method_name, signature
            ),
            class_name=class_name,
            method_name=method_name,
            signature=signature,
            visibility=visibility,
            is_constructor=node.type == "constructor_declaration",
            is_static="static" in declaration_text,
            is_abstract=body is None,
            start_line=node.start_point[0] + 1,
            end_line=node.end_point[0] + 1,
            declaration_line=node.start_point[0] + 1,
            declaration_text=declaration_text,
            method_source=self._node_text(node),
        )

        if self._include_method(method, body):
            return method
        return None

    def _include_method(self, method, body):
        if method.is_constructor:
            return False
        if method.visibility == "private":
            return False
        if body is None or not body.named_children:
            return False
        if (
            method.method_name == "main"
            and method.is_static
            and "void" in method.declaration_text
        ):
            return False

        statement_count = self._statement_count(body)
        if (
            method.method_name.startswith("is")
            and "boolean" in method.declaration_text
            and not method.signature
        ):
            return False
        if (
            method.method_name.startswith("set")
            and "void" in method.declaration_text
            and len(method.signature) == 1
            and statement_count == 1
        ):
            return False
        if (
            method.method_name.startswith("get")
            and not method.signature
            and statement_count == 1
        ):
            return False
        return True

    def _statement_count(self, body):
        return len([child for child in body.named_children if child.type != "comment"])

    def _visibility(self, node):
        declaration_text = self._declaration_text(node, {"block", "constructor_body"})
        if declaration_text.startswith("public"):
            return "public"
        if declaration_text.startswith("protected"):
            return "protected"
        if declaration_text.startswith("private"):
            return "private"
        return "default"

    def _method_name(self, node):
        name_node = node.child_by_field_name("name")
        if name_node is not None:
            return self._node_text(name_node)
        identifiers = [child for child in node.children if child.type == "identifier"]
        return self._node_text(identifiers[0]) if identifiers else "unknown"

    def _declaration_text(self, node, excluded_types):
        parts = []
        for child in node.children:
            if child.type not in excluded_types:
                parts.append(self._node_text(child))
        return " ".join(part for part in parts if part).strip()

    def _qualified_method_key(self, class_name, method_name, signature):
        return f"{class_name}.{method_name}({', '.join(signature)})"

    def _node_text(self, node):
        if node is None:
            return ""
        return node.text.decode("utf-8")

    def _class_to_dict(self, class_skeleton):
        return {
            "class_name": class_skeleton.class_name,
            "class_kind": class_skeleton.class_kind,
            "parent_class_name": class_skeleton.parent_class_name,
            "declaration_line": class_skeleton.declaration_line,
            "declaration_text": class_skeleton.declaration_text,
            "methods": [
                self._method_to_dict(method) for method in class_skeleton.methods
            ],
        }

    def _method_to_dict(self, method):
        return {
            "qualified_method_key": method.qualified_method_key,
            "class_name": method.class_name,
            "method_name": method.method_name,
            "signature": method.signature,
            "visibility": method.visibility,
            "is_constructor": method.is_constructor,
            "is_static": method.is_static,
            "is_abstract": method.is_abstract,
            "start_line": method.start_line,
            "end_line": method.end_line,
            "declaration_line": method.declaration_line,
            "declaration_text": method.declaration_text,
            "method_source": method.method_source,
        }
