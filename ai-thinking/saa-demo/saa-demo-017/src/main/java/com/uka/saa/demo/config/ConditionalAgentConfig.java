package com.uka.saa.demo.config;

import com.uka.saa.demo.agent.ConditionalAgent;
import com.uka.saa.demo.agent.ErrorHandlingAgent;
import com.uka.saa.demo.agent.ReportGenerationAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 第 17 讲条件 Agent 配置。
 *
 * @author 公众号：春风不晚
 */
@Configuration
public class ConditionalAgentConfig {

    @Bean
    public ReportGenerationAgent reportGenerationAgent() {
        return new ReportGenerationAgent();
    }

    @Bean
    public ErrorHandlingAgent errorHandlingAgent() {
        return new ErrorHandlingAgent();
    }

    @Bean
    public ConditionalAgent conditionalWritingAgent(ReportGenerationAgent reportGenerationAgent,
                                                    ErrorHandlingAgent errorHandlingAgent) {
        return new ConditionalAgent(Map.of(
                // 1. 默认 ConditionEvaluator 看到 report/summary 会返回 report_generation。
                ConditionalAgent.REPORT_GENERATION, reportGenerationAgent,
                // 2. 默认 ConditionEvaluator 看到 error/exception 会返回 error_handling。
                ConditionalAgent.ERROR_HANDLING, errorHandlingAgent
        ));
    }

}
