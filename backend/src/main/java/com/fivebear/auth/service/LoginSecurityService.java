package com.fivebear.auth.service;

/**
 * 登录安全服务接口
 */
public interface LoginSecurityService {
    
    /**
     * 检查账户是否被锁定（密码错误次数过多）
     * @param username 用户名
     * @return true:被锁定, false:未锁定
     */
    boolean isAccountLocked(String username);
    
    /**
     * 记录登录失败
     * @param username 用户名
     */
    void recordLoginFailure(String username);
    
    /**
     * 清除登录失败记录
     * @param username 用户名
     */
    void clearLoginFailures(String username);
    
    /**
     * 获取剩余错误尝试次数
     * @param username 用户名
     * @return 剩余次数
     */
    int getRemainingAttempts(String username);
    
    /**
     * 获取账户锁定剩余时间（秒）
     * @param username 用户名
     * @return 剩余时间（秒），0表示未锁定
     */
    long getLockTimeRemaining(String username);
    
    /**
     * 强制用户下线（限制多地登录）
     * @param username 用户名
     * @param currentToken 当前登录的token
     */
    void forceOfflineOtherSessions(String username, String currentToken);
    
    /**
     * 检查token是否已被强制下线
     * @param username 用户名
     * @param token 要检查的token
     * @return true:已下线, false:正常
     */
    boolean isTokenForcedOffline(String username, String token);
    
    /**
     * 用户登出时清理session
     * @param username 用户名
     * @param token 用户token
     */
    void cleanUserSession(String username, String token);
} 