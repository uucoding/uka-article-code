package com.uka.saa.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 结构草案 Agent 使用的本地工具。
 * 课程 demo 用 stub 固定结果，真实项目中可以替换成知识库、模板库或外部 API。
 *
 * @author 公众号：春风不晚
 */
public class OutlineStubTools {

    @Tool(name = "create_structure_outline", description = "根据输入主题生成三段式结构草案")
    public String createStructureOutline(@ToolParam(description = "输入主题") String topic) {
        return """
                草案结果：
                1. 问题边界：说明为什么单次判断不足以处理多步骤任务。
                2. 架构拆解：解释 supervisor 如何把草案生成和结构审校交给专门 Agent。
                3. 工程落地：给出 ReactAgent + AgentTool 的最小配置与验证方式。
                主题：%s
                """.formatted(topic);
    }

}
