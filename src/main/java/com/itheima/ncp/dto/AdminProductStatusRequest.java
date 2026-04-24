package com.itheima.ncp.dto;

import lombok.Data;

/**
 * 管理端商品上下架请求体（JSON）。
 */
@Data
public class AdminProductStatusRequest {
    private String status;
}
