package com.itheima.ncp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AdminUserController {

    @GetMapping("/admin/users")
    public String users(Principal principal, Model model) {
        model.addAttribute("currentUsername", principal != null ? principal.getName() : "");
        return "admin/users";
    }
}
