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
        // 从认证信息解析当前用户主键。
        long uid = requireUserId(auth);
        // 加载购物车行数据用于列表展示。
        model.addAttribute("lines", cartService.listLines(uid));
        // 加载购物车总金额用于页脚汇总。
        model.addAttribute("cartTotal", cartService.sumCartTotal(uid));
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
                     RedirectAttributes ra) {
        // 解析当前用户主键。
        long uid = requireUserId(auth);
        try {
            // 调用 service 执行加购逻辑（含库存与状态校验）。
            cartService.addProduct(uid, productId, quantity);
            // 成功消息通过 flash 传递给跳转后页面。
            ra.addFlashAttribute("msg", "已加入购物车");
        } catch (IllegalArgumentException e) {
            // 业务异常转为可读提示，不向前端暴露堆栈。
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "加购失败");
        }
        // PRG 模式：避免表单重复提交。
        return "redirect:/user/cart";
    }

    /**
     * 更新购物车单项数量。
     */
    @PostMapping("/user/cart/update")
    public String update(@RequestParam("cartItemId") long cartItemId,
                        @RequestParam("quantity") int quantity,
                        Authentication auth,
                        RedirectAttributes ra) {
        // 解析当前用户主键。
        long uid = requireUserId(auth);
        try {
            // 更新条目数量，内部会处理库存上限。
            cartService.updateQuantity(uid, cartItemId, quantity);
            ra.addFlashAttribute("msg", "数量已更新");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "更新失败");
        }
        // 重定向回购物车页展示最新状态。
        return "redirect:/user/cart";
    }

    /**
     * 从购物车删除指定条目。
     */
    @PostMapping("/user/cart/remove")
    public String remove(@RequestParam("cartItemId") long cartItemId,
                        Authentication auth,
                        RedirectAttributes ra) {
        // 解析当前用户主键。
        long uid = requireUserId(auth);
        try {
            // 删除指定购物车条目。
            cartService.removeLine(uid, cartItemId);
            ra.addFlashAttribute("msg", "已移除该商品");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "操作失败");
        }
        // 重定向回购物车页。
        return "redirect:/user/cart";
    }

    /**
     * 解析当前登录用户 ID，不存在时抛出业务异常。
     */
    private long requireUserId(Authentication auth) {
        // 统一委托 UserService 做登录态与用户存在性校验。
        return userService.requireUserId(auth);
    }
}
