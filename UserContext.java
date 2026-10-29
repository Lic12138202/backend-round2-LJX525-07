package com.studio.jwt.security;

/**
 * 当前请求的登录用户（ThreadLocal）。
 * 每个请求线程独立存放，互不干扰。
 */
public final class UserContext {

    public static class CurrentUser {
        private final Long id;
        private final String username;
        private final String role;

        public CurrentUser(Long id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }
    }

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();   // 必须清，防止线程复用导致串数据
    }
}
