package com.uka.springai.demo.providers;

import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceProvider {
	
	private final Map<String, Map<String, String>> userProfiles = new HashMap<>();

	public ResourceProvider() {
		// 初始化一些示例数据
		Map<String, String> zsProfile = new HashMap<>();
		zsProfile.put("name", "张三");
		zsProfile.put("email", "zs@qq.com");
		zsProfile.put("age", "32");
		zsProfile.put("location", "北京");

		Map<String, String> lsProfile = new HashMap<>();
		lsProfile.put("name", "李四");
		lsProfile.put("email", "ls@qq.com");
		lsProfile.put("age", "28");
		lsProfile.put("location", "上海");

		userProfiles.put("zs", zsProfile);
		userProfiles.put("ls", lsProfile);
	}

	/**
	 * 直接将 URI 变量作为参数接收的资源方法。
	 * 注解中的 URI 模板定义了将被提取的变量。
	 */
	@McpResource(uri = "user-profile://{username}", name = "用户详情", description = "使用 URI 变量提供特定用户的详细信息")
	public ReadResourceResult getUserDetails(String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(
				List.of(new TextResourceContents("user-profile://" + username, "text/plain", profileInfo)));
	}

	/**
	 * 格式化用户信息
	 * @param profile
	 * @return
	 */
	private String formatProfileInfo(Map<String, String> profile) {
		if (profile.isEmpty()) {
			return "用户信息没找到";
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : profile.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		return sb.toString().trim();
	}

}
