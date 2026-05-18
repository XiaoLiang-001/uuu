package com.itheima.ncp.controller;

import com.itheima.ncp.service.shop.CartService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户购物车控制器，处理购物车展示与增删改数量操作。
 */
@Controller
public class UserCartController {

    private final CartService cartService;
    private final UserService userService;

    public UserCartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    /**
     * 展示当前登录用户购物车内容与总金额。
     */
    @GetMapping("/user/cart")
    public String cart(Authentication auth, Model model) {
        long uid;
        try {
            // 从认证信息解析当前用户主键。
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            model.addAttribute("msg", e.getMessage() != null ? e.getMessage() : "请先登录");
            model.addAttribute("redirectUrl", "/login");
            return "common/alert-redirect";
        }
        // 加载购物车数据用于页面展示。
        bindCartModel(uid, model);
        // 返回购物车页面模板。
        return "user/cart";
    }

    /**
     * 向购物车新增商品。
     */
    @PostMapping("/user/cart/add")
    public String add(@RequestParam("productId") long productId,
                     @RequestParam("quantity") int quantity,
                     Authentication auth,
                     Model model) {
        long uid;
        try {
            // 解析当前用户主键。
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            model.addAttribute("msg", e.getMessage() != null ? e.getMessage() : "请先登录");
            model.addAttribute("redirectUrl", "/login");
            return "common/alert-redirect";
        }
        try {
            // 调用 service 执行加购逻辑（含库存与状态校验）。
            cartService.addProduct(uid, productId, quantity);
            model.addAttribute("msg", "已加入购物车");
            model.addAttribute("redirectUrl", "/user/market");
            return "common/alert-redirect";
        } catch (IllegalArgumentException e) {
            model.addAttribute("msg", e.getMessage() != null ? e.getMessage() : "加购失败");
            model.addAttribute("redirectUrl", "/user/market/" + productId);
            return "common/alert-redirect";
        }
    }

    /**
     * 更新购物车单项数量。
     */
    @PostMapping("/user/cart/update")
    public String update(@RequestParam("cartItemId") long cartItemId,
                        @RequestParam("quantity") int quantity,
                        Authentication auth,
                        Model model) {
        long uid;
        try {
            // 解析当前用户主键。
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            model.addAttribute("msg", e.getMessage() != null ? e.getMessage() : "请先登录");
            model.addAttribute("redirectUrl", "/login");
            return "common/alert-redirect";
        }
        try {
            // 更新条目数量，内部会处理库存上限。
            cartService.updateQuantity(uid, cartItemId, quantity);
            model.addAttribute("msg", "数量已更新");
        } catch (IllegalArgumentException e) {
            model.addAttribute("err", e.getMessage() != null ? e.getMessage() : "更新失败");
        }
        // 直接返回当前页，避免再次跳转。
        bindCartModel(uid, model);
        return "user/cart";
    }

    /**
     * 从购物车删除指定条目。
     */
    @PostMapping("/user/cart/remove")
    public String remove(@RequestParam("cartItemId") long cartItemId,
                        Authentication auth,
                        Model model) {
        long uid;
        try {
            // 解析当前用户主键。
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            model.addAttribute("msg", e.getMessage() != null ? e.getMessage() : "请先登录");
            model.addAttribute("redirectUrl", "/login");
            return "common/alert-redirect";
        }
        try {
            // 删除指定购物车条目。
            cartService.removeLine(uid, cartItemId);
            model.addAttribute("msg", "已移除该商品");
        } catch (IllegalArgumentException e) {
            model.addAttribute("err", e.getMessage() != null ? e.getMessage() : "操作失败");
        }
        // 直接返回当前页，避免再次跳转。
        bindCartModel(uid, model);
        return "user/cart";
    }

    /**
     * 解析当前登录用户 ID，不存在时抛出业务异常。
     */
    private long requireUserId(Authentication auth) {
        // 统一委托 UserService 做登录态与用户存在性校验。
        return userService.requireUserId(auth);
    }

    /**
     * 绑定购物车列表与汇总金额到页面模型。
     */
    private void bindCartModel(long uid, Model model) {
        model.addAttribute("lines", cartService.listLines(uid));
        model.addAttribute("cartTotal", cartService.sumCartTotal(uid));
    }

}
