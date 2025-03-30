package com.fivebear.fivebear_system.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@RedisHash("proxy_ip")
public class ProxyIp {
    @Id
    private Long id;
    
    @Indexed
    private String ip;
    
    @Indexed
    private Integer port;
    
    private String protocol;
    private String country;
    private String city;
    private boolean isActive;
    private Long responseTime;
    private LocalDateTime lastCheckTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 