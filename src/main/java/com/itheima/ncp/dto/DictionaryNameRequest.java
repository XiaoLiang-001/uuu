package com.itheima.ncp.dto;

import lombok.Data;

/**
 * 字典项新增请求（仅名称）。
 */
@Data
public class DictionaryNameRequest {

    /** 字典名称。 */
    private String name;
}
