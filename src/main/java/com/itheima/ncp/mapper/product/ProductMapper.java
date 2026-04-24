package com.itheima.ncp.mapper.product;

import com.itheima.ncp.entity.product.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品数据访问接口。
 */
@Mapper
public interface ProductMapper {

    /**
     * 新增商品。
     */
    int insert(Product product);

    /**
     * 按主键更新商品（通常为选择性字段）。
     */
    int updateById(Product product);

    /**
     * 按主键删除商品。
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按主键查询商品。
     */
    Product findById(@Param("id") Long id);

    /**
     * 查询全部商品（按ID倒序）。
     */
    List<Product> findAllOrderByIdDesc();

    /**
     * 管理端按关键字与状态筛选商品。
     */
    List<Product> findByKeywordAndStatusOrderByIdDesc(@Param("keyword") String keyword, @Param("status") String status);

    /**
     * 查询全部上架商品（按ID倒序）。
     */
    List<Product> findOnShelfOrderByIdDesc();

    /**
     * 统计上架商品数量。
     */
    int countOnShelf();

    /**
     * 分页查询上架商品。
     */
    List<Product> findOnShelfOrderByIdDescPaged(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 判断图片文件名是否仍被任一商品引用。
     */
    @Select("SELECT COUNT(*) FROM agri_product "
            + "WHERE CONCAT(',', COALESCE(images, ''), ',') LIKE CONCAT('%,', #{name}, ',%')")
    int countHavingStoredImage(@Param("name") String name);

    /**
     * 按主键更新商品状态并刷新更新时间。
     */
    @Update("UPDATE agri_product SET status = #{st}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatusByRow(@Param("id") Long id, @Param("st") String status);

    /**
     * 扣减库存（通常要求 SQL 层保证不扣成负数）。
     */
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
}
