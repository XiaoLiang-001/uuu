package com.itheima.ncp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Spring Security 总配置：认证、鉴权、跨域、异常处理与登出策略。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 指定用户加载器与密码校验器。
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // 暴露 AuthenticationManager 给 JSON 登录接口使用。
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 配置认证授权链。
        http
                .cors()
                .and()
                .authorizeRequests()
                // 匿名可访问资源与页面。
                .antMatchers("/login", "/register", "/css/**", "/js/**", "/error", "/error/**", "/favicon.ico", "/files/**", "/.well-known/**").permitAll()
                .antMatchers("/plugins/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/auth/login").permitAll()
                // 管理端接口与页面仅管理员可访问。
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 用户端页面普通用户与管理员都可访问。
                .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler((request, response, e) -> {
                    try {
                        // 禁用账号与普通失败分流到不同 query 参数。
                        if (isDisabled(e)) {
                            response.sendRedirect(
                                    (request.getContextPath() == null ? "" : request.getContextPath()) + "/login?disabled");
                        } else {
                            response.sendRedirect(
                                    (request.getContextPath() == null ? "" : request.getContextPath()) + "/login?error");
                        }
                    } catch (IOException ex) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                })
                .permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, e) -> {
                    String path = request.getRequestURI();
                    // API 请求返回 JSON 错误，页面请求走登录跳转。
                    if (isApiPath(request, path)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"未登录或登录已过期\"}");
                    } else {
                        new LoginUrlAuthenticationEntryPoint("/login").commence(request, response, e);
                    }
                })
                .accessDeniedHandler((request, response, e) -> {
                    String path = request.getRequestURI();
                    // API 权限不足返回 JSON，页面请求返回 403。
                    if (isApiPath(request, path)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"权限不足\"}");
                    } else {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                })
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .and()
                .csrf()
                // API 与 H2 控制台禁用 CSRF，避免前后端分离调用失败。
                .ignoringAntMatchers("/h2-console/**", "/api/**")
                .and()
                // 允许 H2 Console iframe 展示。
                .headers().frameOptions().disable();
    }

    private static boolean isDisabled(org.springframework.security.core.AuthenticationException e) {
        // 直接异常类型判定。
        if (e instanceof DisabledException) {
            return true;
        }
        // 兼容被包装在 cause 中的场景。
        Throwable c = e.getCause();
        return c instanceof DisabledException;
    }

    private static boolean isApiPath(javax.servlet.http.HttpServletRequest request, String path) {
        // 路径为空不是 API。
        if (path == null) {
            return false;
        }
        // 处理带 context-path 的部署场景。
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        if (!context.isEmpty() && !path.startsWith(context)) {
            return false;
        }
        String p = context.isEmpty() ? path : path.substring(context.length());
        // 仅 /api 与 /api/** 视为 API 路径。
        return p.equals("/api") || p.startsWith("/api/");
    }
}
