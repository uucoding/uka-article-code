package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.uka.saa.demo.tools.PublishingTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第 10 讲 Human-in-the-Loop Agent 配置。
 * 演示高风险工具在真正执行前进入人工审批。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class HitlAgentConfig {

    /**
     * 构建带人工审批的写作 Agent。
     *
     * @param chatModel 底层模型
     * @return ReactAgent
     * @author 公众号：春风不晚
     */
    @Bean("hitlAgent")
    public ReactAgent hitlAgent(ChatModel chatModel) {
        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                // 1. 只有命中的工具会触发人工审批，普通查询工具不需要停下来。
                .approvalOn("publishArticle", ToolConfig.builder()
                        .description("发布文章会触达真实读者，必须由人工确认标题和正文后才能执行。")
                        .build())
                .build();

        return ReactAgent.builder()
                // 2. Agent 名称会进入 graph 节点和 checkpoint，方便定位中断点。
                .name("hitl_agent")
                .systemPrompt("""
                        你是一名严谨的中文技术专栏编辑。
                        只有当用户明确要求发布文章时，才可以调用 publishArticle。
                        调用发布工具前不要自行假设审批已经通过。
                        """)
                // 3. MemorySaver 保存中断前的 graph 状态，恢复时继续同一个 threadId。
                .saver(new MemorySaver())
                // 4. HITL Hook 在模型产出工具调用后拦截高风险动作。
                .hooks(humanInTheLoopHook)
                // 5. 发布动作是一个真实 Tool，审批通过后才会被执行。
                .methodTools(new PublishingTools())
                .model(chatModel)
                .build();
    }

}
