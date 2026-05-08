package com.uka.saa.agent;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = AgentAiApplication.class)
public class TestAgent {

    @Autowired
    private DashScopeAgent dashScopeAgent;

    /**
     * @author 公众号：春风不晚
     */
    @Test
    public void test() throws Exception{
        //  使用dashScopeAgent
        AssistantMessage response = dashScopeAgent
                .call(
                        Prompt.builder().messages(UserMessage.builder().text("介绍一下自己").build())
                                .build()
                ).getResult()
                .getOutput();
        System.out.println(response.getText());
    }
}
