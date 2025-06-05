package com.fivebear.websocket;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fivebear.common.util.JwtUtil;
import com.fivebear.platform.PlatformManager;
import com.fivebear.platform.Site;
import com.fivebear.platform.SiteCallback;
import com.fivebear.platform.SiteStatus;
import com.fivebear.platform.PackageInfo;
import com.fivebear.platform.MessageType;

/**
 * 统一的WebSocket处理器
 * 整合登录通知和站点管理功能
 */
@Component
public class UnifiedWebSocketHandler extends TextWebSocketHandler implements SiteCallback {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PlatformManager platformManager;
    
    /**
     * Bean初始化后，注册SiteCallback监听器
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        if (platformManager != null) {
            platformManager.addSiteCallbackListener(this);
        }
    }

    // 存储用户名与WebSocket会话的映射（登录通知用）
    private final ConcurrentHashMap<String, WebSocketSession> loginNotificationSessions = new ConcurrentHashMap<>();
    
    // 存储用户ID与WebSocket会话集合的映射（站点管理用）
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> platformSessions = new ConcurrentHashMap<>();
    
    // 存储会话与用户信息的映射
    private final ConcurrentHashMap<String, String> sessionUsers = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionUserMap = new ConcurrentHashMap<>();
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session中获取用户信息（由TokenHandshakeInterceptor设置）
        String username = (String) session.getAttributes().get("username");
        Long userIdLong = (Long) session.getAttributes().get("userId");
        String userId = userIdLong != null ? userIdLong.toString() : null;

        if (username != null && userId != null) {
            // 登录通知功能：一个用户一个连接
            WebSocketSession oldSession = loginNotificationSessions.put(username, session);
            if (oldSession != null && oldSession.isOpen()) {
                // 通知旧会话被踢下线
                sendForceLogoutMessage(oldSession);
                oldSession.close(CloseStatus.POLICY_VIOLATION.withReason("另一设备登录"));
            }
            
            // 站点管理功能：一个用户可以有多个连接
            platformSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            
            // 建立会话映射
            sessionUsers.put(session.getId(), username);
            sessionUserMap.put(session, userId);
            
            // 注册SiteCallback监听器
            platformManager.addSiteCallbackListener(this);
            
            // 发送连接成功消息
            sendMessageToSession(session, createMessage("SYSTEM_INFO", "WebSocket连接成功", Map.of(
                "username", username,
                "userId", userId,
                "features", List.of("login_notification", "platform_management")
            )));
            
            // 发送当前站点状态
            sendUserSitesSummary(userId);
            
            System.out.println("用户 " + username + " (ID: " + userId + ") 的统一WebSocket连接已建立");
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Missing user authentication"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = sessionUsers.remove(session.getId());
        String userId = sessionUserMap.remove(session);
        
        // 从登录通知会话中移除
        if (username != null) {
            loginNotificationSessions.remove(username, session);
        }
        
        // 从站点管理会话中移除
        if (userId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = platformSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    platformSessions.remove(userId);
                }
            }
        }
        
        System.out.println("用户 " + username + " (ID: " + userId + ") 的统一WebSocket连接已关闭: " + status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = sessionUserMap.get(session);
        String username = sessionUsers.get(session.getId());
        
        if (userId == null || username == null) {
            return;
        }

        try {
            JSONObject request = new JSONObject(message.getPayload());
            String type = request.optString("type", "");
            String action = request.optString("action", "");

            // 根据消息类型分发处理
            if ("platform".equals(type) || !action.isEmpty()) {
                // 站点管理相关消息
                handlePlatformMessage(session, userId, action, request);
            } else if ("login_notification".equals(type)) {
                // 登录通知相关消息
                handleLoginNotificationMessage(session, username, request);
            } else {
                // 默认处理为站点管理消息（向后兼容）
                handlePlatformMessage(session, userId, action, request);
            }

        } catch (Exception e) {
            sendMessageToSession(session, createMessage("ERROR", "消息处理失败: " + e.getMessage(), null));
        }
    }

    /**
     * 处理站点管理相关消息
     */
    private void handlePlatformMessage(WebSocketSession session, String userId, String action, JSONObject request) {
        switch (action) {
            case "GET_SITES_SUMMARY":
                sendUserSitesSummary(userId);
                break;
            case "PING":
                sendMessageToSession(session, createMessage("PONG", "心跳响应", null));
                break;
            default:
                sendMessageToSession(session, createMessage("ERROR", "未知操作: " + action, null));
        }
    }

