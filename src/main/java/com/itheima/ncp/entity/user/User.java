package com.itheima.ncp.entity.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户表（单表存储用户与管理员，通过 role 区分）。
 * <p>对应数据库表：{@code sys_user}。</p>
 * <p>领域实体用 Lombok 生成存取器；不对外 {@code @Data}，避免隐式 {@code equals/hashCode} 与含密码字段的相等语义问题。</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    private Long id;
    private String username;
    private String password;
    private UserRole role;
    /** 用户状态：0 禁用，1 启用 */
    private Integer status;
    private LocalDateTime createdAt;

    /** 是否允许登录（status 为 {@link #STATUS_ENABLED}） */
    public boolean isEnabledAccount() {
        return status != null && status == STATUS_ENABLED;
    }
}
