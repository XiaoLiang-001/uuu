package com.itheima.ncp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 集市筛选下拉的省份选项。
 */
@Getter
@Setter
@NoArgsConstructor
public class ProvinceOptionDto {
    /** 省份主键。 */
    private Long id;
    /** 省份名称。 */
    private String name;
}
