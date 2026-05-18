package com.itheima.ncp.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("sys_user")
public class User {

    /** 禁用状态码。 */
    public static final int STATUS_DISABLED = 0;
    /** 启用状态码。 */
    public static final int STATUS_ENABLED = 1;

    /** 用户主键。 */
    private Long id;
    /** 登录账号。 */
    private String username;
    /** 密码密文。 */
    private String password;
    /** 角色。 */
    private UserRole role;
    /** 用户状态：0 禁用，1 启用 */
    private Integer status;
    /** 注册时间。 */
    private LocalDateTime createdAt;

    /** 是否允许登录（status 为 {@link #STATUS_ENABLED}） */
    public boolean isEnabledAccount() {
        // 仅当状态明确为启用时允许登录。
        return status != null && status == STATUS_ENABLED;
    }
}
