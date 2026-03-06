package com.uka.demo001.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 极速版视觉模型调用服务
 * @author 春风不晚
 */
public class OllamaVisionOptimizeService {

    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "qwen3.5:0.8b";

    // 超时设置
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private static final Pattern MARKDOWN_PATTERN = Pattern.compile("(?s)^```json\\s*|^```\\s*|```$");

    public static String analyzeImage(String base64Img) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL_NAME);

        // 【优化1】System Prompt 封杀思考链
        // 告诉模型不要"想"，直接"做"
        requestBody.put("system", """
            你是一个OCR文字提取与图像分析工具。
            你的唯一任务是提取信息并以JSON格式输出。
            【严厉禁止】：
            1. 禁止输出 "thinking"、"thoughts" 或 <think> 标签。
            2. 禁止包含任何分析过程，直接回答问题。
            3. JSON 中只能包含 "result" 这一个字段。
        """);

        // 【优化2】User Prompt 给出明确的 JSON 模板 (One-Shot)
        requestBody.put("prompt", """
            请分析图片内容。请严格按照以下 JSON 格式直接返回结果：
            {
                "result": "在这里填入你提取到的文字或图片描述"
            }
        """);

        requestBody.put("stream", false);
        requestBody.put("images", Collections.singletonList(base64Img));

        // 【优化3】强制 响应内容是JSON 模式 (Format)
        requestBody.put("format", "json");

        // 【优化4】高级参数：温度设为 0，不让模型发散思维
        JSONObject options = new JSONObject();
        options.put("temperature", 0);
        requestBody.put("options", options);

        // 【优化5】Keep-Alive 参数：让模型常驻显存 30分钟
        requestBody.put("keep_alive", "30m");

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JSONObject jsonResponse = JSON.parseObject(response.body().string());

                // 【特殊处理】应对推理模型的“倔强”
                // 虽然我们禁止了 thinking，但 Qwen 的推理版有时为了满足 format:json 要求，
                // 会自作聪明地把“思考过程”或结果塞进 thinking 字段。
                // 此时，直接读取这个字段反而能拿到数据。
                String aiResponse = jsonResponse.getString("thinking");

                // 如果 thinking 为空，尝试读取标准的 response
                if (aiResponse == null || aiResponse.isEmpty()) {
                    aiResponse = jsonResponse.getString("response");
                }

                return cleanResponse(aiResponse.trim());
            } else {
                throw new RuntimeException("API调用失败: " + response.code());
            }
        }
    }

    // 辅助清洗方法
    public static String cleanResponse(String response) {
        if (response == null || response.isEmpty()) return "";
        String cleaned = MARKDOWN_PATTERN.matcher(response).replaceAll("").trim();
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