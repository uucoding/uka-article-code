package com.uka.springai.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/api/interview")
    public String mockInterview(@RequestParam String message) {
        return this.chatClient.prompt()
                // 1. 注入 System 消息，设定严厉的人设
                .system("你是一个严格的、甚至有点毒舌的高级 Java 面试官。对用户的回答要一针见血地指出缺点，不要说废话，要求十分苛刻。")
                // 2. 注入 User 消息（用户的回答）
                .user(message)
                // 3. 发送请求并获取纯文本内容
                .call()
                .content();
    }

    /**
     * 创建一个 poem 生成器
     * @param message
     * @return
     */
    @GetMapping("/api/creative-writer")
    public String writePoem(@RequestParam String message) {
        return this.chatClient.prompt()
                .system("你是一个疯狂的现代派诗人。")
                .user(message)
                .call()
                .content();
    }

    /**
     * 创建一个 poem 生成器 覆盖YML中的配置
     * @param message
     * @return
     */
    @GetMapping("/api/creative-writer2")
    public String writePoem2(@RequestParam String message) {
        return this.chatClient.prompt()
                .system("你是一个疯狂的现代派诗人。")
                .user(message)
                // 动态设定参数：调高温度激发创造力，限制最大长度
                .options(ChatOptions.builder()
                        .temperature(1.0) // 高温，思维发散
                        .topP(0.8)
                        .maxTokens(20)    // 限制不要写太长
                        .build())
                .call()
                .content();
    }
}
