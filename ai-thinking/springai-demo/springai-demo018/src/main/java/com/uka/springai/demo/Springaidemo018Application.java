package com.uka.springai.demo;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Springaidemo018Application {

    public static void main(String[] args) {
        SpringApplication.run(Springaidemo018Application.class, args).close();
    }


    @Bean
    public CommandLineRunner predefinedQuestions(
            List<McpSyncClient> mcpClients) {

        return args -> {

            for (McpSyncClient mcpClient : mcpClients) {
                System.out.println(">>> MCP Client: " + mcpClient.getClientInfo());

                // ======== 工具调用 ========
                McpSchema.CallToolRequest toolRequest = McpSchema.CallToolRequest.builder()
                        .name("tool")
                        .arguments(Map.of("input", "test input"))
                        .progressToken("工具标记")
                        .build();
                McpSchema.CallToolResult response = mcpClient.callTool(toolRequest);
                System.out.println("【tool工具响应】: " + response);
                // ======== 数据补全 ========
                McpSchema.CompleteResult nameCompletion = mcpClient.completeCompletion(
                        new McpSchema.CompleteRequest(
                                new McpSchema.PromptReference("personalized-message"),
                                new McpSchema.CompleteRequest.CompleteArgument("name", "张")));

                System.out.println("【姓名补全】: " + nameCompletion.completion());

                String nameValue = nameCompletion.completion().values().get(0);

                // ======== 提示词 ========
                try {
                    McpSchema.GetPromptResult promptResponse = mcpClient
                            .getPrompt(new McpSchema.GetPromptRequest("personalized-message", Map.of("name", nameValue)));

                    System.out.println("【提示词响应】: " + promptResponse);
                } catch (Exception e) {
                    System.err.println("Error getting prompt: " + e.getMessage());
                }
                // ======== 资源读取 ========
                var resourceResponse = mcpClient.readResource(new McpSchema.ReadResourceRequest("user-profile://zs"));

                System.out.println("【资源响应】: " + resourceResponse);

            }
        };
    }
}
