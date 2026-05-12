---
name: wechat-writing-rules
description: 在撰写或修改的文章时使用，特别是文章需要明确的开篇、克制的张力、具体的示例以及具备平台意识的结构。
---

# wechat-writing-rules

## 适用场景

当用户要求生成、改写或审查文章时使用本 Skill。典型任务包括文章开头、标题方向、段落节奏、课程型文章表达和写作素材检查。

## 执行步骤

1. 先判断用户是在做写作，而不是普通问答。
2. 文章写作规则，必须调用 shell 执行本 Skill 支持目录中的参考资料读取脚本。脚本会读取 `references` 目录下的文件内容，执行时使用可用 Skills 列表里给出的 supporting files 目录拼出绝对路径。macOS/Linux 环境使用 `sh <skill_path>/scripts/list_reference_files.sh`；Windows 环境使用 `powershell -ExecutionPolicy Bypass -File <skill_path>/scripts/list_reference_files.ps1`。
4. 写作时先给出明确判断，再给具体例子，不要把规则堆成空泛口号。
5. 最终输出应贴合用户给定主题，不要脱离主题复述本 Skill。

## 写作规则

通过脚本读取 `references` 目录下的内容然后应用。

## 输出约束

- 不要编造不存在的参考资料。
- 不要把 Skill 内容原样贴给用户。
- 如果调用脚本失败，直接说明失败原因。
