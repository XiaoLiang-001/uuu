package com.itheima.ncp.service.impl;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.entity.shop.ProductReview;
import com.itheima.ncp.mapper.order.OrderItemMapper;
import com.itheima.ncp.mapper.product.ProductMapper;
import com.itheima.ncp.mapper.review.ProductReviewMapper;
import com.itheima.ncp.service.shop.ProductReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品评价服务实现，校验评价资格并维护评价数据。
 */
@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewMapper productReviewMapper;
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;

    public ProductReviewServiceImpl(ProductReviewMapper productReviewMapper, ProductMapper productMapper,
                                   OrderItemMapper orderItemMapper) {
        this.productReviewMapper = productReviewMapper;
        this.productMapper = productMapper;
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 判断用户是否购买过该商品，从而是否具备评价资格。
     */
    @Override
    public boolean canUserReview(Long userId, long productId) {
        if (userId == null) {
            return false;
        }
        return orderItemMapper.countByUserIdAndProductId(userId, productId) > 0;
    }

    /**
     * 查询商品评价列表（按创建时间倒序）。
     */
    @Override
    public List<ProductReview> listByProductId(Long productId) {
        return productReviewMapper.findByProductIdOrderByCreatedAtDesc(productId);
    }

    /**
     * 新增商品评价，包含商品状态、内容、评分与购买记录校验。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(long productId, Long userId, String loginUsername, String content, Integer rating) {
        Product p = productMapper.findById(productId);
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        String c = content == null ? "" : content.trim();
        if (c.isEmpty()) {
            throw new IllegalArgumentException("请填写评论内容");
        }
        if (c.length() > 2000) {
            throw new IllegalArgumentException("评论过长");
        }
        int r = rating != null ? rating : 5;
        if (r < 1 || r > 5) {
            throw new IllegalArgumentException("评分应为 1～5");
        }
        if (!canUserReview(userId, productId)) {
            throw new IllegalArgumentException("仅购买过该商品的用户可以评论");
        }
        ProductReview rev = new ProductReview();
        rev.setProductId(productId);
        rev.setUserId(userId);
        rev.setUsername(loginUsername != null ? loginUsername.trim() : "");
        rev.setContent(c);
        rev.setRating(r);
        productReviewMapper.insert(rev);
    }
}
