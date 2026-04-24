package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 与登录页 Vue+axios 配合的 JSON 登录：仅建立 Session，不签发 JWT、响应体不含 token。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthApiController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * JSON 登录接口：认证成功后写入 Session SecurityContext 并返回当前账号信息。
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Map<String, Object>> login(@RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        if (req == null || req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return ApiResult.fail(400, "请输入账号");
        }
        if (req.getPassword() == null) {
            return ApiResult.fail(400, "请输入密码");
        }
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    req.getUsername().trim(), req.getPassword());
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("username", auth.getName());
            data.put("role", isAdmin ? "ADMIN" : "USER");
            ApiResult<Map<String, Object>> ok = ApiResult.ok(data);
            ok.setMessage("登录成功");
            return ok;
        } catch (DisabledException e) {
            return ApiResult.fail(403, "该账号已被禁用，无法登录。如需使用请联系管理员处理。");
        } catch (BadCredentialsException e) {
            return ApiResult.fail(401, "用户名或密码错误");
        } catch (AuthenticationException e) {
            return ApiResult.fail(500, "登录失败，请重试");
        }
    }
}
