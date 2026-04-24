package com.itheima.ncp.mapper.cart;

import com.itheima.ncp.entity.shop.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartItemMapper {

    int insert(CartItem row);

    List<CartItem> findByUserId(@Param("userId") Long userId);

    CartItem findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);
}
