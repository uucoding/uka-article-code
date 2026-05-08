package com.uka.saa.structured.model;

import lombok.Data;

/**
 * 写作意图识别结果。
 *
 * @author 公众号：春风不晚
 */
@Data
public class WritingIntent {

    /**
     * 意图类型：write_new、rewrite、expand、polish、generate_assets。
     *
     * @author 公众号：春风不晚
     */
    private String intentType;

    /**
     * 意图中文名称。
     *
     * @author 公众号：春风不晚
     */
    private String intentName;

    /**
     * 判断原因。
     *
     * @author 公众号：春风不晚
     */
    private String reason;

    /**
     * 是否复用已有草稿。
     *
     * @author 公众号：春风不晚
     */
    private Boolean useExistingDraft;

    /**
     * 建议进入的下一阶段。
     *
     * @author 公众号：春风不晚
     */
    private String recommendedStage;
}
