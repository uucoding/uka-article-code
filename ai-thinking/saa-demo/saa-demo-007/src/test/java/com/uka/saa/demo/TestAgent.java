package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.uka.saa.demo.context.WritingContext;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest(classes = { SaaDemo007Application.class })
public class TestAgent {

    @Autowired
    @Qualifier("writingAgent")
    private ReactAgent agent;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testCallAgentWithHooksAndInterceptors() throws Exception {
        WritingContext contextDemo = new WritingContext();
        Map<String, Object> inputs = contextDemo.buildInputs(
                contextDemo.buildMessages("Agent 为什么不能只靠 Prompt", "微信公众号")
        );

        System.out.println("=========> 开始调用 writingAgent");
        System.out.println("=========> 输入上下文: " + inputs);

        AssistantMessage response = agent.call(inputs, RunnableConfig.builder()
                .threadId("lesson-007-thread")
                .build());

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

}
