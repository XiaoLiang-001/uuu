package com.itheima.ncp.mapper.order;

import com.itheima.ncp.entity.shop.ShopOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单主表数据访问接口。
 */
@Mapper
public interface ShopOrderMapper {

    /**
     * 插入订单主记录。
     */
    int insert(ShopOrder order);

    /**
     * 按订单ID查询订单。
     */
    ShopOrder findById(@Param("id") Long id);

    /**
     * 按订单ID + 用户ID查询，用于权限隔离。
     */
    ShopOrder findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询用户订单列表（按ID倒序）。
     */
    List<ShopOrder> findByUserIdOrderByIdDesc(@Param("userId") Long userId);
}
