package com.itheima.ncp.dto;

import lombok.Data;

/** 管理端启停用户（JSON） */
@Data
public class AdminUserStatusRequest {
    /** 目标状态（0禁用/1启用）。 */
    private int status;
}
