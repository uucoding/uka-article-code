package com.uka.saa.structured.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.uka.saa.structured.model.WritingBlueprint;
import com.uka.saa.structured.tool.StructuredWritingTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 配置。
 * 本配置的核心是 outputType：让 Agent 明确最终要交付 WritingBlueprint，而不是自由文本。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class StructuredWritingAgentConfig {

    /**
     * 注册结构化写作蓝图 Agent。
     *
     * @param chatModel 底层模型执行引擎
     * @param structuredWritingTools 外围资产工具
     * @return ReactAgent
     * @author 公众号：春风不晚
     */
    @Bean("structuredWritingAgent")
    public ReactAgent structuredWritingAgent(
            ChatModel chatModel,
            StructuredWritingTools structuredWritingTools) {
        return ReactAgent.builder()
                // 1. 固定 Agent 名称，让日志和状态定位具备稳定锚点。
                .name("structured-writing-agent")
                // 2. systemPrompt 只定义长期角色，不把 JSON 样例和本轮变量硬写进去。
                .systemPrompt("""
                        你是一名严谨的中文技术内容架构师。
                        你不直接写完整正文，而是先把用户需求拆成可执行的结构化写作蓝图。
                        
                        你必须长期遵守下面的规则：
                        1. 先识别用户意图，再生成正文计划
                        2. 先输出结构化蓝图，不直接输出完整正文
                        3. 所有字段都必须服务于后续代码消费
                        4. 不要输出 Markdown 代码块
                        5. 不要输出解释性前缀
                        """)
                // 3. instruction 负责本轮平台与聚焦点，运行时由输入 Map 渲染。
                .instruction("""
                        当前发布平台：{publishingPlatform}
                        本轮聚焦点：{focusPoint}
                        
                        本轮执行要求：
                        1. 先判断 intentType：write_new、rewrite、expand、polish、generate_assets
                        2. 再生成 WritingPlan，必须包含 workingTitle、topicJudgment、coreThesis、readerHook、sections、endingStrategy
                        3. 每个 section 必须包含 heading、purpose、keyPoints、suggestedWordCount
                        4. 然后调用 generate_title_candidates、generate_summary、generate_keywords 生成 assets
                        5. 最终结果必须严格匹配 WritingBlueprint 的字段结构
                        6. 所有判断必须围绕：{focusPoint}
                        """)
                // 4. methodTools 注册外围资产工具，让资产生成从 Prompt 约定变成真实工具调用。
                .methodTools(structuredWritingTools)
                // 5. outputType 会基于 WritingBlueprint 生成结构化输出约束。
                .outputType(WritingBlueprint.class)
                // 6. 内存保存器保留状态，为后续记忆与工作流章节承接。
                .saver(new MemorySaver())
                // 7. ChatModel 仍然是真正执行推理、工具调用和 JSON 生成的引擎。
                .model(chatModel)
                .build();
    }

}
