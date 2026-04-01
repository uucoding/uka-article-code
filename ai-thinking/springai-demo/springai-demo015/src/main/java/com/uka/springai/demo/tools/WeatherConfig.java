package com.uka.springai.demo.tools;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class WeatherConfig {
    // 最佳实践：把工具名称定义为常量，避免硬编码到处乱写。
    public static final String WEATHER_TOOL_NAME = "currentWeatherTool";

    /**
     * 注册一个原生的 Function Bean。
     * 1. Bean 的名称 (WEATHER_TOOL_NAME) 自动成为大模型的“工具名称”。
     * 2. Spring 框架自带的 @Description 注解，自动成为大模型的“工具描述说明书”。
     */
    @Bean(WEATHER_TOOL_NAME)
    @Description("获取指定地点的实时天气情况")
    public Function<WeatherRequest, WeatherResponse> currentWeather() {
        return request -> {
            String location = request.location();
            System.out.println("Spring AI 触发了本地方法！目标城市: " + location);
            // 模拟业务代码：根据城市名称去查不同的温度
            double temp = location.contains("北京") ? 30.0 : 25.0;
            String cond = "多云转晴";
            // 将结果返回给大模型
            return new WeatherResponse(temp, "C", cond);
        };
    }

    // 在 Record 内部，依然可以使用 @ToolParam 为字段增加约束和描述！
    public record WeatherRequest(
            @ToolParam(description = "城市的名称，例如：江苏、南京、北京") String location
    ) {}
    public record WeatherResponse(double temp, String unit, String condition) {}
}
