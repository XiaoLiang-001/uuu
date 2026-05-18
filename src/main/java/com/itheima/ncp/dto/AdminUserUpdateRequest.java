package com.itheima.ncp.dto;

import lombok.Data;

/** 管理端编辑用户（JSON） */
@Data
public class AdminUserUpdateRequest {
    /** 登录账号。 */
    private String username;
    /** 新密码；空串表示不改密。 */
    private String password;
    /** 改密时需提供该用户原密码。 */
    private String oldPassword;
    /** 用户状态（0禁用/1启用）。 */
    private Integer status;
}
