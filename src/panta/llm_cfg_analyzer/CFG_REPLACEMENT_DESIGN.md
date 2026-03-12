# 严格的 `cfg` 模块替换工程说明书

本文档是针对 `src/panta/cfg` 模块的替换工程技术说明。剔除所有非技术描述，专注于**调用链路、数据格式约定、接口规范以及具体的重构落地方案**。

---

## 1. 原 `cfg` 模块在项目中的具体调用位置及作用

原 `cfg` 模块（底层为 `comex`）在 Panta 主流程中被 4 个核心文件调用。我们的重构必须保证这 4 个地方的输入输出接口平滑过渡：

### 1.1 `src/panta/panta.py`
*   **调用位置**: `initial_test_class_skeleton()`
*   **用途**: 当目标测试文件不存在时，解析**源代码文件**。
*   **依赖接口**: 
    *   `CFGDriver(language, src_code)`
    *   `cfg_driver.file_obj["imports"]`：提取源码的 import 语句，用于生成测试骨架。
    *   `line_number_to_node_id_mapping()`：获取 import 语句的具体行号。

### 1.2 `src/panta/unit_test_generator.py`
*   **调用位置**: `initial_test_suite_analysis_AST()`
*   **用途**: 解析**测试代码文件**。
*   **依赖接口**:
    *   `CFGDriver(language, test_code, {"test_code": True})`
    *   提取最后一个 import 语句的 AST 节点 ID，进而通过 mapping 查出插入新 import 的行号。
    *   提取最后一个 `@Test` 方法的 AST 节点 ID，查出插入新测试代码的行号及代码缩进空格数。

### 1.3 `src/panta/prompt_builder.py`
*   **调用位置**: `__init__()` 及 `extract_cfa_info_for_each_method_under_test()`
*   **用途**: 解析**源代码文件**，生成控制流引导（Control Flow Guided）的 Prompt。
*   **依赖接口**:
    *   `CombinedDriver(src_language, src_code)`
    *   `file_obj["class_objects"][0]["methods_under_test"]`：获取所有方法名、圈复杂度（`complexity`）。
    *   `paths` 数组：遍历方法的每条执行路径。
    *   `cfg_node_to_line`：将 Jacoco 报告中未覆盖的行号（Missed Lines）与 path 中的 AST 节点 ID 进行匹配，以挑选出最需要测试的路径。

### 1.4 `src/panta/symprompt.py`
*   **调用位置**: `__init__()` 及 `generate_focal_method_context()`
*   **用途**: 解析**源代码文件**，生成 SymPrompt。
*   **依赖接口**:
    *   同上依赖 `CombinedDriver` 获取方法和路径。
    *   **强依赖字段**: `path['method_calls_within_class']` 和 `path['method_calls_outside_class']`。如果这些字段缺失，该文件会直接抛出 `KeyError` 崩溃。

---

## 2. 原 `cfg` 模块提供的数据结构规范 (Schema)

为保证不破坏现有代码，新模块必须输出以下两个核心数据结构：

### 2.1 节点行号映射字典
*   `node_id_to_line_number`: `Dict[int, List[int]]` (例如 `{1001: [10, 11, 12]}`)
*   `line_number_to_node_id`: `Dict[int, Tuple[int, int]]` (例如 `{10: (10, 1001)}`)

### 2.2 `file_obj` 数据结构字典
必须严格包含以下层级，且字段名不可更改：

