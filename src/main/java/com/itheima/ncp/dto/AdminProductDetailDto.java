package com.itheima.ncp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端商品详情（JSON），供查询单条等接口使用。
 */
@Data
public class AdminProductDetailDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    /** ON_SHELF / OFF_SHELF */
    private String status;
    private List<String> imageNames;
}
