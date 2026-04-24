package com.itheima.ncp.dto;

import lombok.Data;

/**
 * 管理端用户列表行。
 */
@Data
public class AdminUserRowDto {

    private Long id;
    private String username;
    /** USER / ADMIN */
    private String role;
    /** 0 禁用 1 启用 */
    private Integer status;
    private String createdAt;
}
