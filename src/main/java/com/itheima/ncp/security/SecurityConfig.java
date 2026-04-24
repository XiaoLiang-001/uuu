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
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers("/login", "/register", "/css/**", "/js/**", "/error", "/error/**", "/favicon.ico", "/files/**", "/.well-known/**").permitAll()
                .antMatchers("/plugins/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/auth/login").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler((request, response, e) -> {
                    try {
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
                .ignoringAntMatchers("/h2-console/**", "/api/**")
                .and()
                .headers().frameOptions().disable();
    }

    private static boolean isDisabled(org.springframework.security.core.AuthenticationException e) {
        if (e instanceof DisabledException) {
            return true;
        }
        Throwable c = e.getCause();
        return c instanceof DisabledException;
    }

    private static boolean isApiPath(javax.servlet.http.HttpServletRequest request, String path) {
        if (path == null) {
            return false;
        }
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        if (!context.isEmpty() && !path.startsWith(context)) {
            return false;
        }
        String p = context.isEmpty() ? path : path.substring(context.length());
        return p.equals("/api") || p.startsWith("/api/");
    }
}
