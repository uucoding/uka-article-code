package com.uka.springai.demo.tools;

import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.Function;

 /**
  * 天气工具： 实现 Java 原生的 Function 接口
  * @author 公众号： 春风不晚
  */
public class WeatherTools implements Function<WeatherTools.WeatherRequest, WeatherTools.WeatherResponse> {
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        String location = weatherRequest.location();
        System.out.println("Spring AI 触发了本地方法！目标城市: " + location);

        // 模拟业务代码：根据城市名称去查不同的温度
        double temp = location.contains("北京") ? 30.0 : 25.0;
        String cond = "多云转晴";
        // 将结果返回给大模型
        return new WeatherResponse(temp, "C", cond);
    }
     // 在 Record 内部，依然可以使用 @ToolParam 为字段增加约束和描述！
     public record WeatherRequest(@ToolParam(description = "城市的名称，例如：江苏、南京、北京") String location) {}

     public record WeatherResponse(double temp, String unit, String condition) {}
}
