package com.itheima.ncp.dto;

import lombok.Data;

/** 管理端编辑用户（JSON） */
@Data
public class AdminUserUpdateRequest {
    private String username;
    private String password;
    private String oldPassword;
    private int status;
}
