package com.uka.saa.demo.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ModelInterceptor 示例：负责模型请求治理。
 *
 * @author 公众号：春风不晚
 */
public class RiskModelInterceptor extends ModelInterceptor {

    @Override
    public String getName() {
        return "risk_model_interceptor";
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        Map<String, Object> context = new LinkedHashMap<>(request.getContext());
        String tenantId = context.getOrDefault("tenantId", "tenant-unknown").toString();
        String riskLevel = context.getOrDefault("riskLevel", "low").toString();

        // 1. 根据运行态上下文动态补模型护栏，而不是维护一份越来越长的 Prompt。
        SystemMessage systemMessage = appendGuardrail(request.getSystemMessage(), tenantId, riskLevel);
        context.put("modelGuardrailApplied", true);
        System.out.println("=========> ModelInterceptor: 已追加运行时护栏 | tenantId=" + tenantId + " | riskLevel=" + riskLevel);

        // 2. 复制原始请求，只替换 systemMessage 和 context。
        ModelRequest guardedRequest = ModelRequest.builder(request)
                .systemMessage(systemMessage)
                .context(context)
                .build();

        return handler.call(guardedRequest);
    }

    /**
     * 追加运行时护栏。
     *
     * @param original 原始 system message
     * @param tenantId 租户标识
     * @param riskLevel 风险等级
     * @return 新 system message
     * @author 公众号：春风不晚
     */
    public SystemMessage appendGuardrail(SystemMessage original, String tenantId, String riskLevel) {
        String oldText = original == null ? "" : original.getText();
        String guardrail = """
                
                【运行时治理规则】
                1. 当前租户：%s
                2. 当前风险等级：%s
                3. 如果风险等级为 high，必须提示进入人工审核
                """.formatted(tenantId, riskLevel);
        return new SystemMessage(oldText + guardrail);
    }

}
