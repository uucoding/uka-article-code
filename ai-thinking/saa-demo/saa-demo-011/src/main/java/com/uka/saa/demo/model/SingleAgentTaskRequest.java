package com.uka.saa.demo.model;

import java.util.List;

/**
 * 待评估的单 Agent 任务画像。
 *
 * @param name 任务名称
 * @param goals 任务目标
 * @param tools Agent 需要调度的工具
 * @param outputs 下游系统需要消费的输出
 * @param orderedSteps 是否存在明确前后依赖
 * @param independentBranches 是否存在可以并行处理的独立子任务
 * @param dynamicRouting 是否需要先判断入口再分流
 * @param highRiskAction 是否包含发布、删除、转账等高风险动作
 * @author 公众号：春风不晚
 */
public record SingleAgentTaskRequest(
        String name,
        List<String> goals,
        List<String> tools,
        List<String> outputs,
        boolean orderedSteps,
        boolean independentBranches,
        boolean dynamicRouting,
        boolean highRiskAction
) {

    public SingleAgentTaskRequest {
        goals = List.copyOf(goals == null ? List.of() : goals);
        tools = List.copyOf(tools == null ? List.of() : tools);
        outputs = List.copyOf(outputs == null ? List.of() : outputs);
    }

}
