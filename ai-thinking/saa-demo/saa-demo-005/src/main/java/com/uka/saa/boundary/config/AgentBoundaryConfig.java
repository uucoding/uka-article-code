package com.uka.saa.boundary.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.uka.saa.boundary.tool.WritingBoundaryTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  Agent 配置。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class AgentBoundaryConfig {

    /**
     * 注册 演示 Agent。
     * 讲清 ReactAgent 的 instruction、messages、model、tool 四个边界。
     * @param chatModel 底层模型执行引擎
     * @param writingBoundaryTools 写作工具集合
     * @return ReactAgent
     * @author 公众号：春风不晚
     */
    @Bean("agentBoundaryWriter")
    public ReactAgent agentBoundaryWriter(
            ChatModel chatModel,
            WritingBoundaryTools writingBoundaryTools) {
        return ReactAgent.builder()
                // 1. name 是 Agent 的工程身份，后续日志、状态和调试都靠它定位。
                .name("agent-boundary-writer")
                // 2. systemPrompt 只放长期角色与长期纪律，不塞本轮平台、阶段和主题。
                .systemPrompt("""
                        你是一名专业的中文技术专栏编辑。
                        你长期服务于有 Java 与 Spring Boot 背景的工程团队，
                        负责把 AI 工程中的抽象概念写成结构清晰、判断明确、可以落地的文章草稿。
                        
                        你必须长期遵守下面的规则：
                        1. 先讲工程判断，再讲功能罗列
                        2. 语言必须克制、专业、清晰
                        3. 禁止写成营销稿或口号式文案
                        4. 不要输出解释性前缀，不要输出 Markdown 代码块
                        5. 如果需要平台规则或聚焦点提醒，优先调用工具，不要凭空编造
                        """)
                // 3. instruction 承载运行时纪律，并通过输入 Map 中的变量完成模板渲染。
                .instruction("""
                        当前写作阶段：{writingStage}
                        发布平台：{publishingPlatform}
                        本轮聚焦点：{focusPoint}
                        
                        本轮执行要求：
                        1. 如果阶段是 first_draft，请生成一版结构完整的文章草稿
                        2. 如果阶段是 revision，请基于消息历史中的上一版草稿继续修改
                        3. 必须先调用 load_platform_rule 读取平台规则
                        4. 必须调用 load_focus_guardrail 读取聚焦点提醒
                        5. 输出内容必须围绕：{focusPoint}
                        """)
                // 4. methodTools 把带 @Tool 的方法注册为模型可调用的外部能力。
                .methodTools(writingBoundaryTools)
                // 5. saver 先使用内存保存器，让 invoke 路径能够回收最终状态。
                .saver(new MemorySaver())
                // 6. model 是真正执行推理、工具选择与文本生成的底层引擎。
                .model(chatModel)
                .build();
    }

}
