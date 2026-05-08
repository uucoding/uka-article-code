package com.uka.saa.structured.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 工具集合。
 *
 * @author 公众号：春风不晚
 */
@Component
public class StructuredWritingTools {

    /**
     * 生成标题候选。
     *
     * @param workingTitle 工作标题
     * @param coreThesis 核心立场
     * @param publishingPlatform 发布平台
     * @return 标题候选
     * @author 公众号：春风不晚
     */
    @Tool(name = "generate_title_candidates", description = "根据写作蓝图生成标题候选")
    public String generateTitleCandidates(
            @ToolParam(description = "写作计划中的工作标题") String workingTitle,
            @ToolParam(description = "写作计划中的核心立场") String coreThesis,
            @ToolParam(description = "发布平台") String publishingPlatform) {
        // 1. 标题候选属于可复用外围资产，不应该散落在正文自然语言里。
        return """
                推荐标题：拒绝让 AI 返回一段散文，底层透视结构化输出怎么落地
                候选标题：
                1. 告别不可控文本：Spring AI Alibaba 结构化输出实战
                2. 别让 Agent 只会写文章：让它返回业务 DTO
                3. 从自然语言到业务对象：ReactAgent 输出边界拆解
                生成依据：%s；核心立场：%s；发布平台：%s
                """.formatted(workingTitle, coreThesis, publishingPlatform);
    }

    /**
     * 生成摘要。
     *
     * @param topicJudgment 选题判断
     * @param coreThesis 核心立场
     * @return 摘要
     * @author 公众号：春风不晚
     */
    @Tool(name = "generate_summary", description = "根据写作蓝图生成摘要")
    public String generateSummary(
            @ToolParam(description = "选题判断") String topicJudgment,
            @ToolParam(description = "核心立场") String coreThesis) {
        // 1. 摘要先给判断，再给价值，避免变成空泛介绍。
        return "本文先说明为什么自然语言结果难以治理，再通过 "
                + coreThesis
                + " 展示如何把 Agent 输出收束成可校验、可入库、可继续编排的业务 DTO。选题判断："
                + topicJudgment;
    }

    /**
     * 生成关键词。
     *
     * @param topic 主题
     * @param focusPoint 聚焦点
     * @return 关键词
     * @author 公众号：春风不晚
     */
    @Tool(name = "generate_keywords", description = "根据主题与聚焦点生成关键词")
    public String generateKeywords(
            @ToolParam(description = "文章主题") String topic,
            @ToolParam(description = "本轮聚焦点") String focusPoint) {
        // 1. 关键词兼顾主题词、框架词和读者搜索词。
        return "Spring AI Alibaba, ReactAgent, 结构化输出, outputType, BeanOutputConverter, "
                + topic
                + ", "
                + focusPoint;
    }

}
