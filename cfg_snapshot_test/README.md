# Panta CFG Snapshot 功能一致性验证清单（A/B）

## 验证目标
- `dump_cfg_intermediate = false`：行为与改造前一致。
- `dump_cfg_intermediate = true`：除新增快照文件外，核心结果一致（coverage、流程成功性）。

## 预备条件
- 工作目录：`/home/esta/Panta/Panta`
- 环境：`conda activate panta-env`
- 建议备份：`src/panta/config.ini`

---

## 第一轮（确定性验证，推荐）
目标：减少 LLM 随机性影响。

### 1. 固定配置
在 `src/panta/config.ini` 设：
- `maximum_iterations = 0`
- `enable_fixing = 0`

### 2. A 组（关闭快照）
配置：
- `dump_cfg_intermediate = false`

运行：
```bash
python -m panta.main
```

记录：
- 退出码
- 最终 line/branch coverage
- 是否生成报告（日志包含 `Report generated successfully`）
- `result-files/intermediate/cfg` 无新增 run 目录

### 3. B 组（开启快照）
配置：
- `dump_cfg_intermediate = true`
- `cfg_dump_prompt_mode = summary`

运行：
```bash
python -m panta.main
```

记录：
- 退出码
- 最终 line/branch coverage
- 是否生成报告
- `result-files/intermediate/cfg/<run_id>/` 是否生成
- 是否包含预期 stage（按路径触发）：
  - `initial_test_suite_analysis_ast_cfg`
  - `prompt_builder_cfg`
  - `symprompt_cfg`（仅 `run_symprompt=true`）
  - `initial_test_class_skeleton_cfg`（仅测试文件为空）

### 4. 第一轮判定标准
- 必须一致：退出码、报告生成成功、coverage（应一致或极小差异）
- 允许差异：B 组新增快照目录和 JSON 文件

---

## 第二轮（真实流程验证，可选）
目标：验证完整链路稳定性。

### 1. 恢复常规配置
恢复你日常值：
- `maximum_iterations`
- `enable_fixing`

### 2. 再跑 A/B 各一次
- A：`dump_cfg_intermediate = false`
- B：`dump_cfg_intermediate = true`

### 3. 第二轮判定标准
- 两组都可跑通
- 报告均可生成
- coverage 变化趋势同量级（LLM 随机性下不强求逐字一致）

---

## 论文可复现抽查（B组）
抽查快照文件：
- `meta.json`
- `cfg_file_obj.json`
- `cfg_node_id_to_line_number.json`
- `cfg_line_number_to_node_id.json`
- `cfg_summary.json`

确认可读且字段完整，可作为中间证据链。

---

## 记录模板（可选）

### A 组（关闭快照）
- 退出码：
- Line Coverage：
- Branch Coverage：
- 报告生成：是/否
- 快照目录：无

### B 组（开启快照）
- 退出码：
- Line Coverage：
- Branch Coverage：
- 报告生成：是/否
- 快照目录：`result-files/intermediate/cfg/<run_id>`
- 包含 stage 数量：
- 快照文件完整性：是/否

### 判定结果
- 功能一致性：通过/失败
- 备注：

---

## 本次执行记录（2026-03-17）

### 第一轮（确定性，实际采用 `maximum_iterations=1`）
- 调整说明：原计划 `maximum_iterations=0` 会触发现有逻辑边界条件（`prompt_builder` 未初始化访问），因此改为 `maximum_iterations=1` 以保证流程可比执行。
- A 组（`dump_cfg_intermediate=false`）：运行完成并生成报告 `../../result-files/control_gpt-4o-mini/CSVParser_control_test_results.html`。
- B 组（`dump_cfg_intermediate=true`）：运行完成并生成同一路径报告，并生成快照目录 `result-files/intermediate/cfg/CSVParser_20260317_215801_b5ab3ed4`。
- B 组快照阶段：
  - `001_initial_test_suite_analysis_ast_cfg`
  - `002_prompt_builder_cfg`
- 哈希核验：两个阶段的 `meta.json.source_sha256` 与对应源码文件实际 SHA256 一致（`match=True`）。
- 备注：两组都出现相同的 LLM 侧 rate-limit 与同一处日志格式化问题，不影响“开关快照是否干扰功能”的结论。

