package com.studio.jwt.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。
 * 带有效 JWT 时可免密（见 Controller 逻辑）。
 */
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 带有效 JWT 免登录时可不传 */
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
