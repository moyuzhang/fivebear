package com.fivebear.auth.service;

import com.fivebear.auth.entity.LoginRequest;
import com.fivebear.auth.entity.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 用户登出
     */
    void logout(String token);
    
    /**
     * 验证Token
     */
    boolean validateToken(String token);
    
    /**
     * 根据Token获取用户信息
     */
    LoginResponse.UserInfo getUserInfoByToken(String token);
} 