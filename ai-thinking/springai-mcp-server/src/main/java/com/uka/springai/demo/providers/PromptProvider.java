package com.uka.springai.demo.providers;

import io.modelcontextprotocol.spec.McpSchema.*;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prompt 生成
 *
 * @author 公众号：春风不晚
 */
@Service
public class PromptProvider {

		/**
		 * 生成个性化消息提示
		 * @param name 用户名称
		 * @return 个性化消息
		 */
		@McpPrompt(name = "personalized-message",
				description = "根据用户信息生成个性化消息")
		public GetPromptResult personalizedMessage(@McpArg(name = "name", description = "用户名称", required = true) String name) {

			StringBuilder message = new StringBuilder();
			message.append("\n你好, ").append(name).append("!\n");

			message
				.append("我在此可以解答您关于“模型上下文协议”方面的任何疑问。");

			return new GetPromptResult("个性化消息",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
		}
}
