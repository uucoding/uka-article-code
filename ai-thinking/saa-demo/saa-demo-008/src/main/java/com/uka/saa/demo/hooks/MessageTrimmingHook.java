package com.uka.saa.demo.hooks;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 消息修剪 Hook。
 * 短期记忆不能无限追加，本示例只保留最近 N 条消息。
 *
 * @author 公众号：春风不晚
 */
public class MessageTrimmingHook extends MessagesModelHook {

    private final int maxMessages;

    public MessageTrimmingHook(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public String getName() {
        return "message_trimming";
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        if (previousMessages.size() <= maxMessages) {
            System.out.println("=========> MessageTrimmingHook: 当前消息数=" + previousMessages.size() + "，无需修剪");
            return new AgentCommand(previousMessages);
        }

        // 1. 课程 Demo 采用最简单的窗口策略，只保留最近 maxMessages 条消息。
        List<Message> trimmedMessages = previousMessages.subList(previousMessages.size() - maxMessages, previousMessages.size());
        System.out.println("=========> MessageTrimmingHook: 消息数从 " + previousMessages.size()
                + " 修剪为 " + trimmedMessages.size());
        return new AgentCommand(trimmedMessages);
    }

}
