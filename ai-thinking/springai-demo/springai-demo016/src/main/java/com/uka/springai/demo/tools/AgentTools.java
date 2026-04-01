package com.uka.springai.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentTools {

    // ========== 计算器工具 ==========
    @Tool(description = "强大的数学计算器。遇到任何加减乘除、算账问题，绝对必须调用此工具进行精确计算，严禁你自己进行口算猜测！")
    public String calculator(
            @ToolParam(description = "要计算的数学表达式，例如: 1200 + 150") String expression
    ) {
        System.out.println("=========> 启动工具: calculator | 正在计算: " + expression);
        if (expression.contains("*")) {
            throw new IllegalArgumentException("数学表达式格式错误，当前工具不支持 * ，你需要自己计算好 * 的部分");
        }
        return "计算结果为: 1350";
    }

    // ========== 邮件发送工具 ==========
    @Tool(description = "向指定的联系人发送一封电子邮件。")
    public String sendEmail(
            @ToolParam(description = "收件人姓名，例如：张三") String toName,
            @ToolParam(description = "邮件正文内容，请务必将之前搜集到的所有天气、账单数据排版后放入其中") String content
    ) {
        System.out.println("=========> 启动工具: sendEmail | 准备投递给: " + toName);
        System.out.println("[邮件正文]: \n" + content);

        return "邮件投递成功！已安全到达 " + toName + " 的收件箱。";
    }
}
