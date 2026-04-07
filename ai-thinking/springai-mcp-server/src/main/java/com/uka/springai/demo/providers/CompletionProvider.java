package com.uka.springai.demo.providers;

import org.springaicommunity.mcp.annotation.McpComplete;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 补全
 *
 * @author 公众号：春风不晚
 */
@Service
public class CompletionProvider {

	private final Map<String, List<String>> usernameDatabase = new HashMap<>();

	public CompletionProvider() {
		usernameDatabase.put("张", List.of("张三", "张三丰", "张小小"));
		usernameDatabase.put("李", List.of("李四", "李小明"));
	}

	/**
	 * 用于用户状态提示中的用户名补全方法。
	 */
	@McpComplete(uri = "user-status://{username}")
	public List<String> completeUsername(String usernamePrefix) {
		String prefix = usernamePrefix.toLowerCase();
		if (prefix.isEmpty()) {
			return List.of("Enter a username");
		}

		String firstLetter = prefix.substring(0, 1);
		List<String> usernames = usernameDatabase.getOrDefault(firstLetter, List.of());

		return usernames.stream().filter(username -> username.toLowerCase().startsWith(prefix)).toList();
	}

	@McpComplete(prompt = "personalized-message")
	public List<String> completeName(String name) {
		String prefix = name.toLowerCase();
		if (prefix.isEmpty()) {
			return List.of("Enter a username");
		}

		String firstLetter = prefix.substring(0, 1);
		List<String> usernames = usernameDatabase.getOrDefault(firstLetter, List.of());

		return usernames.stream().filter(username -> username.toLowerCase().startsWith(prefix)).toList();
	}
}
