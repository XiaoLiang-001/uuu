package com.itheima.ncp.dto;

/**
 * 创建评价 JSON 请求体。
 */
public class ReviewCreateRequest {
    private Long productId;
    private String content;
    private Integer rating;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
