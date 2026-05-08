package com.uka.saa.boundary.service.impl;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.uka.saa.boundary.model.AgentBoundaryRequest;
import com.uka.saa.boundary.model.AgentBoundaryResult;
import com.uka.saa.boundary.model.AgentBoundarySnapshot;
import com.uka.saa.boundary.service.AgentBoundaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 第 5 讲服务实现。
 * 该实现不是为了堆功能，而是把一次 Agent 调用拆成 instruction、messages、model、tool 四条可观察边界。
 *
 * @author 公众号：春风不晚
 */
@Service
public class AgentBoundaryServiceImpl implements AgentBoundaryService {

    private static final Logger log = LoggerFactory.getLogger(AgentBoundaryServiceImpl.class);

    private static final List<String> TOOL_NAMES = List.of("load_platform_rule", "load_focus_guardrail");

    private final ReactAgent agentBoundaryWriter;

    private final String modelName;

    public AgentBoundaryServiceImpl(
            @Qualifier("agentBoundaryWriter") ReactAgent agentBoundaryWriter,
            @Value("${spring.ai.dashscope.chat.options.model:qwen-max}") String modelName) {
        this.agentBoundaryWriter = agentBoundaryWriter;
        this.modelName = modelName;
    }

    @Override
    public AgentBoundaryResult write(AgentBoundaryRequest request) {
        String threadId = request.resolveThreadId();

        List<String> trace = new ArrayList<>();
        trace.add("1. 进入 ReactAgent saa-demo-05 演示：threadId=" + threadId);

        // 1. messages 边界：把历史草稿、当前需求、修改意见拆成不同身份的消息。
        List<Message> messages = buildMessages(request);
        trace.add("2. messages 边界已就位，消息顺序=" + describeMessageTypes(messages));

        // 2. instruction 边界：把阶段、平台、聚焦点作为运行时变量注入，而不是写死。
        Map<String, Object> inputs = buildAgentInputs(request, messages);
        trace.add("3. instruction 变量已就位，stage=" + inputs.get("writingStage")
                + "，platform=" + inputs.get("publishingPlatform")
                + "，focus=" + inputs.get("focusPoint"));

        // 3. model 边界：底层模型只在 Agent 配置里装配，业务层只记录当前模型锚点。
        trace.add("4. model 边界已固定，当前模型=" + modelName);
        trace.add("5. tool 边界已注册，工具=" + TOOL_NAMES);

        RunnableConfig config = RunnableConfig.builder()
                // 4. threadId 是状态保存和后续记忆扩展的锚点，第 5 讲先把它传稳。
                .threadId(threadId)
                .build();

        OverAllState state;
        try {
            // 5. invoke 会返回最终状态，比 call 更适合观察 messages 与工具响应。
            trace.add("6. 调用 ReactAgent.invoke，回收完整状态而不是只拿最终文本");
            state = agentBoundaryWriter.invoke(inputs, config)
                    .orElseThrow(() -> new IllegalStateException("ReactAgent 未返回最终状态"));
        }
        catch (GraphRunnerException exception) {
            throw new IllegalStateException("ReactAgent 四边界演示执行失败", exception);
        }

        String draft = extractDraft(state);
        AgentBoundarySnapshot snapshot = buildSnapshot(state, inputs);
        trace.add("7. 已提取最终草稿与四边界快照，草稿长度=" + draft.length());
        trace.forEach(log::info);

        return new AgentBoundaryResult(threadId, draft, List.copyOf(trace), snapshot);
    }

    /**
     * 构造消息流。
     *
     * @param request 写作请求
     * @return 消息列表
     * @author 公众号：春风不晚
     */
    public List<Message> buildMessages(AgentBoundaryRequest request) {
        List<Message> messages = new ArrayList<>();

        // 1. 第一条 UserMessage 只表达当前用户要什么，避免把平台规则和工具能力硬塞进来。
        messages.add(UserMessage.builder()
                .text(buildRequirementPrompt(request))
                .metadata(Map.of(
                        "stage", "requirement"
                ))
                .build());

        if (request.hasCurrentDraft()) {
            // 2. 上一版草稿用 AssistantMessage 回放，身份清楚，模型才能理解这是“已有输出”。
            messages.add(AssistantMessage.builder()
                    .content(request.normalizedCurrentDraft())
                    .build());
        }

        if (request.hasRevisionComment()) {
            // 3. 修改意见继续用 UserMessage 表达，避免把“旧答案”和“新要求”揉成一团。
            messages.add(UserMessage.builder()
                    .text(buildRevisionPrompt(request))
                    .metadata(Map.of(
                            "stage", "revision"
                    ))
                    .build());
        }

        return List.copyOf(messages);
    }

