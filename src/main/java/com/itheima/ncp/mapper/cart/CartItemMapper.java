package com.itheima.ncp.mapper.cart;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ncp.entity.shop.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 购物车数据访问接口，提供购物车条目增删改查。
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 插入购物车条目。
     */
    @Insert("INSERT INTO cart_item (user_id, product_id, quantity) VALUES (#{userId}, #{productId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(CartItem row);

    /**
     * 查询指定用户的全部购物车条目。
     */
    @Select("SELECT id, user_id, product_id, quantity, created_at, updated_at "
            + "FROM cart_item WHERE user_id = #{userId} ORDER BY id DESC")
    List<CartItem> findByUserId(@Param("userId") Long userId);

    /**
     * 按用户和商品查询购物车条目（用于判断是否已加购）。
     */
    @Select("SELECT id, user_id, product_id, quantity, created_at, updated_at "
            + "FROM cart_item WHERE user_id = #{userId} AND product_id = #{productId} LIMIT 1")
    CartItem findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 更新购物车条目数量。
     */
    @Update("UPDATE cart_item SET quantity = #{quantity}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 按条目ID + 用户ID删除，防止误删他人数据。
     */
    @Delete("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 清空指定用户购物车。
     */
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 按商品ID清理所有用户购物车中的该商品。
     */
    @Delete("DELETE FROM cart_item WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    /**
     * 统计商品在购物车中的引用数量。
     */
    @Select("SELECT COUNT(*) FROM cart_item WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);
}
