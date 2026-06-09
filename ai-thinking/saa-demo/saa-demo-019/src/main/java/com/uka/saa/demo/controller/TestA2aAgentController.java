package com.uka.saa.demo.controller;

import com.alibaba.cloud.ai.a2a.registry.nacos.discovery.NacosAgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test-a2a")
public class TestA2aAgentController {

    @Autowired
    private NacosAgentCardProvider agentCardProvider;

    @GetMapping()
    public Object nacos(@RequestParam("message")String  message) throws GraphRunnerException {
        A2aRemoteAgent remoteAgent = A2aRemoteAgent.builder()
                .name("writing_research_agent")
                .description("负责围绕写作主题生成研究判断和文章结构")
                .agentCardProvider(agentCardProvider)
                .instruction("{input}")  // 将用户输入传递给远程 Agent，这个是必填的，在 A2aNodeActionWithConfig#buildSendStreamingMessageRequest 会基于此进行构造
                .build();
        return remoteAgent.invoke(message).get().value("output");
    }
}
