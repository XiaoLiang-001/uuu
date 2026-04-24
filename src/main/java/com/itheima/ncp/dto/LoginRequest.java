package com.itheima.ncp.dto;

import lombok.Data;

/** JSON 登录请求体（与登录页 {@code /api/auth/login} 配合，仅建 Session、无 token）。 */
@Data
public class LoginRequest {
    /** 登录账号。 */
    private String username;
    /** 登录密码。 */
    private String password;
}
