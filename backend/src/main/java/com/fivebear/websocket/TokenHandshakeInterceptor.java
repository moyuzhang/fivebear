package com.fivebear.websocket;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.fivebear.common.util.JwtUtil;

/**
 * WebSocket握手拦截器，用于验证JWT token
 */
@Component
public class TokenHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 从查询参数或请求头中获取token
        String token = getTokenFromRequest(request);

        if (token == null) {
            System.err.println("WebSocket握手失败: 缺少token");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            System.err.println("WebSocket握手失败: token无效");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 将用户信息存储在attributes中，供后续使用
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            attributes.put("username", username);
            attributes.put("userId", userId);
            attributes.put("token", token);

            System.out.println("WebSocket握手成功: 用户 " + username + " (ID: " + userId + ")");
            return true;

        } catch (Exception e) {
            System.err.println("WebSocket握手失败: 解析token出错 - " + e.getMessage());
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理（可以为空）
        if (exception != null) {
            System.err.println("WebSocket握手后出现异常: " + exception.getMessage());
        }
    }

    /**
     * 从请求中获取token
     * 支持从查询参数 "token" 或请求头 "Authorization" 中获取
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        // 1. 首先尝试从查询参数获取
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2); // 限制分割次数，避免token中的=被分割
                if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                    try {
                        // URL解码token
                        return java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    } catch (Exception e) {
                        System.err.println("URL解码token失败: " + e.getMessage());
                        return keyValue[1]; // 如果解码失败，返回原值
                    }
                }
            }
        }

        // 2. 尝试从请求头获取
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}