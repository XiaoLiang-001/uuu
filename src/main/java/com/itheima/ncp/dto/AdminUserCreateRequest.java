package com.itheima.ncp.dto;

import lombok.Data;

/** 管理端新增用户（JSON） */
@Data
public class AdminUserCreateRequest {
    /** 登录账号。 */
    private String username;
    /** 原始密码。 */
    private String password;
    /** 角色编码（通常为 USER）。 */
    private String role;
    /** 用户状态（0禁用/1启用）。 */
    private Integer status;
}
