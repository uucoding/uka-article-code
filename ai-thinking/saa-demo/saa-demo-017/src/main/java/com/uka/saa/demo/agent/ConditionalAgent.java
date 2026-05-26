package com.uka.saa.demo.agent;

import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.FlowAgent;
import com.alibaba.cloud.ai.graph.agent.flow.builder.FlowGraphBuilder;
import com.alibaba.cloud.ai.graph.agent.flow.enums.FlowAgentEnum;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;

import java.util.List;
import java.util.Map;

/**
 * 第 17 讲最小条件 Agent。
 * 只演示一件事：把条件分支交给框架内置 ConditionalGraphBuildingStrategy。
 *
 * @author 公众号：春风不晚
 */
public class ConditionalAgent extends FlowAgent {

    public static final String CONDITION_RESULT_KEY = "_condition_result";

    public static final String ERROR_HANDLING = "error_handling";

    public static final String REPORT_GENERATION = "report_generation";

    private final Map<String, Agent> conditionalAgents;

    public ConditionalAgent(Map<String, Agent> conditionalAgents) {
        super("conditional_writing_agent", "根据条件结果选择不同写作分支", null,
                List.copyOf(conditionalAgents.values()));
        this.conditionalAgents = Map.copyOf(conditionalAgents);
    }

    @Override
    protected StateGraph buildSpecificGraph(FlowGraphBuilder.FlowGraphConfig config) throws GraphStateException {
        // 1. 不自己拼 StateGraph，直接把条件分支配置交给框架内置 CONDITIONAL 策略。
        config.setConditionalAgents(conditionalAgents);
        return FlowGraphBuilder.buildGraph(FlowAgentEnum.CONDITIONAL.getType(), config);
    }

}
