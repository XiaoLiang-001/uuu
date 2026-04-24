package com.itheima.ncp.service.shop;

import com.itheima.ncp.dto.CartLineDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 */
public interface CartService {

    /** 查询购物车展示行数据。 */
    List<CartLineDto> listLines(Long userId);

    /** 计算购物车总金额。 */
    BigDecimal sumCartTotal(Long userId);

    /** 向购物车新增商品。 */
    void addProduct(Long userId, long productId, int quantity);

    /** 更新购物车条目数量。 */
    void updateQuantity(Long userId, long cartItemId, int quantity);

    /** 删除购物车条目。 */
    void removeLine(Long userId, long cartItemId);

    /** 清空用户购物车。 */
    void clearCart(Long userId);
}
