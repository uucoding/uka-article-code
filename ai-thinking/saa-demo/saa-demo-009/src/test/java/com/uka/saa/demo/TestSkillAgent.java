package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.ReadSkillTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 第 9 讲 Skills 测试。
 * 测试只调用 Agent，通过 System.out 观察 read_skill 与 shell 的最小链路。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = {SaaDemo009Application.class, TestSkillAgent.FakeChatModelConfig.class})
public class TestSkillAgent {

    @Autowired
    @Qualifier("skillAgent")
    private ReactAgent agent;

    /**
     * 公众号：春风不晚
     */
    @Test
    void testReadWritingSkill() throws Exception {
        System.out.println("=========> 用例一：读取微信公众号写作 Skill");

        AssistantMessage response = agent.call(Map.of(
                "messages", List.of(new UserMessage("""
                        请为 Spring AI Alibaba Skills 这一讲写一个微信公众号开头。
                        如果存在合适的 Skill，请先读取 Skill 再回答。
                        """))
        ), RunnableConfig.builder()
                .threadId("lesson-009-read-skill")
                .addMetadata("_stream_", false)
                .build());

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

    /**
     * 公众号：春风不晚
     */
    @Test
    void testSkillWithShellExtension() throws Exception {
        System.out.println("=========> 用例二：读取 Skill 后调用 shell 扩展脚本");

        AssistantMessage response = agent.call(Map.of(
                "messages", List.of(new UserMessage("""
                        请检查 wechat-writing-rules 这个 Skill 携带了哪些参考资料。
                        如果 Skill 里提供了脚本，请通过 shell 执行脚本并打印结果。
                        """))
        ), RunnableConfig.builder()
                .threadId("lesson-009-shell-extension")
                .addMetadata("_stream_", false)
                .build());

        System.out.println("=========> Agent 最终响应:");
        System.out.println(response.getText());
    }

    /**
     * 测试配置。
     * 用固定模型替换真实 ChatModel，避免测试依赖外部 API。
     *
     * @author 公众号：春风不晚
     */
    @TestConfiguration
    static class FakeChatModelConfig {

        @Bean
        public ChatModel chatModel() {
            return new FakeChatModel();
        }

    }

    /**
     * 测试专用假模型。
     * 用固定决策模拟真实模型的 read_skill 和 shell 调用，避免外部模型影响课程 Demo。
     *
     * @author 公众号：春风不晚
     */
    static class FakeChatModel implements ChatModel {

        private final AtomicInteger callIndex = new AtomicInteger();

        @Override
        public ChatResponse call(Prompt prompt) {
            int index = callIndex.incrementAndGet();
            System.out.println("=========> FakeChatModel 第 " + index + " 次收到 Prompt:");
            System.out.println(prompt.getContents());

            List<ToolResponseMessage.ToolResponse> toolResponses = prompt.getInstructions().stream()
                    .filter(message -> message instanceof ToolResponseMessage)
                    .map(message -> (ToolResponseMessage) message)
                    .flatMap(message -> message.getResponses().stream())
                    .toList();

            boolean hasSkillContent = toolResponses.stream()
                    .anyMatch(response -> response.name().equals(ReadSkillTool.READ_SKILL));
            boolean hasShellResult = toolResponses.stream()
                    .anyMatch(response -> response.name().equals("shell"));
            boolean needShell = prompt.getContents().contains("请检查 wechat-writing-rules 这个 Skill 携带了哪些参考资料");

            AssistantMessage assistantMessage;
            if (!hasSkillContent) {
                // 1. 第一步：模型先按 SkillsInterceptor 暴露的信息调用 read_skill。
                System.out.println("=========> FakeChatModel 决定调用工具: read_skill");
                assistantMessage = AssistantMessage.builder()
                        .content("")
                        .toolCalls(List.of(new AssistantMessage.ToolCall(
                                "tool-read-wechat-skill",
                                "function",
                                ReadSkillTool.READ_SKILL,
                                """
                                        {
                                          "skill_name": "wechat-writing-rules"
                                        }
                                        """
                        )))
                        .build();
            }
            else if (needShell && !hasShellResult) {
                // 2. 第二步：读取 Skill 后，模型看到脚本约定，再调用 shell 执行确定性检查。
                System.out.println("=========> FakeChatModel 决定调用工具: shell");
                assistantMessage = AssistantMessage.builder()
                        .content("")
                        .toolCalls(List.of(new AssistantMessage.ToolCall(
                                "tool-list-reference-files",
                                "function",
                                "shell",
                                """
                                        {
                                          "command": "bash src/main/resources/skills/wechat-writing-rules/scripts/list_reference_files.sh",
                                          "restart": false
                                        }
                                        """
                        )))
                        .build();
            }
            else {
                // 3. 工具返回后，模型基于 Skill 内容或 shell 输出生成最终回答。
                String skillContent = toolResponses.stream()
                        .filter(response -> response.name().equals(ReadSkillTool.READ_SKILL))
                        .map(ToolResponseMessage.ToolResponse::responseData)
                        .findFirst()
                        .orElse("未读取到 Skill 内容");
                String shellResult = toolResponses.stream()
                        .filter(response -> response.name().equals("shell"))
                        .map(ToolResponseMessage.ToolResponse::responseData)
                        .findFirst()
                        .orElse("本轮没有调用 shell");

                System.out.println("=========> FakeChatModel 收到 Skill 内容片段:");
                System.out.println(skillContent.substring(0, Math.min(skillContent.length(), 320)));
                System.out.println("=========> FakeChatModel 收到 shell 结果:");
                System.out.println(shellResult);

                assistantMessage = new AssistantMessage("""
                        Skill 调用链路完成。
                        已读取 Skill：wechat-writing-rules
                        shell 结果：%s
                        
                        示例输出：
                        用户以为 Skills 只是把提示词拆成文件，但真正进入写作平台后，问题不在文件怎么放，而在能力怎么被发现、读取和复用。
                        wechat-writing-rules 这个 Skill 把公众号写作规则、参考资料和可选脚本放在同一个能力目录下，Agent 只在需要时读取它。
                        """.formatted(shellResult));
            }

            return new ChatResponse(List.of(new Generation(assistantMessage)));
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return ChatOptions.builder().build();
        }

    }

}
