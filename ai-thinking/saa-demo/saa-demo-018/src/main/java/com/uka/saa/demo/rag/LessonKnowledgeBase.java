package com.uka.saa.demo.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 第 18 讲的最小知识库。
 * 文档写入内存 VectorStore 时仍会调用真实 EmbeddingModel 完成向量化。
 *
 * @author 公众号：春风不晚
 */
@Component
public class LessonKnowledgeBase {

    private static final int DEFAULT_TOP_K = 3;

    private final VectorStore vectorStore;

    private final AtomicBoolean loaded = new AtomicBoolean(false);

    public LessonKnowledgeBase(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ensureLoaded() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }
        vectorStore.add(documents());
    }

    public List<Document> search(String query) {
        ensureLoaded();
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(DEFAULT_TOP_K)
                .similarityThreshold(0.0)
                .build());
    }

    public VectorStore vectorStore() {
        ensureLoaded();
        return vectorStore;
    }

    public static List<String> snippets(List<Document> documents) {
        return documents.stream()
                .map(Document::getText)
                .toList();
    }

    private List<Document> documents() {
        return List.of(
                Document.builder()
                        .id("lesson-018-two-step")
                        .text("""
                                两步 RAG 的关键是先检索再生成。用户问题进入系统后，应用先用 VectorStore 召回相关文档，
                                再通过 Advisor 或拦截器把这些上下文注入模型请求。它适合 FAQ、文档问答和规则明确的知识查询。
                                """)
                        .metadata(metadata("two-step-rag"))
                        .build(),
                Document.builder()
                        .id("lesson-018-agentic")
                        .text("""
                                Agentic RAG 会把检索能力包装成工具。Agent 先判断当前任务是否需要外部知识，
                                需要时调用 search_lesson_knowledge 这类工具，不需要时直接回答。它适合研究助手和多信息源任务。
                                """)
                        .metadata(metadata("agentic-rag"))
                        .build(),
                Document.builder()
                        .id("lesson-018-hybrid")
                        .text("""
                                混合 RAG 会在检索链路中加入查询改写、检索验证、答案检查等节点。
                                它结合两步 RAG 的可控性和 Agentic RAG 的灵活性，适合准确性要求更高的领域问答。
                                """)
                        .metadata(metadata("hybrid-rag"))
                        .build(),
                Document.builder()
                        .id("lesson-018-conditional")
                        .text("""
                                第 17 讲的 ConditionalAgent 解决的是确定性规则分支：当条件命中 report 或 error 时，
                                流程应该进入固定分支，而不是继续让模型在 Prompt 中猜测路径。
                                """)
                        .metadata(metadata("conditional-agent"))
                        .build(),
                Document.builder()
                        .id("lesson-019-a2a")
                        .text("""
                                第 19 讲进入 A2A。当前 RAG Agent 还在单应用内工作，A2A 要解决的是跨进程、跨服务、
                                跨 Agent 的发现、通信和协作边界。
                                """)
                        .metadata(metadata("a2a"))
                        .build()
        );
    }

    private Map<String, Object> metadata(String topic) {
        return Map.of(
                "lesson", "018",
                "topic", topic,
                "course", "spring-ai-alibaba-agent-framework"
        );
    }

}
