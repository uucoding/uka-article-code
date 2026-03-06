package com.uka.demo001;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Ollama 本地视觉模型调用客户端
 * @author 春风不晚
 */
public class OllamaVisionClientTest {

    // Ollama 本地 API 入口
    private static final String API_URL = "http://localhost:11434/api/generate";
    // 指定Ollama中安装的qwen3.5:0.8b模型模型
    private static final String MODEL_NAME = "qwen3.5:0.8b";
    // 待处理的本地图片库
    private static final String IMAGE_FOLDER = "/Users/wy/Downloads/imgs";
    // 针对大模型推理，需要适当放宽超时时间
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
            .readTimeout(120, TimeUnit.SECONDS)   // 也就是“思考”超时
            .build();
    // 响应结果清洗正则
    private static final Pattern MARKDOWN_PATTERN =
            Pattern.compile("(?s)^```json\\s*|^```\\s*|```$");

    public static void main(String[] args) {
        File folder = new File(IMAGE_FOLDER);
        // 过滤出 jpg 和 png 图片
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            System.out.println("目录为空，请放入图片文件！");
            return;
        }

        System.out.println("开始处理任务...");

        for (File imgFile : files) {
            try {
                // 核心业务逻辑
                analyzeImage(imgFile);
            } catch (Exception e) {
                System.err.println("处理异常 [" + imgFile.getName() + "]: " + e.getMessage());
            }
        }
    }

    private static void analyzeImage(File imgFile) throws IOException {
        System.out.println("正在读取并分析: " + imgFile.getName() + " ...");

        // 1: 图片转 Base64 (这是 API 接收图片的标准格式)
        byte[] fileContent = Files.readAllBytes(imgFile.toPath());
        String base64Img = Base64.getEncoder().encodeToString(fileContent);

        // 2: 构造 Payload (JSON)
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

                // 打印结果
                System.out.println("[分析完成]");
                System.out.println("--------------------------------------------------");
                System.out.println(cleanResponse(aiResponse.trim()));
                System.out.println("--------------------------------------------------\n");
            } else {
                System.out.println("请求被拒绝，状态码: " + response.code());
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
