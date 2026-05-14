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
        // 测试内容：目标、工具和输出都很单一时，不应该拆成多 Agent。
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
        System.out.println(plan);
    }

    @Test
    void orderedComplexTaskShouldUseSequentialAgent() {
        // 测试内容：目标、工具、输出都较多且存在前后依赖时，应推荐顺序型多 Agent。
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

        System.out.println(plan);
    }

    @Test
    void independentBranchesShouldUseParallelAgent() {
        // 测试内容：任务包含可独立处理的分支时，应优先推荐并行型多 Agent。
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
        System.out.println(plan);
    }

    @Test
    void dynamicEntryShouldUseRoutingAgent() {
        // 测试内容：入口需要先识别任务类型并分流时，应推荐路由型多 Agent。
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

        System.out.println(plan);
    }

}