    /**
     * 处理登录通知相关消息
     */
    private void handleLoginNotificationMessage(WebSocketSession session, String username, JSONObject request) {
        // 这里可以处理登录通知相关的消息，如心跳等
        String action = request.optString("action", "");
        if ("ping".equals(action)) {
            sendMessageToSession(session, createMessage("PONG", "心跳响应", null));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = sessionUserMap.get(session);
        String username = sessionUsers.get(session.getId());
        System.err.println("用户 " + username + " (ID: " + userId + ") WebSocket传输错误: " + exception.getMessage());
    }

    // ==================== 登录通知功能 ====================

    /**
     * 发送强制登出通知给指定用户
     */
    public void sendForceLogoutNotification(String username, String reason) {
        WebSocketSession session = loginNotificationSessions.get(username);
        if (session != null && session.isOpen()) {
            sendForceLogoutMessage(session);
        }
    }

    /**
     * 发送强制登出消息
     */
    private void sendForceLogoutMessage(WebSocketSession session) {
        JSONObject message = createMessage("FORCE_LOGOUT", "检测到其他设备登录，您已被强制下线", Map.of(
            "reason", "multiple_login",
            "timestamp", System.currentTimeMillis()
        ));
        sendMessageToSession(session, message);
    }

    // ==================== SiteCallback 实现（站点管理功能）====================

    @Override
    public void onMessage(Site site, String message, MessageType messageType) {
        if (site == null) {
            // 系统级消息，广播给所有用户
            broadcastToAllUsers(createMessage(messageType.name(), message, null));
            return;
        }
        
        String userId = site.getUserId();
        if (userId != null) {
            // 用户隔离消息，只发送给指定用户
            JSONObject messageData = createMessage(messageType.name(), message, Map.of(
                "siteKey", site.getUniqueKey(),
                "siteDomain", site.getDomain(),
                "siteUsername", site.getUsername()
            ));
            sendMessageToUser(userId, messageData);
        }
    }

    @Override
    public void onStatusChanged(Site site, SiteStatus newStatus) {
        String userId = site.getUserId();
        if (userId != null) {
            JSONObject statusData = createMessage("STATUS_CHANGE", 
                String.format("站点 %s 状态变更为: %s", site.getUniqueKey(), newStatus.getDescription()),
                Map.of(
                    "siteKey", site.getUniqueKey(),
                    "newStatus", newStatus.name(),
                    "statusDescription", newStatus.getDescription()
                )
            );
            sendMessageToUser(userId, statusData);
            
            // 发送更新后的站点摘要
            sendUserSitesSummary(userId);
        }
    }

    @Override
    public void onRebateRateChanged(Site site, double newRate) {
        String userId = site.getUserId();
        if (userId != null) {
            JSONObject rateData = createMessage("REBATE_RATE_CHANGE",
                String.format("站点 %s 返点率变更为: %.4f", site.getUniqueKey(), newRate),
                Map.of(
                    "siteKey", site.getUniqueKey(),
                    "newRate", newRate
                )
            );
            sendMessageToUser(userId, rateData);
        }
    }

    @Override
    public void onPackageOddsInfo(Site site, List<PackageInfo> packageInfoList) {
        // 实现包牌赔率更新推送
    }

    @Override
    public void onNumOddsInfo(Site site, org.json.JSONArray oddsJsonArray) {
        // 实现号码赔率更新推送
    }

    // ==================== 消息发送方法 ====================

    /**
     * 发送消息给指定用户的所有会话
     */
    public void sendMessageToUser(String userId, JSONObject message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = platformSessions.get(userId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                sendMessageToSession(session, message);
            }
        }
    }

    /**
     * 发送消息给指定会话
     */
    private void sendMessageToSession(WebSocketSession session, JSONObject message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message.toString()));
            } catch (IOException e) {
                System.err.println("发送WebSocket消息失败: " + e.getMessage());
            }
        }
    }

    /**
     * 广播消息给所有用户
     */
    private void broadcastToAllUsers(JSONObject message) {
        for (CopyOnWriteArraySet<WebSocketSession> sessions : platformSessions.values()) {
            for (WebSocketSession session : sessions) {
                sendMessageToSession(session, message);
            }
        }
    }

    /**
     * 发送用户站点摘要
     */
    public void sendUserSitesSummary(String userId) {
        try {
            Map<String, Object> summary = platformManager.getUserSitesSummary(userId);
            JSONObject summaryMessage = createMessage("SITES_SUMMARY", "站点状态摘要", summary);
            sendMessageToUser(userId, summaryMessage);
        } catch (Exception e) {
            JSONObject errorMessage = createMessage("ERROR", "获取站点摘要失败: " + e.getMessage(), null);
            sendMessageToUser(userId, errorMessage);
        }
    }

    /**
     * 创建标准消息格式
     */
    private JSONObject createMessage(String type, String message, Object data) {
        JSONObject msg = new JSONObject();
        msg.put("type", type);
        msg.put("message", message);
        msg.put("timestamp", LocalDateTime.now().format(formatter));
        if (data != null) {
            msg.put("data", data);
        }
        return msg;
    }

    // ==================== 统计方法 ====================

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return platformSessions.size();
    }

    /**
     * 获取总连接数量
     */
    public int getTotalConnectionCount() {
        return platformSessions.values().stream()
            .mapToInt(sessions -> sessions.size())
            .sum();
    }

    /**
     * 获取用户的连接数量
     */
    public int getUserConnectionCount(String userId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = platformSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }
} 