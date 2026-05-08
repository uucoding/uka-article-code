package com.uka.saa.boundary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类。
 * 本模块只聚焦 ReactAgent 的四个边界：instruction、messages、model、tool。
 *
 * @author 公众号：春风不晚
 */
@SpringBootApplication
public class AgentBoundaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentBoundaryApplication.class, args);
    }

}