    /**
     * 构造 Agent 输入。
     *
     * @param request 写作请求
     * @param messages 消息列表
     * @return Agent 输入
     * @author 公众号：春风不晚
     */
    public Map<String, Object> buildAgentInputs(AgentBoundaryRequest request, List<Message> messages) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        // 1. messages 是 Agent 当前上下文主体，由框架按消息角色交给底层模型。
        inputs.put("messages", messages);
        // 2. writingStage 是 instruction 模板变量，用来区分首稿与改稿。
        inputs.put("writingStage", request.normalizedWritingStage());
        // 3. publishingPlatform 是 instruction 模板变量，也会被工具读取为平台规则入口。
        inputs.put("publishingPlatform", request.normalizedPublishingPlatform());
        // 4. focusPoint 是 instruction 模板变量，负责收紧本轮生成目标。
        inputs.put("focusPoint", request.normalizedFocusPoint());
        return inputs;
    }

    /**
     * 构建四边界快照。
     *
     * @param state 最终状态
     * @param inputs Agent 输入
     * @return 边界快照
     * @author 公众号：春风不晚
     */
    public AgentBoundarySnapshot buildSnapshot(OverAllState state, Map<String, Object> inputs) {
        List<Message> stateMessages = extractMessages(state);
        return new AgentBoundarySnapshot(
                List.of(
                        "writingStage=" + inputs.get("writingStage"),
                        "publishingPlatform=" + inputs.get("publishingPlatform"),
                        "focusPoint=" + inputs.get("focusPoint")
                ),
                stateMessages.stream()
                        .map(message -> message.getMessageType().name())
                        .toList(),
                modelName,
                TOOL_NAMES
        );
    }

    private String buildRequirementPrompt(AgentBoundaryRequest request) {
        return """
                请围绕下面的要求，生成一篇中文技术文章草稿：
                
                主题：%s
                目标读者：%s
                
                草稿要求：
                1. 标题必须表达明确判断
                2. 开头必须说明传统写法的问题
                3. 主体必须解释 instruction、messages、model、tool 四个边界
                4. 结尾必须说明这种拆分对后续多 Agent 与 Graph 的价值
                """.formatted(request.normalizedTopic(), request.normalizedTargetReader());
    }

    private String buildRevisionPrompt(AgentBoundaryRequest request) {
        return """
                请基于上一版草稿继续修改，不要推倒重来。
                
                本轮修改意见：
                %s
                """.formatted(request.normalizedRevisionComment());
    }

    /**
     * 提取返回值
     * @param state
     * @return
     */
    private String extractDraft(OverAllState state) {
        List<Message> messages = extractMessages(state);
        for (int index = messages.size() - 1; index >= 0; index--) {
            Message message = messages.get(index);
            if (message instanceof AssistantMessage assistantMessage
                    && StringUtils.hasText(assistantMessage.getText())) {
                return assistantMessage.getText().trim();
            }
        }
        throw new IllegalStateException("最终状态中没有找到 AssistantMessage 草稿");
    }

    @SuppressWarnings("unchecked")
    private List<Message> extractMessages(OverAllState state) {
        Object rawValue = state.value("messages").orElse(List.of());
        if (!(rawValue instanceof List<?> rawMessages)) {
            return List.of();
        }

        List<Message> messages = new ArrayList<>();
        for (Object rawMessage : rawMessages) {
            if (rawMessage instanceof Message message) {
                messages.add(message);
            }
        }
        return List.copyOf(messages);
    }

    private String describeMessageTypes(List<Message> messages) {
        return messages.stream()
                .map(message -> message.getMessageType().name())
                .toList()
                .toString();
    }

}
