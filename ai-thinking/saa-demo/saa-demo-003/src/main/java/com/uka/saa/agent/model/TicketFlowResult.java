package com.uka.saa.agent.model;

import java.util.List;

/**
 * StateGraph 流程编排结果对象。
 * category 保存图中的分类结果，
 * targetGroup 保存最终分发目标，
 * trace 保存节点执行轨迹。
 *
 * @param category 工单分类
 * @param targetGroup 目标处理小组
 * @param trace 执行轨迹
 * @author 公众号：春风不晚
 */
public record TicketFlowResult(
        String category,
        String targetGroup,
        List<String> trace) {
}
