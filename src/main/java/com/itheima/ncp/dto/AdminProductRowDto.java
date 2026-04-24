package com.itheima.ncp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端商品列表行（供 Vue + Element 表格使用）。
 */
@Data
public class AdminProductRowDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    /** ON_SHELF / OFF_SHELF */
    private String status;
    /** 首张图存储文件名，无则为 null */
    private String coverStoredName;
}
