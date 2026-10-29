package com.studio.jwt.controller;

import com.studio.jwt.dto.ApiResponse;
import com.studio.jwt.dto.TokenVO;
import com.studio.jwt.dto.UserVO;
import com.studio.jwt.model.LoginRequest;
import com.studio.jwt.model.RegisterRequest;
import com.studio.jwt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 认证 + 用户信息接口。
 *
 * POST /api/auth/register   注册（无 JWT）
 * POST /api/auth/login      登录 / 带 JWT 免登录刷新
 * GET  /api/users/{id}      查看用户信息（权限控制）
 * GET  /api/users           管理员查看全部（RBAC 加分）
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("注册成功", authService.register(request));
    }

    /**
     * 登录 / 免登录刷新。
     * Authorization: Bearer xxx 存在且有效 → 刷新令牌，无需密码。
     */
    @PostMapping("/auth/login")
    public ApiResponse<TokenVO> login(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Success", authService.login(authorization, request));
    }

    /** 根据用户 id 获取信息；普通用户只能取自己 */
    @GetMapping("/users/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Long id) {
        return ApiResponse.ok("查询成功", authService.getUserInfo(id));
    }

    /** 管理员：列出所有用户（RBAC） */
    @GetMapping("/users")
    public ApiResponse<List<UserVO>> listUsers() {
        return ApiResponse.ok("查询成功", authService.listAll());
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok("UP", Map.of("app", "jwt-workshop"));
    }
}
