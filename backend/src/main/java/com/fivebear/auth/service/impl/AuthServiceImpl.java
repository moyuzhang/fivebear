package com.fivebear.auth.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fivebear.auth.entity.LoginRequest;
import com.fivebear.auth.entity.LoginResponse;
import com.fivebear.auth.entity.User;
import com.fivebear.auth.mapper.UserMapper;
import com.fivebear.auth.service.AuthService;
import com.fivebear.auth.service.LoginSecurityService;
import com.fivebear.common.util.JwtUtil;


/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private LoginSecurityService loginSecurityService;
    

    
    @Autowired(required = false)
    @Lazy
    private com.fivebear.websocket.UnifiedWebSocketHandler unifiedWebSocketHandler;

    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();

        // 检查账户是否被锁定（仅在LoginSecurityService可用时）
        if (loginSecurityService != null && loginSecurityService.isAccountLocked(username)) {
            long remainingTime = loginSecurityService.getLockTimeRemaining(username);
            throw new RuntimeException("账户已被锁定，请" + (remainingTime / 60) + "分钟后再试");
        }

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                .eq("deleted", false);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            // 记录登录失败（仅在LoginSecurityService可用时）
            if (loginSecurityService != null) {
                loginSecurityService.recordLoginFailure(username);
                int remaining = loginSecurityService.getRemainingAttempts(username);
                throw new RuntimeException("用户名或密码错误，还可尝试" + remaining + "次");
            } else {
                throw new RuntimeException("用户名或密码错误");
            }
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // 记录登录失败（仅在LoginSecurityService可用时）
            if (loginSecurityService != null) {
                loginSecurityService.recordLoginFailure(username);
                int remaining = loginSecurityService.getRemainingAttempts(username);
                if (remaining > 0) {
                    throw new RuntimeException("用户名或密码错误，还可尝试" + remaining + "次");
                } else {
                    throw new RuntimeException("密码错误次数过多，账户已被锁定30分钟");
                }
            } else {
                throw new RuntimeException("用户名或密码错误");
            }
        }

        // 清除登录失败记录（仅在LoginSecurityService可用时）
        if (loginSecurityService != null) {
            loginSecurityService.clearLoginFailures(username);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 通过WebSocket通知用户在其他地方登录（在强制下线之前）
        boolean userNotified = false;
        
        // 使用UnifiedWebSocketHandler通知多地登录
        if (unifiedWebSocketHandler != null) {
            unifiedWebSocketHandler.sendForceLogoutNotification(username, "检测到其他设备登录");
            userNotified = true;
            System.out.println("已通过统一WebSocket通知用户 " + username + " 在其他地方登录");
        }
        
        if (!userNotified) {
            System.out.println("用户 " + username + " 当前不在线，无需发送强制下线通知");
        }

        // 强制其他地方的登录下线（限制多地登录）
        if (loginSecurityService != null) {
            loginSecurityService.forceOfflineOtherSessions(username, token);
        }

        // 更新最后登录时间
        updateLastLoginTime(user);

        // 记录登录成功日志
        recordLoginSuccess(user.getId(), username);

        // 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getStatus(),
                user.getRoleId(),
                getRoleName(user.getRoleId()));

        return new LoginResponse(token, userInfo);
    }

    @Override
    public void logout(String token) {
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            // 清理用户session（仅在LoginSecurityService可用时）
            if (loginSecurityService != null) {
                loginSecurityService.cleanUserSession(username, token);
            }
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        // 检查token是否被强制下线（仅在LoginSecurityService可用时）
        String username = jwtUtil.getUsernameFromToken(token);
        return loginSecurityService == null || !loginSecurityService.isTokenForcedOffline(username, token);
    }

    @Override
    public LoginResponse.UserInfo getUserInfoByToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);

        if (user == null || user.getDeleted() || user.getStatus() == 0) {
            return null;
        }

        return new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getStatus(),
                user.getRoleId(),
                getRoleName(user.getRoleId()));
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginFailureCount(0); // 重置失败次数
        userMapper.updateById(user);
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(Long userId, String username) {
        // TODO: 记录到登录日志表
        System.out.println("登录失败: " + username);
    }

    /**
     * 记录登录成功
     */
    private void recordLoginSuccess(Long userId, String username) {
        // TODO: 记录到登录日志表
        System.out.println("登录成功: " + username);
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(Long roleId) {
        if (roleId == null) {
            return "未分配角色";
        }

        // TODO: 从角色表查询角色名称
        switch (roleId.intValue()) {
            case 1:
                return "管理员";
            case 2:
                return "总监";
            case 3:
                return "大股东";
            default:
                return "未知角色";
        }
    }
}