package com.uka.saa.agent.model;

/**
 * 工单输入对象。
 * 该对象只保存工单原始文本。
 *
 * @param description 工单原始描述
 * @author 公众号：春风不晚
 */
public record TicketRequest(String description) {
}
