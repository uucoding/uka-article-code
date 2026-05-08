package com.uka.saa.boundary.model;

import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 请求对象。
 *
 * @param topic 文章主题
 * @param targetReader 目标读者
 * @param writingStage 当前写作阶段
 * @param publishingPlatform 发布平台
 * @param focusPoint 本轮聚焦点
 * @param currentDraft 上一版草稿
 * @param revisionComment 修改意见
 * @param threadId 线程标识
 * @author 公众号：春风不晚
 */
public record AgentBoundaryRequest(
        String topic,
        String targetReader,
        String writingStage,
        String publishingPlatform,
        String focusPoint,
        String currentDraft,
        String revisionComment,
        String threadId
) {

    /**
     * 归一化主题。
     *
     * @return 主题
     * @author 公众号：春风不晚
     */
    public String normalizedTopic() {
        // 1. 主题为空时给出稳定默认值，避免模型收到空目标后自由发散。
        return StringUtils.hasText(topic) ? topic.trim() : "Spring AI Alibaba Agent 边界设计";
    }

    /**
     * 归一化目标读者。
     *
     * @return 目标读者
     * @author 公众号：春风不晚
     */
    public String normalizedTargetReader() {
        // 1. 默认读者锚定 Java 后端开发者，保证场景不漂移。
        return StringUtils.hasText(targetReader) ? targetReader.trim() : "有 Spring Boot 经验的 Java 后端开发者";
    }

    /**
     * 归一化写作阶段。
     *
     * @return 写作阶段
     * @author 公众号：春风不晚
     */
    public String normalizedWritingStage() {
        // 1. 第 5 讲只区分首稿与改稿，后续复杂意图识别留给后续章节
        return StringUtils.hasText(writingStage) ? writingStage.trim() : "first_draft";
    }

    /**
     * 归一化发布平台。
     *
     * @return 发布平台
     * @author 公众号：春风不晚
     */
    public String normalizedPublishingPlatform() {
        // 1. 发布平台属于运行时变量，不应该写死在 systemPrompt 里。
        return StringUtils.hasText(publishingPlatform) ? publishingPlatform.trim() : "微信公众号";
    }

    /**
     * 归一化聚焦点。
     *
     * @return 聚焦点
     * @author 公众号：春风不晚
     */
    public String normalizedFocusPoint() {
        // 1. 聚焦点用于 instruction 模板渲染，帮助 Agent 收紧本轮判断。
        return StringUtils.hasText(focusPoint) ? focusPoint.trim() : "讲清 ReactAgent 的四个工程边界";
    }

    /**
     * 判断是否存在上一版草稿。
     *
     * @return true 表示存在
     * @author 公众号：春风不晚
     */
    public boolean hasCurrentDraft() {
        // 1. 有上一版草稿时，服务层会把它作为 AssistantMessage 回放。
        return StringUtils.hasText(currentDraft);
    }

    /**
     * 判断是否存在修改意见。
     *
     * @return true 表示存在
     * @author 公众号：春风不晚
     */
    public boolean hasRevisionComment() {
        // 1. 修改意见始终作为新的 UserMessage 进入消息流，而不是拼进旧草稿。
        return StringUtils.hasText(revisionComment);
    }

    /**
     * 归一化上一版草稿。
     *
     * @return 草稿文本
     * @author 公众号：春风不晚
     */
    public String normalizedCurrentDraft() {
        // 1. 去掉首尾空白，避免历史消息携带无意义噪音。
        return StringUtils.hasText(currentDraft) ? currentDraft.trim() : "";
    }

    /**
     * 归一化修改意见。
     *
     * @return 修改意见
     * @author 公众号：春风不晚
     */
    public String normalizedRevisionComment() {
        // 1. 缺少修改意见时给出保守默认动作：收紧结构，而不是重写主题。
        return StringUtils.hasText(revisionComment) ? revisionComment.trim() : "请让结构更清晰，减少空泛表达。";
    }

    /**
     * 解析线程标识。
     *
     * @return threadId
     * @author 公众号：春风不晚
     */
    public String resolveThreadId() {
        // 1. threadId 是后续记忆与状态追踪的锚点，第 5 讲先留个入口。
        return StringUtils.hasText(threadId) ? threadId.trim() : UUID.randomUUID().toString();
    }

}
