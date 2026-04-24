package com.itheima.ncp.service.user;

import com.itheima.ncp.dto.AdminUserRowDto;
import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.entity.user.UserRole;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * 用户业务接口（与瑞吉外卖等课程一致：接口在 service 包，实现类在 service.impl）
 */
public interface UserService {

    User getByUsername(String username);

    User getUserById(Long id);

    boolean isUsernameExists(String username);

    void registerAsEndUser(String normalizedUsername, String encodedPassword);

    long requireUserId(Authentication auth);

    List<AdminUserRowDto> listAllForAdmin();

    List<AdminUserRowDto> listForAdmin(String keyword);

    void updateStatusByAdmin(Long userId, int status, String operatorUsername);

    AdminUserRowDto getByIdForAdmin(Long id);

    void createByAdmin(String username, String rawPassword, UserRole role, int status, String operatorUsername);

    void updateByAdmin(Long userId, String username, String rawNewPasswordOrNullOrBlank,
                      String oldPasswordWhenChanging, int status, String operatorUsername);

    void deleteByAdmin(Long userId, String operatorUsername);

    void updateSelfProfile(String currentUsername, String newUsername,
                          String oldPassword, String newPasswordOrBlank);
}
