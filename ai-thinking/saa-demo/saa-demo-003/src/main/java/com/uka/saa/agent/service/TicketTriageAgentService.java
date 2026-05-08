package com.uka.saa.agent.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uka.saa.agent.model.TicketRequest;
import com.uka.saa.agent.model.TicketTriageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent 版本工单分类服务。
 * 该服务负责调用 ReactAgent、解析返回结果并记录执行轨迹。
 *
 * @author 公众号：春风不晚
 */
@Service
public class TicketTriageAgentService {

    private static final String CATEGORY_NETWORK_FAULT = "NETWORK_FAULT";

    private static final String CATEGORY_ACCOUNT_PERMISSION = "ACCOUNT_PERMISSION";

    private static final String CATEGORY_OTHER = "OTHER";

    /**
     * 用于从 Agent 原始输出中提取 JSON 对象。
     * 有些模型会把 JSON 放进代码块里，所以这里不能直接假设返回值就是纯 JSON。
     */
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*}");

    private static final Logger log = LoggerFactory.getLogger(TicketTriageAgentService.class);

    /**
     * 专门负责工单分类的 ReactAgent。
     */
    private final ReactAgent ticketTriageAgent;

    /**
     * Jackson 对象映射器。
     * 这里用它把 JSON 字符串转成 Java 对象。
     */
    private final ObjectMapper objectMapper;

    public TicketTriageAgentService(
            @Qualifier("ticketTriageAgent") ReactAgent ticketTriageAgent,
            ObjectMapper objectMapper) {
        this.ticketTriageAgent = ticketTriageAgent;
        this.objectMapper = objectMapper;
    }

    /**
     * 使用 ReactAgent 对工单进行分类。
     * 该方法会组织输入、调用 Agent、提取 JSON 并返回标准化后的分类结果。
     *
     * @param request 工单请求
     * @return 分类结果与执行轨迹
     * @author 公众号：春风不晚
     */
    public TicketTriageResult classify(TicketRequest request) {
        // trace 记录本次调用的关键步骤，用于输出执行过程。
        List<String> trace = new ArrayList<>();
        trace.add("1. 开始执行 Agent：准备提示词");

        String rawResponse;
        try {
            // 这里触发一次 Agent 调用，并直接获取文本结果。
            trace.add("2. 调用 ReactAgent，让它理解工单文本");
            rawResponse = ticketTriageAgent.call(
                    """
                请根据下面的工单内容完成分类，并严格输出 JSON：
                {
                  "category": "NETWORK_FAULT | ACCOUNT_PERMISSION | OTHER"
                }

                工单内容：
                %s
                """.formatted(request.description())
            ).getText();
        }
        catch (GraphRunnerException exception) {
            throw new IllegalStateException("ReactAgent 执行工单分类失败", exception);
        }

        trace.add("3. Agent 原始输出: " + rawResponse);

        TicketTriageResult parsedResult = parseResponse(rawResponse);
        String normalizedCategory = normalizeCategory(parsedResult.category());
        trace.add("4. 解析结果，得到分类: " + normalizedCategory);

        // 同步输出日志，便于查看执行过程。
        trace.forEach(log::info);

        return new TicketTriageResult(normalizedCategory, List.copyOf(trace));
    }

    /**
     * 构建发给 Agent 的 Prompt。
     * 该方法负责拼接本次调用的输入内容和返回格式约束。
     *
     * @param description 工单文本
     * @return Prompt 文本
     * @author 公众号：春风不晚
     */
    public String buildPrompt(String description) {
        return """
                请根据下面的工单内容完成分类，并严格输出 JSON：
                {
                  "category": "NETWORK_FAULT | ACCOUNT_PERMISSION | OTHER"
                }

                工单内容：
                %s
                """.formatted(description);
    }

    /**
     * 解析 Agent 返回结果。
     * 该方法先提取 JSON，再反序列化为结果对象。
     *
     * @param rawResponse Agent 原始输出
     * @return 解析后的对象
     * @author 公众号：春风不晚
     */
    public TicketTriageResult parseResponse(String rawResponse) {
        try {
            String json = extractJson(rawResponse);
            return objectMapper.readValue(json, TicketTriageResult.class);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("Agent 返回结果不是合法 JSON: " + rawResponse, exception);
        }
    }

    /**
     * 从原始输出里提取 JSON 字符串。
     *
     * @param rawResponse Agent 原始输出
     * @return JSON 字符串
     * @author 公众号：春风不晚
     */
    public String extractJson(String rawResponse) {
        if (!StringUtils.hasText(rawResponse)) {
            throw new IllegalStateException("Agent 返回为空，无法完成工单分类");
        }

        // 先去掉代码块标记，再尝试提取 JSON 对象。
        String normalized = rawResponse
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();

        Matcher matcher = JSON_OBJECT_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return matcher.group();
        }
        return normalized;
    }

    /**
     * 统一归一化分类值。
     * 该方法把模型返回值收敛到固定分类常量。
     *
     * @param category Agent 返回分类
     * @return 标准分类
     * @author 公众号：春风不晚
     */
    public String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return CATEGORY_OTHER;
        }

        String normalized = category.trim().toUpperCase();
        return switch (normalized) {
            case CATEGORY_NETWORK_FAULT -> CATEGORY_NETWORK_FAULT;
            case CATEGORY_ACCOUNT_PERMISSION -> CATEGORY_ACCOUNT_PERMISSION;
            default -> CATEGORY_OTHER;
        };
    }
}
