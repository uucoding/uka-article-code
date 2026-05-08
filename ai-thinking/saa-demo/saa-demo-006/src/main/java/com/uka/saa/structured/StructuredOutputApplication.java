package com.uka.saa.structured;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 本模块聚焦结构化输出：让 Agent 返回可控 DTO，而不是一段不可治理的自然语言。
 *
 * @author 公众号：春风不晚
 */
@SpringBootApplication
public class StructuredOutputApplication {

    public static void main(String[] args) {
        // 1. 启动 Spring 容器，让结构化 Agent、工具和服务完成装配。
        SpringApplication.run(StructuredOutputApplication.class, args);
    }

}
