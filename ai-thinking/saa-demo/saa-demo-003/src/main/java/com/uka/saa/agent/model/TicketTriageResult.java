package com.uka.saa.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * ReactAgent 分类结果对象。
 * category 保存分类结果，trace 保存本次调用的执行轨迹。
 *
 * @param category 分类结果
 * @param trace 执行轨迹
 * @author 公众号：春风不晚
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketTriageResult(String category, List<String> trace) {
}
