package com.uka.saa.structured.service.impl;

import com.uka.saa.structured.StructuredOutputApplication;
import com.uka.saa.structured.model.StructuredWritingRequest;
import com.uka.saa.structured.model.WritingBlueprint;
import com.uka.saa.structured.model.WritingBlueprintResult;
import com.uka.saa.structured.service.StructuredWritingService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 DTO 契约、输入组装和 JSON 解析。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = StructuredOutputApplication.class)
class StructuredWritingServiceImplTest {

    @Autowired
    private StructuredWritingService  structuredWritingService;

    /**
     * @author 公众号：春风不晚
     */
    @Test
    void shouldBuildAgentInputsForStructuredOutput() {
        StructuredWritingRequest request = new StructuredWritingRequest(
                "Spring AI Alibaba ReactAgent 结构化输出为什么重要?",
                "有 Spring Boot 经验的 Java 后端开发者",
                "技术深度清晰、表达克制",
                "微信公众号",
                "让 Agent 返回可控 DTO",
                800,
                null,
                "先生成蓝图，不要直接写正文"
        );

        WritingBlueprintResult writingBlueprintResult = structuredWritingService.generateBlueprint(request);
        System.out.println(writingBlueprintResult);
    }

}
