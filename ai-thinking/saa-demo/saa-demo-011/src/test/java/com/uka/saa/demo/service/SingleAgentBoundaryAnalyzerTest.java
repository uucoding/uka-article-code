package com.uka.saa.demo.service;

import com.uka.saa.demo.model.AgentWorkflowMode;
import com.uka.saa.demo.model.SingleAgentSplitPlan;
import com.uka.saa.demo.model.SingleAgentTaskRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 11 讲单 Agent 职责边界测试。
 *
 * @author 公众号：春风不晚
 */
class SingleAgentBoundaryAnalyzerTest {

    private final SingleAgentBoundaryAnalyzer analyzer = new SingleAgentBoundaryAnalyzer();

    @Test
    void simpleTaskShouldStayInSingleAgent() {
        SingleAgentTaskRequest request = new SingleAgentTaskRequest(
                "生成一段文章摘要",
                List.of("总结输入内容"),
                List.of("read_context"),
                List.of("summary_text"),
                false,
                false,
                false,
                false
        );

        SingleAgentSplitPlan plan = analyzer.analyze(request);

        assertThat(plan.shouldSplit()).isFalse();
        assertThat(plan.recommendedMode()).isEqualTo(AgentWorkflowMode.SINGLE_AGENT);
        assertThat(plan.signals()).isEmpty();
        assertThat(plan.suggestedAgents()).containsExactly("single_agent");
    }

    @Test
    void orderedComplexTaskShouldUseSequentialAgent() {
        SingleAgentTaskRequest request = new SingleAgentTaskRequest(
                "生成可发布的长文",
                List.of("规划结构", "收集资料", "撰写正文", "审查风险"),
                List.of("read_skill", "search_private_knowledge", "search_public_web", "publish_content"),
                List.of("outline", "evidence_list", "draft", "review_result"),
                true,
                false,
                false,
                true
        );

        SingleAgentSplitPlan plan = analyzer.analyze(request);

        assertThat(plan.shouldSplit()).isTrue();
        assertThat(plan.recommendedMode()).isEqualTo(AgentWorkflowMode.SEQUENTIAL_AGENT);
        assertThat(plan.signals()).contains(
                "目标混杂：一个 Agent 同时承担多个不同目标",
                "工具过多：工具的调用时机、风险和输入边界开始混在一起",
                "输出责任混杂：同一轮结果要服务多个下游消费者",
                "流程依赖：后一步必须等待前一步产出",
                "高风险动作：执行前需要独立审查或人工确认"
        );
        assertThat(plan.suggestedAgents()).contains(
                "planning_agent",
                "tool_execution_agent",
                "result_writer_agent",
                "review_agent"
        );
    }

    @Test
    void independentBranchesShouldUseParallelAgent() {
        SingleAgentTaskRequest request = new SingleAgentTaskRequest(
                "同时分析多份资料",
                List.of("分析资料 A", "分析资料 B", "合并结论"),
                List.of("search_private_knowledge", "search_public_web"),
                List.of("source_a_summary", "source_b_summary", "merged_summary"),
                false,
                true,
                false,
                false
        );

        SingleAgentSplitPlan plan = analyzer.analyze(request);

        assertThat(plan.shouldSplit()).isTrue();
        assertThat(plan.recommendedMode()).isEqualTo(AgentWorkflowMode.PARALLEL_AGENT);
        assertThat(plan.signals()).contains("可并行子任务：多个分支没有强依赖，适合拆开执行");
        assertThat(plan.suggestedAgents()).contains("parallel_worker_agent");
    }

    @Test
    void dynamicEntryShouldUseRoutingAgent() {
        SingleAgentTaskRequest request = new SingleAgentTaskRequest(
                "处理不同类型的用户请求",
                List.of("识别意图", "选择处理路径", "返回结果"),
                List.of("classify_intent", "search_private_knowledge", "write_reply"),
                List.of("route", "answer"),
                false,
                false,
                true,
                false
        );

        SingleAgentSplitPlan plan = analyzer.analyze(request);

        assertThat(plan.shouldSplit()).isTrue();
        assertThat(plan.recommendedMode()).isEqualTo(AgentWorkflowMode.LLM_ROUTING_AGENT);
        assertThat(plan.signals()).contains("入口分流：需要先判断任务类型，再交给专门 Agent");
        assertThat(plan.suggestedAgents()).contains("routing_agent");
    }

}
