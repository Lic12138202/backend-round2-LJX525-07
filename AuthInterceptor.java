package com.studio.jwt.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：从 Authorization 头取 JWT，验签后把用户信息放入 UserContext。
 *
 * Header 格式：Authorization: Bearer &lt;token&gt;
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 预检请求直接放行（跨域 OPTIONS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录：缺少 Authorization 头\",\"data\":null}");
            return false;
        }

        String token = auth.substring(7);   // 去掉 "Bearer "
        Claims claims = jwtUtil.parseQuietly(token);
        if (claims == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录凭证无效或已过期\",\"data\":null}");
            return false;
        }

        // 验签通过 → 记录当前用户，供后续业务使用
        Long uid = claims.get("uid", Long.class);
        UserContext.set(new UserContext.CurrentUser(uid, claims.getSubject(), claims.get("role", String.class)));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();   // 请求结束清理，防内存泄漏/线程串扰
    }
}
