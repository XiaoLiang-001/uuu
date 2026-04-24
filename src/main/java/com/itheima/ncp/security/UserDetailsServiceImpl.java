package com.itheima.ncp.security;

import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户加载实现，从业务用户表构建认证用户信息。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从业务层按用户名查询用户。
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        // role 为空时拒绝登录，避免生成非法权限。
        if (user.getRole() == null) {
            throw new UsernameNotFoundException("用户未配置角色: " + username);
        }
        // 将业务启用状态映射为 Security disabled 标志。
        boolean accountEnabled = user.isEnabledAccount();
        // 构建 Spring Security 标准 UserDetails。
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!accountEnabled)
                .build();
    }
}
