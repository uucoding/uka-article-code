package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.uka.saa.demo.hook.HybridRagAgentHook;
import com.uka.saa.demo.hook.TwoStepRagMessagesHook;
import com.uka.saa.demo.interceptor.RagContextModelInterceptor;
import com.uka.saa.demo.rag.LessonKnowledgeBase;
import com.uka.saa.demo.tool.KnowledgeSearchTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第 18 讲 ReactAgent RAG 配置。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class RagAgentConfig {

    @Bean
    public VectorStore lessonVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean("twoStepRagAgent")
    public ReactAgent twoStepRagAgent(ChatModel chatModel, LessonKnowledgeBase knowledgeBase) {
        return ReactAgent.builder()
                .name("two_step_rag_agent")
                .description("固定在模型调用前检索课程知识的两步 RAG Agent")
                .systemPrompt("你是 Spring AI Alibaba 进阶课程的助教。")
                .instruction("请基于 Hook 注入的 RAG 参考资料回答用户问题。")
                .hooks(new TwoStepRagMessagesHook(knowledgeBase))
                .saver(new MemorySaver())
                .model(chatModel)
                .build();
    }

    @Bean("agenticRagAgent")
    public ReactAgent agenticRagAgent(ChatModel chatModel, KnowledgeSearchTool knowledgeSearchTool) {
        return ReactAgent.builder()
                .name("agentic_rag_agent")
                .description("把课程知识检索能力作为工具交给模型自主调用的 Agentic RAG Agent")
                .systemPrompt("你是 Spring AI Alibaba 进阶课程的助教。")
                .instruction("""
                        当用户问题涉及 RAG、Agent Framework、ConditionalAgent 或 A2A 时，
                        请优先调用 search_lesson_knowledge 工具检索课程资料，再基于工具结果回答。
                        """)
                .tools(knowledgeSearchTool.asToolCallback())
                .saver(new MemorySaver())
                .model(chatModel)
                .build();
    }

    @Bean("hybridRagAgent")
    public ReactAgent hybridRagAgent(ChatModel chatModel, LessonKnowledgeBase knowledgeBase) {
        return ReactAgent.builder()
                .name("hybrid_rag_agent")
                .description("在 AgentHook 中做查询增强和检索，再由 ModelInterceptor 注入上下文的混合 RAG Agent")
                .systemPrompt("你是 Spring AI Alibaba 进阶课程的助教。")
                .instruction("请基于混合 RAG 链路注入的参考资料回答，并在资料不足时说明原因。")
                .hooks(new HybridRagAgentHook(knowledgeBase))
                .interceptors(new RagContextModelInterceptor())
                .saver(new MemorySaver())
                .model(chatModel)
                .build();
    }

}
