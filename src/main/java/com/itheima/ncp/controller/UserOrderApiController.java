package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.OrderCreateRequest;
import com.itheima.ncp.service.shop.OrderService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户订单 JSON API 控制器。
 */
@RestController
public class UserOrderApiController {

    private final UserService userService;
    private final OrderService orderService;

    /** 注入用户与订单服务。 */
    public UserOrderApiController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @PostMapping(value = "/api/user/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Long>> createOrderJson(@RequestBody OrderCreateRequest body, Authentication auth) {
        if (body == null) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "请求体不能为空"));
        }
        long uid;
        try {
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "请先登录";
            return ResponseEntity.status(401).body(ApiResult.fail(401, msg));
        }
        try {
            long orderId = orderService.createOrderFromCart(uid, body.getReceiverName(), body.getReceiverPhone(),
                    body.getReceiverAddress());
            return ResponseEntity.ok(ApiResult.ok(orderId));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "下单失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "下单失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "下单失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /** 从认证信息解析当前用户 ID，未登录则抛出异常由上层转为 401。 */
    private long requireUserId(Authentication auth) {
        return userService.requireUserId(auth);
    }
}
