package com.uka.saa.writer.model;

/**
 * 写作请求。
 *
 * @param topic 主题
 * @param targetReader 目标读者
 * @param style 写作风格
 * @param wordCount 目标字数
 * @author 公众号：春风不晚
 */
public record WritingRequest(
        String topic,
        String targetReader,
        String style,
        Integer wordCount
) {
}
