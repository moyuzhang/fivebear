package com.fivebear.platform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Platform模块的Spring配置类
 */
@Configuration
public class PlatformConfiguration {
    
    @Autowired
    private ExternalSiteService externalSiteService;
    
    /**
     * 配置PlatformManager Bean并注入数据库服务
     * WebSocket处理器通过setter方法延迟注入
     */
    @Bean
    public PlatformManager platformManager() {
        PlatformManager platformManager = new PlatformManager();
        platformManager.setExternalSiteService(externalSiteService);
        return platformManager;
    }
} 