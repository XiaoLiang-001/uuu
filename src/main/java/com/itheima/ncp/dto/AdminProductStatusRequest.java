package com.itheima.ncp.dto;

import lombok.Data;

/**
 * 管理端商品上下架请求体（JSON）。
 */
@Data
public class AdminProductStatusRequest {
    /** 目标状态值：ON_SHELF 或 OFF_SHELF。 */
    private String status;
}
