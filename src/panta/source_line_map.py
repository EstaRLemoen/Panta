class SourceLineMap:
    def __init__(self, source_code: str):
        self._lines = source_code.splitlines()
        self._line_map = {i: line for i, line in enumerate(self._lines, start=1)}

    def get_line(self, line_number: int) -> str:
        return self._line_map.get(line_number, "")

    def get_lines(self) -> list:
        return self._lines

    def get_line_map(self) -> dict:
        return dict(self._line_map)

    def total_lines(self) -> int:
        return len(self._lines)

    def line_numbers(self) -> list:
        return list(self._line_map.keys())

    def reconstruct_source(self) -> str:
        return "\n".join(self._lines)
