package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.CartAddRequest;
import com.itheima.ncp.dto.CartRemoveRequest;
import com.itheima.ncp.dto.CartUpdateRequest;
import com.itheima.ncp.service.shop.CartService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户购物车 JSON API 控制器。
 */
@RestController
public class UserCartApiController {

    private final CartService cartService;
    private final UserService userService;

    /** 注入购物车与用户服务。 */
    public UserCartApiController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping(value = "/api/user/cart/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> addJson(@RequestBody CartAddRequest body, Authentication auth) {
        if (body == null || body.getProductId() == null || body.getProductId() <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "商品参数无效"));
        }
        if (body.getQuantity() == null || body.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "购买数量无效"));
        }
        long uid;
        try {
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "请先登录";
            return ResponseEntity.status(401).body(ApiResult.fail(401, msg));
        }
        try {
            cartService.addProduct(uid, body.getProductId(), body.getQuantity());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "加购失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "加购失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * JSON 修改购物车行数量。
     */
    @PostMapping(value = "/api/user/cart/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> updateJson(@RequestBody CartUpdateRequest body, Authentication auth) {
        if (body == null || body.getCartItemId() == null || body.getCartItemId() <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "购物车条目参数无效"));
        }
        if (body.getQuantity() == null || body.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "数量无效"));
        }
        long uid;
        try {
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "请先登录";
            return ResponseEntity.status(401).body(ApiResult.fail(401, msg));
        }
        try {
            cartService.updateQuantity(uid, body.getCartItemId(), body.getQuantity());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "更新失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "更新失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    @PostMapping(value = "/api/user/cart/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> removeJson(@RequestBody CartRemoveRequest body, Authentication auth) {
        if (body == null || body.getCartItemId() == null || body.getCartItemId() <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "购物车条目参数无效"));
        }
        long uid;
        try {
            uid = requireUserId(auth);
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "请先登录";
            return ResponseEntity.status(401).body(ApiResult.fail(401, msg));
        }
        try {
            cartService.removeLine(uid, body.getCartItemId());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "操作失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "操作失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /** 从认证信息解析当前用户 ID，未登录则抛出异常由上层转为 401。 */
    private long requireUserId(Authentication auth) {
        return userService.requireUserId(auth);
    }
}
