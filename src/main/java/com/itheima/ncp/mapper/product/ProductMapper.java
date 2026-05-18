package com.itheima.ncp.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ncp.dto.ProvinceOptionDto;
import com.itheima.ncp.dto.CategoryOptionDto;
import com.itheima.ncp.entity.product.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品数据访问接口。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 新增商品。
     */
    @Insert("INSERT INTO agri_product (name, description, price, stock, status, images, created_by) "
            + "VALUES (#{name}, #{description}, #{price}, #{stock}, #{status}, #{images}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Product product);

    /**
     * 按主键更新商品（通常为选择性字段）。
     */
    @Update("UPDATE agri_product SET name = #{name}, description = #{description}, price = #{price}, "
            + "stock = #{stock}, status = #{status}, images = #{images}, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id}")
    int updateById(Product product);

    /**
     * 按主键删除商品。
     */
    @Delete("DELETE FROM agri_product WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 按主键查询商品。
     */
    @Select("SELECT ap.id, ap.name, ap.description, ap.price, ap.stock, ap.status, ap.images, ap.created_by, ap.created_at, ap.updated_at, "
            + "app.province_id AS province_id, p.name AS province_name, "
            + "apc.category_id AS category_id, c.name AS category_name "
            + "FROM agri_product ap "
            + "LEFT JOIN agri_product_province app ON app.product_id = ap.id "
            + "LEFT JOIN ncp_province p ON p.id = app.province_id "
            + "LEFT JOIN agri_product_category apc ON apc.product_id = ap.id "
            + "LEFT JOIN ncp_category c ON c.id = apc.category_id "
            + "WHERE ap.id = #{id} "
            + "LIMIT 1")
    Product findById(@Param("id") Long id);

    /**
     * 查询全部商品（按ID倒序）。
     */
    @Select("SELECT ap.id, ap.name, ap.description, ap.price, ap.stock, ap.status, ap.images, ap.created_by, ap.created_at, ap.updated_at, "
            + "app.province_id AS province_id, p.name AS province_name, "
            + "apc.category_id AS category_id, c.name AS category_name "
            + "FROM agri_product ap "
            + "LEFT JOIN agri_product_province app ON app.product_id = ap.id "
            + "LEFT JOIN ncp_province p ON p.id = app.province_id "
            + "LEFT JOIN agri_product_category apc ON apc.product_id = ap.id "
            + "LEFT JOIN ncp_category c ON c.id = apc.category_id "
            + "ORDER BY ap.id DESC")
    List<Product> findAllOrderByIdDesc();

    /**
     * 管理端按关键字、状态、省份、分类筛选商品。
     */
    @Select({
            "<script>",
            "SELECT ap.id, ap.name, ap.description, ap.price, ap.stock, ap.status, ap.images, ap.created_by, ap.created_at, ap.updated_at,",
            "       app.province_id AS province_id, p.name AS province_name,",
            "       apc.category_id AS category_id, c.name AS category_name",
            "FROM agri_product ap",
            "LEFT JOIN agri_product_province app ON app.product_id = ap.id",
            "LEFT JOIN ncp_province p ON p.id = app.province_id",
            "LEFT JOIN agri_product_category apc ON apc.product_id = ap.id",
            "LEFT JOIN ncp_category c ON c.id = apc.category_id",
            "<where>",
            "  <if test=\"keyword != null and keyword != ''\">",
            "    AND ap.name LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "  <if test=\"status != null and status != ''\">",
            "    AND UPPER(TRIM(ap.status)) = UPPER(TRIM(#{status}))",
            "  </if>",
            "  <if test=\"provinceId != null\">",
            "    AND app.province_id = #{provinceId}",
            "  </if>",
            "  <if test=\"categoryId != null\">",
            "    AND apc.category_id = #{categoryId}",
            "  </if>",
            "</where>",
            "ORDER BY ap.id DESC",
            "</script>"
    })
    List<Product> findByKeywordAndStatusAndProvinceOrderByIdDesc(@Param("keyword") String keyword,
                                                                  @Param("status") String status,
                                                                  @Param("provinceId") Long provinceId,
                                                                  @Param("categoryId") Long categoryId);

    /**
     * 查询全部上架商品（按ID倒序）。
     */
    @Select("SELECT ap.id, ap.name, ap.description, ap.price, ap.stock, ap.status, ap.images, ap.created_by, ap.created_at, ap.updated_at "
            + ", app.province_id AS province_id, p.name AS province_name, "
            + "apc.category_id AS category_id, c.name AS category_name "
            + "FROM agri_product ap "
            + "LEFT JOIN agri_product_province app ON app.product_id = ap.id "
            + "LEFT JOIN ncp_province p ON p.id = app.province_id "
            + "LEFT JOIN agri_product_category apc ON apc.product_id = ap.id "
            + "LEFT JOIN ncp_category c ON c.id = apc.category_id "
            + "WHERE ap.status = 'ON_SHELF' ORDER BY ap.id DESC")
    List<Product> findOnShelfOrderByIdDesc();

    /**
     * 统计上架商品数量。
     */
    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM agri_product ap",
            "LEFT JOIN agri_product_province app ON app.product_id = ap.id",
            "LEFT JOIN agri_product_category apc ON apc.product_id = ap.id",
            "WHERE ap.status = 'ON_SHELF'",
            "  <if test=\"keyword != null and keyword != ''\">",
            "    AND ap.name LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "  <if test=\"provinceId != null\">",
            "    AND app.province_id = #{provinceId}",
            "  </if>",
            "  <if test=\"categoryId != null\">",
            "    AND apc.category_id = #{categoryId}",
            "  </if>",
            "</script>"
    })
    int countOnShelfByFilter(@Param("keyword") String keyword, @Param("provinceId") Long provinceId, @Param("categoryId") Long categoryId);

    /**
     * 分页查询上架商品。
     */
    @Select({
            "<script>",
            "SELECT ap.id, ap.name, ap.description, ap.price, ap.stock, ap.status, ap.images, ap.created_by, ap.created_at, ap.updated_at,",
            "       app.province_id AS province_id, p.name AS province_name,",
            "       apc.category_id AS category_id, c.name AS category_name",
            "FROM agri_product ap",
            "LEFT JOIN agri_product_province app ON app.product_id = ap.id",
            "LEFT JOIN ncp_province p ON p.id = app.province_id",
            "LEFT JOIN agri_product_category apc ON apc.product_id = ap.id",
            "LEFT JOIN ncp_category c ON c.id = apc.category_id",
            "WHERE ap.status = 'ON_SHELF'",
            "  <if test=\"keyword != null and keyword != ''\">",
            "    AND ap.name LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "  <if test=\"provinceId != null\">",
            "    AND app.province_id = #{provinceId}",
            "  </if>",
            "  <if test=\"categoryId != null\">",
            "    AND apc.category_id = #{categoryId}",
            "  </if>",
            "ORDER BY ap.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<Product> findOnShelfPagedByFilter(@Param("keyword") String keyword,
                                           @Param("provinceId") Long provinceId,
                                           @Param("categoryId") Long categoryId,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    /**
     * 查询集市可筛选省份（仅显示有上架商品的省份）。
     */
    @Select("SELECT DISTINCT p.id, p.name "
            + "FROM ncp_province p "
            + "INNER JOIN agri_product_province app ON app.province_id = p.id "
            + "INNER JOIN agri_product ap ON ap.id = app.product_id "
            + "WHERE ap.status = 'ON_SHELF' "
            + "ORDER BY p.name ASC")
    List<ProvinceOptionDto> findProvinceOptionsForMarket();

    /**
     * 查询集市可筛选分类（仅显示有上架商品的分类）。
     */
    @Select("SELECT DISTINCT c.id, c.name "
            + "FROM ncp_category c "
            + "INNER JOIN agri_product_category apc ON apc.category_id = c.id "
            + "INNER JOIN agri_product ap ON ap.id = apc.product_id "
            + "WHERE ap.status = 'ON_SHELF' "
            + "ORDER BY c.name ASC")
    List<CategoryOptionDto> findCategoryOptionsForMarket();

    /**
     * 查询集市可筛选分类（可按省份进一步收敛，仅显示有上架商品的分类）。
     */
    @Select({
            "<script>",
            "SELECT DISTINCT c.id, c.name",
            "FROM ncp_category c",
            "INNER JOIN agri_product_category apc ON apc.category_id = c.id",
            "INNER JOIN agri_product ap ON ap.id = apc.product_id",
            "LEFT JOIN agri_product_province app ON app.product_id = ap.id",
            "WHERE ap.status = 'ON_SHELF'",
            "  <if test=\"provinceId != null\">",
            "    AND app.province_id = #{provinceId}",
            "  </if>",
            "ORDER BY c.name ASC",
            "</script>"
    })
    List<CategoryOptionDto> findCategoryOptionsForMarketByProvince(@Param("provinceId") Long provinceId);

    /**
     * 查询全部省份（管理端商品编辑页下拉框）。
     */
    @Select("SELECT id, name FROM ncp_province ORDER BY name ASC")
    List<ProvinceOptionDto> findAllProvinceOptions();

    /**
     * 查询全部分类（管理端商品编辑页下拉框）。
     */
    @Select("SELECT id, name FROM ncp_category ORDER BY name ASC")
    List<CategoryOptionDto> findAllCategoryOptions();

    /**
     * 按省份名称查询（用于管理端手填省份时复用已有字典项）。
     */
    @Select("SELECT id, name FROM ncp_province WHERE name = #{name} LIMIT 1")
    ProvinceOptionDto findProvinceByName(@Param("name") String name);

    /**
     * 按分类名称查询（用于管理端手填分类时复用已有字典项）。
     */
    @Select("SELECT id, name FROM ncp_category WHERE name = #{name} LIMIT 1")
    CategoryOptionDto findCategoryByName(@Param("name") String name);

    /**
     * 省份字典插入（已存在时忽略，依赖 uk_ncp_province_name 唯一键）。
     */
    @Insert("INSERT IGNORE INTO ncp_province(name) VALUES(#{name})")
    int insertProvinceIgnore(@Param("name") String name);

    /**
     * 分类字典插入（已存在时忽略，依赖 uk_ncp_category_name 唯一键）。
     */
    @Insert("INSERT IGNORE INTO ncp_category(name) VALUES(#{name})")
    int insertCategoryIgnore(@Param("name") String name);

    /**
     * 写入或更新商品-省份关联。
     */
    @Insert("INSERT INTO agri_product_province(product_id, province_id) VALUES(#{productId}, #{provinceId}) "
            + "ON DUPLICATE KEY UPDATE province_id = VALUES(province_id)")
    int upsertProductProvince(@Param("productId") Long productId, @Param("provinceId") Long provinceId);

    /**
     * 写入或更新商品-分类关联。
     */
    @Insert("INSERT INTO agri_product_category(product_id, category_id) VALUES(#{productId}, #{categoryId}) "
            + "ON DUPLICATE KEY UPDATE category_id = VALUES(category_id)")
    int upsertProductCategory(@Param("productId") Long productId, @Param("categoryId") Long categoryId);

    /**
     * 删除商品的省份关联（选择“未指定产地”时使用）。
     */
    @Delete("DELETE FROM agri_product_province WHERE product_id = #{productId}")
    int deleteProductProvince(@Param("productId") Long productId);

    /**
     * 删除商品的分类关联（选择“未指定分类”时使用）。
     */
    @Delete("DELETE FROM agri_product_category WHERE product_id = #{productId}")
    int deleteProductCategory(@Param("productId") Long productId);

    /**
     * 统计某省份被商品引用次数。
     */
    @Select("SELECT COUNT(*) FROM agri_product_province WHERE province_id = #{provinceId}")
    int countProductsByProvinceId(@Param("provinceId") Long provinceId);

    /**
     * 统计某分类被商品引用次数。
     */
    @Select("SELECT COUNT(*) FROM agri_product_category WHERE category_id = #{categoryId}")
    int countProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 删除省份字典项。
     */
    @Delete("DELETE FROM ncp_province WHERE id = #{id}")
    int deleteProvinceById(@Param("id") Long id);

    /**
     * 删除分类字典项。
     */
    @Delete("DELETE FROM ncp_category WHERE id = #{id}")
    int deleteCategoryById(@Param("id") Long id);

    /**
     * 判断图片文件名是否仍被任一商品引用。
     */
    @Select("SELECT COUNT(*) FROM agri_product "
            + "WHERE CONCAT(',', COALESCE(images, ''), ',') LIKE CONCAT('%,', #{name}, ',%')")
    int countHavingStoredImage(@Param("name") String name);

    /**
     * 扣减库存（通常要求 SQL 层保证不扣成负数）。
     */
    @Update("UPDATE agri_product SET stock = stock - #{qty}, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND stock >= #{qty}")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
}
