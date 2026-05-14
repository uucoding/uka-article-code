package com.uka.saa.demo.model;

import java.util.List;

/**
 * 单 Agent 职责边界分析结果。
 *
 * @param shouldSplit 是否建议拆成多 Agent
 * @param recommendedMode 推荐的多 Agent 编排形态
 * @param complexityScore 复杂度分数
 * @param signals 触发拆分的具体信号
 * @param suggestedAgents 建议拆出的职责单元
 * @author 公众号：春风不晚
 */
public record SingleAgentSplitPlan(
        boolean shouldSplit,
        AgentWorkflowMode recommendedMode,
        int complexityScore,
        List<String> signals,
        List<String> suggestedAgents
) {

    public SingleAgentSplitPlan {
        // 输出结果同样保持不可变，确保测试和调用方拿到的是稳定的分析快照。
        signals = List.copyOf(signals == null ? List.of() : signals);
        suggestedAgents = List.copyOf(suggestedAgents == null ? List.of() : suggestedAgents);
    }

}
