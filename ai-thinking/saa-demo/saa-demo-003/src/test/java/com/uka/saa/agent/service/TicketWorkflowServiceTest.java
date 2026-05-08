package com.uka.saa.agent.service;

import com.uka.saa.agent.AgentAiApplication;
import com.uka.saa.agent.model.TicketFlowResult;
import com.uka.saa.agent.model.TicketRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Graph 工单流程服务测试。
 * 该测试用于验证图执行后的分类结果、分发结果和节点轨迹。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = AgentAiApplication.class)
public class TicketWorkflowServiceTest {

    @jakarta.annotation.Resource
    private TicketWorkflowService ticketWorkflowService;

    /**
     * 网络问题应该分发到网络组。
     *
     * @author 公众号：春风不晚
     */
    @Test
    public void shouldDispatchNetworkTicketToNetworkGroup() {
        TicketFlowResult result = ticketWorkflowService.dispatch(
                new TicketRequest("公司网络断了")
        );

        System.out.println("Graph 执行过程（高优工单）：");
        result.trace().forEach(System.out::println);
    }

    /**
     * 账号问题应该分发到账号组。
     *
     * @author 公众号：春风不晚
     */
    @Test
    public void shouldDispatchAccountTicketToAccountGroup() {
        TicketFlowResult result = ticketWorkflowService.dispatch(
                new TicketRequest("我的账号登录不上了")
        );

        System.out.println("Graph 执行过程（普通工单）：");
        result.trace().forEach(System.out::println);
    }

    /**
     * 其他问题应该默认分发到服务台。
     *
     * @author 公众号：春风不晚
     */
    @Test
    public void shouldDispatchOtherTicketToServiceDesk() {
        TicketFlowResult result = ticketWorkflowService.dispatch(
                new TicketRequest("投影仪坏了")
        );

        System.out.println("Graph 执行过程（其他工单）：");
        result.trace().forEach(System.out::println);
    }
}
