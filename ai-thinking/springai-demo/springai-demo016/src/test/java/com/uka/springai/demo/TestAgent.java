package com.uka.springai.demo;

import com.uka.springai.demo.tools.AgentTools;
import com.uka.springai.demo.tools.WeatherTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Springaidemo016Application.class)
public class TestAgent {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * 公众号：春风不晚
     */
    @Test
    public void testManualMethodTool() {
        String task = """
             帮我查一下杭州明天的天气，然后计算出如果明天去杭州出差3天，机票1200元，每天打车50元，总共需要报销多少钱？最后根据天气情况和算出来的报销额，发一封差旅提醒邮件给张三。
                """;
        // 引导大模型开启 ReAct 思考模式
        String systemPrompt = """
            你是一个极其聪明的高级企业行政 Agent。你拥有多件外部物理工具可以使用。
            
            执行规则：
            1. 面对复杂任务，你必须将任务拆解为多个步骤，一步一步执行 (Think step by step)。
            2. 遇到算账问题，绝对禁止口算！必须调用 calculator 工具！
            3. 遇到查天气问题，必须调用 getWeather 工具！
            4. 所有外部数据收集完毕并计算无误后，再调用 sendEmail 工具发送最终报告。
            """;

        String response = chatClientBuilder.build().prompt()
                .system(systemPrompt)
                .user(task)
                // 挂载多个工具
                .tools(new WeatherTools(), new AgentTools())
                .call()
                .content();

        System.out.println(response);
    }

}
