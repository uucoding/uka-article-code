package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 第 14 讲 LlmRoutingAgent 配置。
 * 只演示统一写作入口下的任务分流：生成、标题、审校、澄清。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class RoutingWritingAgentConfig {

    @Bean
    public ReactAgent articleWriterAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("article_writer_agent")
                .description("当用户要写一篇新文章、生成提纲、扩写主题或从零开始产出正文时使用")
                .model(chatModel)
                .instruction("""
                        你是文章生成 Agent。
                        只处理新文章生成、提纲规划和正文起草任务。
                        请基于路由后的任务要求给出可继续写作的提纲或初稿，不要做标题批量优化，也不要做审校。
                        
                        路由后的任务要求：
                        {article_writer_agent_input}
                        
                        用户原始输入：
                        {input}
                        """)
                .outputKey("article_writer_result")
                .build();
    }

    @Bean
    public ReactAgent titlePolishAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("title_polish_agent")
                .description("当用户要改标题、生成标题候选、优化标题点击感或调整标题表达时使用")
                .model(chatModel)
                .instruction("""
                        你是标题优化 Agent。
                        只处理标题候选生成、标题改写和标题表达优化任务。
                        请输出 3 个以内中文标题候选，并说明各自适合的文章角度。
                        
                        路由后的任务要求：
                        {title_polish_agent_input}
                        
                        用户原始输入：
                        {input}
                        """)
                .outputKey("title_polish_result")
                .build();
    }

    @Bean
    public ReactAgent articleReviewAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("article_review_agent")
                .description("当用户贴出已有初稿并要求审校、挑问题、检查结构或检查表达风险时使用")
                .model(chatModel)
                .instruction("""
                        你是文章审校 Agent。
                        只处理已有内容的结构、表达和风险检查。
                        请输出主要问题和修改建议，不要从零生成新文章。
                        
                        路由后的任务要求：
                        {article_review_agent_input}
                        
                        用户原始输入：
                        {input}
                        """)
                .outputKey("article_review_result")
                .build();
    }

    @Bean
    public ReactAgent clarifyRequirementAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("clarify_requirement_agent")
                .description("当用户意图不清、信息不足、无法判断要写文章还是改标题或审校时使用")
                .model(chatModel)
                .instruction("""
                        你是需求澄清 Agent。
                        只处理信息不足或意图不清的写作请求。
                        请提出 2 到 3 个必要澄清问题，帮助用户补齐写作目标、受众和交付形式。
                        
                        路由后的任务要求：
                        {clarify_requirement_agent_input}
                        
                        用户原始输入：
                        {input}
                        """)
                .outputKey("clarify_requirement_result")
                .build();
    }

    @Bean
    public LlmRoutingAgent writingRoutingAgent(ReactAgent articleWriterAgent,
                                               ReactAgent titlePolishAgent,
                                               ReactAgent articleReviewAgent,
                                               ReactAgent clarifyRequirementAgent,
                                               ChatModel chatModel) {
        // 1. 路由目标先收紧到四个明确专家，避免统一入口无限发散。
        List<Agent> agents = List.of(
                articleWriterAgent,
                titlePolishAgent,
                articleReviewAgent,
                clarifyRequirementAgent
        );

        return LlmRoutingAgent.builder()
                .name("writing_routing_agent")
                .description("统一写作入口任务路由 Agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .subAgents(agents)
                // 2. systemPrompt 明确允许的路由目标和选择规则。
                .systemPrompt("""
                        你是 AI 写作助手的任务路由器。
                        请根据用户输入选择最合适的一个专家 Agent，不要同时选择多个 Agent。
                        
                        可选 Agent 只能是：
                        - article_writer_agent：新文章生成、提纲规划、正文起草、主题扩写
                        - title_polish_agent：标题生成、标题改写、标题优化
                        - article_review_agent：已有初稿审校、结构检查、表达检查、风险检查
                        - clarify_requirement_agent：意图不清、信息不足、无法判断用户要什么
                        
                        选择规则：
                        - 用户要求写一篇、生成提纲、扩写主题，选择 article_writer_agent。
                        - 用户要求起标题、改标题、标题更吸引人，选择 title_polish_agent。
                        - 用户贴出已有内容并要求审校、检查、修改建议，选择 article_review_agent。
                        - 用户只表达模糊想法，缺少主题、受众或交付形式，选择 clarify_requirement_agent。
                        """)
                // 3. instruction 会追加到本轮消息后，让路由节点按当前输入做一次选择。
                .instruction("""
                        请只选择一个 Agent，并为该 Agent 生成一条简短的 query。
                        返回必须符合结构化输出要求，不要输出解释。
                        """)
                .build();
    }

}
