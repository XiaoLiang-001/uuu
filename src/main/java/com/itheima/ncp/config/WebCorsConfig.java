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
        // 创建 CORS 配置对象。
        CorsConfiguration c = new CorsConfiguration();
        // 本地开发允许的前端来源（模式匹配）。
        c.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        // 允许的 HTTP 方法。
        c.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 允许全部请求头。
        c.setAllowedHeaders(Arrays.asList("*"));
        // 允许前端读取的响应头。
        c.setExposedHeaders(Arrays.asList("Authorization", "content-type"));
        // 允许携带 Cookie（Session 登录必须）。
        c.setAllowCredentials(true);
        // 预检请求缓存时长（秒）。
        c.setMaxAge(3600L);
        // 绑定 URL 规则到该 CORS 配置。
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        // 返回给 Spring CORS 体系使用。
        return source;
    }
}
