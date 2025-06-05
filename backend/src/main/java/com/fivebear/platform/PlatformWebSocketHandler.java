package com.fivebear.platform;

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

/**
 * 站点管理WebSocket处理器
 * 实现用户隔离的实时消息推送
 */
@Component
public class PlatformWebSocketHandler extends TextWebSocketHandler implements SiteCallback {
    
    @Autowired
    private PlatformManager platformManager;
    
    /**
     * Bean初始化后，将自己注册到PlatformManager
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        if (platformManager != null) {
            platformManager.setWebSocketHandler(this);
            platformManager.addSiteCallbackListener(this);
        }
    }
    
    // 用户会话管理
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionUserMap = new ConcurrentHashMap<>();
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session中获取用户ID（应该在握手阶段设置）
        String userId = getUserIdFromSession(session);
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing user authentication"));
            return;
        }
        
        // 注册用户会话
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionUserMap.put(session, userId);
        
                    // SiteCallback监听器已在@PostConstruct中注册
        
        // 发送连接成功消息
        sendMessageToSession(session, createMessage("SYSTEM_INFO", "WebSocket连接成功", null));
        
        // 发送当前站点状态
        sendUserSitesSummary(userId);
        
        System.out.println("用户 " + userId + " 的WebSocket连接已建立");
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = sessionUserMap.remove(session);
        if (userId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        
        System.out.println("用户 " + userId + " 的WebSocket连接已关闭: " + status);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = sessionUserMap.get(session);
        if (userId == null) {
            return;
        }
        
        try {
            JSONObject request = new JSONObject(message.getPayload());
            String action = request.getString("action");
            
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
            
        } catch (Exception e) {
            sendMessageToSession(session, createMessage("ERROR", "消息处理失败: " + e.getMessage(), null));
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = sessionUserMap.get(session);
        System.err.println("用户 " + userId + " WebSocket传输错误: " + exception.getMessage());
    }
    
    // ==================== SiteCallback 实现 ====================
    
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
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
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
        for (CopyOnWriteArraySet<WebSocketSession> sessions : userSessions.values()) {
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
    
    /**
     * 从WebSocket会话中获取用户ID
     * 需要在握手阶段设置用户信息
     */
    private String getUserIdFromSession(WebSocketSession session) {
        // 从session attributes中获取用户ID
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
    
    /**
     * 获取总连接数量
     */
    public int getTotalConnectionCount() {
        return userSessions.values().stream()
            .mapToInt(sessions -> sessions.size())
            .sum();
    }
    
    /**
     * 获取用户的连接数量
     */
    public int getUserConnectionCount(String userId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }
} 