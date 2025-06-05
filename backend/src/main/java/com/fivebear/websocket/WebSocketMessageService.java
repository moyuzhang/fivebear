package com.fivebear.websocket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fivebear.common.util.JwtUtil;

/**
 * WebSocket消息服务
 * 提供封装的消息发送功能
 * 使用ApplicationContext来避免循环依赖
 */
@Service
public class WebSocketMessageService {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 获取WebSocket处理器（延迟获取以避免循环依赖）
     */
    private UnifiedWebSocketHandler getWebSocketHandler() {
        return applicationContext.getBean(UnifiedWebSocketHandler.class);
    }
    
    // ==================== 系统消息发送 ====================
    
    /**
     * 发送系统信息消息
     */
    public void sendSystemInfo(String message, Object data) {
        sendToAllUsers(WebSocketMessageType.SYSTEM_INFO, message, data);
    }
    
    /**
     * 发送系统警告消息
     */
    public void sendSystemWarning(String message, Object data) {
        sendToAllUsers(WebSocketMessageType.SYSTEM_WARNING, message, data);
    }
    
    /**
     * 发送系统错误消息
     */
    public void sendSystemError(String message, Object data) {
        sendToAllUsers(WebSocketMessageType.SYSTEM_ERROR, message, data);
    }
    
    // ==================== 管理员专用消息 ====================
    
    /**
     * 发送在线用户数量更新（仅管理员）
     */
    public void sendOnlineUserCountToAdmins() {
        UnifiedWebSocketHandler handler = getWebSocketHandler();
        int onlineCount = handler.getOnlineUserCount();
        int totalConnections = handler.getTotalConnectionCount();
        
        Map<String, Object> data = Map.of(
            "onlineUserCount", onlineCount,
            "totalConnections", totalConnections,
            "timestamp", System.currentTimeMillis()
        );
        
        sendToAdmins(WebSocketMessageType.ADMIN_ONLINE_USER_COUNT, "在线用户数量更新", data);
    }
    
    /**
     * 发送系统状态信息（仅管理员）
     */
    public void sendSystemStatusToAdmins(String message, Object data) {
        sendToAdmins(WebSocketMessageType.ADMIN_SYSTEM_STATUS, message, data);
    }
    
    /**
     * 发送用户行为通知（仅管理员）
     */
    public void sendUserActionToAdmins(String username, String action, Object data) {
        String message = String.format("用户 %s 执行了操作: %s", username, action);
        Map<String, Object> actionData = Map.of(
            "username", username,
            "action", action,
            "data", data != null ? data : Map.of(),
            "timestamp", System.currentTimeMillis()
        );
        sendToAdmins(WebSocketMessageType.ADMIN_USER_ACTION, message, actionData);
    }
    
    // ==================== 业务消息发送 ====================
    
    /**
     * 发送业务信息消息给指定用户
     */
    public void sendBusinessInfoToUser(String userId, String message, Object data) {
        sendToUser(userId, WebSocketMessageType.BUSINESS_INFO, message, data);
    }
    
    /**
     * 发送业务警告消息给指定用户
     */
    public void sendBusinessWarningToUser(String userId, String message, Object data) {
        sendToUser(userId, WebSocketMessageType.BUSINESS_WARNING, message, data);
    }
    
    /**
     * 发送业务错误消息给指定用户
     */
    public void sendBusinessErrorToUser(String userId, String message, Object data) {
        sendToUser(userId, WebSocketMessageType.BUSINESS_ERROR, message, data);
    }
    
    // ==================== 连接相关消息 ====================
    
    /**
     * 发送连接成功消息
     */
    public void sendConnectionSuccess(String sessionId, String username, String userId) {
        Map<String, Object> data = Map.of(
            "sessionId", sessionId,
            "username", username,
            "userId", userId,
            "connectionTime", LocalDateTime.now().format(formatter),
            "features", java.util.List.of("login_notification", "platform_management", "system_messages")
        );
        
        JSONObject message = createMessage(WebSocketMessageType.CONNECTION_SUCCESS, "WebSocket连接成功", data);
        getWebSocketHandler().sendMessageToSession(sessionId, message);
    }
    
    // ==================== 核心发送方法 ====================
    
    /**
     * 发送消息给所有用户
     */
    public void sendToAllUsers(WebSocketMessageType messageType, String message, Object data) {
        JSONObject msg = createMessage(messageType, message, data);
        getWebSocketHandler().broadcastToAllUsers(msg);
    }
    
    /**
     * 发送消息给指定用户
     */
    public void sendToUser(String userId, WebSocketMessageType messageType, String message, Object data) {
        JSONObject msg = createMessage(messageType, message, data);
        getWebSocketHandler().sendMessageToUser(userId, msg);
    }
    
    /**
     * 发送消息给管理员
     */
    public void sendToAdmins(WebSocketMessageType messageType, String message, Object data) {
        JSONObject msg = createMessage(messageType, message, data);
        getWebSocketHandler().sendToUsersByRole(1L, msg); // 角色ID 1 = 管理员
    }
    
    /**
     * 发送消息给指定角色的用户
     */
    public void sendToRole(Long roleId, WebSocketMessageType messageType, String message, Object data) {
        JSONObject msg = createMessage(messageType, message, data);
        getWebSocketHandler().sendToUsersByRole(roleId, msg);
    }
    
    // ==================== 消息构建方法 ====================
    
    /**
     * 创建标准格式的WebSocket消息
     */
    private JSONObject createMessage(WebSocketMessageType messageType, String message, Object data) {
        JSONObject msg = new JSONObject();
        msg.put("type", messageType.getType());
        msg.put("message", message);
        msg.put("level", messageType.getLevel().getLevel());
        msg.put("timestamp", LocalDateTime.now().format(formatter));
        
        if (data != null) {
            msg.put("data", data);
        }
        
        // 添加消息元数据
        Map<String, Object> meta = new HashMap<>();
        meta.put("description", messageType.getDescription());
        meta.put("requiresRole", messageType.requiresRole());
        meta.put("requiredRole", messageType.getRequiredRole() != null ? messageType.getRequiredRole().getRoleName() : null);
        msg.put("meta", meta);
        
        return msg;
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 通知用户登录行为（给管理员）
     */
    public void notifyUserLogin(String username, String sessionId) {
        sendUserActionToAdmins(username, "登录", Map.of(
            "sessionId", sessionId,
            "loginTime", LocalDateTime.now().format(formatter)
        ));
    }
    
    /**
     * 通知用户登出行为（给管理员）
     */
    public void notifyUserLogout(String username, String reason) {
        sendUserActionToAdmins(username, "登出", Map.of(
            "reason", reason,
            "logoutTime", LocalDateTime.now().format(formatter)
        ));
    }
    
    /**
     * 通知强制登出行为（给管理员）
     */
    public void notifyForceLogout(String username, String oldSessionId, String reason) {
        sendUserActionToAdmins(username, "强制登出", Map.of(
            "oldSessionId", oldSessionId,
            "reason", reason,
            "forceLogoutTime", LocalDateTime.now().format(formatter)
        ));
    }
} 