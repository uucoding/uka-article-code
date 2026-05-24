package com.uka.saa.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 审校 Agent 使用的本地检查工具。
 *
 * @author 公众号：春风不晚
 */
public class ReviewStubTools {

    @Tool(name = "review_structure_outline", description = "检查结构草案是否具备清晰主线")
    public String reviewStructureOutline(@ToolParam(description = "待审校草案或任务请求") String outline) {
        return """
                审校结论：可以进入下一步执行。
                - 主线完整：从问题边界到架构拆解，再到工程落地。
                - 风险提示：需要明确区分旧文档中的 SupervisorAgent 概念和 1.1.2.2 的 AgentTool 实现方式。
                输入摘要：%s
                """.formatted(outline);
    }

}
