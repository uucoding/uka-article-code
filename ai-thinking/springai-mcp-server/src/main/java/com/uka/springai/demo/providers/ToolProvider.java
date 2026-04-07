package com.uka.springai.demo.providers;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import org.springframework.stereotype.Service;

/**
 * 工具测试
 *
 * @author 公众号：春风不晚
 */
@Service
public class ToolProvider {

	public record Person(String name, Number age) {}

	@McpTool(description = "测试工具", name = "tool", generateOutputSchema = true)
	public String toolLoggingSamplingElicitationProgress(McpSyncRequestContext ctx, @McpToolParam String input) {
		ctx.info("调用工具"); // 调用客户端日志（info 级别）

		ctx.progress(p -> p.percentage(25).message("工具开始执行")); // 调用客户端进度

		ctx.ping(); // 调用客户端 ping

		StructuredElicitResult<Person> elicitationResult = ctx.elicit(e -> e.message("客户端填充用户数据"), Person.class);


		ctx.progress(p -> p.progress(50).message("客户端填充用户数据完成"));

		CreateMessageResult samplingResponse = ctx.sample(s -> s
			.message("sampling 测试消息")
			.maxTokens(500)
			.modelPreferences(mp -> mp.modelHints("OpenAi", "Ollama")
					.costPriority(1.0)
					.speedPriority(1.0)
					.intelligencePriority(1.0)));

		ctx.progress(p -> p.progress(100).message("sampling 测试消息响应完成"));

		ctx.info("工具执行完成");

		return "响应: " + samplingResponse.toString() + ", " + elicitationResult.toString();
	}

}
