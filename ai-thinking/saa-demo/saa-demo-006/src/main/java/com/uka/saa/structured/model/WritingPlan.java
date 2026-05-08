package com.uka.saa.structured.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 正文写作计划。
 *
 * @author 公众号：春风不晚
 */
@Data
public class WritingPlan {

    /**
     * 工作标题。
     *
     * @author 公众号：春风不晚
     */
    private String workingTitle;

    /**
     * 选题判断。
     *
     * @author 公众号：春风不晚
     */
    private String topicJudgment;

    /**
     * 核心立场。
     *
     * @author 公众号：春风不晚
     */
    private String coreThesis;

    /**
     * 读者切入点。
     *
     * @author 公众号：春风不晚
     */
    private String readerHook;

    /**
     * 章节计划。
     *
     * @author 公众号：春风不晚
     */
    private List<WritingSection> sections = new ArrayList<>();

    /**
     * 结尾策略。
     *
     * @author 公众号：春风不晚
     */
    private String endingStrategy;
}
