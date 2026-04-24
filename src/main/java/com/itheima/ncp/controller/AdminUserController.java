package com.itheima.ncp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * 管理端用户页面控制器，仅负责返回用户管理页面模板。
 */
@Controller
public class AdminUserController {

    /**
     * 打开管理端用户列表页，并注入当前登录用户名供页面展示。
     */
    @GetMapping("/admin/users")
    public String users(Principal principal, Model model) {
        model.addAttribute("currentUsername", principal != null ? principal.getName() : "");
        return "admin/users";
    }
}
