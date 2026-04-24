package com.itheima.ncp.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 购物车行（展示用） */
@Data
public class CartLineDto {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private String coverStoredName;
    private BigDecimal unitPrice;
    private int quantity;
    private int stock;
    private BigDecimal lineSubtotal;
}
