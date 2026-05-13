package com.uka.saa.demo.model;

/**
 * 多 Agent 拆分后的候选编排形态。
 *
 * @author 公众号：春风不晚
 */
public enum AgentWorkflowMode {

    /**
     * 单 Agent 仍然可以承担当前任务。
     */
    SINGLE_AGENT,

    /**
     * 由一个主 Agent 把专门 Agent 当工具调用。
     */
    AGENT_TOOL,

    /**
     * 由当前 Agent 把对话控制权交给更合适的 Agent。
     */
    HANDOFF,

    /**
     * 多个 Agent 按固定顺序依次执行。
     */
    SEQUENTIAL_AGENT,

    /**
     * 多个 Agent 处理彼此独立的子任务。
     */
    PARALLEL_AGENT,

    /**
     * 先判断任务类型，再路由到对应 Agent。
     */
    LLM_ROUTING_AGENT,

    /**
     * 由监督 Agent 统一规划、分派和收口。
     */
    SUPERVISOR_AGENT

}
