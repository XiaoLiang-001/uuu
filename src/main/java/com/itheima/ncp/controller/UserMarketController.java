package com.itheima.ncp.controller;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.entity.shop.ProductReview;
import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.service.product.ProductService;
import com.itheima.ncp.service.shop.ProductReviewService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户端商城控制器，负责商品列表、详情页展示以及分页参数准备。
 */
@Controller
public class UserMarketController {

    private static final List<Integer> DEFAULT_PAGE_SIZES =
            java.util.Arrays.asList(6, 12, 18, 24, 30, 36, 48);

    private final ProductService productService;
    private final ProductReviewService productReviewService;
    private final UserService userService;

    public UserMarketController(ProductService productService, ProductReviewService productReviewService,
                                UserService userService) {
        this.productService = productService;
        this.productReviewService = productReviewService;
        this.userService = userService;
    }

    /**
     * 商城列表页，按分页参数查询在售商品并构建分页模型。
     */
    @GetMapping("/user/market")
    public String list(@RequestParam(value = "page", defaultValue = "1") int page,
                      @RequestParam(value = "size", defaultValue = "12") int size,
                      Model model) {
        // 页码最小为 1。
        if (page < 1) {
            page = 1;
        }
        // 页大小最小限制。
        if (size < 1) {
            size = 12;
        }
        // 页大小最大限制，防止一次拉取过多数据。
        if (size > 48) {
            size = 48;
        }
        // 查询总条数并计算总页数。
        int total = productService.countOnShelf();
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        // 页码超界时截断到最后一页。
        if (page > totalPages) {
            page = totalPages;
        }
        // 计算分页偏移量。
        int offset = (page - 1) * size;
        // 查询当前页商品与首图映射。
        List<Product> products = productService.listOnShelfPage(offset, size);
        Map<Long, String> covers = productService.mapFirstImageStoredByProducts(products);

        // 页面可选页大小列表，若当前 size 不在默认值中则动态补入。
        List<Integer> sizeChoices = new ArrayList<Integer>(DEFAULT_PAGE_SIZES);
        if (!sizeChoices.contains(size)) {
            sizeChoices.add(size);
            Collections.sort(sizeChoices);
        }

        // 注入分页与商品数据给模板渲染。
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSizeChoices", sizeChoices);
        model.addAttribute("products", products);
        model.addAttribute("covers", covers);
        return "user/market";
    }

    /**
     * 商品详情页，包含图片与评价信息，并判断当前用户是否可评价。
     */
    @GetMapping("/user/market/{id:\\d+}")
    public String detail(@PathVariable long id, Authentication authentication, Model model,
                        RedirectAttributes ra) {
        // 仅允许查看存在且上架中的商品。
        Product p = productService.getById(id);
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            ra.addFlashAttribute("err", "商品不存在或已下架");
            return "redirect:/user/market";
        }
        // 准备图片、描述与评价展示数据。
        List<String> imageNames = productService.splitStoredImageNames(p);
        String cover = imageNames.isEmpty() ? null : imageNames.get(0);
        String desc = p.getDescription();
        boolean descBlank = desc == null || desc.trim().isEmpty();
        List<ProductReview> reviews = productReviewService.listByProductId(id);
        // 默认不可评价，登录且购买过后才可评价。
        boolean canReview = false;
        if (authentication != null && authentication.isAuthenticated()) {
            User u = userService.getByUsername(authentication.getName());
            if (u != null && u.getId() != null) {
                canReview = productReviewService.canUserReview(u.getId(), id);
            }
        }
        // 注入详情页模型。
        model.addAttribute("product", p);
        model.addAttribute("imageNames", imageNames);
        model.addAttribute("coverImageName", cover);
        model.addAttribute("descriptionBlank", descBlank);
        model.addAttribute("reviews", reviews);
        model.addAttribute("canReview", canReview);
        return "user/market-detail";
    }
}
