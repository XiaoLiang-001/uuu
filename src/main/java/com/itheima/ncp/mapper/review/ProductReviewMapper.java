package com.itheima.ncp.mapper.review;

import com.itheima.ncp.entity.shop.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductReviewMapper {

    int insert(ProductReview review);

    List<ProductReview> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);
}
