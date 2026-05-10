package com.uka.saa.demo.context;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Context Engineering 示例。
 * 这里不做业务分层，只演示 messages 和运行态上下文应该怎么拆开。
 *
 * @author 公众号：春风不晚
 */
public class WritingContext {

    /**
     * 构造用户消息。
     *
     * @param topic 写作主题
     * @param platform 发布平台
     * @return 消息列表
     * @author 公众号：春风不晚
     */
    public List<Message> buildMessages(String topic, String platform) {
        return List.of(UserMessage.builder()
                .text("""
                        请围绕下面的主题生成一版技术文章草稿。
                        
                        主题：%s
                        发布平台：%s
                        
                        要求：
                        1. 先讲工程判断，再讲实现细节
                        2. 语言克制、专业、清晰
                        3. 不要输出 Markdown 代码块
                        """.formatted(topic, platform))
                .metadata(Map.of("stage", "lesson-007"))
                .build());
    }

    /**
     * 构造 Agent 输入。
     *
     * @param messages 消息列表
     * @return Agent 输入 Map
     * @author 公众号：春风不晚
     */
    public Map<String, Object> buildInputs(List<Message> messages) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        // 1. messages 只放用户真正要完成的任务。
        inputs.put("messages", messages);
        // 2. instruction 模板变量负责表达层面的约束。
        inputs.put("publishingPlatform", "微信公众号");
        inputs.put("focusPoint", "Agent 生命周期治理、模型请求治理、工具调用审计");
        // 3. 运行态字段交给 Hook / Interceptor 消费，不直接塞进 Prompt。
        inputs.put("requestId", "req-007");
        inputs.put("tenantId", "tenant-a");
        inputs.put("userLevel", "free");
        inputs.put("riskLevel", "high");
        return inputs;
    }

}
