package com.itheima.ncp.mapper.order;

import com.itheima.ncp.entity.shop.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细数据访问接口。
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 新增订单明细。
     */
    int insert(OrderItem oi);

    /**
     * 按订单ID查询明细列表。
     */
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计用户是否购买过指定商品（用于评价资格校验）。
     */
    @Select("SELECT COUNT(*) FROM order_item oi "
            + "INNER JOIN shop_order so ON oi.order_id = so.id "
            + "WHERE so.user_id = #{userId} AND oi.product_id = #{productId}")
    int countByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
