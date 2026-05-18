package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.AdminProductDetailDto;
import com.itheima.ncp.dto.AdminProductBatchRequest;
import com.itheima.ncp.dto.AdminProductRowDto;
import com.itheima.ncp.dto.AdminProductStatusRequest;
import com.itheima.ncp.dto.CategoryOptionDto;
import com.itheima.ncp.dto.ProvinceOptionDto;
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
import java.util.LinkedHashSet;
import java.util.Set;
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
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "provinceId", required = false) Long provinceId,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        // 筛选参数容错解析：无效状态值按“不过滤状态”处理。
        ProductStatus status = parseStatusParam(statusStr);
        List<Product> products = productService.listForAdmin(keyword, status, provinceId, categoryId);
        // 批量映射首图，减少前端渲染时的额外计算。
        Map<Long, String> covers = productService.mapFirstImageStoredByProducts(products);
        List<AdminProductRowDto> rows = products.stream().map(p -> toRow(p, covers.get(p.getId()))).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.ok(rows));
    }

    /**
     * 管理端省份筛选选项。
     */
    @GetMapping("/province-options")
    public ResponseEntity<ApiResult<List<ProvinceOptionDto>>> provinceOptions() {
        return ResponseEntity.ok(ApiResult.ok(productService.listAllProvinceOptions()));
    }

    /**
     * 管理端分类筛选选项。
     */
    @GetMapping("/category-options")
    public ResponseEntity<ApiResult<List<CategoryOptionDto>>> categoryOptions() {
        return ResponseEntity.ok(ApiResult.ok(productService.listAllCategoryOptions()));
    }

    /**
     * 查询商品详情，用于管理端编辑页回显。
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<AdminProductDetailDto>> get(@PathVariable Long id) {
        // 先查商品，未命中返回 404。
        Product p = productService.getById(id);
        if (p == null) {
            return ResponseEntity.status(404).body(ApiResult.fail(404, "商品不存在"));
        }
        // 将实体转换为详情 DTO，避免前端依赖数据库字段结构。
        AdminProductDetailDto d = new AdminProductDetailDto();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
        d.setStock(p.getStock());
        d.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        d.setCategoryName(p.getCategoryName());
        d.setProvinceName(p.getProvinceName());
        // 详情返回图片名列表，由前端统一拼接访问 URL。
        d.setImageNames(productService.splitStoredImageNames(p));
        return ResponseEntity.ok(ApiResult.ok(d));
    }

    /**
     * 删除商品，仅允许删除符合业务约束的记录。
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable Long id) {
        try {
            // 管理员删除优先：不因关联业务阻断删除。
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            // 业务错误返回 400，给前端可读提示。
            String msg = e.getMessage() != null ? e.getMessage() : "删除失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            // 兜底处理数据库约束等运行时异常，避免前端只看到“删除失败”。
            String msg = resolveDeleteErrorMessage(e);
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * 更新商品上下架状态。
     */
    @PatchMapping(value = "/{id:\\d+}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> updateStatus(@PathVariable Long id,
                                                        @RequestBody AdminProductStatusRequest body) {
        // 基础请求体校验。
        if (body == null || body.getStatus() == null || body.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态无效"));
        }
        // 取出状态字符串并准备解析枚举。
        String statusStr = body.getStatus();
        final ProductStatus status;
        try {
            status = ProductStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (Exception e) {
            // 状态值非法时返回 400，而不是透传枚举解析异常。
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态无效"));
        }
        try {
            // 具体状态更新逻辑由 service 执行。
            productService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "更新失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * 批量更新商品上下架状态。
     */
    @PatchMapping(value = "/batch-status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> batchUpdateStatus(@RequestBody AdminProductBatchRequest body) {
        if (body == null || body.getStatus() == null || body.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态无效"));
        }
        ProductStatus status;
        try {
            status = ProductStatus.valueOf(body.getStatus().trim().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "状态无效"));
        }
        Set<Long> ids = normalizeIds(body.getIds());
        if (ids.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "请选择要操作的商品"));
        }
        List<String> failed = new java.util.ArrayList<String>();
        for (Long id : ids) {
            try {
                productService.updateStatus(id, status);
            } catch (Exception e) {
                String reason = e.getMessage() == null ? "未知原因" : e.getMessage();
                failed.add(id + "(" + reason + ")");
            }
        }
        if (!failed.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "部分商品操作失败: " + String.join("，", failed)));
        }
        return ResponseEntity.ok(ApiResult.<Void>ok());
    }

    /**
     * 批量删除商品（仅支持已下架商品）。
     */
    @DeleteMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> batchDelete(@RequestBody AdminProductBatchRequest body) {
        Set<Long> ids = normalizeIds(body == null ? null : body.getIds());
        if (ids.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "请选择要删除的商品"));
        }
        List<String> failed = new java.util.ArrayList<String>();
        for (Long id : ids) {
            try {
                productService.deleteProduct(id);
            } catch (Exception e) {
                String reason = resolveDeleteErrorMessage(e);
                failed.add(id + "(" + reason + ")");
            }
        }
        if (!failed.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "部分商品删除失败: " + String.join("，", failed)));
        }
        return ResponseEntity.ok(ApiResult.<Void>ok());
    }

    /**
     * 解析列表筛选状态参数；非法值按未筛选处理。
     */
    private static ProductStatus parseStatusParam(String statusStr) {
        // 空参数表示不按状态过滤。
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        try {
            return ProductStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // 列表场景下忽略非法状态参数，保证页面可正常加载。
            return null;
        }
    }

    private static Set<Long> normalizeIds(List<Long> ids) {
        Set<Long> out = new LinkedHashSet<Long>();
        if (ids == null) {
            return out;
        }
        for (Long id : ids) {
            if (id != null && id > 0) {
                out.add(id);
            }
        }
        return out;
    }

    /**
     * 解析删除失败的可读原因，优先给出业务可理解提示。
     */
    private static String resolveDeleteErrorMessage(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String cls = cur.getClass().getName();
            String msg = cur.getMessage();
            if (cls.contains("DataIntegrityViolation")
                    || cls.contains("SQLIntegrityConstraintViolationException")
                    || cls.contains("ConstraintViolationException")
                    || (msg != null && (msg.contains("foreign key") || msg.contains("a foreign key constraint fails")))) {
                return "删除失败，请稍后重试";
            }
            cur = cur.getCause();
        }
        return e.getMessage() != null ? e.getMessage() : "删除失败";
    }

    /**
     * 将商品实体映射为管理端列表行 DTO。
     */
    private static AdminProductRowDto toRow(Product p, String coverStoredName) {
        // 列表行 DTO 组装。
        AdminProductRowDto d = new AdminProductRowDto();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setPrice(p.getPrice());
        d.setStock(p.getStock());
        // status 允许为空，前端可显示为“未知”。
        d.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        d.setCategoryName(p.getCategoryName());
        d.setProvinceName(p.getProvinceName());
        // 首图文件名由外部映射传入。
        d.setCoverStoredName(coverStoredName);
        return d;
    }
}
