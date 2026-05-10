package com.uka.saa.demo.hook;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Hook 示例：负责 Agent 生命周期审计。
 *
 * @author 公众号：春风不晚
 */
public class WritingLifecycleHook extends AgentHook {

    public static final String TRACE_KEY = "agentTrace";

    @Override
    public String getName() {
        return "writing_lifecycle";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        String requestId = state.value("requestId", "req-unknown");
        String tenantId = state.value("tenantId", "tenant-unknown");
        String riskLevel = state.value("riskLevel", "low");

        // 1. beforeAgent 适合记录链路开始状态。
        String trace = "beforeAgent: requestId=%s, tenantId=%s, riskLevel=%s".formatted(
                requestId, tenantId, riskLevel);
        System.out.println(trace);

        return CompletableFuture.completedFuture(Map.of(
                TRACE_KEY, List.of(trace),
                "agentStartedAt", Instant.now().toString()
        ));
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        String requestId = state.value("requestId", "req-unknown");

        // 2. afterAgent 适合记录链路结束状态。
        String trace = "afterAgent: requestId=%s, finished=true".formatted(requestId);
        System.out.println(trace);

        return CompletableFuture.completedFuture(Map.of(
                TRACE_KEY, List.of(trace),
                "agentFinishedAt", Instant.now().toString()
        ));
    }

    @Override
    public Map<String, KeyStrategy> getKeyStrategys() {
        // 3. 轨迹要追加，不要被 afterAgent 覆盖。
        return Map.of(TRACE_KEY, new AppendStrategy());
    }

}
