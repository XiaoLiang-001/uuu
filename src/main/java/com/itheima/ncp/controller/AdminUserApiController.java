package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.entity.user.UserRole;
import com.itheima.ncp.dto.AdminUserCreateRequest;
import com.itheima.ncp.dto.AdminUserRowDto;
import com.itheima.ncp.dto.AdminUserStatusRequest;
import com.itheima.ncp.dto.AdminUserUpdateRequest;
import com.itheima.ncp.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    private final UserService userService;

    public AdminUserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResult<List<AdminUserRowDto>>> list(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(ApiResult.ok(userService.listForAdmin(keyword)));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<AdminUserRowDto>> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResult.ok(userService.getByIdForAdmin(id)));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "用户不存在";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResult<Void>> create(
            @RequestBody(required = false) AdminUserCreateRequest body,
            @RequestParam(value = "username", required = false) String usernameParam,
            @RequestParam(value = "password", required = false) String passwordParam,
            @RequestParam(value = "role", required = false) String roleParam,
            @RequestParam(value = "status", required = false) Integer statusParam,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            String username = firstNonBlank(body == null ? null : body.getUsername(), usernameParam);
            String password = firstNonBlank(body == null ? null : body.getPassword(), passwordParam);
            String roleRaw = firstNonBlank(body == null ? null : body.getRole(), roleParam);
            Integer statusRaw = firstNonNull(body == null ? null : body.getStatus(), statusParam);
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
    @PostMapping(value = "/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> update(
            @PathVariable Long id,
            @RequestBody(required = false) AdminUserUpdateRequest body,
            @RequestParam(value = "username", required = false) String usernameParam,
            @RequestParam(value = "password", required = false) String passwordParam,
            @RequestParam(value = "oldPassword", required = false) String oldPasswordParam,
            @RequestParam(value = "status", required = false) Integer statusParam,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            String username = firstNonBlank(body == null ? null : body.getUsername(), usernameParam);
            String password = firstNonNull(body == null ? null : body.getPassword(), passwordParam);
            String oldPassword = firstNonNull(body == null ? null : body.getOldPassword(), oldPasswordParam);
            Integer statusRaw = firstNonNull(body == null ? null : body.getStatus(), statusParam);
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

    @PostMapping(value = "/{id:\\d+}/status")
    public ResponseEntity<ApiResult<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody(required = false) AdminUserStatusRequest body,
            @RequestParam(value = "status", required = false) Integer statusParam,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "未登录"));
        }
        try {
            Integer statusRaw = firstNonNull(body == null ? null : body.getStatus(), statusParam);
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

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary;
        }
        return fallback;
    }

    private static <T> T firstNonNull(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }

    private ResponseEntity<ApiResult<Void>> internalError(String action, Exception e) {
        String detail = e == null ? null : e.getMessage();
        String msg = action + "，服务器内部错误";
        if (detail != null && !detail.trim().isEmpty()) {
            msg = msg + "：" + detail;
        }
        return ResponseEntity.status(500).body(ApiResult.fail(500, msg));
    }

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
