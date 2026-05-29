package com.uka.saa.demo.model;

import java.util.List;

/**
 * Agentic RAG 检索工具返回值。
 *
 * @author 公众号：春风不晚
 */
public record KnowledgeSearchResponse(List<String> snippets) {
}
