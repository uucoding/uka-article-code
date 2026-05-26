package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.uka.saa.demo.agent.ConditionalAgent;
import com.uka.saa.demo.agent.ErrorHandlingAgent;
import com.uka.saa.demo.agent.ReportGenerationAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 17 讲条件 Agent 测试。
 * 直接观察状态字段，验证条件边只进入一个分支。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = SaaDemo017Application.class)
public class TestConditionalAgent {

    @Autowired
    @Qualifier("conditionalWritingAgent")
    private Agent conditionalWritingAgent;

    @Test
    void shouldRouteToReportGenerationBranchWhenInputRequiresReport() throws GraphRunnerException {
        OverAllState state = conditionalWritingAgent.invoke("请写一份 report，介绍 Agent 条件分支的最小实现。", RunnableConfig.builder()
                        // 1. 每个测试使用独立 threadId，避免 MemorySaver 复用上一轮状态。
                        .threadId("lesson-017-report")
                        .build())
                .orElseThrow();

        System.out.println(state.value(ConditionalAgent.CONDITION_RESULT_KEY, ""));
        System.out.println(state.value(ReportGenerationAgent.OUTPUT_KEY, ""));
    }

    @Test
    void shouldRouteToErrorHandlingBranchWhenInputMentionsError() throws GraphRunnerException {
        OverAllState state = conditionalWritingAgent.invoke("请处理写作流程里的 error，并给出风险说明。", RunnableConfig.builder()
                        .threadId("lesson-017-error")
                        .build())
                .orElseThrow();

        System.out.println(state.value(ConditionalAgent.CONDITION_RESULT_KEY, ""));
        System.out.println(state.value(ErrorHandlingAgent.OUTPUT_KEY, ""));
    }
}
