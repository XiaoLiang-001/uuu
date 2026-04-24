package com.itheima.ncp.dto;

import lombok.Data;

/** 管理端编辑用户（JSON） */
@Data
public class AdminUserUpdateRequest {
    /** 登录账号。 */
    private String username;
    /** 新密码（可为空表示不修改）。 */
    private String password;
    /** 旧密码（修改密码时用于校验）。 */
    private String oldPassword;
    /** 用户状态（0禁用/1启用）。 */
    private int status;
}
