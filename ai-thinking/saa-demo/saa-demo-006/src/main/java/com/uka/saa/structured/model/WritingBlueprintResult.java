package com.uka.saa.structured.model;

import java.util.List;

/**
 * 第 6 讲返回结果。
 *
 * @param blueprint 结构化写作蓝图
 * @param rawContent 模型原始 JSON 文本
 * @param jsonSchema 当前 DTO 对应的 JSON Schema
 * @param trace 执行轨迹
 * @author 公众号：春风不晚
 */
public record WritingBlueprintResult(
        WritingBlueprint blueprint,
        String rawContent,
        String jsonSchema,
        List<String> trace
) {
}
