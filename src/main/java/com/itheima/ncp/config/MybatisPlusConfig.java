package com.itheima.ncp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件；分页器按当前数据源自动识别（MySQL / 测试用 H2）。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MP 拦截器容器。
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 注册分页拦截器，数据库类型指定为 MySQL。
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.MYSQL);
        // 添加到总拦截器链。
        interceptor.addInnerInterceptor(page);
        // 交由 Spring 管理。
        return interceptor;
    }
}