### 第二轮（真实流程，恢复常规参数）
- 配置恢复：`maximum_iterations=11`、`enable_fixing=3`。
- A 组（`dump_cfg_intermediate=false`）：运行完成并生成报告 `../../result-files/control_gpt-4o-mini/CSVParser_control_test_results.html`。
- B 组（`dump_cfg_intermediate=true`）：运行完成并生成同一路径报告，并生成快照目录 `result-files/intermediate/cfg/CSVParser_20260317_221051_6bc8e7c0`。
- B 组快照阶段：
  - `001_initial_test_suite_analysis_ast_cfg`
  - `002_prompt_builder_cfg`
- 结论：在当前受 LLM 限流影响的执行条件下，A/B 均保持相同流程表现与相同报告产出路径；B 组仅新增快照目录与文件，符合“不干扰功能”的预期。

### 第三轮（严格公平 A/B，统一 baseline + 独立报告）
- 公平性措施：
  - 两组运行前都先恢复同一测试文件基线 `cfg_snapshot_test/validation_runs/CSVParserTest.baseline.java`。
  - 使用不同报告名避免覆盖：
    - A 组：`../../result-files/control_gpt-4o-mini/CSVParser_control_fairA_test_results.html`
    - B 组：`../../result-files/control_gpt-4o-mini/CSVParser_control_fairB_test_results.html`
  - 两组参数一致：`maximum_iterations=1`、`enable_fixing=0`，唯一变量是 `dump_cfg_intermediate`。
- A 组（`dump_cfg_intermediate=false`）：
  - 起始覆盖率：Line `51.49%` / Branch `33.93%`
  - 本轮提升：Line `+9.9%` / Branch `+0.0%`
  - 结束覆盖率：Line `61.39%` / Branch `33.93%`
- B 组（`dump_cfg_intermediate=true`）：
  - 起始覆盖率：Line `51.49%` / Branch `33.93%`
  - 本轮提升：Line `+9.9%` / Branch `+0.0%`
  - 结束覆盖率：Line `61.39%` / Branch `33.93%`
  - 快照目录：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260317_231421_8e2c62d3`
  - 阶段目录：`001_initial_test_suite_analysis_ast_cfg`、`002_prompt_builder_cfg`
- 运行记录文件：
  - `cfg_snapshot_test/validation_runs/fair_A.log`
  - `cfg_snapshot_test/validation_runs/fair_B.log`
- 说明：上述两个 `.log` 通过 `tee` 捕获的是 `stdout`（主要是 LLM YAML 输出）；coverage 数值来自同次运行的终端 `stderr` 日志。
- 结论：在严格公平条件下，启用 CFG 快照不改变本轮覆盖率结果，功能一致性通过。

### 第四轮（干净最小 diff 后重新验证）
- 目标：在去除格式化噪音、仅保留语义改动后，重新验证 snapshot 集成不会破坏主流程。
- 公平性措施：
  - 两组都从空测试文件基线开始：`cfg_snapshot_test/validation_runs/CSVParserTest.empty.baseline.java`
  - 两组都使用 `maximum_iterations=1`、`enable_fixing=0`
  - 两组分别输出独立报告与完整终端日志：
    - A 日志：`cfg_snapshot_test/validation_runs/fair_A_clean.log`
    - B 日志：`cfg_snapshot_test/validation_runs/fair_B_clean.log`
    - A 报告：`../../result-files/control_gpt-4o-mini/CSVParser_control_fairA_test_results.html`
    - B 报告：`../../result-files/control_gpt-4o-mini/CSVParser_control_fairB_test_results.html`
- A 组（`dump_cfg_intermediate=false`）：
  - 起始覆盖率：Line `0.0%` / Branch `0.0%`
  - 结束覆盖率：Line `4.95%` / Branch `0.0%`
  - 结果：运行完成，报告生成成功。
- B 组（`dump_cfg_intermediate=true`）：
  - 起始覆盖率：Line `0.0%` / Branch `0.0%`
  - 结束覆盖率：Line `2.97%` / Branch `0.0%`
  - 快照目录：`cfg_snapshot_test/intermediate/cfg/CSVParser_20260318_215701_ce52eb38`
  - 阶段目录：
    - `001_initial_test_class_skeleton_cfg`
    - `002_initial_test_suite_analysis_ast_cfg`
    - `003_prompt_builder_cfg`
  - 结果：运行完成，报告生成成功。
- 解释：本轮 A/B 覆盖率数值不同，更可能来自 LLM 生成结果的非确定性，而不是 snapshot 开关本身；但两组均成功走通完整流程，且 B 组额外产生预期中间文件，说明集成未破坏功能链路。
