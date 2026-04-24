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

    @GetMapping("/user/checkout")
    public String checkout(Authentication auth, Model model) {
        long uid = requireUserId(auth);
        if (cartService.listLines(uid).isEmpty()) {
            model.addAttribute("err", "购物车为空，无法结算");
        }
        model.addAttribute("lines", cartService.listLines(uid));
        model.addAttribute("cartTotal", cartService.sumCartTotal(uid));
        return "user/checkout";
    }

    @PostMapping("/user/orders")
    public String createOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("receiverAddress") String receiverAddress,
            Authentication auth,
            RedirectAttributes ra) {
        long uid = requireUserId(auth);
        try {
            long orderId = orderService.createOrderFromCart(uid, receiverName, receiverPhone, receiverAddress);
            ra.addFlashAttribute("msg", "订单已提交");
            return "redirect:/user/orders/" + orderId;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "下单失败");
            return "redirect:/user/checkout";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("err", e.getMessage() != null ? e.getMessage() : "下单失败");
            return "redirect:/user/checkout";
        }
    }

    @GetMapping("/user/orders")
    public String orders(Authentication auth, Model model) {
        long uid = requireUserId(auth);
        List<ShopOrder> orders = orderService.listOrders(uid);
        model.addAttribute("orders", orders);
        Map<Long, String> orderCovers = new HashMap<Long, String>();
        Map<Long, Integer> orderItemCounts = new HashMap<Long, Integer>();
        for (ShopOrder o : orders) {
            if (o.getId() == null) {
                continue;
            }
            List<OrderItem> its = orderService.listOrderItems(o.getId());
            orderItemCounts.put(o.getId(), its.size());
            for (OrderItem it : its) {
                if (it.getProductId() == null) {
                    continue;
                }
                Product p = productService.getById(it.getProductId());
                if (p == null) {
                    continue;
                }
                List<String> names = productService.splitStoredImageNames(p);
                if (!names.isEmpty()) {
                    orderCovers.put(o.getId(), names.get(0));
                    break;
                }
            }
        }
        model.addAttribute("orderCovers", orderCovers);
        model.addAttribute("orderItemCounts", orderItemCounts);
        return "user/orders";
    }

    @GetMapping("/user/orders/{id:\\d+}")
    public String orderDetail(@PathVariable long id, Authentication auth, Model model, RedirectAttributes ra) {
        long uid = requireUserId(auth);
        ShopOrder o = orderService.getOrderForUser(id, uid);
        if (o == null) {
            ra.addFlashAttribute("err", "订单不存在");
            return "redirect:/user/orders";
        }
        model.addAttribute("order", o);
        List<OrderItem> items = orderService.listOrderItems(id);
        model.addAttribute("items", items);
        Map<Long, String> itemCovers = new HashMap<Long, String>();
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
                itemCovers.put(it.getProductId(), in.get(0));
            }
        }
        model.addAttribute("itemCovers", itemCovers);
        return "user/order-detail";
    }

    private long requireUserId(Authentication auth) {
        return userService.requireUserId(auth);
    }
}
