package com.studio.jwt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与校验工具。
 *
 * JWT 三段：Header.Payload.Signature（都经 Base64URL 编码）
 *   Header  = {"alg":"HS256","typ":"JWT"}
 *   Payload = 业务声明（uid、username、role、exp 过期时间）
 *   Signature = 用密钥对 前两段 做 HMAC-SHA256 签名，防篡改
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireSeconds;
    private final double refreshThreshold;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expire-seconds}") long expireSeconds,
            @Value("${app.jwt.refresh-threshold}") double refreshThreshold) {
        // HS256 要求密钥足够长（>=256 bit），短密钥会被 jjwt 拒绝
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
        this.refreshThreshold = refreshThreshold;
    }

    /** 签发令牌 */
    public String generate(Long uid, String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(username)               // sub：主体
                .claim("uid", uid)               // 自定义声明
                .claim("role", role)
                .issuedAt(now)                   // iat
                .expiration(exp)                 // exp：过期时间
                .signWith(key)                   // HMAC 签名
                .compact();
    }

    /** 解析并验签；非法/过期抛异常 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)                 // 验签密钥
                .build()
                .parseSignedClaims(token)        // 解析失败（篡改/过期）会抛 JwtException
                .getPayload();
    }

    /** 校验并取出声明（失败返回 null） */
    public Claims parseQuietly(String token) {
        try {
            return parse(token);
        } catch (Exception e) {
            return null;
        }
    }

    /** 是否需要刷新：剩余寿命 < 总寿命 × 阈值 */
    public boolean needsRefresh(Claims claims) {
        Date exp = claims.getExpiration();
        if (exp == null) {
            return false;
        }
        long total = expireSeconds * 1000;
        long remain = exp.getTime() - System.currentTimeMillis();
        return remain < total * refreshThreshold;
    }

    /** 过期时间戳（毫秒） */
    public long expireAtMillis() {
        return System.currentTimeMillis() + expireSeconds * 1000;
    }
}
