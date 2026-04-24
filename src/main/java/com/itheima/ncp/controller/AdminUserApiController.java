package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.entity.user.UserRole;
import com.itheima.ncp.dto.AdminUserCreateRequest;
import com.itheima.ncp.dto.AdminUserRowDto;
import com.itheima.ncp.dto.AdminUserStatusRequest;
import com.itheima.ncp.dto.AdminUserUpdateRequest;
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
 * 管理端用户 REST API，提供列表、新增、状态更新与删除能力。
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
        return ResponseEntity.ok(ApiResult.ok(userService.listForAdmin(keyword)));
    }

    /**
     * 根据用户 ID 查询单个用户详情。
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<AdminUserRowDto>> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResult.ok(userService.getByIdForAdmin(id)));
        } catch (IllegalArgumentException e) {
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
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(ApiResult.fail(400, "请求体无效"));
            }
            String username = body.getUsername();
            String password = body.getPassword();
            String roleRaw = body.getRole();
            Integer statusRaw = body.getStatus();
            UserRole role = parseRole(roleRaw, UserRole.USER);
            int st = statusRaw != null ? statusRaw : 1;
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
     * 保存编辑：与前端使用 JSON 体（application/json），避免 iframe+form-urlencoded 下 @RequestParam 不绑定问题。
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
        if (detail != null && !detail.trim().isEmpty()) {
            msg = msg + "：" + detail;
        }
        return ResponseEntity.status(500).body(ApiResult.fail(500, msg));
    }

    /**
     * 将字符串角色值解析为枚举，支持空值回退默认角色。
     */
    private static UserRole parseRole(String roleStr, UserRole defaultIfBlank) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            if (defaultIfBlank != null) {
                return defaultIfBlank;
            }
            throw new IllegalArgumentException("请选择角色");
        }
        try {
            return UserRole.valueOf(roleStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("角色无效");
        }
    }
}
