package com.uka.springai.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherTools {

    // 1. 定义出参 (Java Record)
    public record WeatherResponse(double temp, String unit, String condition) {}

    /**
     * 2. 带有入参的复杂工具
     */
    @Tool(description = "获取指定地点的实时天气情况")
    public WeatherResponse getWeather(
            // 3. @ToolParam 告诉大模型这个参数的具体物理意义和必填性。
            // 它是大模型能够从随意的话语中，精准提取出 "北京" 二字的绝对依靠！
            @ToolParam(description = "城市的名称，例如：江苏、南京、北京", required = true)
            String location
    ) {
        System.out.println("=========> 启动工具：getWeather ｜ 准备查询: " + location + " 的天气");

        // 模拟业务代码：根据城市名称去查不同的温度
        double temp = location.contains("北京") ? 30.0 : 25.0;
        String cond = "多云转晴";
        // 将结果返回给大模型
        return new WeatherResponse(temp, "C", cond);
    }
}
