package com.itheima.ncp.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ncp.entity.shop.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细数据访问接口。
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 新增订单明细。
     */
    @Insert("INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, subtotal) "
            + "VALUES (#{orderId}, #{productId}, #{productName}, #{unitPrice}, #{quantity}, #{subtotal})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(OrderItem oi);

    /**
     * 按订单ID查询明细列表。
     */
    @Select("SELECT id, order_id, product_id, product_name, unit_price, quantity, subtotal "
            + "FROM order_item WHERE order_id = #{orderId} ORDER BY id ASC")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计用户是否购买过指定商品（用于评价资格校验）。
     */
    @Select("SELECT COUNT(*) FROM order_item oi "
            + "INNER JOIN shop_order so ON oi.order_id = so.id "
            + "WHERE so.user_id = #{userId} AND oi.product_id = #{productId}")
    int countByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 统计商品在订单明细中的引用数量。
     */
    @Select("SELECT COUNT(*) FROM order_item WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);

    /**
     * 按商品删除订单明细中的关联记录（管理员强制删除商品时使用）。
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM order_item WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);
}
