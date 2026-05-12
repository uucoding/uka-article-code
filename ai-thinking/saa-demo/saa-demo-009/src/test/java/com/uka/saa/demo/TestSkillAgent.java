package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.ReadSkillTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 9 讲 Skills 测试。
 * 测试只调用 Agent，通过 System.out 观察 read_skill 与 shell 的最小链路。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = {SaaDemo009Application.class})
public class TestSkillAgent {

    @Autowired
    @Qualifier("skillAgent")
    private ReactAgent agent;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testSkillWithShellExtension() throws Exception {
        AssistantMessage response = agent.call(Map.of(
                "messages", List.of(new UserMessage("""
                        写一篇关于 Spring AI Alibaba 介绍的文章。
                        """))
        ), RunnableConfig.builder()
                .threadId("lesson-009-shell-extension")
                .build());

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

}
