package com.fivebear.auth.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fivebear.auth.service.LoginSecurityService;

/**
 * 登录安全服务实现类
 */
@Service
public class LoginSecurityServiceImpl implements LoginSecurityService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis key 前缀
    private static final String LOGIN_FAILURE_PREFIX = "login:failure:";
    private static final String ACCOUNT_LOCK_PREFIX = "login:lock:";
    private static final String USER_SESSION_PREFIX = "login:session:";
    
    // 配置参数
    private static final int MAX_LOGIN_ATTEMPTS = 5; // 最大登录尝试次数
    private static final long LOCK_TIME_MINUTES = 30; // 锁定时间（分钟）
    private static final long FAILURE_WINDOW_MINUTES = 15; // 失败记录窗口时间（分钟）
    
    public LoginSecurityServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean isAccountLocked(String username) {
        String lockKey = ACCOUNT_LOCK_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }
    
    @Override
    public void recordLoginFailure(String username) {
        String failureKey = LOGIN_FAILURE_PREFIX + username;
        String lockKey = ACCOUNT_LOCK_PREFIX + username;
        
        // 增加失败次数
        Integer attempts = (Integer) redisTemplate.opsForValue().get(failureKey);
        attempts = (attempts == null) ? 1 : attempts + 1;
        
        // 设置失败记录，在窗口时间内有效
        redisTemplate.opsForValue().set(failureKey, attempts, FAILURE_WINDOW_MINUTES, TimeUnit.MINUTES);
        
        // 如果达到最大尝试次数，锁定账户
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockKey, true, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
            // 清除失败次数记录
            redisTemplate.delete(failureKey);
        }
    }
    
    @Override
    public void clearLoginFailures(String username) {
        String failureKey = LOGIN_FAILURE_PREFIX + username;
        String lockKey = ACCOUNT_LOCK_PREFIX + username;
        
        // 清除失败记录和锁定状态
        redisTemplate.delete(failureKey);
        redisTemplate.delete(lockKey);
    }
    
    @Override
    public int getRemainingAttempts(String username) {
        String failureKey = LOGIN_FAILURE_PREFIX + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(failureKey);
        attempts = (attempts == null) ? 0 : attempts;
        
        return Math.max(0, MAX_LOGIN_ATTEMPTS - attempts);
    }
    
    @Override
    public long getLockTimeRemaining(String username) {
        String lockKey = ACCOUNT_LOCK_PREFIX + username;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return (ttl == null || ttl < 0) ? 0 : ttl;
    }
    
    @Override
    public void forceOfflineOtherSessions(String username, String currentToken) {
        String sessionKey = USER_SESSION_PREFIX + username;
        
        // 设置当前用户的活跃token，覆盖之前的session
        redisTemplate.opsForValue().set(sessionKey, currentToken, 24, TimeUnit.HOURS);
    }
    
    @Override
    public boolean isTokenForcedOffline(String username, String token) {
        String sessionKey = USER_SESSION_PREFIX + username;
        String activeToken = (String) redisTemplate.opsForValue().get(sessionKey);
        
        // 如果没有活跃token记录，或者token不匹配，则认为被强制下线
        return activeToken == null || !activeToken.equals(token);
    }
    
    @Override
    public void cleanUserSession(String username, String token) {
        String sessionKey = USER_SESSION_PREFIX + username;
        String activeToken = (String) redisTemplate.opsForValue().get(sessionKey);
        
        // 只有当前token是活跃token时才清理
        if (token.equals(activeToken)) {
            redisTemplate.delete(sessionKey);
        }
    }
} 