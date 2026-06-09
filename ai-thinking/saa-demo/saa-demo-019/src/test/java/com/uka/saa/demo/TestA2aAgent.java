package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.a2a.RemoteAgentCardProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.util.Optional;

/**
 * 第 19 讲 A2A 测试。
 * 重点验证发现到的 AgentCard 会被 A2aRemoteAgent 用于真实 HTTP 调用。
 *
 * @author 公众号：春风不晚
 */
public class TestA2aAgent {

    /**
     * 测试 RemoteAgentCardProvider
     *
     * 公众号：春风不晚
     * @throws Exception
     */
    @Test
    public void testRemoteAgent() throws Exception {

        AgentCardProvider agentCardProvider = RemoteAgentCardProvider.newProvider("http://localhost:9519");
        A2aRemoteAgent remoteAgent = A2aRemoteAgent.builder()
                .name("writing_research_agent")
                .description("负责围绕写作主题生成研究判断和文章结构")
                .agentCardProvider(agentCardProvider)
                .instruction("{input}")  // 将用户输入传递给远程 Agent，这个是必填的，在 A2aNodeActionWithConfig#buildSendStreamingMessageRequest 会基于此进行构造
                .build();
        Optional<OverAllState> result = remoteAgent.invoke("请讲清 A2A 和普通 HTTP 调用的区别");
        result.ifPresent(state -> {
            System.out.println("调用成功: " + state.value("output"));
        });
    }

    /**
     * 测试 RemoteAgentCardProvider
     *
     * 公众号：春风不晚
     * @throws Exception
     */
    @Test
    public void testNacosAgent() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        // 构建带参数的 URL
        String url = UriComponentsBuilder.fromHttpUrl("http://localhost:9519/test-a2a")
                .queryParam("message", "请讲清 A2A 和普通 HTTP 调用的区别")
                .toUriString();

        // 发送 GET 请求，接收 String 类型响应
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        System.out.println("调用成功: " + response.getBody());
    }

}
