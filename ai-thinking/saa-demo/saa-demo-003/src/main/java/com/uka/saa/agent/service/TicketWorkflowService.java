package com.uka.saa.agent.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.uka.saa.agent.model.TicketFlowResult;
import com.uka.saa.agent.model.TicketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Graph 版本工单流程服务。
 * 该服务负责执行图、收集节点轨迹并从最终状态中提取结果。
 *
 * @author 公众号：春风不晚
 */
@Service
public class TicketWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(TicketWorkflowService.class);

    private static final String STATE_TICKET_DESCRIPTION = "ticketDescription";

    private static final String STATE_CATEGORY = "category";

    private static final String STATE_TARGET_GROUP = "targetGroup";

    private static final String CATEGORY_OTHER = "OTHER";

    private static final String GROUP_SERVICE = "服务台";

    /**
     * 编译后的 Graph。
     * 它内部已经包含了节点、边和状态更新规则。
     */
    private final CompiledGraph ticketDispatchGraph;

    public TicketWorkflowService(@Qualifier("ticketDispatchGraph") CompiledGraph ticketDispatchGraph) {
        this.ticketDispatchGraph = ticketDispatchGraph;
    }

    /**
     * 执行工单分发流程。
     * 该方法会创建图输入、执行整张图并汇总最终状态。
     *
     * @param request 工单请求
     * @return 流程结果
     * @author 公众号：春风不晚
     */
    public TicketFlowResult dispatch(TicketRequest request) {

        // 输入 Map 会作为 Graph 的初始状态。
        Map<String, Object> inputs = Map.of(
                STATE_TICKET_DESCRIPTION, request.description()
        );

        List<String> trace = new ArrayList<>();
        AtomicReference<OverAllState> finalState = new AtomicReference<>();

        // stream(...) 会按节点输出执行结果，便于收集完整轨迹。
        ticketDispatchGraph.stream(
                        inputs,
                        RunnableConfig.builder()
                                // 每次运行生成独立 threadId，避免 MemorySaver 复用历史状态。
                                .threadId(UUID.randomUUID().toString())
                                .build()
                )
                .toStream()
                .forEach(nodeOutput -> {
                    finalState.set(nodeOutput.state());
                    String message = formatTrace(nodeOutput);
                    trace.add(message);
                    log.info(message);
                });

        OverAllState state = finalState.get();
        if (state == null) {
            throw new IllegalStateException("Graph 未返回最终状态");
        }

        return new TicketFlowResult(
                state.value(STATE_CATEGORY, CATEGORY_OTHER),
                state.value(STATE_TARGET_GROUP, GROUP_SERVICE),
                List.copyOf(trace)
        );
    }

    /**
     * 把节点输出格式化为轨迹文本。
     *
     * @param nodeOutput 节点输出
     * @return 文本轨迹
     * @author 公众号：春风不晚
     */
    public String formatTrace(NodeOutput nodeOutput) {
        if (nodeOutput.isSTART()) {
            return "1. Graph 启动，从 START 进入流程";
        }
        if (nodeOutput.isEND()) {
            return "5. Graph 到达 END，流程结束";
        }

        OverAllState state = nodeOutput.state();
        return "节点 " + nodeOutput.node()
                + " 执行后: category=" + state.value(STATE_CATEGORY, "-")
                + ", targetGroup=" + state.value(STATE_TARGET_GROUP, "-");
    }
}
