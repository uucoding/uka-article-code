package com.uka.demo001.controller;

import com.alibaba.fastjson2.JSONObject;
import com.uka.demo001.service.OllamaVisionOptimizeService;
import com.uka.demo001.service.OllamaVisionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/vision")
public class VisionController {

    // 标准版接口
    @PostMapping("/analyze")
    public JSONObject analyze(@RequestBody Map<String, String> payload) {
        try {
            // 调用上一篇写的标准服务
            String result = OllamaVisionService.analyzeImage(payload.get("image"));
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            return JSONObject.of("error", e.getMessage());
        }
    }

    // 🚀 极速版接口
    @PostMapping("/analyze-fast")
    public JSONObject analyzeFast(@RequestBody Map<String, String> payload) {
        try {
            // 调用本次优化的极速服务
            String result = OllamaVisionOptimizeService.analyzeImage(payload.get("image"));
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            return JSONObject.of("error", e.getMessage());
        }
    }
}
