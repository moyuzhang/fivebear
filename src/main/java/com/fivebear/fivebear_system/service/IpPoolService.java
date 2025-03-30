package com.fivebear.fivebear_system.service;

import com.fivebear.fivebear_system.entity.IpPoolSettings;
import com.fivebear.fivebear_system.model.IpInfo;

import java.util.List;
import java.util.Map;

public interface IpPoolService {
    Map<String, Object> getStatus();
    void start();
    void stop();
    void refresh();
    IpPoolSettings getSettings();
    void saveSettings(IpPoolSettings settings);
    Map<String, Object> getIpList(int page, int pageSize, Boolean validStatus, String keyword);
    IpInfo getFastestIp();
    void addProxy(Map<String, Object> proxy);
    void deleteProxy(Map<String, Object> proxy);
    void batchDeleteProxies(List<Map<String, Object>> proxies);
    Map<String, Object> testProxy(Map<String, Object> proxy);
    Map<String, Object> quickTestProxy(Map<String, Object> testInfo);
    Map<String, Object> getStats();
    Map<String, Object> getInfo();
    Map<String, Object> getIpPoolList(Map<String, Object> params);
    void addIpPool(Map<String, Object> data);
    void updateIpPool(Map<String, Object> data);
    void deleteIpPool(String id);
} 