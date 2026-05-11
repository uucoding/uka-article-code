package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Memory 测试。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = {SaaDemo008Application.class})
public class TestMemoryAgent {

    @Autowired
    @Qualifier("memoryAgent")
    private ReactAgent agent;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testShortTermAndLongTermMemory() throws Exception {
        System.out.println("=========> 第一轮：保存用户长期写作画像");
        AssistantMessage first = agent.call(Map.of(
                "userId", "user-001",
                "messages", List.of(new UserMessage("请记住：我喜欢克制、工程判断强的标题风格，常写 Spring AI Alibaba。"))
        ), RunnableConfig.builder()
                // 1. threadId 只负责短期会话隔离。
                .threadId("writing-session-001")
                .build());
        System.out.println(first.getText());

        System.out.println("=========> 第二轮：换一个 threadId，读取同一个 userId 的长期画像");
        AssistantMessage second = agent.call(Map.of(
                "userId", "user-001",
                "messages", List.of(new UserMessage("请按我的个人风格，给第 8 讲起一个标题。"))
        ), RunnableConfig.builder()
                // 2. 换新的 threadId，证明长期画像不依赖短期会话。
                .threadId("writing-session-002")
                .build());
        System.out.println(second.getText());
    }

}
