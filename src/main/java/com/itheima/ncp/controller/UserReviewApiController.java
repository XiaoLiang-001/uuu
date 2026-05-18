package com.itheima.ncp.controller;

import com.itheima.ncp.common.ApiResult;
import com.itheima.ncp.dto.ReviewCreateRequest;
import com.itheima.ncp.dto.ReviewUpdateRequest;
import com.itheima.ncp.entity.shop.ProductReview;
import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.service.shop.ProductReviewService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户评价 JSON API 控制器。
 */
@RestController
public class UserReviewApiController {

    private final UserService userService;
    private final ProductReviewService productReviewService;

    /** 注入用户与评价服务。 */
    public UserReviewApiController(UserService userService, ProductReviewService productReviewService) {
        this.userService = userService;
        this.productReviewService = productReviewService;
    }

    /** 当前登录用户的评价列表。 */
    @GetMapping("/api/user/reviews")
    public ResponseEntity<ApiResult<List<ProductReview>>> listMine(Authentication auth) {
        User u = resolveCurrentUser(auth);
        if (u == null || u.getId() == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResult.ok(productReviewService.listByUserId(u.getId())));
    }

    /** 提交商品评价（星级默认 5）。 */
    @PostMapping(value = "/api/user/reviews", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> addJson(@RequestBody ReviewCreateRequest body, Authentication auth) {
        if (body == null || body.getProductId() == null || body.getProductId() <= 0) {
            return ResponseEntity.badRequest().body(ApiResult.fail(400, "商品参数无效"));
        }
        User u = resolveCurrentUser(auth);
        if (u == null || u.getId() == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "请先登录"));
        }
        int rating = body.getRating() == null ? 5 : body.getRating();
        try {
            productReviewService.addReview(body.getProductId(), u.getId(), u.getUsername(), body.getContent(), rating);
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "评论失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "评论失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /** 修改本人已发布的评价（内容与星级可选）。 */
    @PutMapping(value = "/api/user/reviews/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<Void>> updateJson(@PathVariable("id") Long id,
                                                      @RequestBody ReviewUpdateRequest body,
                                                      Authentication auth) {
        User u = resolveCurrentUser(auth);
        if (u == null || u.getId() == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "用户不存在"));
        }
        try {
            Integer rating = body == null ? null : body.getRating();
            String content = body == null ? null : body.getContent();
            productReviewService.updateReview(id, u.getId(), content, rating);
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "修改失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "修改失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /** 删除本人评价。 */
    @DeleteMapping("/api/user/reviews/{id:\\d+}")
    public ResponseEntity<ApiResult<Void>> deleteJson(@PathVariable("id") Long id, Authentication auth) {
        User u = resolveCurrentUser(auth);
        if (u == null || u.getId() == null) {
            return ResponseEntity.status(401).body(ApiResult.fail(401, "用户不存在"));
        }
        try {
            productReviewService.deleteReview(id, u.getId());
            return ResponseEntity.ok(ApiResult.<Void>ok());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "删除失败";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "删除失败，请稍后重试";
            return ResponseEntity.badRequest().body(ApiResult.fail(400, msg));
        }
    }

    /**
     * 解析当前登录用户，兼容参数注入与上下文两种来源。
     */
    private User resolveCurrentUser(Authentication auth) {
        Authentication actual = auth;
        if (actual == null) {
            actual = SecurityContextHolder.getContext().getAuthentication();
        }
        if (actual == null || !actual.isAuthenticated()) {
            return null;
        }
        String name = actual.getName();
        if (name == null || name.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(name.trim())) {
            return null;
        }
        return userService.getByUsername(name.trim());
    }
}
