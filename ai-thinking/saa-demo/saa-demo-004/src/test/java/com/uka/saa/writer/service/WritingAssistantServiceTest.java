package com.uka.saa.writer.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.uka.saa.writer.AgentAiApplication;
import com.uka.saa.writer.model.WritingRequest;
import com.uka.saa.writer.model.WritingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 第一版写作助手服务测试。
 * 该测试用于验证 Prompt 组装、Agent 调用与执行轨迹输出。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = AgentAiApplication.class)
class WritingAssistantServiceTest {

    @Autowired
    private WritingAssistantService writingAssistantService;

    @Autowired
    @Qualifier("writingAssistantAgent")
    private ReactAgent writingAssistantAgent;

    /**
     * 验证写作 Agent 是否能够返回文章初稿。
     *
     * @author 公众号：春风不晚
     */
    @Test
    void shouldGenerateDraftByReactAgent() throws Exception {
        WritingResult result = writingAssistantService.write(
                new WritingRequest(
                        "为什么 Java 开发者应该关注 Spring AI Alibaba",
                        "有 Java 与 Spring Boot 背景的后端开发者",
                        "偏技术科普，兼具一点产品视角",
                        500
                )
        );

        System.out.println("AI 写作助手执行过程：");
        result.trace().forEach(System.out::println);
        System.out.println("\n生成初稿：\n");
        System.out.println(result.draft());
    }

}
