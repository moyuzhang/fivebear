package com.fivebear.platform;

/**
 * 站点状态枚举,统一管理所有站点状态
 */
public enum SiteStatus {
    // 通用状态
    NOT_LOGGED_IN("未登录"),
    LOGGING_IN("登录中"),
    LOGGED_IN("已登录"),
    LOGGED_OUT("已退出登录"),
    IDLE("空闲"),

    // 数据同步状态
    FETCHING_DATA("获取数据中"),
    SETTING_DATA("设置数据中"),

    // 会员操作状态
    BETTING("下注中"),
    FETCHING_ODDS("获取赔率中"),
    CANCELLING_ORDER("退单中"),

    // 错误状态
    ERROR("错误"),
    NETWORK_ERROR("网络错误"),
    AUTHENTICATION_ERROR("认证错误"),

    COLLECT_BET("预下注"),

    // 添加新的状态
    NEW_STATE("新状态");

    private final String description;

    SiteStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}