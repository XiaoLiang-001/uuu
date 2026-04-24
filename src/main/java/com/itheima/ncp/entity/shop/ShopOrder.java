package com.itheima.ncp.entity.shop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ShopOrder {

    private Long id;
    private Long userId;
    private String orderNo;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime createdAt;
}
