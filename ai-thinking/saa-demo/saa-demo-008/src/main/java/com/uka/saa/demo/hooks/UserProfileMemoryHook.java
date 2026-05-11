package com.uka.saa.demo.hooks;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.store.Store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 用户画像长期记忆 Hook。
 * 每次模型调用前，从 MemoryStore 读取当前用户的长期写作画像并写回 Agent state。
 *
 * @author 公众号：春风不晚
 */
public class UserProfileMemoryHook extends ModelHook {

    private static final String PROFILE_KEY = "writing_profile";

    private final Store memoryStore;

    public UserProfileMemoryHook(Store memoryStore) {
        this.memoryStore = memoryStore;
    }

    @Override
    public String getName() {
        return "user_profile_memory";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        String userId = state.value("userId", "anonymous");

        // 1. 优先使用本轮 RunnableConfig 携带的 Store，没有时回退到 Spring 注入的 Store。
        Store store = config.store() != null ? config.store() : memoryStore;
        String profile = store.getItem(List.of("users", userId), PROFILE_KEY)
                .map(item -> item.getValue().toString())
                .orElse("暂无长期写作画像");

        System.out.println("=========> UserProfileMemoryHook: userId=" + userId + ", profile=" + profile);

        // 2. 写回 state，后续 instruction 模板或模型请求组装可以继续消费。
        return CompletableFuture.completedFuture(Map.of("writingProfile", profile));
    }

}
