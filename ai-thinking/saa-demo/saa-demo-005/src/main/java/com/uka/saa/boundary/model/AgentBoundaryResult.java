package com.uka.saa.boundary.model;

import java.util.List;

/**
 * 返回结果。
 *
 * @param threadId 线程标识
 * @param draft 生成或修改后的草稿
 * @param trace 执行轨迹
 * @param snapshot 四个边界快照
 * @author 公众号：春风不晚
 */
public record AgentBoundaryResult(
        String threadId,
        String draft,
        List<String> trace,
        AgentBoundarySnapshot snapshot
) {
}
