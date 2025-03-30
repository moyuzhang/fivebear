package com.fivebear.fivebear_system.service.impl;

import com.fivebear.fivebear_system.entity.IpPoolSettings;
import com.fivebear.fivebear_system.model.IpInfo;
import com.fivebear.fivebear_system.service.IpPoolService;
import com.fivebear.fivebear_system.service.MqttService;
import com.fivebear.fivebear_system.exception.IpPoolException;
import com.fivebear.fivebear_system.utils.HttpClientUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IpPoolServiceImpl implements IpPoolService {
    private final Map<String, Object> ipPoolStatus = new ConcurrentHashMap<>();
    private final Map<String, IpInfo> ipPool = new ConcurrentHashMap<>();
    private IpPoolSettings settings = new IpPoolSettings();
    private boolean isRunning = false;
    private final MqttService mqttService;

    @Autowired
    public IpPoolServiceImpl(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    // 发送生命周期消息的辅助方法
    private void sendLifecycleMessage(String status, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("status", status);
            payload.put("message", message);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("ipPoolSize", ipPool.size());
            payload.put("validIps", ipPool.values().stream()
                    .filter(IpInfo::isValid)
                    .count());
            
            String jsonPayload = new ObjectMapper().writeValueAsString(payload);
            mqttService.publish("ip_pool/lifecycle", jsonPayload);
            log.info("IP池生命周期消息: {} - {}", status, message);
        } catch (Exception e) {
            log.error("发送生命周期消息失败", e);
        }
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isRunning", isRunning);
        status.put("totalIps", ipPool.size());
        status.put("validIps", ipPool.values().stream()
                .filter(IpInfo::isValid)
                .count());
        return status;
    }

    @Override
    @Transactional
    public void start() {
        if (!isRunning) {
            try {
                sendLifecycleMessage("STARTING", "IP池服务启动中");
                
                // 1. 加载配置
                loadSettings();
                sendLifecycleMessage("STARTING", "配置加载完成");
                
                // 2. 初始化连接
                initializeConnections();
                sendLifecycleMessage("STARTING", "连接初始化完成");
                
                // 3. 启动IP池的定时任务
                startIpPoolTasks();
                sendLifecycleMessage("RUNNING", "IP池服务启动完成");
                
                isRunning = true;
            } catch (Exception e) {
                sendLifecycleMessage("ERROR", "IP池启动失败: " + e.getMessage());
                throw new IpPoolException("IP池启动失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void stop() {
        if (isRunning) {
            try {
                sendLifecycleMessage("STOPPING", "IP池服务停止中");
                
                // 1. 停止定时任务
                stopIpPoolTasks();
                sendLifecycleMessage("STOPPING", "定时任务已停止");
                
                // 2. 保存状态
                saveState();
                sendLifecycleMessage("STOPPING", "状态已保存");
                
                // 3. 关闭连接
                closeConnections();
                sendLifecycleMessage("STOPPED", "IP池服务已停止");
                
                isRunning = false;
            } catch (Exception e) {
                sendLifecycleMessage("ERROR", "IP池停止失败: " + e.getMessage());
                throw new IpPoolException("IP池停止失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void refresh() {
        try {
            sendLifecycleMessage("MAINTENANCE", "开始刷新IP池");
            
            // 1. 获取所有IP
            List<IpInfo> ips = new ArrayList<>(ipPool.values());
            sendLifecycleMessage("MAINTENANCE", "开始检测 " + ips.size() + " 个IP");
            
            // 2. 刷新IP池中的所有IP
            ips.parallelStream().forEach(ip -> {
                try {
                    testAndUpdateIp(ip);
                } catch (Exception e) {
                    log.error("IP检测失败: {}", ip.getIp(), e);
                }
            });
            
            sendLifecycleMessage("RUNNING", "IP池刷新完成");
        } catch (Exception e) {
            sendLifecycleMessage("ERROR", "IP池刷新失败: " + e.getMessage());
            throw new IpPoolException("IP池刷新失败", e);
        }
    }

    @Override
    public IpPoolSettings getSettings() {
        return settings;
    }

    @Override
    @Transactional
    public void saveSettings(IpPoolSettings settings) {
        try {
            sendLifecycleMessage("MAINTENANCE", "开始更新IP池设置");
            
            // 1. 保存设置
            this.settings = settings;
            
            // 2. 应用新的设置
            applySettings();
            
            sendLifecycleMessage("RUNNING", "IP池设置更新完成");
        } catch (Exception e) {
            sendLifecycleMessage("ERROR", "IP池设置更新失败: " + e.getMessage());
            throw new IpPoolException("IP池设置更新失败", e);
        }
    }

    @Override
    public Map<String, Object> getIpList(int page, int pageSize, Boolean validStatus, String keyword) {
        List<IpInfo> filteredIps = ipPool.values().stream()
                .filter(ip -> validStatus == null || ip.isValid() == validStatus)
                .filter(ip -> keyword == null || 
                        ip.getIp().contains(keyword) || 
                        ip.getCountry().contains(keyword) ||
                        ip.getCity().contains(keyword))
                .collect(Collectors.toList());

        int total = filteredIps.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<IpInfo> pagedIps = filteredIps.subList(start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("items", pagedIps);
        return result;
    }

    @Override
    public IpInfo getFastestIp() {
        return ipPool.values().stream()
                .filter(IpInfo::isValid)
                .min(Comparator.comparingLong(IpInfo::getResponseTime))
                .orElse(null);
    }

    @Override
    @Transactional
    public void addProxy(Map<String, Object> proxy) {
        try {
            sendLifecycleMessage("MAINTENANCE", "开始添加代理IP");
            
            IpInfo ipInfo = new IpInfo();
            ipInfo.setIp((String) proxy.get("ip"));
            ipInfo.setPort((Integer) proxy.get("port"));
            ipInfo.setProtocol((String) proxy.get("protocol"));
            ipInfo.setCountry((String) proxy.get("country"));
            ipInfo.setCity((String) proxy.get("city"));
            ipInfo.setValid(true);
            ipInfo.setResponseTime(0L);
            ipInfo.setLastCheckTime(new Date());

            ipPool.put(ipInfo.getIp(), ipInfo);
            testAndUpdateIp(ipInfo);
            
            sendLifecycleMessage("RUNNING", "代理IP添加完成: " + ipInfo.getIp());
        } catch (Exception e) {
            sendLifecycleMessage("ERROR", "添加代理IP失败: " + e.getMessage());
            throw new IpPoolException("添加代理IP失败", e);
        }
    }

    @Override
    @Transactional
    public void deleteProxy(Map<String, Object> proxy) {
        try {
            sendLifecycleMessage("MAINTENANCE", "开始删除代理IP");
            
            String ip = (String) proxy.get("ip");
            ipPool.remove(ip);
            
            sendLifecycleMessage("RUNNING", "代理IP删除完成: " + ip);
        } catch (Exception e) {
            sendLifecycleMessage("ERROR", "删除代理IP失败: " + e.getMessage());
            throw new IpPoolException("删除代理IP失败", e);
        }
    }

    @Override
    @Transactional
    public void batchDeleteProxies(List<Map<String, Object>> proxies) {
        try {
            sendLifecycleMessage("MAINTENANCE", "开始批量删除代理IP");
            
            proxies.forEach(this::deleteProxy);
            
            sendLifecycleMessage("RUNNING", "批量删除代理IP完成，共删除 " + proxies.size() + " 个IP");
        } catch (Exception e) {
            sendLifecycleMessage("ERROR", "批量删除代理IP失败: " + e.getMessage());
            throw new IpPoolException("批量删除代理IP失败", e);
        }
    }

    @Override
    public Map<String, Object> testProxy(Map<String, Object> proxy) {
        String ip = (String) proxy.get("ip");
        Integer port = (Integer) proxy.get("port");
        String protocol = (String) proxy.get("protocol");

        Map<String, Object> result = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();
            String response = HttpClientUtils.get("http://example.com", ip, port, protocol);
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            result.put("success", true);
            result.put("responseTime", responseTime);
            result.put("message", "代理测试成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "代理测试失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> quickTestProxy(Map<String, Object> testInfo) {
        return testProxy(testInfo);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIps", ipPool.size());
        stats.put("validIps", ipPool.values().stream()
                .filter(IpInfo::isValid)
                .count());
        stats.put("averageResponseTime", ipPool.values().stream()
                .filter(IpInfo::isValid)
                .mapToLong(IpInfo::getResponseTime)
                .average()
                .orElse(0.0));
        return stats;
    }

    @Override
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", getStatus());
        info.put("stats", getStats());
        info.put("settings", settings);
        return info;
    }

    @Override
    public Map<String, Object> getIpPoolList(Map<String, Object> params) {
        return getIpList(
                (Integer) params.getOrDefault("page", 1),
                (Integer) params.getOrDefault("pageSize", 10),
                (Boolean) params.get("validStatus"),
                (String) params.get("keyword")
        );
    }

    @Override
    @Transactional
    public void addIpPool(Map<String, Object> data) {
        // 实现添加IP池的逻辑
    }

    @Override
    @Transactional
    public void updateIpPool(Map<String, Object> data) {
        // 实现更新IP池的逻辑
    }

    @Override
    @Transactional
    public void deleteIpPool(String id) {
        // 实现删除IP池的逻辑
    }

    private void loadSettings() {
        // TODO: 从配置加载设置
        settings = new IpPoolSettings();
    }

    private void initializeConnections() {
        // TODO: 初始化必要的连接
    }

    private void startIpPoolTasks() {
        // TODO: 启动IP池的定时任务
    }

    private void stopIpPoolTasks() {
        // TODO: 停止IP池的定时任务
    }

    private void saveState() {
        // TODO: 保存IP池状态
    }

    private void closeConnections() {
        // TODO: 关闭所有连接
    }

    private void applySettings() {
        // TODO: 应用新的设置
    }

    private void testAndUpdateIp(IpInfo ipInfo) {
        try {
            Map<String, Object> testResult = testProxy(Map.of(
                    "ip", ipInfo.getIp(),
                    "port", ipInfo.getPort(),
                    "protocol", ipInfo.getProtocol()
            ));

            ipInfo.setValid((Boolean) testResult.get("success"));
            ipInfo.setResponseTime((Long) testResult.getOrDefault("responseTime", 0L));
            ipInfo.setLastCheckTime(new Date());
            
            // 发送IP检测结果
            Map<String, Object> checkResult = new HashMap<>();
            checkResult.put("ip", ipInfo.getIp());
            checkResult.put("valid", ipInfo.isValid());
            checkResult.put("responseTime", ipInfo.getResponseTime());
            checkResult.put("timestamp", System.currentTimeMillis());
            
            mqttService.publish("ip_pool/check_result", 
                new ObjectMapper().writeValueAsString(checkResult));
        } catch (Exception e) {
            log.error("IP检测失败: {}", ipInfo.getIp(), e);
        }
    }
} 