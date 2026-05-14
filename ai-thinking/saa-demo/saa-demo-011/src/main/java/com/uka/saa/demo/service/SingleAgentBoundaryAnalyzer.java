package com.uka.saa.demo.service;

import com.uka.saa.demo.model.AgentWorkflowMode;
import com.uka.saa.demo.model.SingleAgentSplitPlan;
import com.uka.saa.demo.model.SingleAgentTaskRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 单 Agent 职责边界分析器。
 * 课程 Demo 使用确定性规则，把“什么时候该拆多 Agent”这件事先讲清楚。
 *
 * @author 公众号：春风不晚
 */
@Service
public class SingleAgentBoundaryAnalyzer {

    /**
     * 分析一个任务是否已经超过单 Agent 的稳定职责边界。
     *
     * @param request 任务画像
     * @return 拆分建议
     */
    public SingleAgentSplitPlan analyze(SingleAgentTaskRequest request) {
        // 先收集任务画像中的拆分信号，再根据规则选择最合适的编排模式。
        List<String> signals = collectSignals(request);
        AgentWorkflowMode mode = chooseMode(request, signals);
        boolean shouldSplit = mode != AgentWorkflowMode.SINGLE_AGENT;

        return new SingleAgentSplitPlan(
                shouldSplit,
                mode,
                calculateComplexityScore(request, signals),
                signals,
                shouldSplit ? suggestAgents(request) : List.of("single_agent")
        );
    }

    private List<String> collectSignals(SingleAgentTaskRequest request) {
        List<String> signals = new ArrayList<>();

        // 目标、工具、输出数量过多，说明单个 Agent 的职责边界开始变宽。
        if (request.goals().size() > 2) {
            signals.add("目标混杂：一个 Agent 同时承担多个不同目标");
        }
        if (request.tools().size() > 3) {
            signals.add("工具过多：工具的调用时机、风险和输入边界开始混在一起");
        }
        if (request.outputs().size() > 2) {
            signals.add("输出责任混杂：同一轮结果要服务多个下游消费者");
        }

        // 流程、并行、路由和风险信号，决定后续更适合哪种多 Agent 协作形态。
        if (request.orderedSteps()) {
            signals.add("流程依赖：后一步必须等待前一步产出");
        }
        if (request.independentBranches()) {
            signals.add("可并行子任务：多个分支没有强依赖，适合拆开执行");
        }
        if (request.dynamicRouting()) {
            signals.add("入口分流：需要先判断任务类型，再交给专门 Agent");
        }
        if (request.highRiskAction()) {
            signals.add("高风险动作：执行前需要独立审查或人工确认");
        }

        return signals;
    }

    private AgentWorkflowMode chooseMode(SingleAgentTaskRequest request, List<String> signals) {
        // 信号不足两个且没有高风险动作时，继续使用单 Agent 更简单。
        if (signals.size() < 2 && !request.highRiskAction()) {
            return AgentWorkflowMode.SINGLE_AGENT;
        }
        // 优先处理会明显改变编排方式的结构性信号。
        if (request.dynamicRouting()) {
            return AgentWorkflowMode.LLM_ROUTING_AGENT;
        }
        if (request.independentBranches()) {
            return AgentWorkflowMode.PARALLEL_AGENT;
        }
        if (request.orderedSteps()) {
            return AgentWorkflowMode.SEQUENTIAL_AGENT;
        }
        if (request.highRiskAction()) {
            return AgentWorkflowMode.SUPERVISOR_AGENT;
        }
        // 工具数量过多时，可先拆成“Agent 调工具型”职责结构。
        if (request.tools().size() > 3) {
            return AgentWorkflowMode.AGENT_TOOL;
        }
        // 其余复杂但没有固定流程特征的任务，适合通过交接给专门 Agent 处理。
        return AgentWorkflowMode.HANDOFF;
    }

    private int calculateComplexityScore(SingleAgentTaskRequest request, List<String> signals) {
        // 分数只用于演示：信号是主权重，目标、工具、输出数量作为补充权重。
        int score = signals.size() * 10;
        score += Math.max(0, request.goals().size() - 1) * 2;
        score += Math.max(0, request.tools().size() - 1) * 2;
        score += Math.max(0, request.outputs().size() - 1) * 2;
        return score;
    }

    private List<String> suggestAgents(SingleAgentTaskRequest request) {
        List<String> agents = new ArrayList<>();

        // 建议的 Agent 名称对应课程里的职责拆分，不绑定具体框架实现。
        if (!request.goals().isEmpty() || request.orderedSteps()) {
            agents.add("planning_agent");
        }
        if (!request.tools().isEmpty()) {
            agents.add("tool_execution_agent");
        }
        if (!request.outputs().isEmpty()) {
            agents.add("result_writer_agent");
        }
        if (request.highRiskAction()) {
            agents.add("review_agent");
        }
        if (request.dynamicRouting()) {
            agents.add("routing_agent");
        }
        if (request.independentBranches()) {
            agents.add("parallel_worker_agent");
        }

        // 多个规则可能给出同一个 Agent，最终结果去重后保持首次出现顺序。
        return agents.stream().distinct().toList();
    }

}
