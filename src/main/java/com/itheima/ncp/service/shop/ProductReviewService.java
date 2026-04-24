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

    /** 提交商品评价。 */
    void addReview(long productId, Long userId, String loginUsername, String content, Integer rating);
}
