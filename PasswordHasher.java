package com.studio.jwt.security;

/**
 * 密码散列算法接口（策略模式）。
 * 多算法切换：新增算法只需加一个实现类 + factory 分支。
 */
public interface PasswordHasher {

    /** 算法名，与 user.hash_algo / yml 配置对应 */
    String name();

    /** 生成盐（BCrypt 内置盐，可返回 null） */
    String generateSalt();

    /** 散列密码 */
    String hash(String password, String salt);

    /** 校验密码 */
    boolean verify(String password, String salt, String expectedHash);
}
