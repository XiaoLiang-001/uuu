package com.itheima.ncp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.itheima.ncp.config.cache.CacheNames;
import com.itheima.ncp.dto.AdminUserRowDto;
import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.entity.user.UserRole;
import com.itheima.ncp.mapper.user.UserMapper;
import com.itheima.ncp.service.user.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现，承载注册、登录用户查询与管理端用户维护能力。
 */
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

    /**
     * 按用户名查询用户，自动处理空白输入。
     */
    @Override
    @Cacheable(cacheNames = CacheNames.USER_BY_NAME, key = "#username.trim()",
            condition = "#username != null && !#username.trim().isEmpty()",
            unless = "#result == null")
    public User getByUsername(String username) {
        // 空用户名直接返回空，调用方据此判断“未命中”。
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        // 使用 MP 条件构造器按 trim 口径查询。
        QueryWrapper<User> qw = new QueryWrapper<User>()
                .apply("TRIM(username) = TRIM({0})", username.trim())
                .last("LIMIT 1");
        return userMapper.selectOne(qw);
    }

    /**
     * 按主键查询用户。
     */
    @Override
    public User getUserById(Long id) {
        // 主键为空时不查库，直接返回空。
        return id == null ? null : userMapper.selectById(id);
    }

    /**
     * 判断用户名是否已存在。
     */
    @Override
    public boolean isUsernameExists(String username) {
        // 空值视为不存在，避免无意义 SQL。
        if (username == null || username.isEmpty()) {
            return false;
        }
        // 使用 trim 口径统计，和注册/修改口径保持一致。
        return countByUsernameTrim(username) > 0;
    }

    /**
     * 注册普通用户账号。
     */
    @Override
    public void registerAsEndUser(String normalizedUsername, String encodedPassword) {
        // 构建新用户实体。
        User user = new User();
        // normalizedUsername 由上层校验并规范化后传入。
        user.setUsername(normalizedUsername);
        // 存储的是已加密密码。
        user.setPassword(encodedPassword);
        // 自注册默认普通用户角色。
        user.setRole(UserRole.USER);
        // 新用户默认启用。
        user.setStatus(User.STATUS_ENABLED);
        // 记录创建时间。
        user.setCreatedAt(LocalDateTime.now());
        // 写入数据库时统一走重试包装，降低瞬时连接波动影响。
        executeWriteWithRetry("注册用户", new Runnable() {
            @Override
            public void run() {
                userMapper.insert(user);
            }
        });
    }

    /**
     * 从认证对象解析当前用户 ID。
     */
    @Override
    public long requireUserId(Authentication auth) {
        // 认证对象为空或未认证，直接视为未登录。
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("未登录");
        }
        // 通过认证名反查业务用户。
        User u = getByUsername(auth.getName());
        // 若账号已被删除等场景，给出明确错误。
        if (u == null || u.getId() == null) {
            throw new IllegalStateException("用户不存在");
        }
        // 返回用户主键供后续业务使用。
        return u.getId();
    }

    /**
     * 查询管理端用户全量列表。
     */
    @Override
    public List<AdminUserRowDto> listAllForAdmin() {
        // 全量查询并映射为管理端展示 DTO。
        return userMapper.selectList(new QueryWrapper<User>().orderByDesc("id")).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    /**
     * 按关键字查询管理端用户列表。
     */
    @Override
    public List<AdminUserRowDto> listForAdmin(String keyword) {
        // 关键字统一 trim，空串表示不过滤。
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        // 使用 MP 条件构造器动态拼接关键字条件。
        QueryWrapper<User> qw = new QueryWrapper<User>();
        if (!normalizedKeyword.isEmpty()) {
            qw.like("username", normalizedKeyword);
        }
        qw.orderByDesc("id");
        return userMapper.selectList(qw).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    /**
     * 管理员修改用户启用状态。
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.USER_BY_NAME, allEntries = true)
    public void updateStatusByAdmin(Long userId, int status, String operatorUsername) {
        // 仅允许启用/禁用两个状态值。
        if (status != User.STATUS_DISABLED && status != User.STATUS_ENABLED) {
            throw new IllegalArgumentException("状态值无效");
        }
        // 校验目标用户存在。
        User target = requireUser(userId);
        // 规范化操作者用户名。
        String operator = operatorUsername == null ? "" : operatorUsername.trim();
        // 必须能在库中定位到当前操作者。
        if (operator.isEmpty() || countByUsernameTrim(operator) <= 0) {
            throw new IllegalArgumentException("无法识别当前操作者");
        }
        // 旧数据脏值保护：role 为空时拒绝管理操作。
        if (target.getRole() == null) {
            throw new IllegalArgumentException("该用户 role 在库中无效或空，请先在数据库中将其置为 USER 或 ADMIN 后再管理");
        }
        // 本页限定只管理普通用户。
        if (target.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("仅可管理普通用户，不能对管理员账号执行此操作");
        }
        // 防止自禁用导致当前会话不可用。
        if (operatorUsername != null && operatorUsername.equals(target.getUsername())
                && status == User.STATUS_DISABLED) {
            throw new IllegalArgumentException("不能禁用当前登录账号");
        }

        executeWriteWithRetry("更新用户状态", new Runnable() {
            @Override
            public void run() {
                // 仅更新状态字段。
                userMapper.update(null,
                        new UpdateWrapper<User>()
                                .set("status", status)
                                .eq("id", userId));
            }
        });
    }

    /**
     * 查询管理端单个用户详情。
     */
    @Override
    public AdminUserRowDto getByIdForAdmin(Long id) {
        // 复用 requireUser 统一不存在判定。
        User u = requireUser(id);
        return toRow(u);
    }

    /**
     * 管理员新增普通用户。
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.USER_BY_NAME, allEntries = true)
    public void createByAdmin(String username, String rawPassword, UserRole role, int status, String operatorUsername) {
        // 先确认操作者有效。
        requireOperator(operatorUsername);
        // 用户名统一做 trim + 格式校验。
        String normalized = normalizeAndValidateUsername(username);
        // 密码长度校验。
        validateRawPassword(rawPassword);
        // 用户管理页仅允许新增普通用户。
        if (role != UserRole.USER) {
            throw new IllegalArgumentException("本页仅允许新增普通用户");
        }
        // 状态值合法性校验。
        if (status != User.STATUS_DISABLED && status != User.STATUS_ENABLED) {
            throw new IllegalArgumentException("状态值无效");
        }
        // 账号唯一性校验。
        if (countByUsernameTrim(normalized) > 0) {
            throw new IllegalArgumentException("登录账号已存在");
        }
        // 组装待插入用户。
        User user = new User();
        user.setUsername(normalized);
        // 入库前进行密码加密。
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        executeWriteWithRetry("创建用户", new Runnable() {
            @Override
            public void run() {
                // 插入用户记录。
                userMapper.insert(user);
            }
        });
    }

    /**
     * 管理员更新普通用户信息，可选修改密码。
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.USER_BY_NAME, allEntries = true)
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
        if (countByUsernameTrimExceptId(normalized, userId) > 0) {
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

    /**
     * 管理员删除普通用户。
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.USER_BY_NAME, allEntries = true)
    public void deleteByAdmin(Long userId, String operatorUsername) {
        // 先确认操作者有效。
        requireOperator(operatorUsername);
        // 校验目标用户存在。
        User target = requireUser(userId);
        if (target.getRole() == null) {
            throw new IllegalArgumentException("该用户 role 在库中无效或空，请先将 sys_user.role 修正为 USER 或 ADMIN 后再试");
        }
        // 管理员账号保护。
        if (target.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        // 防止删除当前登录账号导致会话异常。
        if (operatorUsername != null && operatorUsername.equals(target.getUsername())) {
            throw new IllegalArgumentException("不能删除当前登录账号");
        }
        executeWriteWithRetry("删除用户", new Runnable() {
            @Override
            public void run() {
                // 按主键删除。
                userMapper.deleteById(userId);
            }
        });
    }

    /**
     * 用户修改个人资料（用户名/密码）。
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.USER_BY_NAME, allEntries = true)
    public void updateSelfProfile(String currentUsername, String newUsername,
                                 String oldPassword, String newPasswordOrBlank) {
        // 当前登录名必须可用。
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("当前登录状态无效，请重新登录");
        }
        // 加载当前用户实体。
        User currentUser = getByUsername(currentUsername.trim());
        if (currentUser == null) {
            throw new IllegalArgumentException("当前用户不存在，请重新登录");
        }

        // 个人信息修改前必须先验证旧密码。
        String oldPwd = oldPassword == null ? "" : oldPassword.trim();
        if (oldPwd.isEmpty()) {
            throw new IllegalArgumentException("请先输入当前密码");
        }
        if (!passwordEncoder.matches(oldPwd, currentUser.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }

        // 校验新用户名合法性。
        String normalizedUsername = normalizeAndValidateUsername(newUsername);
        // 若改了用户名，则检查唯一性。
        if (!Objects.equals(normalizedUsername, currentUser.getUsername())
                && countByUsernameTrimExceptId(normalizedUsername, currentUser.getId()) > 0) {
            throw new IllegalArgumentException("登录账号已被占用");
        }

        // 空字符串表示不改密码。
        String newPwd = newPasswordOrBlank == null ? "" : newPasswordOrBlank.trim();
        boolean passwordChanged = !newPwd.isEmpty();
        if (passwordChanged) {
            validateRawPassword(newPwd);
            // 禁止将新密码改成与当前密码相同，避免无效修改。
            if (passwordEncoder.matches(newPwd, currentUser.getPassword())) {
                throw new IllegalArgumentException("新密码不能与当前密码相同");
            }
        }

        // 构造选择性更新对象。
        User patch = new User();
        patch.setId(currentUser.getId());
        patch.setUsername(normalizedUsername);
        patch.setPassword(passwordChanged ? passwordEncoder.encode(newPwd) : null);
        executeWriteWithRetry("更新个人资料", new Runnable() {
            @Override
            public void run() {
                // 执行资料更新。
                userMapper.updateByIdSelective(patch);
            }
        });
    }

    /**
     * 校验操作者是否合法且存在。
     */
    private void requireOperator(String operatorUsername) {
        // 统一处理操作者空值与空白字符串。
        String operator = operatorUsername == null ? "" : operatorUsername.trim();
        // 操作者必须真实存在。
        if (operator.isEmpty() || countByUsernameTrim(operator) <= 0) {
            throw new IllegalArgumentException("无法识别当前操作者");
        }
    }

    /**
     * 统计用户名（按 trim 口径）是否存在。
     */
    private int countByUsernameTrim(String username) {
        return Math.toIntExact(userMapper.selectCount(
                new QueryWrapper<User>().apply("TRIM(username) = TRIM({0})", username)));
    }

    /**
     * 统计除指定 id 外用户名（按 trim 口径）是否存在。
     */
    private int countByUsernameTrimExceptId(String username, Long excludedId) {
        QueryWrapper<User> qw = new QueryWrapper<User>()
                .apply("TRIM(username) = TRIM({0})", username);
        if (excludedId != null) {
            qw.ne("id", excludedId);
        }
        return Math.toIntExact(userMapper.selectCount(qw));
    }

    /**
     * 规范化并校验用户名格式。
     */
    private static String normalizeAndValidateUsername(String username) {
        // 统一去除首尾空白。
        String normalized = username == null ? "" : username.trim();
        // 长度限制。
        if (normalized.length() < 2 || normalized.length() > 64) {
            throw new IllegalArgumentException("登录账号长度应为 2～64 个字符");
        }
        // 字符集限制：中文/字母/数字/下划线。
        if (!normalized.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("登录账号仅支持中文、字母、数字与下划线");
        }
        return normalized;
    }

    /**
     * 校验原始密码长度。
     */
    private static void validateRawPassword(String raw) {
        // 密码仅做长度边界校验，复杂度规则可后续扩展。
        if (raw == null || raw.length() < 3 || raw.length() > 128) {
            throw new IllegalArgumentException("密码长度应为 3～128 个字符");
        }
    }

    /**
     * 执行写操作并在通信异常时自动重试一次。
     */
    private void executeWriteWithRetry(String action, Runnable runnable) {
        // 首次执行写操作。
        try {
            runnable.run();
        } catch (RuntimeException first) {
            // 非通信类异常直接抛出，不做重试。
            if (!isCommunicationFailure(first)) {
                throw first;
            }
            // 通信瞬断场景重试一次，提升管理操作成功率。
            try {
                Thread.sleep(120L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            try {
                // 二次执行。
                runnable.run();
            } catch (RuntimeException second) {
                // 第二次仍失败则直接抛出。
                throw second;
            }
        }
    }

    /**
     * 判断异常链中是否包含数据库通信失败特征。
     */
    private static boolean isCommunicationFailure(Throwable t) {
        Throwable cur = t;
        // 顺着 cause 链判断，兼容不同 JDBC/连接池包装异常。
        while (cur != null) {
            String cls = cur.getClass().getName();
            String msg = cur.getMessage();
            // 基于异常类型关键字判断。
            if (cls.contains("CommunicationsException") || cls.contains("SQLTransientConnectionException")) {
                return true;
            }
            // 基于异常信息关键字判断（兼容包装异常）。
            if (msg != null && msg.toLowerCase().contains("communications link failure")) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * 查询并确保用户存在。
     */
    private User requireUser(Long id) {
        // 统一空值/不存在判定。
        User u = getUserById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return u;
    }

    /**
     * 用户实体转管理端列表 DTO。
     */
    private AdminUserRowDto toRow(User u) {
        // 组装管理端列表 DTO。
        AdminUserRowDto d = new AdminUserRowDto();
        d.setId(u.getId());
        d.setUsername(u.getUsername());
        // role 允许为空，前端可据此显示“异常数据”。
        d.setRole(u.getRole() != null ? u.getRole().name() : null);
        d.setStatus(u.getStatus());
        // createdAt 为空时不填格式化时间。
        if (u.getCreatedAt() != null) {
            d.setCreatedAt(CREATED_FMT.format(u.getCreatedAt()));
        }
        return d;
    }
}
