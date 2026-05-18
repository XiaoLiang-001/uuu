package com.itheima.ncp.dto;

/**
 * 更新购物车数量 JSON 请求体。
 */
public class CartUpdateRequest {
    private Long cartItemId;
    private Integer quantity;

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
