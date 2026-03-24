# ver0.2_exp0.2.25 运行索引

## 代码状态

- `llm_analyzer.py`
  - 增加 `direct_path_text` 相关可观测性
  - `direct_path_text` 行号 remap 到 original line
  - 逐 method 调用 LLM
  - 强化单路径单 outcome prompt 规则与示例
- `prompt_builder.py`
  - 优先消费 `_llm_direct_path_text` / `_llm_path_prompt_text`
  - 使用 `_llm_membership_lines`
  - 保留 `selected_paths` snapshot 可观测性
- `panta.py`
  - 仅保留 `prompt_builder` 缺席时 `path_history` 兜底

## 相关实验

### analyzer-only 验证

- `cfg_snapshot_test/validation_runs/ver0.2_exp0.2.20_llm_analyzer_only.log`
  - 用于确认 prompt 更新后 LLM 是否仍会省略 listed methods
- `cfg_snapshot_test/validation_runs/ver0.2_exp0.2.21_llm_analyzer_per_method.log`
  - 首次逐 method 调用实验
- `cfg_snapshot_test/validation_runs/ver0.2_exp0.2.22_llm_analyzer_per_method.log`
  - 加强单路径单 outcome说明后的 analyzer-only 检查
- `cfg_snapshot_test/validation_runs/ver0.2_exp0.2.23_llm_analyzer_per_method.log`
  - 强 split 示例后的 analyzer-only 检查

### smoke 验证

- `cfg_snapshot_test/validation_runs/ver0.2_exp0.2.24_smoke1_pre_redesign.log`
  - 当前状态下的 1 轮 llm smoke，主要验证集成是否仍可运行
- `cfg_snapshot_test/validation_runs/ver0.2_exp0.2.25_smoke3_current_llm.log`
  - 当前状态下的 3 轮 llm smoke，确认 path-guided 轮次是否已真正参与

## 相关 snapshot

- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_231257_207be5aa`
  - direct text 行号 remap 后的 llm snapshot
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260325_014242_76349994`
  - 当前状态 1 轮 llm smoke snapshot
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260325_015853_1cf361fc`
  - 当前状态 3 轮 llm smoke snapshot
