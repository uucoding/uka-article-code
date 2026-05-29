package com.uka.saa.demo.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.uka.saa.demo.rag.LessonKnowledgeBase;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.uka.saa.demo.hook.TwoStepRagMessagesHook.RAG_CONTEXT_KEY;

/**
 * 混合 RAG 的最小 AgentHook：Agent 开始时做一次查询增强和检索。
 *
 * @author 公众号：春风不晚
 */
@HookPositions({HookPosition.BEFORE_AGENT})
public class HybridRagAgentHook extends AgentHook {

    public static final String ENHANCED_QUERY_KEY = "enhanced_query";

    private final LessonKnowledgeBase knowledgeBase;

    public HybridRagAgentHook(LessonKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    public String getName() {
        return "hybrid_rag_agent_hook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        String question = latestUserQuestion(state);
        if (question.isBlank()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        String enhancedQuery = enhanceQuery(question);
        List<Document> documents = knowledgeBase.search(enhancedQuery);
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        config.context().put(ENHANCED_QUERY_KEY, enhancedQuery);
        config.context().put(RAG_CONTEXT_KEY, context);
        config.context().put("retrievalVerified", !documents.isEmpty());

        return CompletableFuture.completedFuture(Map.of(
                ENHANCED_QUERY_KEY, enhancedQuery,
                RAG_CONTEXT_KEY, context,
                "retrievalVerified", !documents.isEmpty()
        ));
    }

    private String enhanceQuery(String question) {
        return question + "\n请优先检索 Spring AI Alibaba Agent Framework 中的 RAG 架构边界。";
    }

    private String latestUserQuestion(OverAllState state) {
        Optional<Object> messagesValue = state.value("messages");
        if (messagesValue.isEmpty() || !(messagesValue.get() instanceof List<?> messages)) {
            return "";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) instanceof UserMessage userMessage) {
                return userMessage.getText();
            }
            if (messages.get(i) instanceof Message message && message.getMessageType().name().equals("USER")) {
                return message.getText();
            }
        }
        return "";
    }

}
