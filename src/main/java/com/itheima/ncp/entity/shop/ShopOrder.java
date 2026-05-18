package com.itheima.ncp.entity.shop;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("shop_order")
public class ShopOrder {

    /** 订单主键。 */
    private Long id;
    /** 下单用户ID。 */
    private Long userId;
    /** 订单号（业务可读）。 */
    private String orderNo;
    /** 订单总金额。 */
    private BigDecimal totalAmount;
    /** 订单状态。 */
    private OrderStatus status;
    /** 收货人姓名。 */
    private String receiverName;
    /** 收货联系电话。 */
    private String receiverPhone;
    /** 收货地址。 */
    private String receiverAddress;
    /** 订单创建时间。 */
    private LocalDateTime createdAt;
}
