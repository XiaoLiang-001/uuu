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

    /** 按用户名查询用户。 */
    User getByUsername(String username);

    /** 按主键查询用户。 */
    User getUserById(Long id);

    /** 判断用户名是否已存在。 */
    boolean isUsernameExists(String username);

    /** 注册普通用户（参数已在上层规范化）。 */
    void registerAsEndUser(String normalizedUsername, String encodedPassword);

    /** 从认证对象解析当前用户ID，不合法时抛异常。 */
    long requireUserId(Authentication auth);

    /** 查询管理端用户全量列表。 */
    List<AdminUserRowDto> listAllForAdmin();

    /** 按关键字查询管理端用户列表。 */
    List<AdminUserRowDto> listForAdmin(String keyword);

    /** 管理员修改普通用户状态。 */
    void updateStatusByAdmin(Long userId, int status, String operatorUsername);

    /** 查询管理端单个用户。 */
    AdminUserRowDto getByIdForAdmin(Long id);

    /** 管理员新增普通用户。 */
    void createByAdmin(String username, String rawPassword, UserRole role, int status, String operatorUsername);

    /** 管理员更新普通用户信息，可选改密。 */
    void updateByAdmin(Long userId, String username, String rawNewPasswordOrNullOrBlank,
                      String oldPasswordWhenChanging, int status, String operatorUsername);

    /** 管理员删除普通用户。 */
    void deleteByAdmin(Long userId, String operatorUsername);

    /** 当前用户修改个人资料。 */
    void updateSelfProfile(String currentUsername, String newUsername,
                          String oldPassword, String newPasswordOrBlank);
}
