package com.fivebear.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivebear.common.util.JwtUtil;

/**
 * 登录通知WebSocket处理器
 */
@Component
public class LoginNotificationHandler extends TextWebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 存储用户名与WebSocket会话的映射
    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    // 存储会话ID与用户名的映射
    private final ConcurrentHashMap<String, String> sessionUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从握手时存储的attributes中获取用户信息
        String username = (String) session.getAttributes().get("username");
        Long userId = (Long) session.getAttributes().get("userId");
        
        if (username != null) {
            // 移除该用户之前的连接
            WebSocketSession oldSession = userSessions.get(username);
            if (oldSession != null && oldSession.isOpen()) {
                try {
                    sendNotificationMessage(oldSession, "force_logout", "您的账户在其他地方登录，当前会话将被强制下线");
                    oldSession.close(CloseStatus.NORMAL);
                } catch (IOException e) {
                    System.err.println("关闭旧WebSocket连接失败: " + e.getMessage());
                }
            }
            
            // 建立新的映射关系
            userSessions.put(username, session);
            sessionUsers.put(session.getId(), username);
            
            // 发送连接成功消息
            sendNotificationMessage(session, "connected", "WebSocket连接已建立");
            
            System.out.println("WebSocket连接建立成功: 用户 " + username + " (ID: " + userId + ")");
        } else {
            System.err.println("WebSocket连接建立失败: 用户信息为空");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            String username = sessionUsers.get(session.getId());
            
            System.out.println("收到用户 " + username + " 的WebSocket消息: " + payload);
            
            // 解析消息
            @SuppressWarnings("unchecked")
            var messageData = objectMapper.readValue(payload, java.util.Map.class);
            
            String type = (String) messageData.get("type");
            
            if ("ping".equals(type)) {
                // 心跳检测
                sendNotificationMessage(session, "pong", "心跳响应");
            } else if ("status".equals(type)) {
                // 获取连接状态
                sendNotificationMessage(session, "status", "连接正常，用户: " + username);
            } else {
                sendNotificationMessage(session, "unknown", "未知消息类型: " + type);
            }
            
        } catch (Exception e) {
            System.err.println("处理WebSocket消息失败: " + e.getMessage());
            sendErrorMessage(session, "消息格式错误");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = sessionUsers.remove(session.getId());
        if (username != null) {
            userSessions.remove(username);
            System.out.println("用户 " + username + " 的WebSocket连接已关闭");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }



    /**
     * 通知用户在其他地方登录
     */
    public void notifyUserLoginElsewhere(String username, String newToken) {
        WebSocketSession session = userSessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                LoginNotificationMessage notification = new LoginNotificationMessage(
                    "force_logout",
                    "您的账户在其他地方登录，当前会话将被强制下线",
                    System.currentTimeMillis(),
                    newToken
                );
                
                String message = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(message));
                
                System.out.println("已通知用户 " + username + " 账户在其他地方登录");
                
                // 延迟关闭连接，给前端处理时间
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 等待2秒
                        if (session.isOpen()) {
                            session.close(CloseStatus.NORMAL);
                        }
                    } catch (Exception e) {
                        System.err.println("延迟关闭WebSocket连接失败: " + e.getMessage());
                    }
                }).start();
                
            } catch (Exception e) {
                System.err.println("发送强制下线通知失败: " + e.getMessage());
            }
        }
    }

    /**
     * 发送通知消息
     */
    private void sendNotificationMessage(WebSocketSession session, String type, String message) {
        try {
            LoginNotificationMessage notification = new LoginNotificationMessage(
                type, message, System.currentTimeMillis(), null
            );
            String jsonMessage = objectMapper.writeValueAsString(notification);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            System.err.println("发送通知消息失败: " + e.getMessage());
        }
    }

    /**
     * 发送成功消息
     */
    private void sendSuccessMessage(WebSocketSession session, String message) {
        sendNotificationMessage(session, "success", message);
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        sendNotificationMessage(session, "error", error);
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String username) {
        WebSocketSession session = userSessions.get(username);
        return session != null && session.isOpen();
    }

    /**
     * 登录通知消息类
     */
    public static class LoginNotificationMessage {
        private String type;
        private String message;
        private long timestamp;
        private String newToken;

        public LoginNotificationMessage() {}

        public LoginNotificationMessage(String type, String message, long timestamp, String newToken) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
            this.newToken = newToken;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public String getNewToken() { return newToken; }
        public void setNewToken(String newToken) { this.newToken = newToken; }
    }
} 