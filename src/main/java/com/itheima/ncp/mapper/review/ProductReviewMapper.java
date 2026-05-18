package com.itheima.ncp.mapper.review;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ncp.entity.shop.ProductReview;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 商品评价数据访问接口，负责评价写入与按商品查询。
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    /**
     * 新增一条商品评价记录。
     */
    @Insert("INSERT INTO product_review (product_id, user_id, username, content, rating) "
            + "VALUES (#{productId}, #{userId}, #{username}, #{content}, #{rating})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ProductReview review);

    /**
     * 按商品 ID 查询评价列表（按创建时间倒序）。
     */
    @Select("SELECT id, product_id, user_id, username, content, rating, created_at FROM product_review "
            + "WHERE product_id = #{productId} ORDER BY created_at DESC, id DESC")
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);

    /**
     * 按用户查询评价列表（携带商品名称）。
     */
    @Select("SELECT pr.id, pr.product_id, ap.name AS product_name, ap.status AS product_status, pr.user_id, pr.username, pr.content, pr.rating, pr.created_at "
            + "FROM product_review pr "
            + "LEFT JOIN agri_product ap ON ap.id = pr.product_id "
            + "WHERE pr.user_id = #{userId} "
            + "ORDER BY pr.created_at DESC, pr.id DESC")
    List<ProductReview> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 按主键查询评价。
     */
    @Select("SELECT id, product_id, user_id, username, content, rating, created_at "
            + "FROM product_review WHERE id = #{id} LIMIT 1")
    ProductReview findById(@Param("id") Long id);

    /**
     * 更新评价内容与评分。
     */
    @Update("UPDATE product_review SET content = #{content}, rating = #{rating} WHERE id = #{id} AND user_id = #{userId}")
    int updateByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId,
                            @Param("content") String content, @Param("rating") Integer rating);

    /**
     * 删除评价。
     */
    @Delete("DELETE FROM product_review WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 按商品删除全部评价。
     */
    @Delete("DELETE FROM product_review WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    /**
     * 统计商品评论数量。
     */
    @Select("SELECT COUNT(*) FROM product_review WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);
}
