package com.itheima.ncp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动入口。
 */
@SpringBootApplication
@MapperScan("com.itheima.ncp.mapper")
public class NcpApplication {

    public static void main(String[] args) {
        // 启动 Spring 容器并加载 Web 应用。
        SpringApplication.run(NcpApplication.class, args);
    }

}
