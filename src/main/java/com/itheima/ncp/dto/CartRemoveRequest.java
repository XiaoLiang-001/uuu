package com.itheima.ncp.dto;

/**
 * 删除购物车条目 JSON 请求体。
 */
public class CartRemoveRequest {
    private Long cartItemId;

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
}
