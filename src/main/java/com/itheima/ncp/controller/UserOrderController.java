package com.itheima.ncp.controller;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.shop.OrderItem;
import com.itheima.ncp.entity.shop.ShopOrder;
import com.itheima.ncp.service.product.ProductService;
import com.itheima.ncp.service.user.UserService;
import com.itheima.ncp.service.shop.CartService;
import com.itheima.ncp.service.shop.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户订单控制器，覆盖结算、下单、订单列表与订单详情流程。
 */
@Controller
public class UserOrderController {

    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;

    public UserOrderController(UserService userService, CartService cartService,
                              OrderService orderService, ProductService productService) {
        this.userService = userService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.productService = productService;
    }

    /**
     * 结算页：展示购物车明细与应付总额。
     */
    @GetMapping("/user/checkout")
    public String checkout(Authentication auth, Model model) {
        // 当前登录用户 ID。
        long uid = requireUserId(auth);
        // 结算页允许空购物车进入，但显式给出提示。
        if (cartService.listLines(uid).isEmpty()) {
            model.addAttribute("err", "购物车为空，无法结算");
        }
        // 结算页展示购物车明细。
        model.addAttribute("lines", cartService.listLines(uid));
        // 结算页展示总金额。
        model.addAttribute("cartTotal", cartService.sumCartTotal(uid));
        return "user/checkout";
    }

    /**
     * 提交订单，基于当前购物车创建订单并跳转详情页。
     */
    @PostMapping("/user/orders")
    public String createOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("receiverAddress") String receiverAddress,
            Authentication auth,
            RedirectAttributes ra) {
        // 当前登录用户 ID。
        long uid = requireUserId(auth);
        try {
            // 由 service 负责完整下单流程（校验、写单、扣库存、清购物车）。
            long orderId = orderService.createOrderFromCart(uid, receiverName, receiverPhone, receiverAddress);
            ra.addFlashAttribute("msg", "订单已提交");
            // 下单成功直接跳转订单详情页。
            return "redirect:/user/orders/" + orderId;
        } catch (IllegalArgumentException e) {
            // 参数或业务规则不满足（如库存不足、地址为空）。
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "下单失败");
            return "redirect:/user/checkout";
        } catch (IllegalStateException e) {
            // 系统一致性异常（如扣库存失败）也回到结算页提示。
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "下单失败");
            return "redirect:/user/checkout";
        }
    }

    /**
     * 订单列表页，聚合封面图与每单商品数量用于卡片展示。
     */
    @GetMapping("/user/orders")
    public String orders(Authentication auth, Model model) {
        // 当前登录用户 ID。
        long uid = requireUserId(auth);
        // 查询当前用户订单列表。
        List<ShopOrder> orders = orderService.listOrders(uid);
        model.addAttribute("orders", orders);
        // 订单封面图映射：orderId -> cover。
        Map<Long, String> orderCovers = new HashMap<Long, String>();
        // 订单商品数量映射：orderId -> itemCount。
        Map<Long, Integer> orderItemCounts = new HashMap<Long, Integer>();
        // 聚合每笔订单的封面图和商品数，减少模板中复杂循环逻辑。
        for (ShopOrder o : orders) {
            if (o.getId() == null) {
                continue;
            }
            List<OrderItem> its = orderService.listOrderItems(o.getId());
            // 缓存每笔订单的条目数量，模板直接展示。
            orderItemCounts.put(o.getId(), its.size());
            for (OrderItem it : its) {
                if (it.getProductId() == null) {
                    continue;
                }
                // 查询商品用于获取图片信息。
                Product p = productService.getById(it.getProductId());
                if (p == null) {
                    continue;
                }
                List<String> names = productService.splitStoredImageNames(p);
                if (!names.isEmpty()) {
                    // 取第一张图作为订单卡片封面后即可结束内层循环。
                    orderCovers.put(o.getId(), names.get(0));
                    break;
                }
            }
        }
        model.addAttribute("orderCovers", orderCovers);
        model.addAttribute("orderItemCounts", orderItemCounts);
        return "user/orders";
    }

    /**
     * 订单详情页，加载订单基础信息、明细及每项商品封面。
     */
    @GetMapping("/user/orders/{id:\\d+}")
    public String orderDetail(@PathVariable long id, Authentication auth, Model model, RedirectAttributes ra) {
        // 当前登录用户 ID。
        long uid = requireUserId(auth);
        // 按订单 ID + 用户 ID 查询，避免越权查看他人订单。
        ShopOrder o = orderService.getOrderForUser(id, uid);
        if (o == null) {
            ra.addFlashAttribute("err", "订单不存在");
            return "redirect:/user/orders";
        }
        model.addAttribute("order", o);
        // 加载订单明细列表。
        List<OrderItem> items = orderService.listOrderItems(id);
        model.addAttribute("items", items);
        // 明细封面图映射：productId -> cover。
        Map<Long, String> itemCovers = new HashMap<Long, String>();
        // 以 productId 去重，避免同商品多次查封面图。
        for (OrderItem it : items) {
            if (it.getProductId() == null) {
                continue;
            }
            if (itemCovers.containsKey(it.getProductId())) {
                continue;
            }
            Product p2 = productService.getById(it.getProductId());
            if (p2 == null) {
                continue;
            }
            List<String> in = productService.splitStoredImageNames(p2);
            if (!in.isEmpty()) {
                // 商品有图时记录第一张封面。
                itemCovers.put(it.getProductId(), in.get(0));
            }
        }
        model.addAttribute("itemCovers", itemCovers);
        return "user/order-detail";
    }

    /**
     * 解析当前登录用户 ID，不存在时抛出异常。
     */
    private long requireUserId(Authentication auth) {
        // 统一委托 UserService 做登录态与用户校验。
        return userService.requireUserId(auth);
    }
}
