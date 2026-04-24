package com.itheima.ncp.controller;

import com.itheima.ncp.service.shop.CartService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserCartController {

    private final CartService cartService;
    private final UserService userService;

    public UserCartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/user/cart")
    public String cart(Authentication auth, Model model) {
        long uid = requireUserId(auth);
        model.addAttribute("lines", cartService.listLines(uid));
        model.addAttribute("cartTotal", cartService.sumCartTotal(uid));
        return "user/cart";
    }

    @PostMapping("/user/cart/add")
    public String add(@RequestParam("productId") long productId,
                     @RequestParam("quantity") int quantity,
                     Authentication auth,
                     RedirectAttributes ra) {
        long uid = requireUserId(auth);
        try {
            cartService.addProduct(uid, productId, quantity);
            ra.addFlashAttribute("msg", "已加入购物车");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "加购失败");
        }
        return "redirect:/user/cart";
    }

    @PostMapping("/user/cart/update")
    public String update(@RequestParam("cartItemId") long cartItemId,
                        @RequestParam("quantity") int quantity,
                        Authentication auth,
                        RedirectAttributes ra) {
        long uid = requireUserId(auth);
        try {
            cartService.updateQuantity(uid, cartItemId, quantity);
            ra.addFlashAttribute("msg", "数量已更新");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "更新失败");
        }
        return "redirect:/user/cart";
    }

    @PostMapping("/user/cart/remove")
    public String remove(@RequestParam("cartItemId") long cartItemId,
                        Authentication auth,
                        RedirectAttributes ra) {
        long uid = requireUserId(auth);
        try {
            cartService.removeLine(uid, cartItemId);
            ra.addFlashAttribute("msg", "已移除该商品");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "操作失败");
        }
        return "redirect:/user/cart";
    }

    private long requireUserId(Authentication auth) {
        return userService.requireUserId(auth);
    }
}
