package com.studio.jwt.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt：现代密码专用散列，盐内置于散列结果中，自带工作因子（cost）。
 * 这里用 spring-security-crypto 提供的实现。
 */
public class BcryptHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String name() {
        return "bcrypt";
    }

    @Override
    public String generateSalt() {
        // BCrypt 把盐编码进结果串，无需单独存
        return null;
    }

    @Override
    public String hash(String password, String saltIgnored) {
        return encoder.encode(password);
    }

    @Override
    public boolean verify(String password, String saltIgnored, String expectedHash) {
        return encoder.matches(password, expectedHash);
    }
}
