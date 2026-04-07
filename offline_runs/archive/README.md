# Archive

这个目录用于存放所有不再保留在 `offline_runs/` 顶层的一次性脚本、历史 wrapper 和历史辅助工具。

按当前规则，除了 `offline_runs/run_batch.sh` 之外，其余 shell wrapper 原则上都应进入这里保存。

## 归档规则

以下内容应进入 archive：

- 一次性实验脚本
- repeat / replay / continuation 脚本
- 依赖特定旧 baseline 或旧 `smoke_test_log/` 路径的脚本
- 没有被其他脚本调用的辅助工具
- 不能通过加参数继续复用的 wrapper

## 当前历史辅助工具

- `run_class_list_batch.py`
  - 旧的 class-list 选择器辅助脚本。
  - 会从 `evaluation/data/class_list.csv` 和 `evaluation/defects4j-codefiles/` 中解析路径，再调用 `offline_runs/run_batch.sh`。
  - 目前已归档，后续如果需要类似能力，建议重新写一个职责更单一的路径解析工具。

## 使用说明

- 归档文件主要用于保留历史参数和调用方式。
- archive 中的脚本通常默认它们原本位于 `offline_runs/` 顶层。
- 如果要再次运行某个归档脚本，请先参考顶层 `README.md` 中关于路径定位和归档脚本复现方式的说明。
