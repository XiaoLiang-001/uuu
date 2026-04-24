package com.itheima.ncp.service.shop;

import com.itheima.ncp.entity.shop.OrderItem;
import com.itheima.ncp.entity.shop.ShopOrder;

import java.util.List;

/**
 * 订单
 */
public interface OrderService {

    /** 从购物车创建订单。 */
    long createOrderFromCart(Long userId, String receiverName, String receiverPhone, String receiverAddress);

    /** 查询当前用户可访问的订单。 */
    ShopOrder getOrderForUser(Long orderId, Long userId);

    /** 查询用户订单列表。 */
    List<ShopOrder> listOrders(Long userId);

    /** 查询订单明细列表。 */
    List<OrderItem> listOrderItems(Long orderId);
}
