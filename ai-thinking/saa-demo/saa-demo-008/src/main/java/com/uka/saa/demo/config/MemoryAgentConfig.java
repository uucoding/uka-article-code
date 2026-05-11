package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.uka.saa.demo.hooks.MessageTrimmingHook;
import com.uka.saa.demo.hooks.UserProfileMemoryHook;
import com.uka.saa.demo.tools.WritingMemoryTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第 8 讲 Memory Agent 配置。
 * 这里同时演示短期记忆 saver 和长期记忆 MemoryStore。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class MemoryAgentConfig {

    /**
     * 注册长期记忆存储。
     *
     * @return Store
     * @author 公众号：春风不晚
     */
    @Bean
    public Store memoryStore() {
        // 1. MemoryStore 是内存版长期记忆实现，适合课程 Demo 和单元测试。
        return new MemoryStore();
    }

    /**
     * 构建带 Memory 的 Agent。
     *
     * @param chatModel 底层模型
     * @param memoryStore 长期记忆存储
     * @return ReactAgent
     * @author 公众号：春风不晚
     */
    @Bean("memoryAgent")
    public ReactAgent memoryAgent(ChatModel chatModel, Store memoryStore) {
        return ReactAgent.builder()
                // 1. Agent 名称用于日志和状态定位。
                .name("memory_agent")
                // 2. systemPrompt 只放长期角色，不把用户画像硬塞在这里。
                .systemPrompt("""
                        你是一名严谨的中文技术专栏编辑。
                        你需要根据用户当前问题、短期上下文和长期写作画像，给出稳定、克制、可落地的写作建议。
                        """)
                // 3. instruction 负责说明本轮如何使用长期记忆工具。
                .instruction("""
                        当前用户：{userId}
                        当前长期画像：{writingProfile}
                        
                        执行要求：
                        1. 如果用户要求你记住长期写作偏好，请调用 saveWritingProfile 保存画像
                        2. 如果用户要求按个人风格写作，请调用 loadWritingProfile 读取画像
                        3. 不要把用户长期偏好只留在本轮 messages 里
                        """)
                // 4. MemorySaver 是短期 state / messages 的内存版 checkpointer。
                .saver(new MemorySaver())
                // 5. MessageTrimmingHook 演示长对话消息治理。
                .hooks(new MessageTrimmingHook(8), new UserProfileMemoryHook(memoryStore))
                // 6. WritingMemoryTools 演示长期记忆显式读写。
                .methodTools(new WritingMemoryTools(memoryStore))
                .model(chatModel)
                .build();
    }

}
