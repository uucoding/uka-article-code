package com.uka.saa.demo.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.uka.saa.demo.hook.TwoStepRagMessagesHook.RAG_CONTEXT_KEY;

/**
 * 从 RunnableConfig context 中取出 RAG 上下文，再写入模型 system message。
 * 这个类展示官方文档里的 AgentHook + ModelInterceptor 组合边界。
 *
 * @author 公众号：春风不晚
 */
public class RagContextModelInterceptor extends ModelInterceptor {

    @Override
    public String getName() {
        return "rag_context_model_interceptor";
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        String ragContext = String.valueOf(request.getContext().getOrDefault(RAG_CONTEXT_KEY, ""));
        if (ragContext.isBlank()) {
            return handler.call(request);
        }

        SystemMessage systemMessage = appendRagContext(request.getSystemMessage(), ragContext);
        Map<String, Object> context = new LinkedHashMap<>(request.getContext());
        context.put("ragContextInjected", true);

        return handler.call(ModelRequest.builder(request)
                .systemMessage(systemMessage)
                .context(context)
                .build());
    }

    public SystemMessage appendRagContext(SystemMessage original, String ragContext) {
        String oldText = original == null ? "" : original.getText();
        return new SystemMessage(oldText + """

                【RAG 参考资料】
                %s
                """.formatted(ragContext));
    }

}
