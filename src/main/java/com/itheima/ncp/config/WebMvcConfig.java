package com.itheima.ncp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 映射 {@code classpath:/plugins/}，使前端静态资源可通过 {@code /plugins/**} 访问。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /plugins/** 映射到 classpath 下的静态目录。
        registry.addResourceHandler("/plugins/**")
                .addResourceLocations("classpath:/plugins/");
    }
}
