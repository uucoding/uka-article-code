package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 第 12 讲 SequentialAgent 配置。
 * 三个子 Agent 只演示最小顺序链路：提纲 -> 初稿 -> 审校。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class SequentialWritingAgentConfig {

    @Bean
    public ReactAgent outlineAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("outline_agent")
                .description("根据用户主题生成文章提纲")
                .model(chatModel)
                .instruction("""
                        你是技术文章策划 Agent。
                        请基于用户输入生成一份中文技术文章提纲，提纲保持 3 个一级段落以内。
                        只输出提纲，不要写正文。
                        
                        用户输入：
                        {input}
                        """)
                .outputKey("outline")
                .build();
    }

    @Bean
    public ReactAgent draftAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("draft_agent")
                .description("根据提纲生成文章初稿")
                .model(chatModel)
                .instruction("""
                        你是技术文章写作 Agent。
                        请严格基于前序提纲写一版中文文章初稿，保留提纲中的主要段落。
                        不要重新设计提纲，不要跳过提纲中的主要段落。
                        
                        前序提纲：
                        {outline}
                        """)
                .outputKey("draft")
                .build();
    }

    @Bean
    public ReactAgent reviewAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("review_agent")
                .description("审校文章初稿并给出最终版本")
                .model(chatModel)
                .instruction("""
                        你是技术文章审校 Agent。
                        请基于前序初稿做最后审校，修正结构松散、表述过满和结论不清的问题。
                        直接输出可交付的最终版本。
                        
                        前序初稿：
                        {draft}
                        """)
                .outputKey("reviewed_article")
                .build();
    }

    @Bean
    public SequentialAgent sequentialWritingAgent(ReactAgent outlineAgent,
                                                  ReactAgent draftAgent,
                                                  ReactAgent reviewAgent) {
        // 1. subAgents 的列表顺序就是执行顺序：提纲 -> 初稿 -> 审校。
        List<Agent> agents = List.of(outlineAgent, draftAgent, reviewAgent);

        return SequentialAgent.builder()
                .name("sequential_writing_agent")
                .description("按提纲、初稿、审校顺序执行写作任务")
                // 2. MemorySaver 提供最小 checkpoint 能力，本讲不展开恢复流程。
                .saver(new MemorySaver())
                .subAgents(agents)
                .build();
    }

}
