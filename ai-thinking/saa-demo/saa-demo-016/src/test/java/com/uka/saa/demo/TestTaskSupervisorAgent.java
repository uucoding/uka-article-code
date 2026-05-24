package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 16 讲 Supervisor 模式测试。
 * 验证专门 Agent 会被 AgentTool 包装成 supervisor 可调用的工具。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest( classes = SaaDemo016Application.class)
public class TestTaskSupervisorAgent {

    @Autowired
    @Qualifier("taskSupervisorAgent")
    private ReactAgent taskSupervisorAgent;

    @Test
    void shouldWrapSpecializedAgentsAsSupervisorTools() throws GraphRunnerException {
        String msg = "请为《Supervisor 如何组织多 Agent 任务》生成结构草案，并检查是否具备清晰主线。";
        AssistantMessage response = taskSupervisorAgent.call(msg);
        System.out.println( response.getText());
    }

}
