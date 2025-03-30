package com.fivebear.fivebear_system.scheduler;

import com.fivebear.fivebear_system.service.IpPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IpPoolScheduler {

    @Autowired
    private IpPoolService ipPoolService;

    @Scheduled(fixedRateString = "${ip.pool.refresh.interval:300000}")
    public void refreshIpPool() {
        ipPoolService.refresh();
    }

    @Scheduled(fixedRateString = "${ip.pool.cleanup.interval:3600000}")
    public void cleanupIpPool() {
        // 清理无效的 IP
        ipPoolService.getIpList(1, Integer.MAX_VALUE, false, null)
                .forEach((ip, value) -> {
                    Map<String, Object> proxy = new HashMap<>();
                    proxy.put("ip", ip);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    proxy.putAll(valueMap);
                    ipPoolService.deleteProxy(proxy);
                });
    }
} 