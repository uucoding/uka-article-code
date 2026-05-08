package com.uka.saa.structured.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 写作蓝图。
 * 这是第 6 讲的核心 DTO，用来替代不可治理的自由文本结果。
 *
 * @author 公众号：春风不晚
 */
@Data
public class WritingBlueprint {

    /**
     * 用户意图识别结果。
     *
     * @author 公众号：春风不晚
     */
    private WritingIntent intent = new WritingIntent();

    /**
     * 正文计划。
     *
     * @author 公众号：春风不晚
     */
    private WritingPlan plan = new WritingPlan();

    /**
     * 发布资产。
     *
     * @author 公众号：春风不晚
     */
    private WritingAssets assets = new WritingAssets();

    /**
     * 下一步执行建议。
     *
     * @author 公众号：春风不晚
     */
    private String nextAction;

    /**
     * 风险提示。
     *
     * @author 公众号：春风不晚
     */
    private List<String> risks = new ArrayList<>();
}
