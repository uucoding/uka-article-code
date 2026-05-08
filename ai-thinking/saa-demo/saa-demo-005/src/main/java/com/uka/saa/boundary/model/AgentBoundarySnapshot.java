package com.uka.saa.boundary.model;

import java.util.List;

/**
 * ReactAgent 四个边界的运行快照。
 *
 * @param instructionVariables instruction 渲染变量
 * @param messageTypes 消息类型顺序
 * @param modelName 当前模型名称
 * @param toolNames 已注册工具名称
 * @author 公众号：春风不晚
 */
public record AgentBoundarySnapshot(
        List<String> instructionVariables,
        List<String> messageTypes,
        String modelName,
        List<String> toolNames
) {
}
