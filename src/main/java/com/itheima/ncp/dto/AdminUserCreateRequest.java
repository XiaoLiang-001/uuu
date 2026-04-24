package com.itheima.ncp.dto;

import lombok.Data;

/** 管理端新增用户（JSON） */
@Data
public class AdminUserCreateRequest {
    private String username;
    private String password;
    private String role;
    private Integer status;
}
