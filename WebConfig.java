package com.studio.jwt.config;

import com.studio.jwt.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 注册拦截器：哪些路径要登录、哪些放行 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")          // 拦截所有 /api
                .excludePathPatterns(                 // 放行：注册/登录/健康检查
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/health"
                );
    }
}
