package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 12 讲 SequentialAgent 测试。
 * 只验证提纲、初稿、审校三段输出是否依次写入 OverAllState。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = SaaDemo012Application.class)
public class TestSequentialAgent {

    @Autowired
    @Qualifier("sequentialWritingAgent")
    private SequentialAgent sequentialWritingAgent;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testSequentialWritingPipeline() throws Exception {
        OverAllState state = sequentialWritingAgent.invoke("请写一篇介绍 SequentialAgent 如何拆写作流水线的技术文章。", RunnableConfig.builder()
                .threadId("lesson-012-sequential")
                .build()).orElseThrow();

        String outline = readText(state, "outline");
        String draft = readText(state, "draft");
        String reviewedArticle = readText(state, "reviewed_article");

        System.out.println("=========> 提纲阶段:");
        System.out.println(outline);
        System.out.println("=========> 初稿阶段:");
        System.out.println(draft);
        System.out.println("=========> 审校阶段:");
        System.out.println(reviewedArticle);
    }

    private String readText(OverAllState state, String key) {
        Object value = state.value(key)
                .orElseThrow(() -> new IllegalStateException("缺少阶段输出: " + key));
        if (value instanceof AssistantMessage assistantMessage) {
            return assistantMessage.getText();
        }
        return String.valueOf(value);
    }

}
