package com.fivebear.websocket;

/**
 * WebSocket消息类型枚举
 */
public enum WebSocketMessageType {
    
    // ==================== 系统通用消息 ====================
    SYSTEM_INFO("SYSTEM_INFO", "系统信息", MessageLevel.INFO),
    SYSTEM_WARNING("SYSTEM_WARNING", "系统警告", MessageLevel.WARNING),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统错误", MessageLevel.ERROR),
    
    // ==================== 连接相关消息 ====================
    CONNECTION_SUCCESS("CONNECTION_SUCCESS", "连接成功", MessageLevel.INFO),
    CONNECTION_CLOSED("CONNECTION_CLOSED", "连接关闭", MessageLevel.INFO),
    FORCE_LOGOUT("FORCE_LOGOUT", "强制登出", MessageLevel.WARNING),
    
    // ==================== 管理员专用消息 ====================
    ADMIN_ONLINE_USER_COUNT("ADMIN_ONLINE_USER_COUNT", "在线用户数量", MessageLevel.INFO, UserRole.ADMIN),
    ADMIN_SYSTEM_STATUS("ADMIN_SYSTEM_STATUS", "系统状态", MessageLevel.INFO, UserRole.ADMIN),
    ADMIN_USER_ACTION("ADMIN_USER_ACTION", "用户行为", MessageLevel.INFO, UserRole.ADMIN),
    
    // ==================== 站点管理消息 ====================
    SITES_SUMMARY("SITES_SUMMARY", "站点摘要", MessageLevel.INFO),
    STATUS_CHANGE("STATUS_CHANGE", "状态变更", MessageLevel.INFO),
    REBATE_RATE_CHANGE("REBATE_RATE_CHANGE", "返点率变更", MessageLevel.INFO),
    
    // ==================== 心跳消息 ====================
    PING("PING", "心跳请求", MessageLevel.INFO),
    PONG("PONG", "心跳响应", MessageLevel.INFO),
    
    // ==================== 业务消息 ====================
    BUSINESS_INFO("BUSINESS_INFO", "业务信息", MessageLevel.INFO),
    BUSINESS_WARNING("BUSINESS_WARNING", "业务警告", MessageLevel.WARNING),
    BUSINESS_ERROR("BUSINESS_ERROR", "业务错误", MessageLevel.ERROR);
    
    private final String type;
    private final String description;
    private final MessageLevel level;
    private final UserRole requiredRole;
    
    WebSocketMessageType(String type, String description, MessageLevel level) {
        this(type, description, level, null);
    }
    
    WebSocketMessageType(String type, String description, MessageLevel level, UserRole requiredRole) {
        this.type = type;
        this.description = description;
        this.level = level;
        this.requiredRole = requiredRole;
    }
    
    public String getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public MessageLevel getLevel() {
        return level;
    }
    
    public UserRole getRequiredRole() {
        return requiredRole;
    }
    
    public boolean requiresRole() {
        return requiredRole != null;
    }
    
    /**
     * 检查用户是否有权限接收此类型消息
     */
    public boolean isAuthorized(Long userRoleId) {
        if (requiredRole == null) {
            return true; // 无特殊权限要求
        }
        return requiredRole.getRoleId().equals(userRoleId);
    }
    
    /**
     * 消息级别枚举
     */
    public enum MessageLevel {
        INFO("info"),
        WARNING("warning"),
        ERROR("error"),
        SUCCESS("success");
        
        private final String level;
        
        MessageLevel(String level) {
            this.level = level;
        }
        
        public String getLevel() {
            return level;
        }
    }
    
    /**
     * 用户角色枚举
     */
    public enum UserRole {
        ADMIN(1L, "管理员"),
        SUPERVISOR(2L, "总监"),
        SHAREHOLDER(3L, "大股东");
        
        private final Long roleId;
        private final String roleName;
        
        UserRole(Long roleId, String roleName) {
            this.roleId = roleId;
            this.roleName = roleName;
        }
        
        public Long getRoleId() {
            return roleId;
        }
        
        public String getRoleName() {
            return roleName;
        }
        
        public static UserRole fromRoleId(Long roleId) {
            if (roleId == null) {
                return null;
            }
            for (UserRole role : values()) {
                if (role.roleId.equals(roleId)) {
                    return role;
                }
            }
            return null;
        }
    }
} 