package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.CategoryOptionDto;
import com.itheima.ncp.dto.DictionaryNameRequest;
import com.itheima.ncp.dto.ProvinceOptionDto;
import com.itheima.ncp.mapper.product.ProductMapper;
import com.itheima.ncp.service.product.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端字典管理 API：省份 + 分类。
 */
@RestController
@RequestMapping("/api/admin/taxonomy")
public class AdminTaxonomyApiController {

    private final ProductMapper productMapper;
    private final ProductService productService;

    /** 注入商品 Mapper（字典读写）与商品服务（缓存失效）。 */
    public AdminTaxonomyApiController(ProductMapper productMapper, ProductService productService) {
        this.productMapper = productMapper;
        this.productService = productService;
    }

    /** 列出全部省份选项（下拉等）。 */
    @GetMapping("/provinces")
    public ResponseEntity<ApiResult<List<ProvinceOptionDto>>> provinces() {
        return ResponseEntity.ok(ApiResult.ok(productMapper.findAllProvinceOptions()));
    }

    /** 列出全部分类选项（下拉等）。 */
    @GetMapping("/categories")
    public ResponseEntity<ApiResult<List<CategoryOptionDto>>> categories() {
        return ResponseEntity.ok(ApiResult.ok(productMapper.findAllCategoryOptions()));
    }

    /** 新增省份（幂等忽略重复名），成功后刷新卖场相关缓存。 */
    @PostMapping(value = "/provinces", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<ProvinceOptionDto>> createProvince(@RequestBody DictionaryNameRequest body) {
        String name = body == null || body.getName() == null ? "" : body.getName().trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "省份名称不能为空"));
        }
        if (name.length() > 32) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "省份名称长度不能超过32个字符"));
        }
        productMapper.insertProvinceIgnore(name);
        ProvinceOptionDto dto = productMapper.findProvinceByName(name);
        if (dto == null || dto.getId() == null) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "省份保存失败"));
        }
        productService.evictMarketCaches();
        return ResponseEntity.ok(ApiResult.ok(dto));
    }

    /** 新增分类（幂等忽略重复名），成功后刷新卖场相关缓存。 */
    @PostMapping(value = "/categories", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<CategoryOptionDto>> createCategory(@RequestBody DictionaryNameRequest body) {
        String name = body == null || body.getName() == null ? "" : body.getName().trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "分类名称不能为空"));
        }
        if (name.length() > 32) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "分类名称长度不能超过32个字符"));
        }
        productMapper.insertCategoryIgnore(name);
        CategoryOptionDto dto = productMapper.findCategoryByName(name);
        if (dto == null || dto.getId() == null) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "分类保存失败"));
        }
        productService.evictMarketCaches();
        return ResponseEntity.ok(ApiResult.ok(dto));
    }

    /** 删除省份：仍被商品引用则拒绝；成功后刷新卖场缓存。 */
    @DeleteMapping("/provinces/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> deleteProvince(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "参数错误"));
        }
        if (productMapper.countProductsByProvinceId(id) > 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "该省份已被商品使用，不能删除"));
        }
        int n = productMapper.deleteProvinceById(id);
        if (n <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "省份不存在或已删除"));
        }
        productService.evictMarketCaches();
        return ResponseEntity.ok(ApiResult.<Void>ok());
    }

    /** 删除分类：仍被商品引用则拒绝；成功后刷新卖场缓存。 */
    @DeleteMapping("/categories/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> deleteCategory(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "参数错误"));
        }
        if (productMapper.countProductsByCategoryId(id) > 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "该分类已被商品使用，不能删除"));
        }
        int n = productMapper.deleteCategoryById(id);
        if (n <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "分类不存在或已删除"));
        }
        productService.evictMarketCaches();
        return ResponseEntity.ok(ApiResult.<Void>ok());
    }
}
