package com.uka.saa.writer.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.uka.saa.writer.model.WritingRequest;
import com.uka.saa.writer.model.WritingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一版写作助手服务。
 * 该服务负责组织 Prompt、调用 Agent 并返回标准结果。
 *
 * @author 公众号：春风不晚
 */
@Service
public class WritingAssistantService {

    private static final Logger log = LoggerFactory.getLogger(WritingAssistantService.class);

    private final ReactAgent writingAssistantAgent;

    public WritingAssistantService(
            @Qualifier("writingAssistantAgent") ReactAgent writingAssistantAgent) {
        this.writingAssistantAgent = writingAssistantAgent;
    }

    /**
     * 生成文章初稿。
     *
     * @param request 写作请求
     * @return 初稿结果与执行轨迹
     * @author 公众号：春风不晚
     */
    public WritingResult write(WritingRequest request) {
        List<String> trace = new ArrayList<>();
        trace.add("1. 开始执行写作任务：准备输入参数");

        String prompt = buildPrompt(request);
        trace.add("2. 已组装用户写作需求，准备调用 ReactAgent");

        String draft;
        try {
            draft = writingAssistantAgent.call(prompt).getText();
        }
        catch (GraphRunnerException exception) {
            throw new IllegalStateException("写作 Agent 执行失败", exception);
        }

        draft = normalizeDraft(draft);
        trace.add("3. ReactAgent 已返回文章初稿");
        trace.add("4. 初稿长度（字符）：" + draft.length());

        trace.forEach(log::info);
        return new WritingResult(draft, List.copyOf(trace));
    }

    /**
     * 构建本次写作请求的用户 Prompt。
     * 这里不在 Controller 拼接文本，而是在 Service 统一组织，
     * 目的是让输入结构可维护、可测试、可复用。
     *
     * @param request 写作请求
     * @return 用户 Prompt
     * @author 公众号：春风不晚
     */
    public String buildPrompt(WritingRequest request) {
        String topic = normalizeText(request == null ? null : request.topic(), "未命名主题");
        String targetReader = normalizeText(request == null ? null : request.targetReader(), "普通互联网读者");
        String style = normalizeText(request == null ? null : request.style(), "专业、清晰、自然");
        int wordCount = normalizeWordCount(request == null ? null : request.wordCount());

        return """
                请根据下面的要求，直接写出一篇中文文章初稿：
                
                主题：%s
                目标读者：%s
                写作风格：%s
                字数要求：%s 字左右
                
                写作要求：
                1. 标题明确，能够准确传达主题
                2. 开头要快速切入问题
                3. 主体内容要有清晰层次
                4. 结尾要有总结或行动建议
                """.formatted(topic, targetReader, style, wordCount);
    }

    private String normalizeText(String text, String defaultValue) {
        return StringUtils.hasText(text) ? text.trim() : defaultValue;
    }

    private int normalizeWordCount(Integer wordCount) {
        if (wordCount == null || wordCount <= 0) {
            return 1200;
        }
        return wordCount;
    }

    private String normalizeDraft(String draft) {
        if (!StringUtils.hasText(draft)) {
            throw new IllegalStateException("写作 Agent 返回为空，无法生成文章初稿");
        }
        return draft.trim();
    }

}
