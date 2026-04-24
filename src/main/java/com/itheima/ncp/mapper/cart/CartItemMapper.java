package com.itheima.ncp.mapper.cart;

import com.itheima.ncp.entity.shop.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车数据访问接口，提供购物车条目增删改查。
 */
@Mapper
public interface CartItemMapper {

    /**
     * 插入购物车条目。
     */
    int insert(CartItem row);

    /**
     * 查询指定用户的全部购物车条目。
     */
    List<CartItem> findByUserId(@Param("userId") Long userId);

    /**
     * 按用户和商品查询购物车条目（用于判断是否已加购）。
     */
    CartItem findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 更新购物车条目数量。
     */
    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 按条目ID + 用户ID删除，防止误删他人数据。
     */
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 清空指定用户购物车。
     */
    int deleteByUserId(@Param("userId") Long userId);
}
