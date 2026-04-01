package com.uka.springai.demo;

import com.uka.springai.demo.tools.DateTimeToolsWithoutAnnotation;
import com.uka.springai.demo.tools.WeatherConfig;
import com.uka.springai.demo.tools.WeatherTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@SpringBootTest(classes = Springaidemo015Application.class)
public class TestToolCalling {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * 公众号：春风不晚
     */
    @Test
    public void testManualMethodTool() {
        // 1. 反射获取方法本体
        Method method = ReflectionUtils.findMethod(DateTimeToolsWithoutAnnotation.class, "getCurrentDateTime");

        // 2. 明确告诉大模型：这这个方法是干嘛用的
        ToolDefinition toolDefinition = ToolDefinition.builder()
                .name(method.getName()) // 工具的唯一代号
                .description("获取用户所在时区的当前日期和时间。当用户询问时间时，必须调用此工具。") // 告诉大模型，这个工具是干什么用的
                .inputSchema(JsonSchemaGenerator.generateForMethodInput(method)) // 告诉大模型，这个方法需要哪些参数
                .build();

        // 3. 将说明书和真实的 Java 实例绑定在一起
        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(toolDefinition) // 塞入说明书
                .toolMethod(method) // 塞入方法本体
                .toolObject(new DateTimeToolsWithoutAnnotation()) // 塞入用来执行该方法的对象实例（如果方法是 static 的，这行可省略）
                .build();

        // 4. 使用 .toolCallbacks() 而不是 .tools()
        String content = chatClientBuilder.build().prompt("今天是几号？")
                .toolCallbacks(toolCallback) // 注意：手动构建的 Callback 需要用这个方法挂载
                .call()
                .content();

        System.out.println(content);
    }


    @Test
    public void testFunctionTool() {
        // 1. 构建 Function 工具
        ToolCallback toolCallback = FunctionToolCallback
                .builder("currentWeather", new WeatherTools()) // 参数1：工具名。参数2：Function 实例。
                .description("获取指定地点的实时天气情况")
                .inputType(WeatherTools.WeatherRequest.class) // 必须指定入参类型，框架底层的转换器会帮你生成 JSON Schema！
                .build();

        // 2. 挂载测试
        ChatClient chatClient = chatClientBuilder.build();
        String content = chatClient.prompt("帮我北京最近的天气咋样啊？需要带伞吗？")
                .toolCallbacks(toolCallback)
                .call()
                .content();
        System.out.println(content);
    }

    @Test
    public void testDynamicBeanTool() {
        ChatClient chatClient = chatClientBuilder.build();
        String content = chatClient.prompt("帮我北京最近的天气咋样啊？需要带伞吗？")
                // 【极简挂载】不需要传入实例！只传我们在 Config 里定义好的 Bean 名称常量！
                .toolNames(WeatherConfig.WEATHER_TOOL_NAME)
                .call()
                .content();

        System.out.println(content);
    }

}
