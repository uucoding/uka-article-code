package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第 19 讲 A2A Agent 配置。
 * 真实生产环境可以把 LocalA2aAgentCardProvider 替换成 Nacos Discovery。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class A2aAgentConfig {

    @Bean("writingResearchAgent")
    public ReactAgent writingResearchAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("writing_research_agent")
                .description("负责把写作主题拆成研究判断和文章骨架的本地 ReactAgent")
                .systemPrompt("你是 Spring AI Alibaba 写作课程的研究助理。")
                .instruction("""
                        请围绕用户给出的写作主题，输出：
                        1. 一个明确主判断
                        2. 三个结构化小标题
                        3. 每个小标题下的核心论据
                        """)
                .model(chatModel)
                .outputKey("messages")
                .build();
    }

}
