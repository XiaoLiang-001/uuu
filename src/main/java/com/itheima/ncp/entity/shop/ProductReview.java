package com.itheima.ncp.entity.shop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductReview {

    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
}
