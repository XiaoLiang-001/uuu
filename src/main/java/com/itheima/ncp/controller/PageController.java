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

/**
 * 通用页面路由控制器，承载登录首页、角色分发与账户相关页面跳转。
 */
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

    /**
     * 根路径入口：根据当前登录角色分发到管理端或用户端首页。
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        // 未登录用户统一跳转登录页。
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        // 统一在入口做角色分流，避免前端自行判断角色后再跳转。
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (admin) {
            return "redirect:/admin/home";
        }
        return "redirect:/user/home";
    }

    /**
     * 打开登录页；已登录用户直接回根路径。
     */
    @GetMapping("/login")
    public String login(Authentication authentication) {
        // 已登录用户无需重复访问登录页。
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    /**
     * 用户端壳页面入口，附带当前用户资料摘要。
     */
    @GetMapping("/user/home")
    public String userHome(Authentication authentication, Model model) {
        // 预加载当前用户资料，供壳页面顶部信息展示。
        bindUserProfile(authentication, model);
        return "user/home";
    }

    /**
     * 用户资料展示页。
     */
    @GetMapping("/user/profile")
    public String userProfile(Authentication authentication, Model model) {
        // 页面渲染前绑定资料模型。
        bindUserProfile(authentication, model);
        return "user/profile";
    }

    /**
     * 用户资料编辑页。
     */
    @GetMapping("/user/profile/edit")
    public String editUserProfile(Authentication authentication, Model model) {
        // 编辑页同样复用资料绑定逻辑。
        bindUserProfile(authentication, model);
        return "user/profile-edit";
    }

    /**
     * 提交个人资料修改；修改成功后主动登出并要求重新登录。
     */
    @PostMapping("/user/profile/update")
    public String updateMyProfile(@RequestParam("username") String username,
                                  @RequestParam("oldPassword") String oldPassword,
                                  @RequestParam(value = "newPassword", required = false) String newPassword,
                                  @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                  Authentication authentication,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  RedirectAttributes ra) {
        // 未登录状态下拒绝处理个人资料更新。
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            String cp = confirmPassword == null ? "" : confirmPassword.trim();
            // 先在控制器做确认密码校验，减少 service 不必要调用。
            if (!newPassword.trim().equals(cp)) {
                ra.addFlashAttribute("err", "两次输入的新密码不一致");
                return "redirect:/user/profile/edit";
            }
        }
        try {
            // 执行个人资料更新（用户名/密码）。
            userService.updateSelfProfile(authentication.getName(), username, oldPassword, newPassword);
            // 用户名/密码变更后主动登出，强制新凭据重新建立会话。
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?profileUpdated";
        } catch (IllegalArgumentException e) {
            // 业务校验失败（如密码错误）通过 flash 回显。
            ra.addFlashAttribute("err", e.getMessage());
            return "redirect:/user/profile/edit";
        }
    }

    /**
     * 将当前用户资料与状态标签写入页面模型，供前端模板渲染。
     */
    private void bindUserProfile(Authentication authentication, Model model) {
        // 优先从认证信息读取用户名。
        String username = authentication != null ? authentication.getName() : "";
        // 查询当前用户实体。
        User u = userService.getByUsername(username);
        if (u != null) {
            // 防止模板中误用密码字段。
            u.setPassword(null);
            model.addAttribute("profile", u);
            // 渲染角色相关展示信息。
            model.addAttribute("roleLabel", u.getRole() == UserRole.ADMIN ? "管理员" : "普通用户");
            model.addAttribute("isAdmin", u.getRole() == UserRole.ADMIN);
            if (u.getRole() != null) {
                model.addAttribute("roleCode", u.getRole().name());
            }
            String un = u.getUsername();
            // 头像字母取用户名首字符，缺省显示 ?。
            model.addAttribute("avatarLetter", (un != null && !un.isEmpty()) ? un.substring(0, 1) : "?");
            if (u.getCreatedAt() != null) {
                // 创建时间转格式化字符串便于页面展示。
                model.addAttribute("createdAtFormatted", CREATED_FMT.format(u.getCreatedAt()));
            }
            Integer st = u.getStatus();
            // 将数据库状态码转换为页面可直接使用的标签与布尔标记。
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
            // 用户不存在时提供缺失标记，页面可给出兜底提示。
            model.addAttribute("profileMissing", true);
            model.addAttribute("profileUsername", username);
            model.addAttribute("isAdmin", false);
        }
    }

    /**
     * 打开管理端壳页面。
     */
    @GetMapping("/admin/home")
    public String adminHome() {
        // 返回管理端壳页面模板。
        return "admin/home";
    }
}
