package com.itheima.ncp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端商品详情（JSON），供查询单条等接口使用。
 */
@Data
public class AdminProductDetailDto {

    /** 商品ID。 */
    private Long id;
    /** 商品名称。 */
    private String name;
    /** 商品描述。 */
    private String description;
    /** 商品单价。 */
    private BigDecimal price;
    /** 库存数量。 */
    private Integer stock;
    /** ON_SHELF / OFF_SHELF */
    private String status;
    /** 商品图片文件名列表。 */
    private List<String> imageNames;
}
