package com.itheima.ncp.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ncp.entity.shop.ShopOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单主表数据访问接口。
 */
@Mapper
public interface ShopOrderMapper extends BaseMapper<ShopOrder> {

    /**
     * 插入订单主记录。
     */
    @Insert("INSERT INTO shop_order (user_id, order_no, total_amount, status, receiver_name, "
            + "receiver_phone, receiver_address) VALUES (#{userId}, #{orderNo}, #{totalAmount}, #{status}, "
            + "#{receiverName}, #{receiverPhone}, #{receiverAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ShopOrder order);

    /**
     * 按订单ID查询订单。
     */
    @Select("SELECT id, user_id, order_no, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at FROM shop_order WHERE id = #{id}")
    ShopOrder findById(@Param("id") Long id);

    /**
     * 按订单ID + 用户ID查询，用于权限隔离。
     */
    @Select("SELECT id, user_id, order_no, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at FROM shop_order WHERE id = #{id} AND user_id = #{userId} LIMIT 1")
    ShopOrder findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询用户订单列表（按ID倒序）。
     */
    @Select("SELECT id, user_id, order_no, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at FROM shop_order WHERE user_id = #{userId} ORDER BY id DESC")
    List<ShopOrder> findByUserIdOrderByIdDesc(@Param("userId") Long userId);
}
