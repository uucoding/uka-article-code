package com.uka.saa.demo.agent;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.internal.node.Node;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async;

/**
 * report_generation 条件分支。
 * 只返回确定性结果，用来验证内置 ConditionalGraphBuildingStrategy 进入了正确分支。
 *
 * @author 公众号：春风不晚
 */
public class ReportGenerationAgent extends BaseAgent {

    public static final String OUTPUT_KEY = "report_generation_result";

    public ReportGenerationAgent() {
        super("report_generation_agent", "生成报告草案", false, false, OUTPUT_KEY, KeyStrategy.REPLACE);
    }

    @Override
    protected StateGraph initGraph() throws GraphStateException {
        StateGraph graph = new StateGraph(name(), KeyStrategy.builder()
                .addStrategy("input")
                .addStrategy(OUTPUT_KEY)
                .build());
        graph.addNode(name(), node_async(this::generate));
        graph.addEdge(START, name());
        graph.addEdge(name(), END);
        return graph;
    }

    @Override
    public Node asNode(boolean includeContents, boolean returnReasoningContents) {
        return new Node(name(), ignored -> node_async(this::generate));
    }

    private Map<String, Object> generate(OverAllState state, RunnableConfig config) {
        String input = state.value("input", "");
        return Map.of(
                OUTPUT_KEY, "报告草案：围绕「" + input + "」整理背景、核心结论和下一步建议。"
        );
    }

}
