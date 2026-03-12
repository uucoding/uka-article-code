package com.uka.springai.demo;

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
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 使用常规方法指定
     * @param subject
     * @param message
     * @return
     */
    @GetMapping("/api/teacher")
    public String teacher(@RequestParam String subject, @RequestParam String message) {
         // 1. 定义包含 {占位符} 的纯文本模板
        String systemTemplate = "现在你是一名{subject}老师。";

        return this.chatClient.prompt()
                // 2. 注入 System 模板，并通过 .param() 绑定 Map 变量
                .system(sp -> sp.text(systemTemplate)
                        .param("subject", subject))
                // 3. User 消息同理也可以用模板
                .user(message)
                .call()
                .content();
    }

    /**
     * 使用Prompt直接定义
     * @param subject
     * @param message
     * @return
     */
    @GetMapping("/api/teacher2")
    public String teacher2(@RequestParam String subject, @RequestParam String message) {
        // 构建用户信息
        UserMessage userMessage = UserMessage.builder().text(message).build();
        // 使用模版创建系统信息
        String systemTemplate = "现在你是一名{subject}老师。";
        SystemPromptTemplate systemPromptTemplate = SystemPromptTemplate.builder()
                .template(systemTemplate)
                .variables(Map.of("subject", subject))
                .build();
        // 构建系统消息
        Message systemMessage = systemPromptTemplate.createMessage();

        // 构建messageList
        Prompt prompt = Prompt.builder()
                .messages(Arrays.asList(userMessage, systemMessage))
                .build();

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    /**
     * 自定义占位符
     * @param subject
     * @return
     */
    @GetMapping("/api/placeholder")
    public String writePoem(@RequestParam String subject) {
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
                        你是一个<subject>老师
                        """)
                .build();
        Message message = promptTemplate.createMessage(Map.of("subject", "美术"));
        return message.getText();
    }

    @Value("classpath:prompts/expert.st")
    private Resource expertPromptResource;

    @GetMapping("/api/expert")
    public String useResourceTemplate() {
        return this.chatClient.prompt()
                // 直接传入 Resource 对象！
                .system(sp -> sp.text(expertPromptResource)
                        .param("role", "Java 性能调优专家")
                        .param("task", "分析长 GC 暂停的原因")
                        .param("format", "Markdown 列表"))
                .user("我的系统经常出现 5 秒以上的 STW，请问怎么排查？")
                .call()
                .content();
    }

    /**
     * 图片识别：使用的是gpt-4o，也可以使用本地的ollama 部署的qwen3.5
     * @return
     */
    @GetMapping("/api/image-vision")
    public String testImageVision() {
        // 1. 从 classpath 读取本地图片资源
        Resource imageResource = new ClassPathResource("tupian.jpg");

        // 2. 构建多模态 UserMessage
        UserMessage userMessage = UserMessage.builder()
                .text("查看这张图的内容")
                .media(Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_JPEG) // 指定媒体类型
                        .data(imageResource)               // 塞入图片数据
                        .build())
                .build();

        // 3. 封装进 Prompt 并发送给大模型
        Prompt prompt = new Prompt(userMessage);

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }



}
