package com.uka.saa.demo.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ToolInterceptor 示例：负责工具权限、审计和失败降级。
 *
 * @author 公众号：春风不晚
 */
public class ToolAuditInterceptor extends ToolInterceptor {

    @Override
    public String getName() {
        return "tool_audit_interceptor";
    }

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        long start = System.nanoTime();
        String userLevel = request.getContext().getOrDefault("userLevel", "free").toString();

        // 1. 免费用户不能调用 VIP 工具，这类权限判断必须落到代码层。
        if (isVipWritingAssetsTool(request.getToolName()) && !"vip".equalsIgnoreCase(userLevel)) {
            System.out.println("=========> ToolInterceptor: 拒绝工具调用 | tool=" + request.getToolName() + " | userLevel=" + userLevel);
            return ToolCallResponse.builder()
                    .toolName(request.getToolName())
                    .toolCallId(request.getToolCallId())
                    .status("denied")
                    .content("当前用户等级无权调用 VIP 写作资产工具")
                    .metadata(Map.of("allowed", false, "reason", "vip_required"))
                    .build();
        }

        try {
            // 2. 继续调用真实工具。
            ToolCallResponse response = handler.call(request);
            long costMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("=========> ToolInterceptor: 工具调用完成 | tool=" + request.getToolName() + " | costMs=" + costMs);

            // 3. 保留工具返回结果，只追加审计元数据。
            Map<String, Object> metadata = new LinkedHashMap<>(response.getMetadata());
            metadata.put("allowed", true);
            metadata.put("costMs", costMs);
            return new ToolCallResponse(
                    response.getResult(),
                    response.getToolName(),
                    response.getToolCallId(),
                    response.getStatus(),
                    metadata
            );
        }
        catch (RuntimeException ex) {
            // 4. 工具异常统一转成错误响应，避免异常直接污染主链路。
            System.out.println("=========> ToolInterceptor: 工具调用异常 | tool=" + request.getToolName() + " | error=" + ex.getMessage());
            return ToolCallResponse.error(request.getToolCallId(), request.getToolName(), ex);
        }
    }

    private boolean isVipWritingAssetsTool(String toolName) {
        return "loadVipWritingAssets".equals(toolName) || "load_vip_writing_assets".equals(toolName);
    }

}
