package com.uka.saa.boundary.service.impl;

import com.uka.saa.boundary.AgentBoundaryApplication;
import com.uka.saa.boundary.model.AgentBoundaryRequest;
import com.uka.saa.boundary.model.AgentBoundaryResult;
import com.uka.saa.boundary.service.AgentBoundaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = AgentBoundaryApplication.class)
class AgentBoundaryServiceImplTest {

    @Autowired
    private AgentBoundaryService agentBoundaryService;

    /**
     * 验证消息边界是否按身份拆分。
     *
     * @author 公众号：春风不晚
     */
    @Test
    void testAgentBoundaryServiceImpl() {
        // 构建threadId
        String threadId = UUID.randomUUID().toString();
        // 构建第一次请求
        AgentBoundaryRequest agentBoundaryRequest = new AgentBoundaryRequest(
                "介绍一下 ReactAgent",
                "有 Spring Boot 背景的 Java 后端开发者",
                "first_draft",
                "微信公众号",
                "讲清 instruction、messages、model、tool 的职责边界",
                null,
                null,
                threadId
        );
        AgentBoundaryResult agentBoundaryResult = agentBoundaryService.write(agentBoundaryRequest);
        System.out.println(agentBoundaryResult.draft());
        System.out.println("==================");
        // 构建第二次请求
        agentBoundaryRequest = new AgentBoundaryRequest(
                "介绍一下 ReactAgent",
                "有 Spring Boot 背景的 Java 后端开发者",
                "revision",
                "微信公众号",
                "讲清 instruction、messages、model、tool 的职责边界",
                agentBoundaryResult.draft(),
                "需要加上前言",
                threadId
        );
        agentBoundaryResult = agentBoundaryService.write(agentBoundaryRequest);
        System.out.println(agentBoundaryResult.draft());
    }

}
