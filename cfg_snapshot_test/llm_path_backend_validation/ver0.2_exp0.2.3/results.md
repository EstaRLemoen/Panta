# ver0.2_exp0.2.3 结果摘要

## 一句话结论

`WP3.5` 的 `segments + transitions` schema 已成功接入主流程并进入 snapshot，但第一轮 3 次迭代 smoke 的 coverage 表现很差，且结果明显受到旧测试残留与生成失败的干扰，因此这轮实验只能证明“链路接通”，不能证明“path 质量已经可用”。

## 本次验证确认的能力

- `LLMCFGAnalyzer` 已开始输出：
  - `transitions`
  - `segments`
- `PromptBuilder` 已透传：
  - `_llm_transitions`
  - `_llm_segments`
- `line_hints` 相比旧版更密，不再只是 sparse anchors
- `meta.json` 中仍正确记录：
  - `path_cfg_backend = llm`
  - `llm_line_mode = preprocessed`

## 快照侧关键证据

- `cfg_file_obj.json` 中已能看到 `_llm_transitions` / `_llm_segments`
- `_llm_precomputed_path_str` 已开始把 transition 和 sequential path 文本拼进去

关键文件：

- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_152129_9692f8e9/002_prompt_builder_cfg/meta.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_152129_9692f8e9/002_prompt_builder_cfg/cfg_file_obj.json`
- `cfg_snapshot_test/intermediate/cfg/CSVParser_20260324_152129_9692f8e9/004_prompt_builder_cfg/cfg_file_obj.json`

## smoke run 行为

- 初始 coverage：`0.0% / 0.0%`
- Iteration 0 后：`0.99% / 0.0%`
- Iteration 1、2：均未继续提升
- 最终正常收口，但 coverage 几乎没有实质改善

## 本轮主要问题

- baseline 不是干净 no-tests 基线，实验可比性较差
- 多次生成测试出现 runtime error
- 尽管 path schema 已更丰富，但实际 prompt 仍没有带来有效 coverage 提升
- 这轮因此更像“新增接口打通验证”，而不是“路径语义质量验证”

## 对后续的启示

- `segments + transitions` 接口已经接通，不能再说是“PromptBuilder 完全拿不到新增信息”
- 但这轮结果不能用来证明 path 质量，必须在干净基线下重跑
- 因此需要补一轮重新清空旧测试后的 smoke，以区分：
  - 是接口没接上
  - 还是 path 质量/测试生成阶段本身有问题
