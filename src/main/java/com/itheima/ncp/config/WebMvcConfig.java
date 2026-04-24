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
        registry.addResourceHandler("/plugins/**")
                .addResourceLocations("classpath:/plugins/");
    }
}
