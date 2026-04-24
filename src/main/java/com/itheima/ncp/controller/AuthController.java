package com.itheima.ncp.controller;

import com.itheima.ncp.service.user.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证页面控制器，处理注册页展示与注册表单提交。
 */
@Controller
public class AuthController {
    private static final String USERNAME_REGEX = "^[\\u4e00-\\u9fa5A-Za-z0-9_]+$";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 打开注册页；若已登录则直接回首页。
     */
    @GetMapping("/register")
    public String registerPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "register";
    }

    /**
     * 处理注册请求，完成用户名与密码校验后创建普通用户。
     */
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword) {
        String normalizedUsername = username == null ? "" : username.trim();
        String rawPassword = password == null ? "" : password;
        String confirm = confirmPassword == null ? "" : confirmPassword;

        if (normalizedUsername.length() < 2 || normalizedUsername.length() > 64) {
            return "redirect:/register?error=username";
        }
        if (!normalizedUsername.matches(USERNAME_REGEX)) {
            return "redirect:/register?error=username";
        }
        if (rawPassword.length() < 3 || rawPassword.length() > 128) {
            return "redirect:/register?error=password";
        }
        if (!rawPassword.equals(confirm)) {
            return "redirect:/register?error=confirm";
        }
        if (userService.isUsernameExists(normalizedUsername)) {
            return "redirect:/register?error=exists";
        }

        userService.registerAsEndUser(normalizedUsername, passwordEncoder.encode(rawPassword));
        return "redirect:/login?registered";
    }
}
