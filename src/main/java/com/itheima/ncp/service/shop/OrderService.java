package com.itheima.ncp.service.shop;

import com.itheima.ncp.entity.shop.OrderItem;
import com.itheima.ncp.entity.shop.ShopOrder;

import java.util.List;

/**
 * 订单
 */
public interface OrderService {

    long createOrderFromCart(Long userId, String receiverName, String receiverPhone, String receiverAddress);

    ShopOrder getOrderForUser(Long orderId, Long userId);

    List<ShopOrder> listOrders(Long userId);

    List<OrderItem> listOrderItems(Long orderId);
}
