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
        // 已登录用户不允许重复访问注册页。
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        // 未登录用户显示注册页。
        return "register";
    }

    /**
     * 处理注册请求，完成用户名与密码校验后创建普通用户。
     */
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword) {
        // 用户名统一去空白后再校验。
        String normalizedUsername = username == null ? "" : username.trim();
        // 密码保留原始输入（不 trim），避免用户有意设置空格密码时被篡改。
        String rawPassword = password == null ? "" : password;
        // 确认密码同样按原始输入处理。
        String confirm = confirmPassword == null ? "" : confirmPassword;

        // 用户名长度校验。
        if (normalizedUsername.length() < 2 || normalizedUsername.length() > 64) {
            return "redirect:/register?error=username";
        }
        // 用户名字符集校验。
        if (!normalizedUsername.matches(USERNAME_REGEX)) {
            return "redirect:/register?error=username";
        }
        // 密码长度校验。
        if (rawPassword.length() < 3 || rawPassword.length() > 128) {
            return "redirect:/register?error=password";
        }
        // 两次输入密码必须一致。
        if (!rawPassword.equals(confirm)) {
            return "redirect:/register?error=confirm";
        }
        // 用户名重复校验。
        if (userService.isUsernameExists(normalizedUsername)) {
            return "redirect:/register?error=exists";
        }

        // 加密后注册普通用户。
        userService.registerAsEndUser(normalizedUsername, passwordEncoder.encode(rawPassword));
        // 注册成功跳登录页并带提示参数。
        return "redirect:/login?registered";
    }
}
