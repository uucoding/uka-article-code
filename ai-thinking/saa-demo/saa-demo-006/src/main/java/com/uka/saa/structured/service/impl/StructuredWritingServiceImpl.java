package com.uka.saa.structured.service.impl;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.uka.saa.structured.model.StructuredWritingRequest;
import com.uka.saa.structured.model.WritingAssets;
import com.uka.saa.structured.model.WritingBlueprint;
import com.uka.saa.structured.model.WritingBlueprintResult;
import com.uka.saa.structured.model.WritingIntent;
import com.uka.saa.structured.model.WritingPlan;
import com.uka.saa.structured.service.StructuredWritingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 第 6 讲服务实现。
 * 这里的关键动作是把模型返回的 JSON 文本解析成 WritingBlueprint，拒绝让业务继续消费散文。
 *
 * @author 公众号：春风不晚
 */
@Service
public class StructuredWritingServiceImpl implements StructuredWritingService {

    private static final Logger log = LoggerFactory.getLogger(StructuredWritingServiceImpl.class);

    private final ReactAgent structuredWritingAgent;

    private final BeanOutputConverter<WritingBlueprint> outputConverter =
            new BeanOutputConverter<>(WritingBlueprint.class);

    public StructuredWritingServiceImpl(@Qualifier("structuredWritingAgent") ReactAgent structuredWritingAgent) {
        this.structuredWritingAgent = structuredWritingAgent;
    }

    @Override
    public WritingBlueprintResult generateBlueprint(StructuredWritingRequest request) {
        StructuredWritingRequest normalizedRequest = normalizeRequest(request);
        List<String> trace = new ArrayList<>();
        trace.add("1. 进入结构化输出链路：本轮只生成 WritingBlueprint，不直接写正文");

        // 1. 先把用户需求收成 UserMessage，让模型知道本轮交付物是写作蓝图。
        List<Message> messages = buildMessages(normalizedRequest);
        trace.add("2. messages 已就位：" + describeMessages(messages));

        // 2. 再把平台和聚焦点作为 instruction 变量放进输入 Map。
        Map<String, Object> inputs = buildAgentInputs(normalizedRequest, messages);
        trace.add("3. instruction 变量已就位：platform=" + inputs.get("publishingPlatform")
                + "，focus=" + inputs.get("focusPoint"));

        String rawContent;
        try {
            // 3. ReactAgent 会在 outputType 约束下返回 WritingBlueprint 结构对应的 JSON 文本。
            trace.add("4. 调用 ReactAgent.call，要求模型返回 WritingBlueprint 结构化结果");
            rawContent = structuredWritingAgent.call(inputs).getText();
        }
        catch (GraphRunnerException exception) {
            throw new IllegalStateException("结构化写作蓝图生成失败", exception);
        }

        // 4. 服务层把 JSON 文本解析成 Java DTO，后续业务只消费稳定对象。
        WritingBlueprint blueprint = parseBlueprint(rawContent);
        trace.add("5. 已将模型输出解析成 WritingBlueprint DTO");
        trace.add("6. 当前意图=" + safeText(blueprint.getIntent().getIntentType())
                + "，章节数=" + blueprint.getPlan().getSections().size()
                + "，关键词数=" + blueprint.getAssets().getKeywords().size());

        trace.forEach(log::info);
        return new WritingBlueprintResult(
                blueprint,
                rawContent,
                outputConverter.getJsonSchema(),
                List.copyOf(trace)
        );
    }

    /**
     * 组装消息列表。
     *
     * @param request 写作请求
     * @return 消息列表
     * @author 公众号：春风不晚
     */
    public List<Message> buildMessages(StructuredWritingRequest request) {
        // 1. 第 6 讲只保留一条用户消息，避免提前引入多轮记忆复杂度。
        return List.of(UserMessage.builder()
                .text(buildBlueprintPrompt(request))
                .metadata(Map.of("stage", "structured_blueprint"))
                .build());
    }

