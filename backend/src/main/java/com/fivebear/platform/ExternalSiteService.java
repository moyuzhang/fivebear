package com.fivebear.platform;

import java.util.List;
import java.util.Optional;

/**
 * 站点数据库服务接口
 */
public interface ExternalSiteService {
    
    /**
     * 保存站点到数据库
     * @param site 站点对象
     * @return 数据库ID
     */
    Long saveSite(Site site);
    
    /**
     * 根据用户ID加载所有站点
     * @param userId 用户ID
     * @return 站点列表
     */
    List<SiteConfig> loadSitesByUserId(String userId);
    
    /**
     * 检查用户是否已经添加过此站点
     * @param userId 用户ID
     * @param domainAccount domain+username组合
     * @return 是否存在
     */
    boolean existsByUserIdAndDomainAccount(String userId, String domainAccount);
    
    /**
     * 根据用户ID和业务key查找站点
     * @param userId 用户ID
     * @param domainAccount domain+username组合
     * @return 站点配置
     */
    Optional<SiteConfig> findByUserIdAndDomainAccount(String userId, String domainAccount);
    
    /**
     * 更新站点状态
     * @param databaseId 数据库ID
     * @param status 新状态
     * @param operatorUserId 操作用户ID
     * @param operationType 操作类型
     */
    void updateSiteStatus(Long databaseId, int status, String operatorUserId, String operationType);
    
    /**
     * 删除站点
     * @param databaseId 数据库ID
     * @param operatorUserId 操作用户ID
     */
    void deleteSite(Long databaseId, String operatorUserId);
    
    /**
     * 检查站点所有权
     * @param databaseId 数据库ID
     * @param userId 用户ID
     * @return 是否拥有此站点
     */
    boolean checkSiteOwnership(Long databaseId, String userId);
    
    /**
     * 站点配置数据类
     */
    public static class SiteConfig {
        private Long id;
        private String url;
        private String username;
        private String password;
        private double rebateRate;
        private int lotteryType;
        private int clientType;
        private int clientRole;
        private String domainAccount;
        private String createdByUserId;
        private double multiplier;
        
        // 构造方法
        public SiteConfig() {}
        
        public SiteConfig(Long id, String url, String username, String password, 
                         double rebateRate, int lotteryType, int clientType, 
                         int clientRole, String domainAccount, String createdByUserId,
                         double multiplier) {
            this.id = id;
            this.url = url;
            this.username = username;
            this.password = password;
            this.rebateRate = rebateRate;
            this.lotteryType = lotteryType;
            this.clientType = clientType;
            this.clientRole = clientRole;
            this.domainAccount = domainAccount;
            this.createdByUserId = createdByUserId;
            this.multiplier = multiplier;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public double getRebateRate() { return rebateRate; }
        public void setRebateRate(double rebateRate) { this.rebateRate = rebateRate; }
        
        public int getLotteryType() { return lotteryType; }
        public void setLotteryType(int lotteryType) { this.lotteryType = lotteryType; }
        
        public int getClientType() { return clientType; }
        public void setClientType(int clientType) { this.clientType = clientType; }
        
        public int getClientRole() { return clientRole; }
        public void setClientRole(int clientRole) { this.clientRole = clientRole; }
        
        public String getDomainAccount() { return domainAccount; }
        public void setDomainAccount(String domainAccount) { this.domainAccount = domainAccount; }
        
        public String getCreatedByUserId() { return createdByUserId; }
        public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }
        
        public double getMultiplier() { return multiplier; }
        public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    }
} 