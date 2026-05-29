package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.uka.saa.demo.hook.TwoStepRagMessagesHook;
import com.uka.saa.demo.model.KnowledgeSearchRequest;
import com.uka.saa.demo.model.KnowledgeSearchResponse;
import com.uka.saa.demo.interceptor.RagContextModelInterceptor;
import com.uka.saa.demo.rag.LessonKnowledgeBase;
import com.uka.saa.demo.tool.KnowledgeSearchTool;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.uka.saa.demo.hook.TwoStepRagMessagesHook.RAG_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 18 讲 RAG 集成测试。
 * 需要 AI_DASHSCOPE_API_KEY，因为内存 VectorStore 的写入也会调用真实 EmbeddingModel。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = SaaDemo018Application.class)
public class TestRagAgent {

    @Autowired
    @Qualifier("twoStepRagAgent")
    private ReactAgent twoStepRagAgent;

    @Autowired
    @Qualifier("agenticRagAgent")
    private ReactAgent agenticRagAgent;

    @Autowired
    @Qualifier("hybridRagAgent")
    private ReactAgent hybridRagAgent;

    @Test
    void testTwoStepRagAgent() throws Exception {
        AssistantMessage message = twoStepRagAgent.call("两步 RAG 适合什么场景？",
                RunnableConfig.builder().threadId("lesson-018-two-step").build());

        System.out.println(message.getText());
    }

    @Test
    void testAgenticRagAgent() throws Exception {
        AssistantMessage message = agenticRagAgent.call("介绍一下 Agentic RAG",
                RunnableConfig.builder().threadId("lesson-018-agentic-rag").build());
        System.out.println(message.getText());
    }

    @Test
    void testHybridRagAgent() throws Exception {
        AssistantMessage message = hybridRagAgent.call("介绍一下 混合 RAG",
                RunnableConfig.builder().threadId("lesson-018-two-step").build());

        System.out.println(message.getText());
    }
}
