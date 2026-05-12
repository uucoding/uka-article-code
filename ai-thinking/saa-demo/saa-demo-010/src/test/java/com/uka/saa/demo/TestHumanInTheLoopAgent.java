package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata.ToolFeedback;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata.ToolFeedback.FeedbackResult;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 第 10 讲 Human-in-the-Loop 测试。
 * 测试覆盖人工通过、人工改写和人工拒绝三条恢复路径。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = {SaaDemo010Application.class})
public class TestHumanInTheLoopAgent {

    @Autowired
    @Qualifier("hitlAgent")
    private ReactAgent agent;

    /**
     * 用例一：人工审批通过后执行发布工具
     *
     * 公众号：春风不晚
     */
    @Test
    void testApprovePublishToolCall() throws Exception {
        String threadId = "lesson-010-approve";
        InterruptionMetadata interruption = requestPublishing(threadId);
        if (interruption == null) {
            System.out.println("=========> Agent 未进入人工确认");
            return;
        }
        InterruptionMetadata approved = buildFeedback(interruption, FeedbackResult.APPROVED,
                "标题和正文确认无误，可以发布。", ToolFeedback::getArguments);

        AssistantMessage response = resume(threadId, approved);

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

    /**
     * 用例二：人工改写工具参数后再执行发布工具
     * 公众号：春风不晚
     */
    @Test
    void testEditPublishToolCallBeforeResume() throws Exception {
        String threadId = "lesson-010-edit";
        InterruptionMetadata interruption = requestPublishing(threadId);
        if (interruption == null) {
            System.out.println("=========> Agent 未进入人工确认");
            return;
        }
        InterruptionMetadata edited = buildFeedback(interruption, FeedbackResult.EDITED,
                "标题需要更克制，使用人工修订版本。",
                arguments -> """
                        {
                          "title": "拒绝 Agent 越权发布，HITL 如何接住人工审批",
                          "content": "这是一篇经过人工修订标题后的第 10 讲正文。"
                        }
                        """);

        AssistantMessage response = resume(threadId, edited);

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

    /**
     * 用例三：人工拒绝后不执行发布工具
     * 公众号：春风不晚
     */
    @Test
    void testRejectPublishToolCallBeforeExecution() throws Exception {String threadId = "lesson-010-reject";
        InterruptionMetadata interruption = requestPublishing(threadId);
        if (interruption == null) {
            System.out.println("=========> Agent 未进入人工确认");
            return;
        }
        InterruptionMetadata rejected = buildFeedback(interruption, FeedbackResult.REJECTED,
                "正文还没有完成事实核验，本次不允许发布。", ToolFeedback::getArguments);

        AssistantMessage response = resume(threadId, rejected);

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

    /**
     * 发起发布请求
     * @param threadId
     * @return
     * @throws GraphRunnerException
     */
    private InterruptionMetadata requestPublishing(String threadId) throws GraphRunnerException {
        RunnableConfig runnableConfig = RunnableConfig.builder()
                // 1. threadId 是后续恢复执行的定位键。
                .threadId(threadId)
                .build();
        // 运行直到触发中断
        Optional<NodeOutput> result = agent.invokeAndGetOutput(" 请发布第 10 讲文章，标题是《拒绝 Agent 越权发布》，正文是一段用于验证 HITL 的课程内容。", runnableConfig);

        if (result.isPresent() && result.get() instanceof InterruptionMetadata interruptionMetadata) {
            // 中断包含需要审查的工具反馈
            List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();
            for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                System.out.println("工具: " + feedback.getName());
                System.out.println("参数: " + feedback.getArguments());
                System.out.println("描述: " + feedback.getDescription());
            }
            return interruptionMetadata;
        }
        return null;
    }

    /**
     * 构建人工反馈
     * @param interruption
     * @param result
     * @param description
     * @param argumentsMapper
     * @return
     */
    private InterruptionMetadata buildFeedback(InterruptionMetadata interruption,
                                               FeedbackResult result,
                                               String description,
                                               Function<ToolFeedback, String> argumentsMapper) {
        List<ToolFeedback> feedbacks = interruption.toolFeedbacks().stream()
                .map(feedback -> ToolFeedback.builder(feedback)
                        // 1. result 决定恢复时是执行、改写执行，还是拒绝执行。
                        .result(result)
                        // 2. arguments 在 改写（EDITED） 场景会替换原始工具参数。
                        .arguments(argumentsMapper.apply(feedback))
                        // 3. description 会在拒绝场景进入模型上下文，作为后续改写依据。
                        .description(description)
                        .build())
                .toList();

        return InterruptionMetadata.builder(interruption)
                .toolFeedbacks(feedbacks)
                .build();
    }

    /**
     * 恢复执行
     * @param threadId
     * @param feedback
     * @return
     * @throws Exception
     */
    private AssistantMessage resume(String threadId, InterruptionMetadata feedback) throws Exception {
        return agent.call(Map.of(), RunnableConfig.builder()
                // 1. 继续使用同一个 threadId，从中断前的 checkpoint 恢复。
                .threadId(threadId)
                // 2. HUMAN_FEEDBACK 会被 HumanInTheLoopHook 消费。
                .addHumanFeedback(feedback)
                .build());
    }

}
