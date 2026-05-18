package com.itheima.ncp.entity.shop;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 订单明细实体。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("order_item")
public class OrderItem {

    /** 明细主键。 */
    private Long id;
    /** 订单主键。 */
    private Long orderId;
    /** 商品主键。 */
    private Long productId;
    /** 下单时快照的商品名。 */
    private String productName;
    /** 下单时快照的商品单价。 */
    private BigDecimal unitPrice;
    /** 购买数量。 */
    private Integer quantity;
    /** 行小计（单价 * 数量）。 */
    private BigDecimal subtotal;
}
