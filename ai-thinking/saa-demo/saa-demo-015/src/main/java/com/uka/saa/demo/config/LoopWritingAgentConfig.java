package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.LoopMode;
import com.uka.saa.demo.agent.WritingRevisionAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第 15 讲内置 LoopAgent 配置。
 * 只演示同一个子 Agent 被固定执行 3 轮。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class LoopWritingAgentConfig {

    @Bean
    public WritingRevisionAgent writingRevisionAgent() {
        return new WritingRevisionAgent(
                "writing_revision_agent",
                "对写作草稿做一轮风险词修正"
        );
    }

    @Bean
    public LoopAgent writingRevisionLoopAgent(WritingRevisionAgent writingRevisionAgent) {
        return LoopAgent.builder()
                .name("writing_revision_loop_agent")
                .description("固定执行 3 轮写作风险词修正")
                .subAgent(writingRevisionAgent)
                .loopStrategy(LoopMode.count(3))
                .build();
    }

}
