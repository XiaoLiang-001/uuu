package com.itheima.ncp.service.shop;

import com.itheima.ncp.dto.CartLineDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 */
public interface CartService {

    List<CartLineDto> listLines(Long userId);

    BigDecimal sumCartTotal(Long userId);

    void addProduct(Long userId, long productId, int quantity);

    void updateQuantity(Long userId, long cartItemId, int quantity);

    void removeLine(Long userId, long cartItemId);

    void clearCart(Long userId);
}
