package com.itheima.ncp.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 购物车行（展示用） */
@Data
public class CartLineDto {

    /** 购物车条目ID。 */
    private Long cartItemId;
    /** 商品ID。 */
    private Long productId;
    /** 商品名。 */
    private String productName;
    /** 封面图存储名。 */
    private String coverStoredName;
    /** 单价。 */
    private BigDecimal unitPrice;
    /** 购买数量。 */
    private int quantity;
    /** 当前库存。 */
    private int stock;
    /** 行小计金额。 */
    private BigDecimal lineSubtotal;
}
