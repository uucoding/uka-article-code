package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Springaidemo017Application.class)
public class TestMcp {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 公众号：春风不晚
     */
    @Test
    public void tesWithoutMcp() {
// ========== 对比测试 1：未挂载 MCP 的普通大模型 ==========
        String response = chatClientBuilder.build().prompt()
                .user("为4人晚餐推荐菜单，要求参考 HowToCook 指南")
                .call()
                .content();

        System.out.println("【未挂载 MCP】回答：\n" + response);

        // ========== 对比测试 2：挂载了远端 MCP 外设的超级 Agent ==========
        response = chatClientBuilder.build().prompt()
                .user("请使用 howtocook 的 MCP 服务，为我精准查询并推荐4人晚餐菜单")
                // 将远程拉取到的所有工具，一次性挂载给大模型！
                .toolCallbacks(toolCallbackProvider)
                .call()
                .content();

        System.out.println("【挂载 MCP 后】回答：\n" + response);

    }

}
