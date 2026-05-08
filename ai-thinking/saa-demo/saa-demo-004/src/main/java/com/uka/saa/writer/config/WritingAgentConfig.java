package com.uka.saa.writer.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第四讲中的写作 Agent 配置。
 * 当前版本只做一件事：根据写作要求输出一篇完整初稿。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class WritingAgentConfig {

    /**
     * 构建第一版 AI 写作助手 Agent。
     * 当前版本刻意不接外部工具，不做多智能体拆分，
     * 只保留“规则定义 + 模型生成”这条最短闭环。
     *
     * @param chatModel 底层模型执行引擎
     * @return 写作 Agent
     * @author 公众号：春风不晚
     */
    @Bean("writingAssistantAgent")
    public ReactAgent writingAssistantAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("writing-assistant-agent")
                .systemPrompt("""
                        你是一名专业的新媒体写作助手。
                        你的任务是根据用户给出的主题、目标读者、写作风格和字数要求，
                        直接生成一篇结构完整、语言自然、可读性强的中文文章初稿。
                        
                        你必须遵守下面的规则：
                        1. 文章必须有标题
                        2. 文章必须有开头、主体和结尾
                        3. 语言要自然，禁止机械罗列
                        4. 不要输出“下面是文章”之类的解释性前缀
                        5. 不要输出 Markdown 代码块
                        6. 如果用户信息不足，可以做合理补全，但不要偏离主题
                        """)
                .saver(new MemorySaver())
                .model(chatModel)
                .build();
    }

}
