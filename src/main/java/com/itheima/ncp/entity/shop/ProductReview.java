package com.itheima.ncp.entity.shop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 商品评价实体。
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductReview {

    /** 评价主键。 */
    private Long id;
    /** 被评价商品ID。 */
    private Long productId;
    /** 评价用户ID。 */
    private Long userId;
    /** 评价用户名快照。 */
    private String username;
    /** 评价内容。 */
    private String content;
    /** 评分（1~5）。 */
    private Integer rating;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
