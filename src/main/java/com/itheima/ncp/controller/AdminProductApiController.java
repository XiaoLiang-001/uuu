package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.AdminProductDetailDto;
import com.itheima.ncp.dto.AdminProductRowDto;
import com.itheima.ncp.dto.AdminProductStatusRequest;
import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.service.product.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端商品 REST API，提供商品查询、详情、上下架与删除接口。
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductApiController {

    private final ProductService productService;

    public AdminProductApiController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 列表：支持 keyword 模糊匹配名称、status=ON_SHELF|OFF_SHELF。
     */
    @GetMapping
    public ResponseEntity<ApiResult<List<AdminProductRowDto>>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String statusStr) {
        ProductStatus status = parseStatusParam(statusStr);
        List<Product> products = productService.listForAdmin(keyword, status);
        Map<Long, String> covers = productService.mapFirstImageStoredByProducts(products);
        List<AdminProductRowDto> rows = products.stream().map(p -> toRow(p, covers.get(p.getId()))).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.ok(rows));
    }

    @GetMapping("/{id:\\d+}")
    /**
     * 查询商品详情，用于管理端编辑页回显。
     */
    public ResponseEntity<ApiResult<AdminProductDetailDto>> get(@PathVariable Long id) {
        Product p = productService.getById(id);
        if (p == null) {
            return ResponseEntity.status(404).body(ApiResult.fail(404, "商品不存在"));
        }
        AdminProductDetailDto d = new AdminProductDetailDto();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
        d.setStock(p.getStock());
        d.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        d.setImageNames(productService.splitStoredImageNames(p));
        return ResponseEntity.ok(ApiResult.ok(d));
    }

    /**
     * 删除商品，仅允许删除符合业务约束的记录。
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "删除失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * 更新商品上下架状态。
     */
    @PatchMapping(value = "/{id:\\d+}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody AdminProductStatusRequest body) {
        if (body == null || body.getStatus() == null || body.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态无效"));
        }
        String statusStr = body.getStatus();
        final ProductStatus status;
        try {
            status = ProductStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态无效"));
        }
        try {
            productService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "更新失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * 解析列表筛选状态参数；非法值按未筛选处理。
     */
    private static ProductStatus parseStatusParam(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        try {
            return ProductStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 将商品实体映射为管理端列表行 DTO。
     */
    private static AdminProductRowDto toRow(Product p, String coverStoredName) {
        AdminProductRowDto d = new AdminProductRowDto();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setPrice(p.getPrice());
        d.setStock(p.getStock());
        d.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        d.setCoverStoredName(coverStoredName);
        return d;
    }
}