```json
{
  "imports": [
    {
      "id": 1001,
      "value": "import java.util.List;"
    }
  ],
  "class_objects": [
    {
      "class_declaration": {
        "id": 1002,
        "name": "Calculator",
        "value": "public class Calculator {"
      },
      "fields": [],
      "constructors": [],
      "methods_under_test": [
        {
          "method_declaration": {
            "id": 1003,
            "name": "add",
            "complexity": 2,
            "nodes": [1003] // 必须包含该方法对应的所有节点 ID（我们简化为方法级的单一 ID）
          },
          "paths": [
            {
              "path": [
                {
                  "id": 1004,
                  "statement": "if(a>0)",
                  "conditional": "True"
                }
              ],
              // 以下 4 个字段在原项目中被直接访问，必须初始化为空数组/None
              "true_branches": [],
              "catch_exception_at": null,
              "method_calls_within_class": [],
              "method_calls_outside_class": [],
              
              // 新增字段：用于绕过原项目中恶心的 AST 节点拼凑逻辑
              "_llm_precomputed_path_str": "when a > 0 is True returns: a + b;"
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 3. 新 `LLMCFGAnalyzer` 的接口与实现规范

新模块位于 `src/panta/llm_cfg_analyzer/llm_analyzer.py`。

### 3.1 接口定义
暴露与 `CombinedDriver` 类似的数据属性：
*   `analyzer = LLMCFGAnalyzer(src_language, src_code)`
*   `analyzer.file_obj`
*   `analyzer.node_id_to_line_number`
*   `analyzer.line_number_to_node_id`

### 3.2 实现思路
1. **纯正则提取 (`extract_deterministic_info`)**: 仅用于提取 `package`、`import` 语句以及匹配 `class_declaration`。这些信息足以满足 `panta.py` 创建测试骨架的需求。
2. **LLM 提取路径 (`analyze_with_llm`)**: 将带有行号的代码发送给 LLM，强制其返回包含 `start_line`、`end_line`、`complexity` 和 `paths` 的 JSON。
3. **数据组装 (`parse_llm_response`)**: 
   * 将 `start_line` 到 `end_line` 的所有行号绑定到该方法的虚拟 ID 上，以此解决 Jacoco 覆盖率行号映射的问题。
   * 严格补齐 `method_calls_within_class` 等防御性空字段。

---

## 4. 各文件对应的修改方案 (Build Mode Action Plan)

### 修改 1: 补齐 `llm_analyzer.py` 的数据结构
在伪装 `paths` 数组时，必须加上 `true_branches: []`, `catch_exception_at: None`, `method_calls_within_class: []`, `method_calls_outside_class: []`。

### 修改 2: `src/panta/prompt_builder.py` 逻辑简化
*   **删除**: 第 108-118 行左右通过遍历 `path_nodes` 拼接 `path_conditions_str` 的逻辑。
*   **替换**: 直接赋值 `path_conditions_str = path["_llm_precomputed_path_str"]`。

### 修改 3: `src/panta/unit_test_generator.py` 去 AST 化
*   **重写**: `initial_test_suite_analysis_AST()`。
*   **逻辑**: 放弃调用 CFG 解析测试文件。直接将 `test_code` 按行 `split('\n')`，倒序遍历寻找 `import ` 和 `@Test`，利用正则算出行号和缩进。

### 修改 4: 替换入口引用
*   在 `panta.py` 和 `symprompt.py` 中，移除 `from cfg.src.comex...`。
*   导入 `from .llm_cfg_analyzer.llm_analyzer import LLMCFGAnalyzer` 并实例化使用。

---

## 5. 关于 Windows 系统兼容性的补充说明

**澄清**：替换掉 `cfg` 模块，**只能**解决因 `tree-sitter` 的 C 语言编译依赖，以及 `cfg/src/comex/utils/timeout.py` 中使用了 Linux 专有信号量 `signal.SIGALRM` 导致的 Windows 崩溃问题。

**遗留问题**：原 Panta 在执行测试命令（`mvn test`）时，其 `src/panta/command_executor.py` 的超时处理中使用了 `os.killpg(os.getpgid(p.pid), signal.SIGTERM)`。这两个系统调用在 Windows 的 `os` 模块中不存在。
**解决方案**：如果在 Windows 上运行，仍需修改 `command_executor.py`，加入 `if os.name == 'nt': subprocess.call(['taskkill', '/F', '/T', '/PID', str(p.pid)])` 以实现跨平台兼容。