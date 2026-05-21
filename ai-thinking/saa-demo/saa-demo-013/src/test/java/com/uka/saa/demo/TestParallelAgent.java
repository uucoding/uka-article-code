package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 13 讲 ParallelAgent 测试。
 * 只验证结构、表达、风险三类评审是否并发写入 OverAllState，并聚合成统一结果。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = SaaDemo013Application.class)
public class TestParallelAgent {

        @Autowired
        @Qualifier("parallelWritingReviewAgent")
        private ParallelAgent parallelWritingReviewAgent;
    /**
     * 公众号：春风不晚
     */
    @Test
    void testParallelWritingReview() throws Exception {
        OverAllState state = parallelWritingReviewAgent.invoke("""
                ParallelAgent 可以把同一份文章初稿交给多个评审 Agent 同时处理。
                但如果子任务之间存在前后依赖，就不应该为了并发而并发。
                """, RunnableConfig.builder()
                .threadId("lesson-013-parallel")
                .build()).orElseThrow();

        String structureReview = readText(state, "structure_review");
        String styleReview = readText(state, "style_review");
        String riskReview = readText(state, "risk_review");
        String parallelReviewReport = readText(state, "parallel_review_report");

        System.out.println("=========> 结构评审:");
        System.out.println(structureReview);
        System.out.println("=========> 表达评审:");
        System.out.println(styleReview);
        System.out.println("=========> 风险评审:");
        System.out.println(riskReview);
        System.out.println("=========> 聚合评审报告:");
        System.out.println(parallelReviewReport);
    }

    private String readText(OverAllState state, String key) {
        Object value = state.value(key)
                .orElseThrow(() -> new IllegalStateException("缺少并发输出: " + key));
        if (value instanceof AssistantMessage assistantMessage) {
            return assistantMessage.getText();
        }
        return String.valueOf(value);
    }

}
