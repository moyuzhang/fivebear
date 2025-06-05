package com.fivebear.platform;
/**
 * 消息类型枚举
 */
public enum MessageType {
    // 状态相关消息
    STATUS_CHANGE("状态变化"),
    LOGIN_SUCCESS("登录成功"),
    LOGIN_FAILURE("登录失败"),
    LOGOUT("退出登录"),

    // 数据相关消息
    DATA_UPDATE("数据更新"),
    DATA_ERROR("数据错误"),

    // 操作相关消息
    OPERATION_SUCCESS("操作成功"),
    OPERATION_FAILURE("操作失败"),

    // 系统消息
    SYSTEM_INFO("系统信息"),
    SYSTEM_WARNING("系统警告"),
    SYSTEM_ERROR("系统错误"),

    // 网络相关消息
    NETWORK_ERROR("网络错误"),
    CONNECTION_LOST("连接断开"),
    CONNECTION_RESTORED("连接恢复");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}