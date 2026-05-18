package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.entity.user.UserRole;
import com.itheima.ncp.dto.AdminUserCreateRequest;
import com.itheima.ncp.dto.AdminUserUpdateRequest;
import com.itheima.ncp.dto.AdminUserRowDto;
import com.itheima.ncp.dto.AdminUserStatusRequest;
import com.itheima.ncp.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/**
 * 管理端用户 REST API，提供列表、新增、编辑、单条查询、状态更新与删除能力。
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    private final UserService userService;

    public AdminUserApiController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 按关键字查询管理端用户列表。
     */
    @GetMapping
    public ResponseEntity<ApiResult<List<AdminUserRowDto>>> list(
            @RequestParam(value = "keyword", required = false) String keyword) {
        // 关键字可为空；为空时返回全量列表。
        return ResponseEntity.ok(ApiResult.ok(userService.listForAdmin(keyword)));
    }

    /**
     * 根据用户 ID 查询单个用户详情。
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<AdminUserRowDto>> getOne(@PathVariable Long id) {
        try {
            // 查询单个用户并封装标准成功响应。
            return ResponseEntity.ok(ApiResult.ok(userService.getByIdForAdmin(id)));
        } catch (IllegalArgumentException e) {
            // 业务异常（例如用户不存在）统一返回 400。
            String msg = e.getMessage() != null ? e.getMessage() : "用户不存在";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * 新增普通用户账号。
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> create(
            @RequestBody AdminUserCreateRequest body,
            @AuthenticationPrincipal UserDetails principal) {
        // API 层先做登录态兜底，避免进入 service 后才抛出非业务异常。
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            // 空请求体直接返回 400，便于前端快速定位调用问题。
            if (body == null) {
                return ResponseEntity.badRequest().body(ApiResult.fail(400, "请求体无效"));
            }
            String username = body.getUsername();
            String password = body.getPassword();
            String roleRaw = body.getRole();
            Integer statusRaw = body.getStatus();
            // 角色为空时默认 USER，状态为空时默认启用。
            UserRole role = parseRole(roleRaw, UserRole.USER);
            int st = statusRaw != null ? statusRaw : 1;
            // 具体业务校验（重复账号、权限等）由 service 统一负责。
            userService.createByAdmin(username, password, role, st, principal.getUsername());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "创建失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            return internalError("创建失败", e);
        }
    }

    /**
     * 保存编辑：JSON 体（application/json）。
     */
    @PatchMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> update(
            @PathVariable Long id,
            @RequestBody AdminUserUpdateRequest body,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(ApiResult.fail(400, "请求体无效"));
            }
            String username = body.getUsername();
            String password = body.getPassword();
            String oldPassword = body.getOldPassword();
            Integer statusRaw = body.getStatus();
            if (statusRaw == null) {
                return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态不能为空"));
            }
            userService.updateByAdmin(id, username, password, oldPassword, statusRaw, principal.getUsername());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "更新失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            return internalError("保存失败", e);
        }
    }

    /**
     * 删除指定用户（不允许删除管理员及当前登录用户）。
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            // 删除规则（不可删管理员/当前用户）在 service 中集中控制。
            userService.deleteByAdmin(id, principal.getUsername());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "删除失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            return internalError("删除失败", e);
        }
    }

    /**
     * 修改用户启用/禁用状态。
     */
    @PatchMapping(value = "/{id:\\d+}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody AdminUserStatusRequest body,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(ApiResult.fail(400, "请求体无效"));
            }
            Integer statusRaw = body.getStatus();
            if (statusRaw == null) {
                return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态不能为空"));
            }
            // 具体的可操作性判断（是否管理员、是否当前用户等）交由 service 统一处理。
            userService.updateStatusByAdmin(id, statusRaw, principal.getUsername());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "更新失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            return internalError("状态更新失败", e);
        }
    }

    /**
     * 统一拼装 500 错误响应，附带后端异常信息便于排障。
     */
    private ResponseEntity<ApiResult<Void>> internalError(String action, Exception e) {
        String detail = e == null ? null : e.getMessage();
        String msg = action + "，服务器内部错误";
        // 开发与联调阶段返回详细原因，帮助快速定位数据库或参数问题。
        if (detail != null && !detail.trim().isEmpty()) {
            msg = msg + "：" + detail;
        }
        return ResponseEntity.status(500).body(ApiResult.fail(500, msg));
    }

    /**
     * 将字符串角色值解析为枚举，支持空值回退默认角色。
     */
    private static UserRole parseRole(String roleStr, UserRole defaultIfBlank) {
        // 空值时按默认角色回退。
        if (roleStr == null || roleStr.trim().isEmpty()) {
            if (defaultIfBlank != null) {
                return defaultIfBlank;
            }
            throw new IllegalArgumentException("请选择角色");
        }
        try {
            // 统一大写后映射枚举，兼容前端大小写差异。
            return UserRole.valueOf(roleStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // 抛出业务友好提示，而不是枚举原始异常信息。
            throw new IllegalArgumentException("角色无效");
        }
    }
}
