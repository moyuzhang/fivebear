package com.fivebear.fivebear_system.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Date;

@Data
@RedisHash("ip_info")
public class IpInfo {
    @Id
    private String ip;
    
    @Indexed
    private Integer port;
    
    private String protocol;
    private String country;
    private String city;
    private boolean valid;
    private Long responseTime;
    private Date lastCheckTime;
} 