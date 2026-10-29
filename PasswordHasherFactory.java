package com.studio.jwt.security;

import java.util.Map;

/**
 * 算法工厂：按名字取散列器。
 * 当前实现：pbkdf2 / bcrypt / sha256，可扩展。
 */
public final class PasswordHasherFactory {

    private static final Map<String, PasswordHasher> HASHERS = Map.of(
            "pbkdf2", new Pbkdf2Hasher(),
            "bcrypt", new BcryptHasher(),
            "sha256", new Sha256Hasher()
    );

    private PasswordHasherFactory() {
    }

    /** 按配置/用户记录中的算法名获取 */
    public static PasswordHasher of(String algorithm) {
        PasswordHasher hasher = HASHERS.get(algorithm.toLowerCase());
        if (hasher == null) {
            throw new IllegalArgumentException("未知散列算法: " + algorithm);
        }
        return hasher;
    }

    /** 是否支持该算法 */
    public static boolean supports(String algorithm) {
        return HASHERS.containsKey(algorithm.toLowerCase());
    }
}
