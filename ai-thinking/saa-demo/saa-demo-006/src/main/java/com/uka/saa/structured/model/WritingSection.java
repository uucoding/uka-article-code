package com.uka.saa.structured.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个章节计划。
 *
 * @author 公众号：春风不晚
 */
@Data
public class WritingSection {

    /**
     * 章节标题。
     *
     * @author 公众号：春风不晚
     */
    private String heading;

    /**
     * 章节目的。
     *
     * @author 公众号：春风不晚
     */
    private String purpose;

    /**
     * 关键点。
     *
     * @author 公众号：春风不晚
     */
    private List<String> keyPoints = new ArrayList<>();

    /**
     * 建议字数。
     *
     * @author 公众号：春风不晚
     */
    private Integer suggestedWordCount;
}
