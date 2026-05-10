package com.uka.saa.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 写作工具示例。
 *
 * @author 公众号：春风不晚
 */
public class WritingTools {

    @Tool(description = "读取指定发布平台的写作规则")
    public String loadPlatformRule(
            @ToolParam(description = "发布平台，例如：微信公众号、掘金") String platform
    ) {
        System.out.println("=========> 启动工具: loadPlatformRule | platform=" + platform);
        if ("微信公众号".equals(platform)) {
            return "微信公众号规则：标题克制，开头快速交代问题，正文要有工程判断和落地步骤。";
        }
        return "通用规则：结构清晰、判断明确、不要写成口号式文案。";
    }

    @Tool(description = "读取 VIP 用户专属的写作资产")
    public String loadVipWritingAssets(
            @ToolParam(description = "写作主题") String topic
    ) {
        System.out.println("=========> 启动工具: loadVipWritingAssets | topic=" + topic);
        return "VIP 写作资产：围绕《" + topic + "》补充架构图、对比表和测试用例。";
    }

}
