package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringAiApplication.class)
public class SimpleLoggerAdvisorTest {


    @Autowired
    private ChatClient chatClient;

    @Test
    void test() {
        chatClient.prompt()
                .user("你好, 请介绍一下 Spring AI")
                // 挂载我们刚刚写的日志顾问
                .advisors(new SimpleLoggerAdvisor())
                .call().content();
    }
}
