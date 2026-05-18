package com.itheima.ncp.controller;

import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.service.shop.ProductReviewService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户评价控制器，处理商品评价提交并回写提示信息。
 */
@Controller
public class UserReviewController {

    private final UserService userService;
    private final ProductReviewService productReviewService;

    public UserReviewController(UserService userService, ProductReviewService productReviewService) {
        this.userService = userService;
        this.productReviewService = productReviewService;
    }

    /**
     * 我的评论页面。
     */
    @GetMapping("/user/reviews")
    public String myReviews(Authentication auth, Model model, RedirectAttributes ra) {
        User u = userService.getByUsername(auth.getName());
        if (u == null || u.getId() == null) {
            ra.addFlashAttribute("err", "用户不存在");
            return "redirect:/user/profile";
        }
        model.addAttribute("reviews", productReviewService.listByUserId(u.getId()));
        return "user/reviews";
    }

    /**
     * 提交商品评价并回跳详情页。
     */
    @PostMapping("/user/reviews")
    public String add(@RequestParam("productId") long productId,
                     @RequestParam("content") String content,
                     @RequestParam(value = "rating", defaultValue = "5") int rating,
                     Authentication auth,
                     RedirectAttributes ra) {
        // 从登录态获取当前用户信息。
        User u = userService.getByUsername(auth.getName());
        if (u == null || u.getId() == null) {
            ra.addFlashAttribute("err", "用户不存在");
            return "redirect:/user/market";
        }
        try {
            // 调用 service 完成评价校验与保存。
            productReviewService.addReview(productId, u.getId(), u.getUsername(), content, rating);
            ra.addFlashAttribute("msg", "评价已提交，感谢您的反馈");
        } catch (IllegalArgumentException e) {
            // 业务校验失败时回显可读提示。
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "评论失败");
        }
        // 回到对应商品详情页。
        return "redirect:/user/market/" + productId;
    }

}
