package com.fivebear.fivebear_system.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@RedisHash("ip_pool_settings")
public class IpPoolSettings {
    @Id
    private String id;

    private Integer minIpCount;
    private Integer maxIpCount;
    private Integer checkInterval;
    private Integer responseTimeout;
    private Integer maxFailCount;
    private Boolean isAutoRefresh;
    private Integer refreshInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 