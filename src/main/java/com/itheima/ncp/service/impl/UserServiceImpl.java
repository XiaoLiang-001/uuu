package com.itheima.ncp.service.impl;

import com.itheima.ncp.dto.AdminUserRowDto;
import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.entity.user.UserRole;
import com.itheima.ncp.mapper.user.UserMapper;
import com.itheima.ncp.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter CREATED_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String USERNAME_REGEX = "^[\\u4e00-\\u9fa5A-Za-z0-9_]+$";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userMapper.findByUsername(username.trim());
    }

    @Override
    public User getUserById(Long id) {
        return id == null ? null : userMapper.findById(id);
    }

    @Override
    public boolean isUsernameExists(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return userMapper.countByUsernameTrim(username) > 0;
    }

    @Override
    public void registerAsEndUser(String normalizedUsername, String encodedPassword) {
        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(encodedPassword);
        user.setRole(UserRole.USER);
        user.setStatus(User.STATUS_ENABLED);
        user.setCreatedAt(LocalDateTime.now());
        executeWriteWithRetry("注册用户", new Runnable() {
            @Override
            public void run() {
                userMapper.insert(user);
            }
        });
    }

    @Override
    public long requireUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("未登录");
        }
        User u = getByUsername(auth.getName());
        if (u == null || u.getId() == null) {
            throw new IllegalStateException("用户不存在");
        }
        return u.getId();
    }

    @Override
    public List<AdminUserRowDto> listAllForAdmin() {
        return userMapper.findAllOrderByIdDesc().stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminUserRowDto> listForAdmin(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return userMapper.findByKeywordOrderByIdDesc(normalizedKeyword).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatusByAdmin(Long userId, int status, String operatorUsername) {
        if (status != User.STATUS_DISABLED && status != User.STATUS_ENABLED) {
            throw new IllegalArgumentException("状态值无效");
        }
        User target = requireUser(userId);
        String operator = operatorUsername == null ? "" : operatorUsername.trim();
        if (operator.isEmpty() || userMapper.countByUsernameTrim(operator) <= 0) {
            throw new IllegalArgumentException("无法识别当前操作者");
        }
        if (target.getRole() == null) {
            throw new IllegalArgumentException("该用户 role 在库中无效或空，请先在数据库中将其置为 USER 或 ADMIN 后再管理");
        }
        if (target.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("仅可管理普通用户，不能对管理员账号执行此操作");
        }
        if (operatorUsername != null && operatorUsername.equals(target.getUsername())
                && status == User.STATUS_DISABLED) {
            throw new IllegalArgumentException("不能禁用当前登录账号");
        }

        executeWriteWithRetry("更新用户状态", new Runnable() {
            @Override
            public void run() {
                userMapper.updateStatus(userId, status);
            }
        });
    }

    @Override
    public AdminUserRowDto getByIdForAdmin(Long id) {
        User u = requireUser(id);
        return toRow(u);
    }

    @Override
    public void createByAdmin(String username, String rawPassword, UserRole role, int status, String operatorUsername) {
        requireOperator(operatorUsername);
        String normalized = normalizeAndValidateUsername(username);
        validateRawPassword(rawPassword);
        if (role != UserRole.USER) {
            throw new IllegalArgumentException("本页仅允许新增普通用户");
        }
        if (status != User.STATUS_DISABLED && status != User.STATUS_ENABLED) {
            throw new IllegalArgumentException("状态值无效");
        }
        if (userMapper.countByUsernameTrim(normalized) > 0) {
            throw new IllegalArgumentException("登录账号已存在");
        }
        User user = new User();
        user.setUsername(normalized);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        executeWriteWithRetry("创建用户", new Runnable() {
            @Override
            public void run() {
                userMapper.insert(user);
            }
        });
    }

    @Override
    public void updateByAdmin(Long userId, String username, String rawNewPasswordOrNullOrBlank,
                              String oldPasswordWhenChanging, int status, String operatorUsername) {
        requireOperator(operatorUsername);
        User target = requireUser(userId);
        if (target.getRole() == null) {
            throw new IllegalArgumentException("该用户 role 在库中无效或空，请先将 sys_user.role 修正为 USER 或 ADMIN 后再试");
        }
        if (target.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("管理员账号不可在此修改");
        }
        String normalized = normalizeAndValidateUsername(username);
        if (status != User.STATUS_DISABLED && status != User.STATUS_ENABLED) {
            throw new IllegalArgumentException("状态值无效");
        }
        if (userMapper.countByUsernameTrimExceptId(normalized, userId) > 0) {
            throw new IllegalArgumentException("登录账号已被占用");
        }
        if (operatorUsername != null && operatorUsername.equals(target.getUsername())
                && status == User.STATUS_DISABLED) {
            throw new IllegalArgumentException("不能将当前登录账号设为禁用");
        }

        User patch = new User();
        patch.setId(userId);
        patch.setUsername(normalized);
        patch.setStatus(status);
        if (rawNewPasswordOrNullOrBlank != null) {
            String newPw = rawNewPasswordOrNullOrBlank.trim();
            if (!newPw.isEmpty()) {
                validateRawPassword(newPw);
                String oldIn = oldPasswordWhenChanging == null ? "" : oldPasswordWhenChanging.trim();
                if (oldIn.isEmpty()) {
                    throw new IllegalArgumentException("修改密码时请填写该用户的原密码");
                }
                if (!passwordEncoder.matches(oldIn, target.getPassword())) {
                    throw new IllegalArgumentException("原密码不正确");
                }
                patch.setPassword(passwordEncoder.encode(newPw));
            } else {
                patch.setPassword(null);
            }
        } else {
            patch.setPassword(null);
        }
        executeWriteWithRetry("更新用户信息", new Runnable() {
            @Override
            public void run() {
                userMapper.updateByIdSelective(patch);
            }
        });
    }

    @Override
    public void deleteByAdmin(Long userId, String operatorUsername) {
        requireOperator(operatorUsername);
        User target = requireUser(userId);
        if (target.getRole() == null) {
            throw new IllegalArgumentException("该用户 role 在库中无效或空，请先将 sys_user.role 修正为 USER 或 ADMIN 后再试");
        }
        if (target.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        if (operatorUsername != null && operatorUsername.equals(target.getUsername())) {
            throw new IllegalArgumentException("不能删除当前登录账号");
        }
        executeWriteWithRetry("删除用户", new Runnable() {
            @Override
            public void run() {
                userMapper.deleteById(userId);
            }
        });
    }

    @Override
    public void updateSelfProfile(String currentUsername, String newUsername,
                                 String oldPassword, String newPasswordOrBlank) {
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("当前登录状态无效，请重新登录");
        }
        User currentUser = getByUsername(currentUsername.trim());
        if (currentUser == null) {
            throw new IllegalArgumentException("当前用户不存在，请重新登录");
        }

        String oldPwd = oldPassword == null ? "" : oldPassword.trim();
        if (oldPwd.isEmpty()) {
            throw new IllegalArgumentException("请先输入当前密码");
        }
        if (!passwordEncoder.matches(oldPwd, currentUser.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }

        String normalizedUsername = normalizeAndValidateUsername(newUsername);
        if (!Objects.equals(normalizedUsername, currentUser.getUsername())
                && userMapper.countByUsernameTrimExceptId(normalizedUsername, currentUser.getId()) > 0) {
            throw new IllegalArgumentException("登录账号已被占用");
        }

        String newPwd = newPasswordOrBlank == null ? "" : newPasswordOrBlank.trim();
        boolean passwordChanged = !newPwd.isEmpty();
        if (passwordChanged) {
            validateRawPassword(newPwd);
            if (passwordEncoder.matches(newPwd, currentUser.getPassword())) {
                throw new IllegalArgumentException("新密码不能与当前密码相同");
            }
        }

        User patch = new User();
        patch.setId(currentUser.getId());
        patch.setUsername(normalizedUsername);
        patch.setPassword(passwordChanged ? passwordEncoder.encode(newPwd) : null);
        executeWriteWithRetry("更新个人资料", new Runnable() {
            @Override
            public void run() {
                userMapper.updateByIdSelective(patch);
            }
        });
    }

    private void requireOperator(String operatorUsername) {
        String operator = operatorUsername == null ? "" : operatorUsername.trim();
        if (operator.isEmpty() || userMapper.countByUsernameTrim(operator) <= 0) {
            throw new IllegalArgumentException("无法识别当前操作者");
        }
    }

    private static String normalizeAndValidateUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        if (normalized.length() < 2 || normalized.length() > 64) {
            throw new IllegalArgumentException("登录账号长度应为 2～64 个字符");
        }
        if (!normalized.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("登录账号仅支持中文、字母、数字与下划线");
        }
        return normalized;
    }

    private static void validateRawPassword(String raw) {
        if (raw == null || raw.length() < 3 || raw.length() > 128) {
            throw new IllegalArgumentException("密码长度应为 3～128 个字符");
        }
    }

    private void executeWriteWithRetry(String action, Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException first) {
            if (!isCommunicationFailure(first)) {
                throw first;
            }
            try {
                Thread.sleep(120L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            try {
                runnable.run();
            } catch (RuntimeException second) {
                throw second;
            }
        }
    }

    private static boolean isCommunicationFailure(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            String cls = cur.getClass().getName();
            String msg = cur.getMessage();
            if (cls.contains("CommunicationsException") || cls.contains("SQLTransientConnectionException")) {
                return true;
            }
            if (msg != null && msg.toLowerCase().contains("communications link failure")) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private User requireUser(Long id) {
        User u = getUserById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return u;
    }

    private AdminUserRowDto toRow(User u) {
        AdminUserRowDto d = new AdminUserRowDto();
        d.setId(u.getId());
        d.setUsername(u.getUsername());
        d.setRole(u.getRole() != null ? u.getRole().name() : null);
        d.setStatus(u.getStatus());
        if (u.getCreatedAt() != null) {
            d.setCreatedAt(CREATED_FMT.format(u.getCreatedAt()));
        }
        return d;
    }
}
