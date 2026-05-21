package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.uka.saa.demo.config.RoutingWritingAgentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 14 讲 LlmRoutingAgent 测试。 验证统一入口会路由到不同写作专家。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = SaaDemo014Application.class)
public class TestRoutingAgent {

    @Autowired
    @Qualifier("writingRoutingAgent")
    private LlmRoutingAgent writingRoutingAgent;

    @Test
    void shouldRouteToArticleWriter() throws Exception {
        OverAllState state = invoke("请帮我写一篇介绍 RoutingAgent 如何给写作任务分流的技术文章。", "route-writer");

        System.out.println("=======> 文章生成结果：");
        System.out.println(readText(state, "article_writer_result"));
    }

    @Test
    void shouldRouteToTitlePolish() throws Exception {
        OverAllState state = invoke("给这篇文章起 3 个更适合技术专栏的标题。", "route-title");
        System.out.println("=======> 标题优化专家结果：");
        System.out.println(readText(state, "title_polish_result"));
        System.out.println("=======> 最终答复从 merged_result 读取：");
        System.out.println(readText(state, "merged_result"));
    }

    @Test
    void shouldRouteToArticleReview() throws Exception {
        OverAllState state = invoke("请审校这段初稿，看看结构和表达有什么问题：RoutingAgent 可以处理所有任务。", "route-review");

        System.out.println("=======> 文章审校结果：");
        System.out.println(readText(state, "article_review_result"));
    }

    @Test
    void shouldRouteToClarifyRequirement() throws Exception {
        OverAllState state = invoke("我想写点关于 AI 的东西，但是还没想清楚。", "route-clarify");

        System.out.println("=======> 需求澄清结果：");
        System.out.println(readText(state, "clarify_requirement_result"));
        System.out.println("=======> 最终答复从 merged_result 读取：");
        System.out.println(readText(state, "merged_result"));
    }

    private OverAllState invoke(String input, String threadId) throws Exception {
        return writingRoutingAgent.invoke(input, RunnableConfig.builder()
                .threadId("lesson-014-" + threadId)
                .build()).orElseThrow();
    }

    private String readText(OverAllState state, String key) {
        Object value = state.value(key)
                .orElseThrow(() -> new IllegalStateException("缺少路由输出: " + key));
        return readText(value, key);
    }

    private String readText(Object value, String outputKey) {
        if (value instanceof AssistantMessage assistantMessage) {
            return assistantMessage.getText();
        }
        if (value instanceof Message message) {
            return message.getText();
        }
        if (value instanceof GraphResponse<?> graphResponse) {
            Optional<?> result = graphResponse.resultValue();
            if (result.isPresent()) {
                Object resultValue = result.get();
                if (resultValue instanceof Map<?, ?> map) {
                    return readText(map.get(outputKey), outputKey);
                }
                return readText(resultValue, outputKey);
            }
            return "";
        }
        return value != null ? String.valueOf(value) : "";
    }

}
