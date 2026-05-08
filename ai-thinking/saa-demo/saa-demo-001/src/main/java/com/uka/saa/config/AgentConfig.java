package com.uka.saa.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 公众号：春风不晚
 */
@Configuration
public class AgentConfig {

    @Bean
    ReactAgent assistantAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                // 1. 身份锚定：赋予 Agent 独立的人格与业务边界，而非散落的硬编码 Prompt
                .name("assistant-agent")
                .systemPrompt("你是一个企业内部智能助手。")
                // 2. 状态接管：预留状态保存能力，为后续多轮和执行状态做准备
                .saver(new MemorySaver())
                // 3. 核心驱动：底层大模型（ChatModel）此时变为 Agent 内部的一个执行引擎
                .model(chatModel)
                .build();
    }
}
