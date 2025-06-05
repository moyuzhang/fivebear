package com.fivebear.platform;
import java.util.List;

import org.json.JSONArray;

/**
 * 站点回调接口，用于处理站点状态变化等事件
 */
public interface SiteCallback {
    /**
     * 站点状态变化时的回调方法
     * 
     * @param site      发生变化的站点
     * @param newStatus 新的状态
     */
    void onStatusChanged(Site site, SiteStatus newStatus);

    /**
     * 返点率变化时的回调方法
     * 
     * @param site    发生变化的站点
     * @param newRate 新的返点率
     */
    void onRebateRateChanged(Site site, double newRate);

    /**
     * 消息推送回调方法
     * 
     * @param site        发送消息的站点
     * @param message     消息内容
     * @param messageType 消息类型
     */
    void onMessage(Site site, String message, MessageType messageType);

    /**
     * 推送包牌赔率信息
     * 
     * @param site        站点对象
     * @param packageInfo 包牌信息（含赔率等）
     */
    void onPackageOddsInfo(Site site, List<PackageInfo> packageInfoList);

    /**
     * 推送二字定赔率信息
     * 
     * @param site          站点对象
     * @param oddsJsonArray 赔率信息JSON数组
     */
    void onNumOddsInfo(Site site, JSONArray oddsJsonArray);
}