package com.uka.saa.agent.service;

import com.uka.saa.agent.AgentAiApplication;
import com.uka.saa.agent.model.TicketRequest;
import com.uka.saa.agent.model.TicketTriageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Agent 工单分类服务测试。
 * 该测试用于验证工单分类服务的调用链路和结果输出。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = AgentAiApplication.class)
public class TicketTriageAgentServiceTest {

    private static final String CATEGORY_NETWORK_FAULT = "NETWORK_FAULT";

    @Autowired
    private TicketTriageAgentService ticketTriageAgentService;

    /**
     * 验证 Agent 调用后的执行轨迹输出。
     *
     * @author 公众号：春风不晚
     */
    @Test
    public void shouldClassifyTicketByAgent() {
        TicketTriageResult result = ticketTriageAgentService.classify(
                new TicketRequest("公司网络断了")
        );

        System.out.println("Agent 执行过程：");
        result.trace().forEach(System.out::println);
    }
}
