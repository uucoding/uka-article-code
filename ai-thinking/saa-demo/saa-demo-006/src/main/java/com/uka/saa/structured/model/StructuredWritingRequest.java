package com.uka.saa.structured.model;

import org.springframework.util.StringUtils;

/**
 * 请求对象。
 * 本讲只让 Agent 生成写作蓝图，不直接进入正文生产。
 *
 * @param topic 文章主题
 * @param targetReader 目标读者
 * @param style 写作风格
 * @param publishingPlatform 发布平台
 * @param focusPoint 本轮聚焦点
 * @param wordCount 目标字数
 * @param currentDraft 已有草稿
 * @param userComment 用户补充说明
 * @author 公众号：春风不晚
 */
public record StructuredWritingRequest(
        String topic,
        String targetReader,
        String style,
        String publishingPlatform,
        String focusPoint,
        Integer wordCount,
        String currentDraft,
        String userComment
) {

    public String normalizedTopic() {
        // 1. 主题为空时给出稳定默认值，避免结构化结果失去主轴。
        return StringUtils.hasText(topic) ? topic.trim() : "Spring AI Alibaba 结构化输出";
    }

    public String normalizedTargetReader() {
        // 1. 读者定位直接影响大纲深度和示例颗粒度。
        return StringUtils.hasText(targetReader) ? targetReader.trim() : "有 Spring Boot 经验的 Java 后端开发者";
    }

    public String normalizedStyle() {
        // 1. 风格是运行时变量，不应该硬编码进 Agent 长期角色。
        return StringUtils.hasText(style) ? style.trim() : "技术深度清晰、表达克制、有工程判断";
    }

    public String normalizedPublishingPlatform() {
        // 1. 发布平台用于指导标题、摘要和关键词资产。
        return StringUtils.hasText(publishingPlatform) ? publishingPlatform.trim() : "微信公众号";
    }

    public String normalizedFocusPoint() {
        // 1. 聚焦点用于 instruction 模板，收紧本轮结构化结果。
        return StringUtils.hasText(focusPoint) ? focusPoint.trim() : "让 Agent 返回可校验的业务 DTO";
    }

    public int normalizedWordCount() {
        // 1. 字数默认值保持保守，避免计划章节过重。
        return wordCount == null || wordCount <= 0 ? 1500 : wordCount;
    }

    public boolean hasCurrentDraft() {
        // 1. 有草稿时，Agent 应该判断是否进入 rewrite 或 polish，而不是默认 write_new。
        return StringUtils.hasText(currentDraft);
    }

    public String normalizedCurrentDraft() {
        // 1. 草稿只作为意图判断依据，正文生成留到后续实战篇。
        return StringUtils.hasText(currentDraft) ? currentDraft.trim() : "暂无草稿";
    }

    public String normalizedUserComment() {
        // 1. 用户补充说明用于判断本轮意图，缺失时给出稳定占位。
        return StringUtils.hasText(userComment) ? userComment.trim() : "请先生成可执行的写作蓝图。";
    }

}
