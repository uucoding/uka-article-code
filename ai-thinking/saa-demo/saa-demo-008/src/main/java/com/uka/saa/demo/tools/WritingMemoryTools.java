package com.uka.saa.demo.tools;

import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

/**
 * 长期记忆工具。
 * Agent 通过工具显式保存和读取用户写作画像。
 *
 * @author 公众号：春风不晚
 */
public class WritingMemoryTools {

    private final Store memoryStore;

    public WritingMemoryTools(Store memoryStore) {
        this.memoryStore = memoryStore;
    }

    @Tool(description = "保存用户长期写作偏好")
    public String saveWritingProfile(
            @ToolParam(description = "用户 ID") String userId,
            @ToolParam(description = "偏好的标题风格") String titleStyle,
            @ToolParam(description = "常写领域") String domain
    ) {
        // 1. namespace 决定这份长期记忆属于哪个用户。
        List<String> namespace = List.of("users", userId);

        // 2. value 只存稳定画像，不存本轮闲聊流水。
        Map<String, Object> value = Map.of(
                "titleStyle", titleStyle,
                "domain", domain
        );

        // 3. key 决定这类长期记忆是什么。
        memoryStore.putItem(StoreItem.of(namespace, "writing_profile", value));
        System.out.println("=========> 长期记忆写入: namespace=" + namespace + ", key=writing_profile, value=" + value);
        return "写作画像已保存：" + value;
    }

    @Tool(description = "读取用户长期写作偏好")
    public String loadWritingProfile(
            @ToolParam(description = "用户 ID") String userId
    ) {
        // 1. 使用相同 namespace + key 读取用户画像。
        String profile = memoryStore.getItem(List.of("users", userId), "writing_profile")
                .map(item -> item.getValue().toString())
                .orElse("暂无写作画像");
        System.out.println("=========> 长期记忆读取: userId=" + userId + ", profile=" + profile);
        return profile;
    }

}
