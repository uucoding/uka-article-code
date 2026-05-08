package com.uka.saa.boundary.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 工具集合。
 *
 * @author 公众号：春风不晚
 */
@Component
public class WritingBoundaryTools {

    /**
     * 读取发布平台的写作规则。
     *
     * @param publishingPlatform 发布平台
     * @return 平台规则
     * @author 公众号：春风不晚
     */
    @Tool(name = "load_platform_rule", description = "读取指定发布平台的写作规则")
    public String loadPlatformRule(
            @ToolParam(description = "发布平台，例如微信公众号、技术博客、内部知识库") String publishingPlatform) {
        // 1. 工具返回的是外部能力结果，不应该再混进 Agent 的长期 systemPrompt。
        return switch (publishingPlatform) {
            case "技术博客" -> """
                    技术博客规则：
                    1. 标题要直接表达技术判断
                    2. 正文必须包含问题背景、方案拆解、代码验证
                    3. 结尾要给出可复用的工程原则
                    """;
            case "内部知识库" -> """
                    内部知识库规则：
                    1. 先给结论，再给背景
                    2. 步骤要可执行，避免情绪化表达
                    3. 必须写清适用范围和风险边界
                    """;
            default -> """
                    微信公众号规则：
                    1. 开头 3 句内必须打透痛点
                    2. 小标题要体现判断，不要只有目录感
                    3. 结尾要留下下一步问题
                    """;
        };
    }

    /**
     * 读取本轮聚焦点对应的写作提醒。
     *
     * @param focusPoint 本轮聚焦点
     * @return 写作提醒
     * @author 公众号：春风不晚
     */
    @Tool(name = "load_focus_guardrail", description = "读取本轮聚焦点对应的写作提醒")
    public String loadFocusGuardrail(
            @ToolParam(description = "本轮写作聚焦点") String focusPoint) {
        // 1. 这里模拟外部规范库，后续会展开 Tool / Function / Skills。
        return """
                聚焦点《%s》的写作提醒：
                1. 不要把所有概念平铺成 API 清单
                2. 必须解释这个边界解决了什么工程问题
                3. 必须用一个可运行的小闭环验证判断
                """.formatted(focusPoint);
    }

}
