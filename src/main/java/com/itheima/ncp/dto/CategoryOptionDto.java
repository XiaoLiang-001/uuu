package com.itheima.ncp.dto;

import lombok.Data;

/**
 * 商品分类选项。
 */
@Data
public class CategoryOptionDto {

    /** 分类ID。 */
    private Long id;
    /** 分类名称。 */
    private String name;
}
