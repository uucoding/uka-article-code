---
name: wechat-writing-rules
description: Use when writing or revising Chinese technical articles for WeChat official accounts, especially when the article needs a clear opening, restrained technical tension, concrete examples, and platform-aware structure.
---

# wechat-writing-rules

## 适用场景

当用户要求生成、改写或审查微信公众号技术文章时使用本 Skill。典型任务包括文章开头、标题方向、段落节奏、课程型技术文章表达和写作素材检查。

## 执行步骤

1. 先判断用户是在做微信公众号技术写作，而不是普通技术问答。
2. 如果只需要写作规则，直接读取本 Skill 后按规则输出。
3. 如果用户要求检查本 Skill 携带了哪些参考资料，可以调用 shell 执行 `src/main/resources/skills/wechat-writing-rules/scripts/list_reference_files.sh`。
4. 写作时先给出明确判断，再给具体例子，不要把规则堆成空泛口号。
5. 最终输出应贴合用户给定主题，不要脱离主题复述本 Skill。

## 写作规则

- 开头要直接指出读者会遇到的问题，不要从大背景铺陈。
- 技术判断要具体，例如说明 Prompt、Tool、Hook、Memory、Skill 各自负责什么。
- 避免"赋能"、"抓手"、"闭环"这类空泛词。
- 减少连续列表，优先用短段落解释判断。
- 如果需要引用参考资料，先检查 `references` 目录里有什么，再决定是否使用。

## 输出约束

- 不要编造不存在的参考资料。
- 不要把 Skill 内容原样贴给用户。
- 如果调用脚本失败，直接说明失败原因。
