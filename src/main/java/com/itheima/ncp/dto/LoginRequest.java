package com.itheima.ncp.dto;

import lombok.Data;

/** JSON 登录请求体（与登录页 {@code /api/auth/login} 配合，仅建 Session、无 token）。 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
