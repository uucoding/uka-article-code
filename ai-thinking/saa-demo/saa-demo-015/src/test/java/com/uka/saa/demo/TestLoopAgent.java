package com.uka.saa.demo;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.uka.saa.demo.agent.WritingRevisionAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 第 15 讲内置 LoopAgent 测试。
 * 验证同一个单轮修正 Agent 会被 LoopAgent 固定执行 3 轮。
 *
 * @author 公众号：春风不晚
 */
@SpringBootTest(classes = SaaDemo015Application.class)
public class TestLoopAgent {

    @Autowired
    @Qualifier("writingRevisionLoopAgent")
    private LoopAgent writingRevisionLoopAgent;

    @Test
    void test() throws Exception {
        OverAllState state = invoke(
                "这套方案 100% 保证一定不会出错，是唯一选择并且立即见效。",
                "remaining-issues"
        );
        System.out.println("=======> 内置 LoopAgent 三轮后仍有风险项：");
        System.out.println(rounds(state));
        System.out.println("=======> 剩余风险项：");
        System.out.println(remainingIssues(state));
        System.out.println("=======> 最终结果：");
        System.out.println(state.value(WritingRevisionAgent.CURRENT_COPY_KEY, ""));
    }

    private OverAllState invoke(String input, String threadId) throws Exception {
        return writingRevisionLoopAgent.invoke(input, RunnableConfig.builder()
                .threadId("lesson-015-" + threadId)
                .build()).orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private List<String> rounds(OverAllState state) {
        return state.value(WritingRevisionAgent.REVIEW_ROUNDS_KEY, List.class)
                .map(value -> (List<String>) value)
                .orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    private List<String> remainingIssues(OverAllState state) {
        return state.value(WritingRevisionAgent.REMAINING_ISSUES_KEY, List.class)
                .map(value -> (List<String>) value)
                .orElse(List.of());
    }

}
