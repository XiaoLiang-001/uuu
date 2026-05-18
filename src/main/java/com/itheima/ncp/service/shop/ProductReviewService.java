package com.itheima.ncp.service.shop;

import com.itheima.ncp.entity.shop.ProductReview;

import java.util.List;

/**
 * 商品评价
 */
public interface ProductReviewService {

    /** 判断用户是否具备评价该商品资格。 */
    boolean canUserReview(Long userId, long productId);

    /** 查询商品评价列表。 */
    List<ProductReview> listByProductId(Long productId);

    /** 查询用户自己的评价列表。 */
    List<ProductReview> listByUserId(Long userId);

    /** 提交商品评价。 */
    void addReview(long productId, Long userId, String loginUsername, String content, Integer rating);

    /** 修改自己的评价。 */
    void updateReview(Long reviewId, Long userId, String content, Integer rating);

    /** 删除自己的评价。 */
    void deleteReview(Long reviewId, Long userId);
}
