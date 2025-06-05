package com.fivebear.common.filter;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fivebear.common.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 跳过系统监控API的JWT认证
        if (requestURI.startsWith("/api/system/")) {
            logger.info("JWT Filter - Skipping JWT authentication for system API: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        logger.info("JWT Filter - Request URI: {}, Auth Header Present: {}", 
            request.getRequestURI(), authHeader != null);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("JWT Filter - Token extracted, length: {}", token.length());
            
            try {
                boolean isValid = jwtUtil.validateToken(token);
                logger.info("JWT Filter - Token validation result: {}", isValid);
                
                if (isValid) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    logger.info("JWT Filter - Username: {}, UserId: {}", username, userId);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("JWT Filter - Authentication set for user: {}", username);
                        
                        // 在request中设置用户信息，方便Controller使用
                        request.setAttribute("userId", userId);
                        request.setAttribute("username", username);
                    }
                } else {
                    logger.warn("JWT Filter - Token validation failed");
                }
            } catch (Exception e) {
                logger.warn("JWT token validation failed: {}", e.getMessage());
                // 清除可能的错误认证信息
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.info("JWT Filter - No valid Authorization header found");
        }
        
        // 确保过滤器链继续执行
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Error in filter chain: {}", e.getMessage(), e);
            throw e;
        }
    }
} 