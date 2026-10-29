package com.studio.jwt.service;

import com.studio.jwt.config.AppProperties;
import com.studio.jwt.dto.TokenVO;
import com.studio.jwt.dto.UserVO;
import com.studio.jwt.exception.BizException;
import com.studio.jwt.mapper.UserMapper;
import com.studio.jwt.model.LoginRequest;
import com.studio.jwt.model.RegisterRequest;
import com.studio.jwt.model.User;
import com.studio.jwt.security.JwtUtil;
import com.studio.jwt.security.PasswordHasher;
import com.studio.jwt.security.PasswordHasherFactory;
import com.studio.jwt.security.UserContext;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证业务：注册 / 登录 / 免登录刷新。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;
    private final AppProperties props;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, AppProperties props, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.props = props;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册：查重 → 用配置的算法散列 → 存库。
     * hash_algo 一并写入，切换算法后老用户仍能按原算法校验。
     */
    public UserVO register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword();

        if (username.isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw BizException.badRequest("用户名长度必须在 3-50 之间");
        }
        if (password == null || password.length() < 6) {
            throw BizException.badRequest("密码长度不能少于 6 位");
        }
        if (userMapper.selectByUsername(username) != null) {
            throw BizException.conflict("用户名已存在，请换一个");
        }

        String role = request.getRole() == null ? "USER" : request.getRole().trim().toUpperCase();
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw BizException.badRequest("role 只能是 USER 或 ADMIN");
        }

        // 取「当前配置指定」的散列算法
        String algoName = props.getHash().getAlgorithm();
        PasswordHasher hasher = PasswordHasherFactory.of(algoName);
        String salt = hasher.generateSalt();
        String hash = hasher.hash(password, salt);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hash);
        user.setSalt(salt);
        user.setHashAlgo(algoName);     // 记住算法，登录时按它校验
        user.setRole(role);
        userMapper.insert(user);

        log.info("注册成功: username={}, algo={}, role={}", username, algoName, role);
        return toVO(user);
    }

    /**
     * 登录主入口。
     * 1) 携带有效 JWT → 免密码，直接换新令牌（刷新）
     * 2) 否则用用户名密码登录
     */
    public TokenVO login(String authorizationHeader, LoginRequest request) {
        // ---------- 免登录 / 刷新 ----------
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            Claims claims = jwtUtil.parseQuietly(authorizationHeader.substring(7));
            if (claims != null) {
                Long uid = claims.get("uid", Long.class);
                User user = userMapper.selectById(uid);
                if (user == null) {
                    throw BizException.unauthorized("登录凭证无效或已过期");
                }
                String newToken = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
                log.info("免登录刷新令牌: username={}", user.getUsername());
                return new TokenVO(newToken, jwtUtil.expireAtMillis(),
                        user.getUsername(), user.getRole());
            }
        }

        // ---------- 账号密码登录 ----------
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword();
        if (username.isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw BizException.badRequest("密码不能为空（未携带有效 JWT 时）");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw BizException.unauthorized("用户名或密码错误");
        }

        // 关键：用该用户记录的算法校验（支持历史用户在切换算法后仍能登录）
        PasswordHasher hasher = PasswordHasherFactory.of(user.getHashAlgo());
        if (!hasher.verify(password, user.getSalt(), user.getPasswordHash())) {
            throw BizException.unauthorized("用户名或密码错误");
        }

        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        log.info("登录成功: username={}", username);
        return new TokenVO(token, jwtUtil.expireAtMillis(), user.getUsername(), user.getRole());
    }

    /** 权限控制：普通用户只能查自己；管理员可查任何人 */
    public UserVO getUserInfo(Long id) {
        UserContext.CurrentUser cur = UserContext.get();
        if (cur == null) {
            throw BizException.unauthorized("未登录");
        }
        if (!cur.isAdmin() && !cur.getId().equals(id)) {
            throw BizException.forbidden("普通用户只能查看自己的用户信息");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        return toVO(user);
    }

    /** 管理员：查看全部用户 */
    public List<UserVO> listAll() {
        UserContext.CurrentUser cur = UserContext.get();
        if (cur == null) {
            throw BizException.unauthorized("未登录");
        }
        if (!cur.isAdmin()) {
            throw BizException.forbidden("仅管理员可查看所有用户");
        }
        return userMapper.selectAll().stream().map(this::toVO).toList();
    }

    private UserVO toVO(User u) {
        return new UserVO(u.getId(), u.getUsername(), u.getRole(), u.getCreatedAt());
    }
}
