package com.itheima.ncp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 跨域配置。须同时配合 {@code SecurityConfig#configure} 中的 {@code http.cors()}，否则
 * 走 Spring Security 过滤链的接口在浏览器预检（OPTIONS）时可能无正确 CORS 头，导致独立前端
 *（如 Vite:5173）下登录、管理接口等报网络错误。生产环境请按域名收紧白名单。
 */
@Configuration
public class WebCorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        c.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        c.setAllowedHeaders(Arrays.asList("*"));
        c.setExposedHeaders(Arrays.asList("Authorization", "content-type"));
        c.setAllowCredentials(true);
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
