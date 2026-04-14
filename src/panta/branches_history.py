from dataclasses import dataclass, field


@dataclass
class BHUnit:
    line_number: int
    selection_history: dict = field(default_factory=dict)
    first_covered_round: int = -1
    selected_count: int = 0

    def record_selection(self, round_num: int, selected: bool):
        self.selection_history[round_num] = selected
        if selected:
            self.selected_count += 1

    def mark_covered(self, round_num: int):
        if self.first_covered_round == -1:
            self.first_covered_round = round_num


class BranchHistory:
    def __init__(self):
        self._branches: dict = {}

    @staticmethod
    def select_branch_lines(branch_units: list, max_count: int) -> list:
        selectable_units = [
            unit
            for unit in branch_units
            if unit is not None and unit.first_covered_round == -1
        ]
        sorted_units = sorted(
            selectable_units,
            key=lambda unit: (unit.selected_count, unit.line_number),
        )
        return [unit.line_number for unit in sorted_units[:max_count]]

    def initialize_branches(self, branch_lines: list):
        for line_num in branch_lines:
            if line_num not in self._branches:
                self._branches[line_num] = BHUnit(line_number=line_num)

    def record_selections(self, round_num: int, selected_lines: list):
        for line_num in self._branches:
            selected = line_num in selected_lines
            self._branches[line_num].record_selection(round_num, selected)

    def update_coverage(self, round_num: int, branch_missed: list):
        missed_set = set(branch_missed)
        for line_num, unit in self._branches.items():
            if line_num not in missed_set:
                unit.mark_covered(round_num)

    def get_branch(self, line_number: int):
        return self._branches.get(line_number)
