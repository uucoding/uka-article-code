package com.uka.saa.demo.config;

import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.uka.saa.demo.tools.OutlineStubTools;
import com.uka.saa.demo.tools.ReviewStubTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 第 16 讲 Supervisor 模式配置。
 * Spring AI Alibaba 1.1.2.2 中使用 ReactAgent + AgentTool 搭建监督者模式。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class TaskSupervisorConfig {

    @Bean
    public OutlineStubTools outlineStubTools() {
        return new OutlineStubTools();
    }

    @Bean
    public ReviewStubTools reviewStubTools() {
        return new ReviewStubTools();
    }

    @Bean
    public ReactAgent draftOutlineAgent(ChatModel chatModel, OutlineStubTools outlineStubTools) {
        return ReactAgent.builder()
                .name("draft_outline")
                .description("根据输入主题生成结构草案")
                .model(chatModel)
                // 1. AgentTool 会把这个 inputType 包装成工具入参 input。
                .inputType(String.class)
                .instruction("""
                        你是结构草案 Agent。
                        你只负责把用户输入拆成可执行结构草案。
                        请优先调用 create_structure_outline 工具生成草案，不要审校草案，也不要输出最终结论。
                        """)
                .methodTools(outlineStubTools)
                .saver(new MemorySaver())
                .build();
    }

    @Bean
    public ReactAgent reviewOutlineAgent(ChatModel chatModel, ReviewStubTools reviewStubTools) {
        return ReactAgent.builder()
                .name("review_outline")
                .description("检查结构草案是否具备清晰主线和可继续执行的结构")
                .model(chatModel)
                // 2. 每个专门 Agent 都只接收一段自然语言请求，降低 supervisor 拼参复杂度。
                .inputType(String.class)
                .instruction("""
                        你是结构审校 Agent。
                        你只负责检查草案结构、主线和风险点。
                        请优先调用 review_structure_outline 工具完成审校，不要重新生成草案。
                        """)
                .methodTools(reviewStubTools)
                .saver(new MemorySaver())
                .build();
    }

    @Bean
    public ReactAgent taskSupervisorAgent(ChatModel chatModel,
                                          ReactAgent draftOutlineAgent,
                                          ReactAgent reviewOutlineAgent) {
        return ReactAgent.builder()
                .name("task_supervisor_agent")
                .description("中心监督者 Agent，负责决定调用草案 Agent 或审校 Agent，并汇总结果")
                .model(chatModel)
                .instruction("""
                        你是任务监督者 Agent。
                        你的职责是观察用户请求，决定是否调用下面的专门 Agent 工具，并把多个工具结果合成为最终回复。
                        
                        可用工具：
                        - draft_outline：当用户需要生成结构草案时调用。
                        - review_outline：当用户需要检查结构、主线或风险时调用。
                        
                        调度规则：
                        - 如果用户只要求生成草案，调用 draft_outline 后汇总结果。
                        - 如果用户只要求审校已有草案，调用 review_outline 后汇总结果。
                        - 如果用户同时要求生成并检查草案，先调用 draft_outline，再把草案结果交给 review_outline，最后汇总两者结论。
                        - 不要自己替代专门 Agent 完成草案生成或审校。
                        """)
                // 3. Supervisor 只看到两个高层工具，不直接复制子 Agent 的工具细节。
                .tools(
                        AgentTool.getFunctionToolCallback(draftOutlineAgent),
                        AgentTool.getFunctionToolCallback(reviewOutlineAgent)
                )
                .saver(new MemorySaver())
                .build();
    }

}
