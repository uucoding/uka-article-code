package com.uka.springai.demo.tools;

import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 普通的 Java 类，没有挂载任何 Spring AI 的 @Tool 注解。
 *
 * @author 公众号： 春风不晚
 */
public class DateTimeToolsWithoutAnnotation {

    // 获取用户时区的当前日期和时间
    public String getCurrentDateTime() {
        System.out.println("Spring AI 触发了本地方法：获取用户时区的当前日期和时间");
        // 业务代码，调用系统 API 拿到真实时间
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    // 设置闹钟 (模拟接收字符串参数)
    public void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("已成功设置闹钟，时间为: " + alarmTime + "\n");
    }
}
