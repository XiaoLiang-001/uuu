package com.itheima.ncp.mapper.product;

import com.itheima.ncp.entity.product.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper {

    int insert(Product product);

    int updateById(Product product);

    int deleteById(@Param("id") Long id);

    Product findById(@Param("id") Long id);

    List<Product> findAllOrderByIdDesc();

    List<Product> findByKeywordAndStatusOrderByIdDesc(@Param("keyword") String keyword, @Param("status") String status);

    List<Product> findOnShelfOrderByIdDesc();

    int countOnShelf();

    List<Product> findOnShelfOrderByIdDescPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM agri_product "
            + "WHERE CONCAT(',', COALESCE(images, ''), ',') LIKE CONCAT('%,', #{name}, ',%')")
    int countHavingStoredImage(@Param("name") String name);

    @Update("UPDATE agri_product SET status = #{st}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatusByRow(@Param("id") Long id, @Param("st") String status);

    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
}
