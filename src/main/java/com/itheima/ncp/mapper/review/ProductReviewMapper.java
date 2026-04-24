package com.itheima.ncp.mapper.review;

import com.itheima.ncp.entity.shop.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品评价数据访问接口，负责评价写入与按商品查询。
 */
@Mapper
public interface ProductReviewMapper {

    /**
     * 新增一条商品评价记录。
     */
    int insert(ProductReview review);

    /**
     * 按商品 ID 查询评价列表（按创建时间倒序）。
     */
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);
}
