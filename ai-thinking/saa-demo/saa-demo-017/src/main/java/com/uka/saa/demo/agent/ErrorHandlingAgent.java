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
 * error_handling 条件分支。
 * 只返回确定性结果，用来验证内置 ConditionalGraphBuildingStrategy 进入了正确分支。
 *
 * @author 公众号：春风不晚
 */
public class ErrorHandlingAgent extends BaseAgent {

    public static final String OUTPUT_KEY = "error_handling_result";

    public ErrorHandlingAgent() {
        super("error_handling_agent", "处理错误场景", false, false, OUTPUT_KEY, KeyStrategy.REPLACE);
    }

    @Override
    protected StateGraph initGraph() throws GraphStateException {
        StateGraph graph = new StateGraph(name(), KeyStrategy.builder()
                .addStrategy("input")
                .addStrategy(OUTPUT_KEY)
                .build());
        graph.addNode(name(), node_async(this::handle));
        graph.addEdge(START, name());
        graph.addEdge(name(), END);
        return graph;
    }

    @Override
    public Node asNode(boolean includeContents, boolean returnReasoningContents) {
        return new Node(name(), ignored -> node_async(this::handle));
    }

    private Map<String, Object> handle(OverAllState state, RunnableConfig config) {
        String input = state.value("input", "");
        return Map.of(
                OUTPUT_KEY, "错误处理：已识别「" + input + "」中的异常线索，建议先定位原因再继续写作流程。"
        );
    }

}
