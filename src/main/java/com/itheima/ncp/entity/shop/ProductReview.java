package com.itheima.ncp.entity.shop;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("product_review")
public class ProductReview {

    /** 评价主键。 */
    private Long id;
    /** 被评价商品ID。 */
    private Long productId;
    /** 被评价商品名称（列表展示用）。 */
    @TableField(exist = false)
    private String productName;
    /** 被评价商品状态（列表跳转提示用）。 */
    @TableField(exist = false)
    private String productStatus;
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
