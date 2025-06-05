package com.fivebear.auth.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fivebear.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    private final AuthService authService;
    
    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 忽略登录、注册等不需要认证的接口
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/auth/login") || 
            requestPath.startsWith("/auth/register") ||
            requestPath.startsWith("/auth/security") ||
            requestPath.startsWith("/swagger") ||
            requestPath.startsWith("/v3/api-docs") ||
            requestPath.startsWith("/druid")) {
            return true;
        }
        
        // 获取token
        String token = getTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供认证令牌\"}");
            return false;
        }
        
        // 验证token（包括是否被强制下线）
        if (!authService.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"认证令牌无效或已在其他地方登录\"}");
            return false;
        }
        
        return true;
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 