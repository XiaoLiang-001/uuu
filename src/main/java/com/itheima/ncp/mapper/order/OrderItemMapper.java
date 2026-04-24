package com.itheima.ncp.mapper.order;

import com.itheima.ncp.entity.shop.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    int insert(OrderItem oi);

    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(*) FROM order_item oi "
            + "INNER JOIN shop_order so ON oi.order_id = so.id "
            + "WHERE so.user_id = #{userId} AND oi.product_id = #{productId}")
    int countByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
