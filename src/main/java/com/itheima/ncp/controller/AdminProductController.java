package com.itheima.ncp.controller;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.service.product.ProductService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端商品页面控制器，处理商品新增、编辑表单提交与页面跳转。
 */
@Controller
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 打开商品管理列表页。
     */
    @GetMapping("/admin/products")
    public String list() {
        return "admin/products";
    }

    /**
     * 打开新增商品表单页。
     */
    @GetMapping("/admin/products/new")
    public String newForm() {
        return "admin/product-form";
    }

    /**
     * 处理新增商品表单提交，校验关键字段后写入商品与图片信息。
     */
    @PostMapping(value = "/admin/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String create(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") String priceStr,
            @RequestParam("stock") int stock,
            @RequestParam(value = "status", defaultValue = "ON_SHELF") String statusStr,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            RedirectAttributes ra) {
        try {
            BigDecimal price = new BigDecimal(priceStr.trim());
            ProductStatus status = ProductStatus.valueOf(statusStr.trim().toUpperCase());
            if (status != ProductStatus.ON_SHELF && status != ProductStatus.OFF_SHELF) {
                throw new IllegalArgumentException("状态只能选择上架或下架");
            }
            String op = principal != null ? principal.getUsername() : null;
            productService.createProduct(op, name, description, price, stock, status, images);
            ra.addFlashAttribute("msg", "商品已创建");
        } catch (Exception e) {
            String m = e.getMessage() != null ? e.getMessage() : (e.getCause() != null ? e.getCause().getMessage() : "创建失败");
            ra.addFlashAttribute("error", m);
            return "redirect:/admin/products/new";
        }
        return "redirect:/admin/products";
    }

    /**
     * 打开商品编辑页，并加载商品详情及图片列表。
     */
    @GetMapping("/admin/products/{id:\\d+}")
    public String editForm(@PathVariable long id, Model model, RedirectAttributes ra) {
        Product p = productService.getById(id);
        if (p == null) {
            ra.addFlashAttribute("err", "商品不存在");
            return "redirect:/admin/products";
        }
        model.addAttribute("product", p);
        model.addAttribute("imageNames", productService.splitStoredImageNames(p));
        return "admin/product-detail";
    }

    /**
     * 处理商品编辑保存，支持新增图片和删除指定历史图片。
     */
    @PostMapping(value = "/admin/products/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String update(
            @PathVariable long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") String priceStr,
            @RequestParam("stock") int stock,
            @RequestParam("status") String statusStr,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "removeImages", required = false) String[] removeImages,
            RedirectAttributes ra) {
        try {
            BigDecimal price = new BigDecimal(priceStr.trim());
            ProductStatus status = ProductStatus.valueOf(statusStr.trim().toUpperCase());
            if (status != ProductStatus.ON_SHELF && status != ProductStatus.OFF_SHELF) {
                throw new IllegalArgumentException("状态只能选择上架或下架");
            }
            productService.updateProduct(id, name, description, price, stock, status, images, removeImages);
            ra.addFlashAttribute("msg", "已保存");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "图片处理失败");
        } catch (Exception e) {
            String m = e.getMessage() != null ? e.getMessage() : "保存失败";
            ra.addFlashAttribute("error", m);
        }
        return "redirect:/admin/products/" + id;
    }
}
