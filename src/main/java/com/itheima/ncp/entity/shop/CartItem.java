package com.itheima.ncp.entity.shop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 购物车条目实体。
 */
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    /** 购物车条目主键。 */
    private Long id;
    /** 所属用户ID。 */
    private Long userId;
    /** 商品ID。 */
    private Long productId;
    /** 购买数量。 */
    private Integer quantity;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
