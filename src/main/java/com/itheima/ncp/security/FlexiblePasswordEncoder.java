package com.itheima.ncp.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 若数据库中 password 为 BCrypt 密文（以 $2a$/$2b$/$2y$ 开头），则按 BCrypt 校验；
 * 否则视为明文，与登录密码做字符串相等比较。
 */
public class FlexiblePasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    private static boolean looksLikeBcrypt(String stored) {
        // 长度过短直接判定不是 BCrypt 串。
        if (stored == null || stored.length() < 7) {
            return false;
        }
        // 识别常见 BCrypt 前缀。
        return stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
    }

    @Override
    public String encode(CharSequence rawPassword) {
        // 新写入密码统一使用 BCrypt。
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String storedPassword) {
        // 库中密码为空时直接失败。
        if (storedPassword == null) {
            return false;
        }
        // 原始密码为空时直接失败。
        if (rawPassword == null) {
            return false;
        }
        // BCrypt 密文按 BCrypt 规则校验。
        if (looksLikeBcrypt(storedPassword)) {
            return bcrypt.matches(rawPassword, storedPassword);
        }
        // 历史明文兼容：trim 后做字符串比较。
        String raw = rawPassword.toString().trim();
        String stored = storedPassword.trim();
        return raw.equals(stored);
    }
}
