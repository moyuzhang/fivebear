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
import com.fivebear.platform.MessageType;
import com.fivebear.platform.PackageInfo;
import com.fivebear.platform.PlatformManager;
import com.fivebear.platform.Site;
import com.fivebear.platform.SiteCallback;
import com.fivebear.platform.SiteStatus;

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
    
    @Autowired(required = false)
    private WebSocketMessageService webSocketMessageService;
    
    @Autowired(required = false)
    private com.fivebear.auth.mapper.UserMapper userMapper;
    
    /**
     * Bean初始化后，注册SiteCallback监听器
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        if (platformManager != null) {
            platformManager.addSiteCallbackListener(this);
        }
    }

    // ==================== WebSocket会话管理 ====================
    
    /**
     * 用户会话信息类
     */
    private static class UserSessionInfo {
        private final String sessionId;
        private final String username;
        private final String userId;
        private final WebSocketSession session;
        private final long connectionTime;
        
        public UserSessionInfo(String sessionId, String username, String userId, WebSocketSession session) {
            this.sessionId = sessionId;
            this.username = username;
            this.userId = userId;
            this.session = session;
            this.connectionTime = System.currentTimeMillis();
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public String getUserId() { return userId; }
        public WebSocketSession getSession() { return session; }
        public long getConnectionTime() { return connectionTime; }
        public boolean isSessionOpen() { return session != null && session.isOpen(); }
    }
    
    // 核心管理：sessionId -> 完整的用户会话信息
    private final ConcurrentHashMap<String, UserSessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    
    // 索引：username -> 最新的sessionId（用于登录通知/强制下线）
    private final ConcurrentHashMap<String, String> userLatestSessionId = new ConcurrentHashMap<>();
    
    // 索引：userId -> 该用户的所有sessionId集合（用于站点管理广播）
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<String>> userAllSessionIds = new ConcurrentHashMap<>();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String username = (String) session.getAttributes().get("username");
        Long userIdLong = (Long) session.getAttributes().get("userId");
        String userId = userIdLong != null ? userIdLong.toString() : null;

        if (username != null && userId != null && sessionId != null) {
            // 创建用户会话信息
            UserSessionInfo sessionInfo = new UserSessionInfo(sessionId, username, userId, session);
            
            // 存储会话信息（以sessionId为主键）
            sessionInfoMap.put(sessionId, sessionInfo);
            
            // 处理用户的最新连接（登录通知用）
            String oldLatestSessionId = userLatestSessionId.put(username, sessionId);
            if (oldLatestSessionId != null && !oldLatestSessionId.equals(sessionId)) {
                System.out.println("用户 " + username + " 建立新连接 " + sessionId + "，替换旧连接 " + oldLatestSessionId);
            }
            
            // 添加到用户的所有连接集合（站点管理用）
            userAllSessionIds.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
            
            // 注册SiteCallback监听器
            platformManager.addSiteCallbackListener(this);
            
            // 发送连接成功消息
            if (webSocketMessageService != null) {
                webSocketMessageService.sendConnectionSuccess(sessionId, username, userId);
                webSocketMessageService.notifyUserLogin(username, sessionId);
            } else {
                // 兜底方案
                sendMessageToSession(sessionId, createMessage("SYSTEM_INFO", "WebSocket连接成功", Map.of(
                    "sessionId", sessionId,
                    "username", username,
                    "userId", userId,
                    "features", List.of("login_notification", "platform_management")
                )));
            }
            
            // 发送当前站点状态
            sendUserSitesSummary(userId);
            
            // 广播在线用户数量更新（仅给管理员）
            broadcastOnlineUserCount();
            
            System.out.println("用户 " + username + " (ID: " + userId + ") 连接已建立，sessionId: " + sessionId + 
                ", 当前连接数: " + getUserConnectionCount(userId));
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Missing user authentication"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        UserSessionInfo sessionInfo = sessionInfoMap.remove(sessionId);
        
        if (sessionInfo != null) {
            String username = sessionInfo.getUsername();
            String userId = sessionInfo.getUserId();
            
            // 从最新连接记录中移除（仅当这是最新连接时）
            userLatestSessionId.remove(username, sessionId);
            
            // 从用户所有连接中移除
            CopyOnWriteArraySet<String> userSessionIds = userAllSessionIds.get(userId);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userAllSessionIds.remove(userId);
                }
            }
            
            // 通知管理员用户下线
            if (webSocketMessageService != null) {
                webSocketMessageService.notifyUserLogout(username, "正常关闭连接");
            }
            
            // 广播在线用户数量更新（仅给管理员）
            broadcastOnlineUserCount();
            
            System.out.println("用户 " + username + " (ID: " + userId + ") 连接已关闭，sessionId: " + sessionId + 
                ", 状态: " + status + ", 剩余连接数: " + getUserConnectionCount(userId));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        UserSessionInfo sessionInfo = sessionInfoMap.get(sessionId);
        
        if (sessionInfo == null) {
            System.err.println("未找到sessionId " + sessionId + " 对应的会话信息");
            return;
        }

        String username = sessionInfo.getUsername();
        String userId = sessionInfo.getUserId();

        try {
            JSONObject request = new JSONObject(message.getPayload());
            String type = request.optString("type", "");
            String action = request.optString("action", "");

            // 根据消息类型分发处理
            if ("platform".equals(type) || !action.isEmpty()) {
                // 站点管理相关消息
                handlePlatformMessage(sessionId, userId, action, request);
            } else if ("login_notification".equals(type)) {
                // 登录通知相关消息
                handleLoginNotificationMessage(sessionId, username, request);
            } else {
                // 默认处理为站点管理消息（向后兼容）
                handlePlatformMessage(sessionId, userId, action, request);
            }

        } catch (Exception e) {
            sendMessageToSession(sessionId, createMessage("ERROR", "消息处理失败: " + e.getMessage(), null));
        }
    }

    /**
     * 处理站点管理相关消息
     */
    private void handlePlatformMessage(String sessionId, String userId, String action, JSONObject request) {
        switch (action) {
            case "GET_SITES_SUMMARY":
                sendUserSitesSummary(userId);
                break;
            case "PING":
                sendMessageToSession(sessionId, createMessage("PONG", "心跳响应", null));
                break;
            default:
                sendMessageToSession(sessionId, createMessage("ERROR", "未知操作: " + action, null));
        }
    }

    /**
     * 处理登录通知相关消息
     */
    private void handleLoginNotificationMessage(String sessionId, String username, JSONObject request) {
        // 这里可以处理登录通知相关的消息，如心跳等
        String action = request.optString("action", "");
        if ("ping".equals(action)) {
            sendMessageToSession(sessionId, createMessage("PONG", "心跳响应", null));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        UserSessionInfo sessionInfo = sessionInfoMap.get(session.getId());
        if (sessionInfo != null) {
            String username = sessionInfo.getUsername();
            String userId = sessionInfo.getUserId();
            System.err.println("用户 " + username + " (ID: " + userId + ") WebSocket传输错误: " + exception.getMessage());
        }
    }

    // ==================== 登录通知功能 ====================

    /**
     * 发送强制登出通知给指定用户并断开连接
     */
    public void sendForceLogoutNotification(String username, String reason) {
        // 立即获取当前用户的session（避免被新连接替换）
        String oldSessionId = userLatestSessionId.get(username);
        
        if (oldSessionId != null && !oldSessionId.isEmpty()) {
            System.out.println("检测到用户 " + username + " 在其他地方登录，准备强制下线旧连接: " + oldSessionId);
            
            // 立即从映射中移除旧session（防止新连接影响）
            userLatestSessionId.remove(username, oldSessionId);
            
            // 通知管理员强制登出事件
            if (webSocketMessageService != null) {
                webSocketMessageService.notifyForceLogout(username, oldSessionId, reason);
            }
            
            // 发送强制登出消息给旧连接
            sendForceLogoutMessage(oldSessionId);
            
            // 延迟断开旧连接，给客户端处理时间
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // 等待3秒
                    
                    // 直接使用保存的oldSessionId来断开连接
                    UserSessionInfo sessionInfo = sessionInfoMap.get(oldSessionId);
                    if (sessionInfo != null && sessionInfo.isSessionOpen()) {
                        sessionInfo.getSession().close(CloseStatus.POLICY_VIOLATION.withReason("账户在其他地方登录"));
                        System.out.println("已强制断开用户 " + username + " 的旧WebSocket连接: " + oldSessionId);
                    } else {
                        System.out.println("用户 " + username + " 的旧连接 " + oldSessionId + " 已经关闭或不存在");
                    }
                } catch (Exception e) {
                    System.err.println("断开用户 " + username + " 的WebSocket连接失败: " + e.getMessage());
                }
            }).start();
        } else {
            System.out.println("用户 " + username + " 没有活跃的WebSocket连接，无需强制下线");
        }
    }

    /**
     * 主动断开指定用户的所有WebSocket连接
     */
    public void disconnectUser(String username, String reason) {
        String latestSessionId = userLatestSessionId.get(username);
        if (latestSessionId != null) {
            UserSessionInfo sessionInfo = sessionInfoMap.get(latestSessionId);
            if (sessionInfo != null) {
                String userId = sessionInfo.getUserId();
                disconnectUserById(userId, reason);
            }
        }
    }

    /**
     * 主动断开指定用户ID的所有WebSocket连接
     */
    public void disconnectUserById(String userId, String reason) {
        CopyOnWriteArraySet<String> sessionIds = userAllSessionIds.get(userId);
        if (sessionIds != null) {
            System.out.println("准备断开用户 " + userId + " 的所有WebSocket连接，原因: " + reason);
            
            // 发送断开通知
            JSONObject disconnectMessage = createMessage("FORCE_LOGOUT", reason, Map.of(
                "reason", "admin_disconnect",
                "timestamp", System.currentTimeMillis()
            ));
            
            for (String sessionId : sessionIds) {
                UserSessionInfo sessionInfo = sessionInfoMap.get(sessionId);
                if (sessionInfo != null && sessionInfo.isSessionOpen()) {
                    try {
                        // 发送断开通知
                        sessionInfo.getSession().sendMessage(new TextMessage(disconnectMessage.toString()));
                        
                        // 延迟断开连接，给客户端处理时间
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000); // 等待2秒
                                if (sessionInfo.isSessionOpen()) {
                                    sessionInfo.getSession().close(CloseStatus.POLICY_VIOLATION.withReason(reason));
                                    System.out.println("已断开用户 " + userId + " 的WebSocket连接: " + sessionId);
                                }
                            } catch (Exception e) {
                                System.err.println("断开WebSocket连接失败: " + e.getMessage());
                            }
                        }).start();
                        
                    } catch (IOException e) {
                        System.err.println("发送断开通知失败: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("用户 " + userId + " 没有活跃的WebSocket连接");
        }
    }

    /**
     * 主动断开指定sessionId的WebSocket连接
     */
    public void disconnectSession(String sessionId, String reason) {
        UserSessionInfo sessionInfo = sessionInfoMap.get(sessionId);
        if (sessionInfo != null && sessionInfo.isSessionOpen()) {
            try {
                System.out.println("准备断开WebSocket连接: " + sessionId + "，原因: " + reason);
                
                // 发送断开通知
                JSONObject disconnectMessage = createMessage("FORCE_LOGOUT", reason, Map.of(
                    "reason", "session_disconnect",
                    "timestamp", System.currentTimeMillis()
                ));
                sessionInfo.getSession().sendMessage(new TextMessage(disconnectMessage.toString()));
                
                // 延迟断开连接，给客户端处理时间
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 等待2秒
                        if (sessionInfo.isSessionOpen()) {
                            sessionInfo.getSession().close(CloseStatus.POLICY_VIOLATION.withReason(reason));
                            System.out.println("已断开WebSocket连接: " + sessionId);
                        }
                    } catch (Exception e) {
                        System.err.println("断开WebSocket连接失败: " + e.getMessage());
                    }
                }).start();
                
            } catch (IOException e) {
                System.err.println("发送断开通知失败: " + e.getMessage());
            }
        } else {
            System.out.println("WebSocket连接不存在或已关闭: " + sessionId);
        }
    }

    /**
     * 发送强制登出消息
     */
    private void sendForceLogoutMessage(String sessionId) {
        JSONObject message = createMessage("FORCE_LOGOUT", "检测到其他设备登录，您已被强制下线", Map.of(
            "reason", "multiple_login",
            "timestamp", System.currentTimeMillis()
        ));
        sendMessageToSession(sessionId, message);
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
        CopyOnWriteArraySet<String> sessionIds = userAllSessionIds.get(userId);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                sendMessageToSession(sessionId, message);
            }
        }
    }

    /**
     * 发送消息给指定会话
     */
    public void sendMessageToSession(String sessionId, JSONObject message) {
        UserSessionInfo sessionInfo = sessionInfoMap.get(sessionId);
        if (sessionInfo != null && sessionInfo.isSessionOpen()) {
            try {
                sessionInfo.getSession().sendMessage(new TextMessage(message.toString()));
            } catch (IOException e) {
                System.err.println("发送WebSocket消息失败: " + e.getMessage());
            }
        }
    }

    /**
     * 广播消息给所有用户
     */
    public void broadcastToAllUsers(JSONObject message) {
        for (CopyOnWriteArraySet<String> sessionIds : userAllSessionIds.values()) {
            for (String sessionId : sessionIds) {
                sendMessageToSession(sessionId, message);
            }
        }
    }

    /**
     * 发送消息给指定角色的所有用户
     */
    public void sendToUsersByRole(Long roleId, JSONObject message) {
        for (UserSessionInfo sessionInfo : sessionInfoMap.values()) {
            // 通过session attributes获取用户角色信息
            Long userRoleId = getUserRoleFromSession(sessionInfo.getSession());
            if (userRoleId != null && userRoleId.equals(roleId)) {
                sendMessageToSession(sessionInfo.getSessionId(), message);
            }
        }
    }

    /**
     * 从WebSocket会话中获取用户角色ID
     */
    private Long getUserRoleFromSession(WebSocketSession session) {
        try {
            String token = (String) session.getAttributes().get("token");
            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                // 这里需要通过用户服务获取角色信息，暂时返回null
                // TODO: 集成用户服务获取角色信息
                return getUserRoleById(userId);
            }
        } catch (Exception e) {
            System.err.println("获取用户角色失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 根据用户ID获取角色ID
     */
    private Long getUserRoleById(Long userId) {
        if (userId == null || userMapper == null) {
            return null;
        }
        
        try {
            com.fivebear.auth.entity.User user = userMapper.selectById(userId);
            return user != null ? user.getRoleId() : null;
        } catch (Exception e) {
            System.err.println("查询用户角色失败: " + e.getMessage());
            return null;
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
        return userAllSessionIds.size();
    }

    /**
     * 获取总连接数量
     */
    public int getTotalConnectionCount() {
        return userAllSessionIds.values().stream()
            .mapToInt(sessionIds -> sessionIds.size())
            .sum();
    }

    /**
     * 获取用户的连接数量
     */
    public int getUserConnectionCount(String userId) {
        CopyOnWriteArraySet<String> sessionIds = userAllSessionIds.get(userId);
        return sessionIds != null ? sessionIds.size() : 0;
    }

    /**
     * 广播在线用户数量更新（仅发送给管理员）
     */
    private void broadcastOnlineUserCount() {
        // 现在使用WebSocketMessageService来发送管理员专用消息
        // 这里暂时保留原有实现，但在connectionEstablished和connectionClosed中会调用新的服务
        if (webSocketMessageService != null) {
            webSocketMessageService.sendOnlineUserCountToAdmins();
        } else {
            // 兜底方案：使用旧的广播方式
            JSONObject message = createMessage("ONLINE_USER_COUNT", "在线用户数量更新", getOnlineUserCount());
            broadcastToAllUsers(message);
        }
    }
} 