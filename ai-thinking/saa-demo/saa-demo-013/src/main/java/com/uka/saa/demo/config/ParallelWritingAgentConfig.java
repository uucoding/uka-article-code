package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 第 13 讲 ParallelAgent 配置。
 * 三个子 Agent 只演示同一份初稿的并发评审：结构、表达、风险。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class ParallelWritingAgentConfig {

    @Bean
    public ReactAgent structureReviewAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("structure_review_agent")
                .description("检查文章结构是否清晰")
                .model(chatModel)
                .instruction("""
                        你是技术文章结构评审 Agent。
                        请只检查文章结构，包括段落顺序、主线是否清晰、是否存在跳跃。
                        输出 3 条以内结构评审意见，不要改写全文。
                        
                        待评审初稿：
                        {input}
                        """)
                .outputKey("structure_review")
                .build();
    }

    @Bean
    public ReactAgent styleReviewAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("style_review_agent")
                .description("检查文章表达是否克制准确")
                .model(chatModel)
                .instruction("""
                        你是技术文章表达评审 Agent。
                        请只检查表达问题，包括术语是否准确、句子是否过满、是否有口号式表达。
                        输出 3 条以内表达评审意见，不要改写全文。
                        
                        待评审初稿：
                        {input}
                        """)
                .outputKey("style_review")
                .build();
    }

    @Bean
    public ReactAgent riskReviewAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("risk_review_agent")
                .description("检查文章交付风险")
                .model(chatModel)
                .instruction("""
                        你是技术文章风险评审 Agent。
                        请只检查交付风险，包括事实断言是否过强、是否缺少边界、是否容易误导读者。
                        输出 3 条以内风险评审意见，不要改写全文。
                        
                        待评审初稿：
                        {input}
                        """)
                .outputKey("risk_review")
                .build();
    }

    @Bean
    public ParallelAgent parallelWritingReviewAgent(ReactAgent structureReviewAgent,
                                                    ReactAgent styleReviewAgent,
                                                    ReactAgent riskReviewAgent) {
        // 1. 三个子 Agent 都读取同一份 input，彼此不依赖。
        List<Agent> agents = List.of(structureReviewAgent, styleReviewAgent, riskReviewAgent);

        return ParallelAgent.builder()
                .name("parallel_writing_review_agent")
                .description("并发完成文章结构、表达、风险三类评审")
                // 2. MemorySaver 提供最小 checkpoint 能力，本讲不展开恢复流程。
                .saver(new MemorySaver())
                .subAgents(agents)
                // 3. 聚合结果写入 parallel_review_report，方便后续统一读取。
                .mergeOutputKey("parallel_review_report")
                .mergeStrategy(new WritingReviewMergeStrategy())
                // 4. 显式限制并发数，避免把并发当成无上限执行。
                .maxConcurrency(3)
                .build();
    }

    static class WritingReviewMergeStrategy implements ParallelAgent.MergeStrategy {

        @Override
        public Object merge(Map<String, Object> subAgentResults, OverAllState overallState) {
            // 1. 聚合层只做格式收口，不重新生成评审结论。
            return """
                    【结构评审】
                    %s
                    
                    【表达评审】
                    %s
                    
                    【风险评审】
                    %s
                    """.formatted(
                    readText(subAgentResults.get("structure_review")),
                    readText(subAgentResults.get("style_review")),
                    readText(subAgentResults.get("risk_review")));
        }

        private String readText(Object value) {
            if (value instanceof AssistantMessage assistantMessage) {
                return assistantMessage.getText();
            }
            if (value == null) {
                return "未产出评审结果";
            }
            return String.valueOf(value);
        }

    }

}