    /**
     * 组装 Agent 输入。
     *
     * @param request 写作请求
     * @param messages 消息列表
     * @return Agent 输入
     * @author 公众号：春风不晚
     */
    public Map<String, Object> buildAgentInputs(StructuredWritingRequest request, List<Message> messages) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        // 1. messages 是 Agent 识别当前上下文的标准入口。
        inputs.put("messages", messages);
        // 2. publishingPlatform 是 instruction 模板变量，决定资产适配场景。
        inputs.put("publishingPlatform", request.normalizedPublishingPlatform());
        // 3. focusPoint 是 instruction 模板变量，负责收紧结构化输出的判断重心。
        inputs.put("focusPoint", request.normalizedFocusPoint());
        return inputs;
    }

    /**
     * 解析模型输出。
     *
     * @param rawContent 原始 JSON 文本
     * @return 写作蓝图
     * @author 公众号：春风不晚
     */
    public WritingBlueprint parseBlueprint(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalStateException("模型返回为空，无法解析 WritingBlueprint");
        }

        // 1. BeanOutputConverter 负责把模型 JSON 文本映射成 Java DTO。
        WritingBlueprint blueprint = outputConverter.convert(rawContent.trim());
        if (blueprint == null) {
            throw new IllegalStateException("结构化输出解析失败，未得到 WritingBlueprint");
        }

        // 2. 服务层补齐空节点，避免调用方继续做大量 null 判断。
        normalizeBlueprint(blueprint);
        return blueprint;
    }

    private StructuredWritingRequest normalizeRequest(StructuredWritingRequest request) {
        if (request != null) {
            return request;
        }
        return new StructuredWritingRequest(null, null, null, null, null, null, null, null);
    }

    private String buildBlueprintPrompt(StructuredWritingRequest request) {
        return """
                请围绕下面的写作需求，输出一个结构化写作蓝图。
                注意：你现在交付的是 WritingBlueprint，不是完整正文。
                
                主题：%s
                目标读者：%s
                写作风格：%s
                目标字数：%s 字左右
                是否已有草稿：%s
                当前草稿：%s
                用户补充说明：%s
                
                蓝图要求：
                1. intent 必须判断用户到底要 write_new、rewrite、expand、polish、generate_assets 中哪一种
                2. plan 必须给出 workingTitle、topicJudgment、coreThesis、readerHook、sections、endingStrategy
                3. sections 必须有 3 到 5 个章节，每个章节都要有 heading、purpose、keyPoints、suggestedWordCount
                4. assets 必须包含 recommendedTitle、titleCandidates、summary、keywords
                5. risks 至少给出 2 条结构化输出或后续写作风险
                6. nextAction 必须说明下一步应该进入哪个写作阶段
                """.formatted(
                request.normalizedTopic(),
                request.normalizedTargetReader(),
                request.normalizedStyle(),
                request.normalizedWordCount(),
                request.hasCurrentDraft() ? "是" : "否",
                request.normalizedCurrentDraft(),
                request.normalizedUserComment()
        );
    }

    private void normalizeBlueprint(WritingBlueprint blueprint) {
        if (blueprint.getIntent() == null) {
            blueprint.setIntent(new WritingIntent());
        }
        if (blueprint.getPlan() == null) {
            blueprint.setPlan(new WritingPlan());
        }
        if (blueprint.getAssets() == null) {
            blueprint.setAssets(new WritingAssets());
        }
        if (blueprint.getRisks() == null) {
            blueprint.setRisks(List.of());
        }
        if (blueprint.getPlan().getSections() == null) {
            blueprint.getPlan().setSections(List.of());
        }
        if (blueprint.getAssets().getTitleCandidates() == null) {
            blueprint.getAssets().setTitleCandidates(List.of());
        }
        if (blueprint.getAssets().getKeywords() == null) {
            blueprint.getAssets().setKeywords(List.of());
        }
    }

    private String describeMessages(List<Message> messages) {
        return messages.stream()
                .map(message -> message.getMessageType().name())
                .collect(Collectors.joining(" -> "));
    }

    private String safeText(String text) {
        return StringUtils.hasText(text) ? text.trim() : "未提供";
    }

}
