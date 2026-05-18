package com.itheima.ncp.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理端商品批量操作请求体。
 */
@Data
public class AdminProductBatchRequest {

    /** 批量操作商品ID列表。 */
    private List<Long> ids;
    /** 批量目标状态（仅批量上下架接口使用）。 */
    private String status;
}
