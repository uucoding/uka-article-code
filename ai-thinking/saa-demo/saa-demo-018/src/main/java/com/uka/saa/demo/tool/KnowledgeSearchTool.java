package com.uka.saa.demo.tool;

import com.uka.saa.demo.model.KnowledgeSearchRequest;
import com.uka.saa.demo.model.KnowledgeSearchResponse;
import com.uka.saa.demo.rag.LessonKnowledgeBase;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Agentic RAG 的检索工具。
 * 生产环境可以把这里的 VectorStore 替换为 Redis、Milvus、Elasticsearch 或内部文档系统。
 *
 * @author 公众号：春风不晚
 */
@Component
public class KnowledgeSearchTool {

    private final LessonKnowledgeBase knowledgeBase;

    public KnowledgeSearchTool(LessonKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public KnowledgeSearchResponse search(KnowledgeSearchRequest request) {
        return new KnowledgeSearchResponse(LessonKnowledgeBase.snippets(knowledgeBase.search(request.query())));
    }

    public ToolCallback asToolCallback() {
        return FunctionToolCallback.builder("search_lesson_knowledge",
                        (Function<KnowledgeSearchRequest, KnowledgeSearchResponse>) this::search)
                .description("检索第 18 讲课程知识，适合回答 RAG、Agent Framework、A2A 预告相关问题。")
                .inputType(KnowledgeSearchRequest.class)
                .build();
    }

}
