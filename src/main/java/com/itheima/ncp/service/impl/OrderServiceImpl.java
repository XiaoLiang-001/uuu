package com.itheima.ncp.service.impl;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.entity.shop.CartItem;
import com.itheima.ncp.entity.shop.OrderItem;
import com.itheima.ncp.entity.shop.OrderStatus;
import com.itheima.ncp.entity.shop.ShopOrder;
import com.itheima.ncp.mapper.cart.CartItemMapper;
import com.itheima.ncp.mapper.order.OrderItemMapper;
import com.itheima.ncp.mapper.order.ShopOrderMapper;
import com.itheima.ncp.mapper.product.ProductMapper;
import com.itheima.ncp.service.shop.CartService;
import com.itheima.ncp.service.shop.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现，负责结算下单、订单明细写入与订单查询。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;

    public OrderServiceImpl(CartItemMapper cartItemMapper, ProductMapper productMapper,
                           ShopOrderMapper shopOrderMapper, OrderItemMapper orderItemMapper,
                           CartService cartService) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.shopOrderMapper = shopOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartService = cartService;
    }

    /**
     * 从购物车创建订单：校验库存后写入主订单与明细，并清空购物车。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createOrderFromCart(Long userId, String receiverName, String receiverPhone, String receiverAddress) {
        String rn = receiverName == null ? "" : receiverName.trim();
        String rp = receiverPhone == null ? "" : receiverPhone.trim();
        String ra = receiverAddress == null ? "" : receiverAddress.trim();
        if (rn.isEmpty() || rn.length() > 64) {
            throw new IllegalArgumentException("请填写收货人（不超过 64 字）");
        }
        if (rp.isEmpty() || rp.length() > 32) {
            throw new IllegalArgumentException("请填写联系电话");
        }
        if (ra.isEmpty() || ra.length() > 512) {
            throw new IllegalArgumentException("请填写收货地址");
        }

        List<CartItem> cartRows = cartItemMapper.findByUserId(userId);
        if (cartRows.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem row : cartRows) {
            Product p = productMapper.findById(row.getProductId());
            if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
                throw new IllegalArgumentException("存在已下架商品，请返回购物车调整：" + row.getProductId());
            }
            int qty = row.getQuantity() != null ? row.getQuantity() : 0;
            int stock = p.getStock() != null ? p.getStock() : 0;
            if (qty < 1 || qty > stock) {
                throw new IllegalArgumentException("库存不足或数量异常：" + p.getName());
            }
            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            total = total.add(line);
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        String orderNo = buildOrderNo();
        ShopOrder order = new ShopOrder();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.ORDERED);
        order.setReceiverName(rn);
        order.setReceiverPhone(rp);
        order.setReceiverAddress(ra);
        shopOrderMapper.insert(order);
        Long orderId = order.getId();

        for (CartItem row : cartRows) {
            Product p = productMapper.findById(row.getProductId());
            int qty = row.getQuantity() != null ? row.getQuantity() : 0;
            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            OrderItem oi = new OrderItem();
            oi.setOrderId(orderId);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setUnitPrice(p.getPrice());
            oi.setQuantity(qty);
            oi.setSubtotal(line);
            orderItemMapper.insert(oi);
            int dec = productMapper.decreaseStock(p.getId(), qty);
            if (dec != 1) {
                throw new IllegalStateException("扣减库存失败，请重试：" + p.getName());
            }
        }

        cartService.clearCart(userId);
        return orderId;
    }

    /**
     * 生成订单号（时间戳 + 随机数）。
     */
    private static String buildOrderNo() {
        return "O" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000, 99999);
    }

    /**
     * 查询用户可见的单个订单。
     */
    @Override
    public ShopOrder getOrderForUser(Long orderId, Long userId) {
        return shopOrderMapper.findByIdAndUserId(orderId, userId);
    }

    /**
     * 按时间倒序查询用户订单列表。
     */
    @Override
    public List<ShopOrder> listOrders(Long userId) {
        return shopOrderMapper.findByUserIdOrderByIdDesc(userId);
    }

    /**
     * 查询订单对应的所有商品明细。
     */
    @Override
    public List<OrderItem> listOrderItems(Long orderId) {
        return orderItemMapper.findByOrderId(orderId);
    }
}
