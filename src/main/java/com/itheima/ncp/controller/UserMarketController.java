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
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 12;
        }
        if (size > 48) {
            size = 48;
        }
        int total = productService.countOnShelf();
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        if (page > totalPages) {
            page = totalPages;
        }
        int offset = (page - 1) * size;
        List<Product> products = productService.listOnShelfPage(offset, size);
        Map<Long, String> covers = productService.mapFirstImageStoredByProducts(products);

        List<Integer> sizeChoices = new ArrayList<Integer>(DEFAULT_PAGE_SIZES);
        if (!sizeChoices.contains(size)) {
            sizeChoices.add(size);
            Collections.sort(sizeChoices);
        }

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
        Product p = productService.getById(id);
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            ra.addFlashAttribute("err", "商品不存在或已下架");
            return "redirect:/user/market";
        }
        List<String> imageNames = productService.splitStoredImageNames(p);
        String cover = imageNames.isEmpty() ? null : imageNames.get(0);
        String desc = p.getDescription();
        boolean descBlank = desc == null || desc.trim().isEmpty();
        List<ProductReview> reviews = productReviewService.listByProductId(id);
        boolean canReview = false;
        if (authentication != null && authentication.isAuthenticated()) {
            User u = userService.getByUsername(authentication.getName());
            if (u != null && u.getId() != null) {
                canReview = productReviewService.canUserReview(u.getId(), id);
            }
        }
        model.addAttribute("product", p);
        model.addAttribute("imageNames", imageNames);
        model.addAttribute("coverImageName", cover);
        model.addAttribute("descriptionBlank", descBlank);
        model.addAttribute("reviews", reviews);
        model.addAttribute("canReview", canReview);
        return "user/market-detail";
    }
}
