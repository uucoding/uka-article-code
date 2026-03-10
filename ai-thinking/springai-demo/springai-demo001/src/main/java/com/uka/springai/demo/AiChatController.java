package com.uka.springai.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChatController {

    private final ChatModel chatModel;

    private final ChatClient chatClient;

    public AiChatController(ChatModel chatModel, ChatClient.Builder chatClientBuilder) {
        this.chatModel = chatModel;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 使用ChatModel访问
     * @param message
     * @return
     */
    @GetMapping("/api/chat-model")
    public String simpleChatModel(@RequestParam String message) {
        return this.chatModel.call(message);
    }

    /**
     * 使用ChatClient访问
     * @param message
     * @return
     */
    @GetMapping("/api/chat-client")
    public String simpleChatClient(@RequestParam String message) {
        return this.chatClient
                .prompt()                  // 1. 初始化对话请求流
                .user(message)             // 2. 传入我们要发送给大模型的文本
                .call()                    // 3. 发送网络请求，同步等待大模型响应
                .content();                // 4. 提取大模型返回的纯文本正文内容
    }
}
