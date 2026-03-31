package com.uka.springai.demo;

import com.uka.springai.demo.tools.DateTimeToolsWithAnnotation;
import com.uka.springai.demo.tools.WeatherTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Springaidemo014Application.class)
public class TestToolCalling {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testWithoutTool() {
        ChatClient chatClient = chatClientBuilder.build();
        String content = chatClient.prompt("今天是几号？")
                .call()
                .content();
        System.out.println(content);
    }
    /**
     * 公众号：春风不晚
     */
    @Test
    void testWithTool() {
        ChatClient chatClient = chatClientBuilder.build();
        String content = chatClient.prompt("今天是几号？")
                .tools(new DateTimeToolsWithAnnotation())
                .call()
                .content();
        System.out.println(content);
    }
    /**
     * 公众号：春风不晚
     */
    @Test
    void testWithWeatherTools() {
        ChatClient chatClient = chatClientBuilder.build();
        String content = chatClient.prompt("帮我北京最近的天气咋样啊？需要带伞吗？")
                .tools(new WeatherTools())
                .call()
                .content();
        System.out.println(content);
    }

}
