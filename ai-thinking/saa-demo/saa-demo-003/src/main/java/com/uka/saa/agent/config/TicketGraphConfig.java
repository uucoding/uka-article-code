package com.uka.saa.agent.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.Map;

/**
 * 第三讲中的 Graph 版本配置。
 * 该配置定义一条固定的工单分发图。
 * 图中的节点表示执行步骤，边表示节点之间的流转关系。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class TicketGraphConfig {

    /**
     * 图里会反复用到的状态字段名。
     * 这些字段会写入和读取 Graph 的共享状态。
     */
    private static final String STATE_TICKET_DESCRIPTION = "ticketDescription";

    private static final String STATE_CATEGORY = "category";

    private static final String STATE_TARGET_GROUP = "targetGroup";

    /**
     * 工单分类值。
     */
    private static final String CATEGORY_NETWORK_FAULT = "NETWORK_FAULT";

    private static final String CATEGORY_ACCOUNT_PERMISSION = "ACCOUNT_PERMISSION";

    private static final String CATEGORY_OTHER = "OTHER";

    /**
     * 分发目标组。
     */
    private static final String GROUP_NETWORK = "网络组";

    private static final String GROUP_ACCOUNT = "账号组";

    private static final String GROUP_SERVICE = "服务台";

    /**
     * 条件边路由标记。
     */
    private static final String ROUTE_NETWORK = "ROUTE_NETWORK";

    private static final String ROUTE_ACCOUNT = "ROUTE_ACCOUNT";

    private static final String ROUTE_OTHER = "ROUTE_OTHER";

    /**
     * 构建工单分发流程图。
     * 该图包含四类动作：
     * START -> receive -> classify -> dispatchX -> END
     *
     * @return 编译后的图对象
     * @throws Exception Graph 编译时可能抛出的异常
     * @author 公众号：春风不晚
     */
    @Bean("ticketDispatchGraph")
    public CompiledGraph ticketDispatchGraph() throws Exception {
        // 第二个参数是状态字段的更新策略。
        // 这里全部使用 REPLACE，表示同名状态字段每次都用新值覆盖旧值。
        StateGraph graph = new StateGraph(
                "ticket-dispatch-graph",
                () -> Map.of(
                        STATE_TICKET_DESCRIPTION, KeyStrategy.REPLACE,
                        STATE_CATEGORY, KeyStrategy.REPLACE,
                        STATE_TARGET_GROUP, KeyStrategy.REPLACE
                )
        );

        // 每个节点只负责一个独立动作。
        graph.addNode("receive", AsyncNodeAction.node_async(this::receiveTicket));
        graph.addNode("classify", AsyncNodeAction.node_async(this::classifyTicket));
        graph.addNode("dispatchNetwork", AsyncNodeAction.node_async(this::dispatchToNetworkGroup));
        graph.addNode("dispatchAccount", AsyncNodeAction.node_async(this::dispatchToAccountGroup));
        graph.addNode("dispatchOther", AsyncNodeAction.node_async(this::dispatchToServiceDesk));

        // 显式声明图的入口节点和顺序边。
        graph.addEdge(StateGraph.START, "receive");
        graph.addEdge("receive", "classify");

        // classify 节点执行完成后，根据分类结果选择下一条分支边。
        // 挂载动态路由边：依靠状态字段 (category) 进行系统级分发
        graph.addConditionalEdges(
                "classify",
                AsyncEdgeAction.edge_async(this::routeByCategory),
                Map.of(
                        ROUTE_NETWORK, "dispatchNetwork",
                        ROUTE_ACCOUNT, "dispatchAccount",
                        ROUTE_OTHER, "dispatchOther"
                )
        );

        // 指定汇聚终点，防止图节点无限逸出
        graph.addEdge("dispatchNetwork", StateGraph.END);
        graph.addEdge("dispatchAccount", StateGraph.END);
        graph.addEdge("dispatchOther", StateGraph.END);

        // compile 会把 StateGraph 编译成可执行的 CompiledGraph。
        return graph.compile(
                CompileConfig.builder()
                        .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                        .build()
        );
    }

    /**
     * receive 节点。
     * 该节点表示流程开始后的接收动作，本例中不额外写入状态。
     *
     * @param state 图的全局状态
     * @return 空更新
     * @author 公众号：春风不晚
     */
    public Map<String, Object> receiveTicket(com.alibaba.cloud.ai.graph.OverAllState state) {
        return Map.of();
    }

    /**
     * classify 节点。
     * 该节点根据工单文本做关键词分类，并把分类结果写入状态。
     *
     * @param state 图的全局状态
     * @return 分类结果
     * @author 公众号：春风不晚
     */
    public Map<String, Object> classifyTicket(com.alibaba.cloud.ai.graph.OverAllState state) {
        String description = normalize(state.value(STATE_TICKET_DESCRIPTION, ""));
        if (description.contains("网络") || description.contains("断网")) {
            return Map.of(STATE_CATEGORY, CATEGORY_NETWORK_FAULT);
        }
        if (description.contains("账号") || description.contains("密码") || description.contains("登录")) {
            return Map.of(STATE_CATEGORY, CATEGORY_ACCOUNT_PERMISSION);
        }
        return Map.of(STATE_CATEGORY, CATEGORY_OTHER);
    }

    /**
     * 路由函数。
     * 返回值不是业务结果，而是条件边要匹配的路由标记。
     *
     * @param state 图的全局状态
     * @return 路由标记
     * @author 公众号：春风不晚
     */
    public String routeByCategory(com.alibaba.cloud.ai.graph.OverAllState state) {
        String category = state.value(STATE_CATEGORY, CATEGORY_OTHER);
        if (CATEGORY_NETWORK_FAULT.equals(category)) {
            return ROUTE_NETWORK;
        }
        if (CATEGORY_ACCOUNT_PERMISSION.equals(category)) {
            return ROUTE_ACCOUNT;
        }
        return ROUTE_OTHER;
    }

    /**
     * 把工单分发给网络组。
     *
     * @param state 图的全局状态
     * @return 状态更新
     * @author 公众号：春风不晚
     */
    public Map<String, Object> dispatchToNetworkGroup(com.alibaba.cloud.ai.graph.OverAllState state) {
        return Map.of(STATE_TARGET_GROUP, GROUP_NETWORK);
    }

    /**
     * 把工单分发给账号组。
     *
     * @param state 图的全局状态
     * @return 状态更新
     * @author 公众号：春风不晚
     */
    public Map<String, Object> dispatchToAccountGroup(com.alibaba.cloud.ai.graph.OverAllState state) {
        return Map.of(STATE_TARGET_GROUP, GROUP_ACCOUNT);
    }

    /**
     * 其他问题统一交给服务台。
     *
     * @param state 图的全局状态
     * @return 状态更新
     * @author 公众号：春风不晚
     */
    public Map<String, Object> dispatchToServiceDesk(com.alibaba.cloud.ai.graph.OverAllState state) {
        return Map.of(STATE_TARGET_GROUP, GROUP_SERVICE);
    }

    /**
     * 字符串归一化。
     * 该方法统一处理空值、空格和大小写差异。
     *
     * @param text 原始文本
     * @return 归一化后的文本
     * @author 公众号：春风不晚
     */
    public String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
