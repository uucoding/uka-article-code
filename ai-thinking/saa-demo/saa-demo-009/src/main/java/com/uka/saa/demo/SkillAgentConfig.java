package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 第 9 讲 Skills Agent 配置。
 * 演示 SkillsAgentHook 读取 Skill，以及 Skill 读取后动态注入 Shell 工具。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class SkillAgentConfig {

    /**
     * 注册 classpath 下的 skills 目录。
     *
     * @return ClasspathSkillRegistry
     * @author 公众号：春风不晚
     */
    @Bean
    public ClasspathSkillRegistry skillRegistry() {
        // 1. 从 src/main/resources/skills 扫描 SKILL.md。
        return ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .basePath(System.getProperty("java.io.tmpdir") + "/saa-demo-009-skills")
                .build();
    }

    /**
     * 构建写作 Skill Agent。
     *
     * @param chatModel 底层模型
     * @param skillRegistry Skill 注册表
     * @return ReactAgent
     * @author 公众号：春风不晚
     */
    @Bean("skillAgent")
    public ReactAgent skillAgent(ChatModel chatModel, ClasspathSkillRegistry skillRegistry) {
        ShellTool2 shellTool = ShellTool2.builder(System.getProperty("user.dir"))
                .withCommandTimeout(10_000)
                .withMaxOutputLines(80)
                .build();
        ToolCallback[] shellCallbacks = ToolCallbacks.from(shellTool);

        SkillsAgentHook skillsAgentHook = SkillsAgentHook.builder()
                // 1. SkillsAgentHook 暴露 read_skill 工具，并把 Skill 列表注入模型请求。
                .skillRegistry(skillRegistry)
                // 2. 当模型读取 wechat-writing-rules 后，再把 shell 工具动态注入给模型。
                .groupedTools(Map.of("wechat-writing-rules", List.of(shellCallbacks)))
                .build();

        ShellToolAgentHook shellToolAgentHook = ShellToolAgentHook.builder()
                // 3. ShellToolAgentHook 负责 shell session 的初始化和清理。
                .shellTool2(shellTool)
                .build();

        return ReactAgent.builder()
                .name("skill_agent")
                .systemPrompt("""
                        你是一名严谨的中文技术专栏编辑。
                        当用户任务和可用 Skill 匹配时，先调用 read_skill 读取完整规则，再继续完成任务。
                        如果 Skill 中要求使用底层脚本，并且 shell 工具可用，可以调用 shell 获得确定性结果。
                        """)
                .saver(new MemorySaver())
                .hooks(skillsAgentHook, shellToolAgentHook)
                .model(chatModel)
                .build();
    }

}
