package com.fivebear.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.fivebear.websocket.TokenHandshakeInterceptor;
import com.fivebear.websocket.UnifiedWebSocketHandler;

/**
 * 统一的WebSocket配置类
 * 管理所有模块的WebSocket端点
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TokenHandshakeInterceptor tokenHandshakeInterceptor;
    private final UnifiedWebSocketHandler unifiedWebSocketHandler;

    public WebSocketConfig(TokenHandshakeInterceptor tokenHandshakeInterceptor,
            @Lazy UnifiedWebSocketHandler unifiedWebSocketHandler) {
        this.tokenHandshakeInterceptor = tokenHandshakeInterceptor;
        this.unifiedWebSocketHandler = unifiedWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 统一WebSocket端点 - 处理所有WebSocket功能
        registry.addHandler(unifiedWebSocketHandler, "/ws")
                .setAllowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000")
                .addInterceptors(tokenHandshakeInterceptor)
                .withSockJS();

        // 为了向后兼容，也注册登录通知端点（使用相同的处理器）
        registry.addHandler(unifiedWebSocketHandler, "/ws/login-notification")
                .setAllowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000")
                .addInterceptors(tokenHandshakeInterceptor)
                .withSockJS();
    }
}