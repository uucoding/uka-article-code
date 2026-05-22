package com.uka.saa.demo.agent;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.internal.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async;

/**
 * LoopAgent 的单轮修正子 Agent。
 * 每次执行只修正一个风险词，循环次数交给内置 LoopAgent 控制。
 *
 * @author 公众号：春风不晚
 */
public class WritingRevisionAgent extends BaseAgent {

    public static final String DRAFT_KEY = "draft";

    public static final String CURRENT_COPY_KEY = "current_copy";

    public static final String REVIEW_ROUNDS_KEY = "review_rounds";

    public static final String APPROVED_KEY = "revision_approved";

    public static final String REMAINING_ISSUES_KEY = "remaining_issues";

    public static final String OUTPUT_KEY = "writing_revision_round";

    private final List<RiskRule> riskRules = List.of(
            new RiskRule("100%", "绝对化效果承诺", "尽可能"),
            new RiskRule("不会出错", "不可验证承诺", "降低出错概率"),
            new RiskRule("保证", "确定性承诺", "帮助"),
            new RiskRule("一定", "确定性承诺", "可以"),
            new RiskRule("唯一", "排他性表述", "一种可选"),
            new RiskRule("立即见效", "即时效果承诺", "逐步看到效果")
    );

    public WritingRevisionAgent(String name, String description) {
        super(name, description, false, false, OUTPUT_KEY, KeyStrategy.REPLACE);
    }

    @Override
    protected StateGraph initGraph() throws GraphStateException {
        StateGraph graph = new StateGraph(name(), KeyStrategy.builder()
                .addStrategy("input")
                .addStrategy("messages", KeyStrategy.APPEND)
                .addStrategy(DRAFT_KEY)
                .addStrategy(CURRENT_COPY_KEY)
                .addStrategy(REVIEW_ROUNDS_KEY)
                .addStrategy(APPROVED_KEY)
                .addStrategy(REMAINING_ISSUES_KEY)
                .addStrategy(OUTPUT_KEY)
                .build());

        graph.addNode(name(), node_async(this::reviseOnce));
        graph.addEdge(START, name());
        graph.addEdge(name(), END);
        return graph;
    }

    @Override
    public Node asNode(boolean includeContents, boolean returnReasoningContents) {
        return new Node(name(), ignored -> node_async(this::reviseOnce));
    }

    private Map<String, Object> reviseOnce(OverAllState state, RunnableConfig config) {
        String currentCopy = currentCopy(state);
        List<String> existingRounds = rounds(state);
        int roundNumber = existingRounds.size() + 1;

        // 1. 每一轮只检查当前版本，并修正命中的第一个风险词。
        List<RiskRule> issues = inspect(currentCopy);
        RiskRule firstIssue = issues.isEmpty() ? null : issues.get(0);
        String revisedCopy = firstIssue == null
                ? currentCopy
                : currentCopy.replace(firstIssue.keyword(), firstIssue.replacement());

        // 2. 修正后立即复查，测试直接从状态里观察每轮变化。
        List<String> remainingIssues = summarize(inspect(revisedCopy));
        boolean approved = remainingIssues.isEmpty();

        List<String> newRounds = new ArrayList<>(existingRounds);
        String currentRound = "\n第 " + roundNumber + " 轮：\n"
                + (firstIssue == null ? "未发现风险词" : firstIssue.summary())
                + "；：\n结果：" + revisedCopy + "\n";
        newRounds.add(currentRound);

        return Map.of(
                DRAFT_KEY, state.value(DRAFT_KEY, currentCopy),
                CURRENT_COPY_KEY, revisedCopy,
                REVIEW_ROUNDS_KEY, List.copyOf(newRounds),
                APPROVED_KEY, approved,
                REMAINING_ISSUES_KEY, remainingIssues,
                OUTPUT_KEY, currentRound
        );
    }

    private String currentCopy(OverAllState state) {
        String draft = state.value(DRAFT_KEY, state.value("input", ""));
        return state.value(CURRENT_COPY_KEY, draft);
    }

    @SuppressWarnings("unchecked")
    private List<String> rounds(OverAllState state) {
        Object value = state.value(REVIEW_ROUNDS_KEY).orElse(List.of());
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    private List<RiskRule> inspect(String copy) {
        String text = copy == null ? "" : copy;
        return riskRules.stream()
                .filter(rule -> text.contains(rule.keyword()))
                .toList();
    }

    private List<String> summarize(List<RiskRule> rules) {
        return rules.stream()
                .map(RiskRule::summary)
                .toList();
    }

    private record RiskRule(String keyword, String reason, String replacement) {

        private String summary() {
            return keyword + " -> " + reason;
        }

    }

}
