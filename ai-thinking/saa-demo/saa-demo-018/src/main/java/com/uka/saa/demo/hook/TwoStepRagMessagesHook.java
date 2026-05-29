package com.uka.saa.demo.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import com.uka.saa.demo.rag.LessonKnowledgeBase;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 两步 RAG：在 ReactAgent 调用模型之前固定检索并替换消息。
 *
 * @author 公众号：春风不晚
 */
@HookPositions({HookPosition.BEFORE_MODEL})
public class TwoStepRagMessagesHook extends MessagesModelHook {

    public static final String RAG_CONTEXT_KEY = "rag_context";

    private final LessonKnowledgeBase knowledgeBase;

    public TwoStepRagMessagesHook(LessonKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    public String getName() {
        return "two_step_rag_messages_hook";
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        String question = latestUserQuestion(previousMessages);
        if (question.isBlank()) {
            return new AgentCommand(previousMessages);
        }

        List<Document> documents = knowledgeBase.search(question);
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        config.context().put(RAG_CONTEXT_KEY, context);

        List<Message> enhancedMessages = new ArrayList<>();
        enhancedMessages.add(new SystemMessage("""
                你是 Spring AI Alibaba 进阶课程的助教。
                请严格基于下面的 RAG 参考资料回答问题。资料不足时直接说明资料不足。

                【RAG 参考资料】
                %s
                """.formatted(context)));
        enhancedMessages.addAll(previousMessages);

        return new AgentCommand(enhancedMessages, UpdatePolicy.REPLACE);
    }

    private String latestUserQuestion(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) instanceof UserMessage userMessage) {
                return userMessage.getText();
            }
        }
        return "";
    }

}
