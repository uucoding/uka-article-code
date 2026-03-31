package com.uka.springai.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 以注解方式添加工具
 *
 * @author 公众号： 春风不晚
 */
public class DateTimeToolsWithAnnotation {
    /**
     * 获取用户时区的当前日期和时间
     *
     * 1. @Tool 的 description 参数，就是写给大模型看的“说明书概要”。
     *    大模型会仔细阅读它，一旦觉得用户的提问跟“时间”有关，它就会立刻请求调用。
     * 2. 方法名 getCurrentDateTime 会自动成为这个工具的唯一标识 ID。
     */
    @Tool(description = "获取用户时区的当前日期和时间")
    public String getCurrentDateTime() {
        System.out.println("Spring AI 触发了本地方法：获取用户时区的当前日期和时间");
        // 业务代码，调用系统 API 拿到真实时间
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
