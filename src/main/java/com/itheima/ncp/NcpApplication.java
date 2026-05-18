package com.itheima.ncp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring Boot 启动入口。
 * <p>
 * {@link EnableCaching} 打开 Spring Cache，供业务类上的 {@code @Cacheable} / {@code @CacheEvict} 生效
 *（具体用 Redis 还是本机 Map 由 {@code com.itheima.ncp.config.cache} 包下配置决定）。
 * </p>
 */
@SpringBootApplication
@EnableCaching
@MapperScan("com.itheima.ncp.mapper")
public class NcpApplication {

    public static void main(String[] args) {
        // 启动 Spring 容器并加载 Web 应用。
        SpringApplication.run(NcpApplication.class, args);
    }

}
