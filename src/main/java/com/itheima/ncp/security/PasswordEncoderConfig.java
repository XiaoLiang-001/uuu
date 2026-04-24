package com.itheima.ncp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置，统一注入自定义可兼容编码器。
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 返回支持 BCrypt 与历史明文兼容校验的编码器实现。
        return new FlexiblePasswordEncoder();
    }
}
