package com.itheima.ncp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端商品列表行（供 Vue + Element 表格使用）。
 */
@Data
public class AdminProductRowDto {

    /** 商品ID。 */
    private Long id;
    /** 商品名称。 */
    private String name;
    /** 商品单价。 */
    private BigDecimal price;
    /** 库存数量。 */
    private Integer stock;
    /** ON_SHELF / OFF_SHELF */
    private String status;
    /** 产地省份名称。 */
    private String provinceName;
    /** 分类名称。 */
    private String categoryName;
    /** 首张图存储文件名，无则为 null */
    private String coverStoredName;
}
