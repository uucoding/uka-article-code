package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Springaidemo013Application.class)
public class TestRag {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private VectorStore vectorStore;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testQuestionAnswerAdvisor() {
        ChatClient chatClient = chatClientBuilder.build();
        String message = "公司打车怎么报销";
        String content = chatClient.prompt()
                .user(message)
                .advisors(
                        // 挂载 RAG 拦截器 QuestionAnswerAdvisor
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(3)
                                                .similarityThreshold(0.75)
                                                .filterExpression("year == '2026'")
                                                .build()
                                )
                                .build()
                )
                .call() // 发送请求 (拦截器会自动拿着问题去查库、组装 Prompt)
                .content();
        System.out.println(content);
    }

    /**
     * 公众号：春风不晚
     */
    @Test
    void testQuestionAnswerAdvisorCustomPrompt() {
        String customPrompt = """
        你现在是公司资深的 HR 专家。请严格基于以下【参考资料】的内容回答员工的问题。
        要求：
        1. 语气必须专业、耐心。
        2. 如果参考资料中没有相关信息，请直接回答：“很抱歉，当前的规章制度中暂未找到说明。” 绝不许捏造事实！
        
        【参考资料】：
        ---------------------
        {question_answer_context}
        ---------------------
        【用户问题】：
        ---------------------
        {query}
        ---------------------
        """;
        ChatClient chatClient = chatClientBuilder.build();
        String message = "公司打车怎么报销";
        String content = chatClient.prompt()
                .user(message)
                .advisors(
                        // 挂载 RAG 拦截器 QuestionAnswerAdvisor
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(3)
                                                .similarityThreshold(0.75)
                                                .filterExpression("year == '2026'")
                                                .build()
                                )
                                .promptTemplate(
                                        PromptTemplate.builder().template(customPrompt).build()
                                )
                                .build()
                )
                .call() // 发送请求 (拦截器会自动拿着问题去查库、组装 Prompt)
                .content();
        System.out.println(content);
    }

    /**
     * 公众号：春风不晚
     */
    @Test
    void testRetrievalAugmentationAdvisor() {
        // 定义文档检索器，最终会转化成 SearchRequest 去查询向量库
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.75)
                .topK(3)
                .build();
        // 组装超级 RAG 流水线
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                // 模块 1：润色组件 (帮含糊不清的提问重新组织语言)
                .queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClientBuilder).build())
                // 模块 2：扩展组件 (把 1 个问题扩展成多个变体视角，提高命中率)
                .queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClientBuilder).build())
                // 模块 3：绑定真正的检索器
                .documentRetriever(retriever)
                // (你甚至可以在这里继续挂载 Joiner 和 PostProcessor...)
                .build();
        // 发送请求
        ChatClient chatClient = chatClientBuilder.build();
        String message = "公司打车怎么报销";
        String content = chatClient.prompt()
                .user(message)
                .advisors(ragAdvisor) // 挂载全新模块化 RAG 引擎
                .call()
                .content();
        System.out.println(content);
    }
}
