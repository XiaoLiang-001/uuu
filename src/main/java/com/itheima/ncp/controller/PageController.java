package com.itheima.ncp.controller;

import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.entity.user.UserRole;
import com.itheima.ncp.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;

@Controller
public class PageController {

    private static final DateTimeFormatter CREATED_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserService userService;

    public PageController(UserService userService) {
        this.userService = userService;
    }

    /** 避免浏览器默认请求 /favicon.ico 产生 404 噪音 */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Chrome/Edge 打开开发者工具时会探测该地址；无业务配置时返回空 JSON 即可，避免 Network 里出现 404。
     */
    @GetMapping(value = "/.well-known/appspecific/com.chrome.devtools.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String chromeDevtoolsAppConfig() {
        return "{}";
    }

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (admin) {
            return "redirect:/admin/home";
        }
        return "redirect:/user/home";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/user/home")
    public String userHome(Authentication authentication, Model model) {
        bindUserProfile(authentication, model);
        return "user/home";
    }

    @GetMapping("/user/profile")
    public String userProfile(Authentication authentication, Model model) {
        bindUserProfile(authentication, model);
        return "user/profile";
    }

    @GetMapping("/user/profile/edit")
    public String editUserProfile(Authentication authentication, Model model) {
        bindUserProfile(authentication, model);
        return "user/profile-edit";
    }

    @PostMapping("/user/profile/update")
    public String updateMyProfile(@RequestParam("username") String username,
                                  @RequestParam("oldPassword") String oldPassword,
                                  @RequestParam(value = "newPassword", required = false) String newPassword,
                                  @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                  Authentication authentication,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            String cp = confirmPassword == null ? "" : confirmPassword.trim();
            if (!newPassword.trim().equals(cp)) {
                ra.addFlashAttribute("err", "两次输入的新密码不一致");
                return "redirect:/user/profile/edit";
            }
        }
        try {
            userService.updateSelfProfile(authentication.getName(), username, oldPassword, newPassword);
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?profileUpdated";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage());
            return "redirect:/user/profile/edit";
        }
    }

    private void bindUserProfile(Authentication authentication, Model model) {
        String username = authentication != null ? authentication.getName() : "";
        User u = userService.getByUsername(username);
        if (u != null) {
            u.setPassword(null);
            model.addAttribute("profile", u);
            model.addAttribute("roleLabel", u.getRole() == UserRole.ADMIN ? "管理员" : "普通用户");
            model.addAttribute("isAdmin", u.getRole() == UserRole.ADMIN);
            if (u.getRole() != null) {
                model.addAttribute("roleCode", u.getRole().name());
            }
            String un = u.getUsername();
            model.addAttribute("avatarLetter", (un != null && !un.isEmpty()) ? un.substring(0, 1) : "?");
            if (u.getCreatedAt() != null) {
                model.addAttribute("createdAtFormatted", CREATED_FMT.format(u.getCreatedAt()));
            }
            Integer st = u.getStatus();
            if (st == null) {
                model.addAttribute("statusLabel", "未设置");
                model.addAttribute("statusOk", false);
                model.addAttribute("statusUnset", true);
                model.addAttribute("accountDisabled", false);
            } else if (st == User.STATUS_ENABLED) {
                model.addAttribute("statusLabel", "正常");
                model.addAttribute("statusOk", true);
                model.addAttribute("statusUnset", false);
                model.addAttribute("accountDisabled", false);
            } else if (st == User.STATUS_DISABLED) {
                model.addAttribute("statusLabel", "已禁用");
                model.addAttribute("statusOk", false);
                model.addAttribute("statusUnset", false);
                model.addAttribute("accountDisabled", true);
            } else {
                model.addAttribute("statusLabel", "未知(" + st + ")");
                model.addAttribute("statusOk", false);
                model.addAttribute("statusUnset", false);
                model.addAttribute("accountDisabled", false);
            }
        } else {
            model.addAttribute("profileMissing", true);
            model.addAttribute("profileUsername", username);
            model.addAttribute("isAdmin", false);
        }
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin/home";
    }
}
