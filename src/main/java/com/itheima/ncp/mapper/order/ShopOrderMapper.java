package com.itheima.ncp.mapper.order;

import com.itheima.ncp.entity.shop.ShopOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShopOrderMapper {

    int insert(ShopOrder order);

    ShopOrder findById(@Param("id") Long id);

    ShopOrder findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<ShopOrder> findByUserIdOrderByIdDesc(@Param("userId") Long userId);
}
