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
        if (stored == null || stored.length() < 7) {
            return false;
        }
        return stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (rawPassword == null) {
            return false;
        }
        if (looksLikeBcrypt(storedPassword)) {
            return bcrypt.matches(rawPassword, storedPassword);
        }
        String raw = rawPassword.toString().trim();
        String stored = storedPassword.trim();
        return raw.equals(stored);
    }
}
