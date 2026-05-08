package com.uka.saa.writer.model;

import java.util.List;

/**
 * 写作结果。
 *
 * @param draft 文章初稿
 * @param trace 执行轨迹
 * @author 公众号：春风不晚
 */
public record WritingResult(
        String draft,
        List<String> trace
) {
}
