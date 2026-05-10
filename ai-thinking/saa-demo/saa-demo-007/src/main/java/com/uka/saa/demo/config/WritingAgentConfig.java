package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.uka.saa.demo.interceptor.RiskModelInterceptor;
import com.uka.saa.demo.interceptor.ToolAuditInterceptor;
import com.uka.saa.demo.hook.WritingLifecycleHook;
import com.uka.saa.demo.tools.WritingTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第 7 讲 Agent 示例配置。
 * 读者只需要看这个 Bean，就能理解 Hook、Interceptor、Tool 如何挂到同一个 Agent 上。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class WritingAgentConfig {

    /**
     * 构建写作 Agent。
     *
     * @param chatModel 底层模型
     * @return ReactAgent
     * @author 公众号：春风不晚
     */
    @Bean("writingAgent")
    public ReactAgent writingAgent(ChatModel chatModel) {
        WritingTools tools = new WritingTools();
        WritingLifecycleHook loggingHook = new WritingLifecycleHook();
        RiskModelInterceptor guardrailInterceptor = new RiskModelInterceptor();
        ToolAuditInterceptor toolAuditInterceptor = new ToolAuditInterceptor();

        return ReactAgent.builder()
                // 1. Agent 名称是日志、状态和调试时的定位锚点。
                .name("writing_agent")
                // 2. systemPrompt 只放长期角色，不放租户、用户等级这类运行态字段。
                .systemPrompt("""
                        你是一名严谨的中文技术专栏编辑。
                        你负责生成结构清晰、判断明确、可以落地的技术文章草稿。
                        """)
                // 3. instruction 放本轮执行纪律，变量由 call(inputs) 时传入。
                .instruction("""
                        当前发布平台：{publishingPlatform}
                        本轮聚焦点：{focusPoint}
                        
                        执行要求：
                        1. 先调用 loadPlatformRule 获取平台规则
                        2. 如果需要高级选题资产，可以尝试调用 loadVipWritingAssets
                        3. 最终输出一版文章草稿，重点解释 Hook、Interceptor、Context Engineering 的分工
                        """)
                // 4. 工具、Hook、Interceptor 都在这里一次性挂上，降低使用心智负担。
                .methodTools(tools)
                .hooks(loggingHook)
                .interceptors(guardrailInterceptor, toolAuditInterceptor)
                // 5. Demo 使用内存 saver，避免引入额外中间件。
                .saver(new MemorySaver())
                .model(chatModel)
                .build();
    }

}
