package com.itheima.ncp.service.shop;

import com.itheima.ncp.entity.shop.ProductReview;

import java.util.List;

/**
 * 商品评价
 */
public interface ProductReviewService {

    boolean canUserReview(Long userId, long productId);

    List<ProductReview> listByProductId(Long productId);

    void addReview(long productId, Long userId, String loginUsername, String content, Integer rating);
}
