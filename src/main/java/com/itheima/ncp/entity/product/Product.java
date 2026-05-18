package com.itheima.ncp.entity.product;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，对应商品主表记录。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("agri_product")
public class Product {

    /** 商品主键。 */
    private Long id;
    /** 商品名称。 */
    private String name;
    /** 商品描述。 */
    private String description;
    /** 商品单价。 */
    private BigDecimal price;
    /** 库存数量。 */
    private Integer stock;
    /** 上下架状态。 */
    private ProductStatus status;
    /** 图片存储文件名，多个英文逗号分隔 */
    private String images;
    /** 创建人用户ID。 */
    private Long createdBy;
    /** 产地省份ID（由关联表查询时回填）。 */
    @TableField(exist = false)
    private Long provinceId;
    /** 产地省份名称（由关联表查询时回填）。 */
    @TableField(exist = false)
    private String provinceName;
    /** 商品分类ID（由关联表查询时回填）。 */
    @TableField(exist = false)
    private Long categoryId;
    /** 商品分类名称（由关联表查询时回填）。 */
    @TableField(exist = false)
    private String categoryName;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最近更新时间。 */
    private LocalDateTime updatedAt;
}
