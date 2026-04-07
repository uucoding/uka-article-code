package com.uka.springai.demo;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import org.springframework.stereotype.Service;

/**
 * 注解测试
 */
@Service
public class McpClientHandlerProviders {

	private static final Logger logger = LoggerFactory.getLogger(McpClientHandlerProviders.class);

	@McpProgress(clients = "server1")
	public void progressHandler(ProgressNotification progressNotification) {
		logger.info("MCP 进度: [{}] progress: {} total: {} message: {}",
				progressNotification.progressToken(), progressNotification.progress(),
				progressNotification.total(), progressNotification.message());
	}

	@McpLogging(clients = "server1")
	public void loggingHandler(LoggingMessageNotification loggingMessage) {
		logger.info("MCP 日志: [{}] {}", loggingMessage.level(), loggingMessage.data());
	}

	@McpSampling(clients = "server1")
	public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
		logger.info("MCP SAMPLING: {}", llmRequest);

		String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
		String modelHint = llmRequest.modelPreferences().hints().get(0).name();

		return CreateMessageResult.builder()
				.content(new McpSchema.TextContent("Response " + userPrompt + " with model hint " + modelHint))
				.build();
	}

	public record Person(String name, Number age) {}

	@McpElicitation(clients = "server1")
	public StructuredElicitResult<Person> elicitationHandler(McpSchema.ElicitRequest request) {
		logger.info("MCP ELICITATION: {}", request);
		return new StructuredElicitResult<>(ElicitResult.Action.ACCEPT, new Person("王五", 42), null);
	}

}
