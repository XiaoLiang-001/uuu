package com.itheima.ncp.controller;

import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.service.shop.ProductReviewService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserReviewController {

    private final UserService userService;
    private final ProductReviewService productReviewService;

    public UserReviewController(UserService userService, ProductReviewService productReviewService) {
        this.userService = userService;
        this.productReviewService = productReviewService;
    }

    @PostMapping("/user/reviews")
    public String add(@RequestParam("productId") long productId,
                     @RequestParam("content") String content,
                     @RequestParam(value = "rating", defaultValue = "5") int rating,
                     Authentication auth,
                     RedirectAttributes ra) {
        User u = userService.getByUsername(auth.getName());
        if (u == null || u.getId() == null) {
            ra.addFlashAttribute("err", "用户不存在");
            return "redirect:/user/market";
        }
        try {
            productReviewService.addReview(productId, u.getId(), u.getUsername(), content, rating);
            ra.addFlashAttribute("msg", "评价已提交，感谢您的反馈");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "评论失败");
        }
        return "redirect:/user/market/" + productId;
    }
}
