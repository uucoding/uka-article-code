package com.uka.saa.agent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三讲中的 Agent 版本配置。
 * 该配置只注册一个用于工单分类的 ReactAgent。
 * Agent 接收工单文本后返回分类结果，不参与后续流程编排。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class TicketAgentConfig {

    /**
     * 构建一个专用于工单分类的 ReactAgent。
     * 这里完成三件事：
     * 1. 定义 Agent 名称
     * 2. 定义系统提示词
     * 3. 绑定底层 ChatModel
     *
     * @param chatModel 底层大模型执行引擎
     * @return 工单分类 Agent
     * @author 公众号：春风不晚
     */
    @Bean("ticketTriageAgent")
    public ReactAgent ticketTriageAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("ticket-triage-agent")
                // 约束 Agent 的输出目标和返回格式
                .systemPrompt("""
                        你是一个 IT 工单分类助手。
                        你的任务只有一件事：
                        判断工单属于哪一类。
                        
                        分类只能从下面三个值中选择一个：
                        NETWORK_FAULT
                        ACCOUNT_PERMISSION
                        OTHER
                        
                        你必须严格输出 JSON，禁止输出 Markdown、解释性前缀或多余文本。
                        正确示例：
                        {"category":"NETWORK_FAULT"}
                        """)
                // 保存 Agent 运行过程中的状态
                .saver(new MemorySaver())
                // 绑定大模型驱动 (Layer 01)
                .model(chatModel)
                .build();
    }
}
