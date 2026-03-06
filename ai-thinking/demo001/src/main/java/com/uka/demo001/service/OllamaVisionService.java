package com.uka.demo001.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Ollama 本地视觉模型调用客户端
 * @author 春风不晚
 */
public class OllamaVisionService {

    // Ollama 本地 API 入口
    private static final String API_URL = "http://localhost:11434/api/generate";
    // 指定Ollama中安装的qwen3.5:0.8b模型模型
    private static final String MODEL_NAME = "qwen3.5:0.8b";
    // 针对大模型推理，需要适当放宽超时时间
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
            .readTimeout(120, TimeUnit.SECONDS)   // 也就是“思考”超时
            .build();
    // 响应结果清洗正则
    private static final Pattern MARKDOWN_PATTERN =
            Pattern.compile("(?s)^```json\\s*|^```\\s*|```$");

    public static String analyzeImage(String base64Img) throws IOException {
        // 构造 Payload (JSON)
        // 关键点：设置 stream: false，让接口一次性返回完整结果，方便后端处理
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("prompt", """
                我提供了描述词 请详细描述这张图片的内容，如果是文档请提取关键文字。将最终的数据以纯JSON格式返回，严格遵循以下要求：
                1. 不要使用markdown代码块
                2. 不要使用```json包裹
                3. 不要添加任何解释说明文字
                4. 返回内容必须以{开头，以}结尾。
        """);
        requestBody.put("stream", false);
        requestBody.put("images", Collections.singletonList(base64Img));

        // 3: 发送 POST 请求
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        // 4: 解析响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JSONObject jsonResponse = JSON.parseObject(response.body().string());
                String aiResponse = jsonResponse.getString("response");
                return cleanResponse(aiResponse.trim());
            } else {
                throw new RuntimeException("请求被拒绝，状态码: " + response.code());
            }
        }
    }

    /**
     * 清洗响应字符串，提取JSON部分
     */
    public static String cleanResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "";
        }

        // 1. 去除markdown包裹
        String cleaned = MARKDOWN_PATTERN.matcher(response).replaceAll("").trim();

        // 2. 如果不是以{开头，尝试提取第一个{到最后一个}之间的内容
        if (!cleaned.startsWith("{")) {
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }
        }

        return cleaned;
    }
}
