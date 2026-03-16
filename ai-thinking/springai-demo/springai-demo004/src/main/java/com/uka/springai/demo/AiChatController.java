package com.uka.springai.demo;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Map;

@RestController
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/api/movie")
    public MovieRecommendation recommendMovie(@RequestParam String genre) {
        // 直接要求返回 MovieRecommendation 类的实例！
        return chatClient.prompt()
                .user("请推荐一部经典的" + genre + "电影")
                .call()
                .entity(MovieRecommendation.class);
    }

    public record MovieRecommendation(
            String title,
            String director,
            String summary
    ) {}


    /**
     * 注意：produces 必须指定为 TEXT_EVENT_STREAM_VALUE (即 SSE 协议)
     */
    @GetMapping(value = "/api/stream/poem", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> writePoemStream(@RequestParam String topic,
                                        HttpServletResponse response) {
        // 显式设置响应头 （防止乱码）
        response.setContentType("text/event-stream;charset=UTF-8");
        // 返回 Flux<String>，Spring Boot 会自动将其推送到 HTTP 响应流中
        return chatClient.prompt()
                .user("请写一首关于 " + topic + " 的现代长诗")
                .stream()     // 开启底层大模型的 Stream 模式
                .content();   // 提取源源不断流过来的字符内容
    }

}
