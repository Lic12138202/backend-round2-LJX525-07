package com.studio.jwt.dto;

/** 登录/免登录成功后的令牌信息 */
public class TokenVO {

    private String token;
    private String tokenType = "Bearer";
    private long expireAt;       // 过期时间（毫秒时间戳）
    private String username;
    private String role;

    public TokenVO() {
    }

    public TokenVO(String token, long expireAt, String username, String role) {
        this.token = token;
        this.expireAt = expireAt;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
