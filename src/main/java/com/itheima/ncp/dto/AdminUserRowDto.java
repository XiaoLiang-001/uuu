package com.itheima.ncp.dto;

import lombok.Data;

/**
 * 管理端用户列表行。
 */
@Data
public class AdminUserRowDto {

    /** 用户ID。 */
    private Long id;
    /** 登录账号。 */
    private String username;
    /** USER / ADMIN */
    private String role;
    /** 0 禁用 1 启用 */
    private Integer status;
    /** 创建时间字符串（yyyy-MM-dd HH:mm）。 */
    private String createdAt;
}
