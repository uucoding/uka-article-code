package com.uka.saa.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 发布工具。
 * 课程 Demo 用打印和字符串返回模拟真实发布动作。
 *
 * @author 公众号：春风不晚
 */
public class PublishingTools {

    @Tool(name = "publishArticle", description = "发布文章到公众号")
    public String publishArticle(
            @ToolParam(description = "文章标题") String title,
            @ToolParam(description = "文章正文") String content
    ) {
        // 1. 真实业务中我们可以在这里调用发布工具，这里仅模拟发布。
        System.out.println("=========> 执行发布工具: title=" + title);

        // 2. Demo 只返回确定性结果，避免测试依赖外部平台。
        return "发布成功：title=" + title + ", contentLength=" + content.length();
    }

}
