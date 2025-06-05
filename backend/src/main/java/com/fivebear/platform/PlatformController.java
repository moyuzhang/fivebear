package com.fivebear.platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fivebear.common.result.Result;

/**
 * 站点管理REST API控制器
 */
@RestController
@RequestMapping("/api/platform")
public class PlatformController {
    
    @Autowired
    private PlatformManager platformManager;
    
    /**
     * 获取用户的所有站点
     */
    @GetMapping("/sites")
    public Result<Map<String, Object>> getUserSites(
            @RequestHeader("User-ID") String userId) {
        try {
            Map<String, Object> summary = platformManager.getUserSitesSummary(userId);
            return Result.ok(summary);
        } catch (Exception e) {
            return Result.fail("获取站点列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新站点
     */
    @PostMapping("/sites")
    public Result<Object> addSite(
            @RequestBody SiteCreateRequest request,
            @RequestHeader("User-ID") String userId) {
        try {
            // 验证请求参数
            if (request.getUrl() == null || request.getUsername() == null || 
                request.getPassword() == null) {
                return Result.fail("站点信息不完整");
            }
            
            // 创建站点配置
            ExternalSiteService.SiteConfig config = new ExternalSiteService.SiteConfig();
            config.setUrl(request.getUrl());
            config.setUsername(request.getUsername());
            config.setPassword(request.getPassword());
            config.setRebateRate(request.getRebateRate() != null ? request.getRebateRate() : 0.0);
            config.setLotteryType(request.getLotteryType() != null ? request.getLotteryType() : 1);
            config.setClientType(request.getClientType() != null ? request.getClientType() : 1);
            config.setClientRole(request.getClientRole() != null ? request.getClientRole() : 1);
            config.setMultiplier(request.getMultiplier() != null ? request.getMultiplier() : 1.0);
            config.setCreatedByUserId(userId);
            
            // 计算domain_account
            String domain = extractDomain(request.getUrl());
            config.setDomainAccount(domain + request.getUsername());
            
            // 检查业务唯一性
            if (!platformManager.canAddSite(userId, config.getDomainAccount())) {
                return Result.fail("您已经添加过此站点账户");
            }
            
            // 异步添加站点
            CompletableFuture<Site> future = platformManager.addSiteAsync(config, userId);
            Site site = future.get(); // 这里可以考虑改为异步响应
            
            return Result.ok(Map.of(
                "uniqueKey", site.getUniqueKey(),
                "message", "站点添加成功"
            ));
            
        } catch (Exception e) {
            return Result.fail("添加站点失败: " + e.getMessage());
        }
    }
    
    /**
     * 登录站点
     */
    @PostMapping("/sites/{uniqueKey}/login")
    public Result<Object> loginSite(
            @PathVariable String uniqueKey,
            @RequestHeader("User-ID") String userId) {
        try {
            // 检查站点所有权
            if (!platformManager.checkSiteOwnership(userId, uniqueKey)) {
                return Result.fail("无权限操作此站点");
            }
            
            // 异步登录
            CompletableFuture<Boolean> future = platformManager.loginSiteAsync(userId, uniqueKey);
            Boolean result = future.get();
            
            if (result) {
                return Result.ok(Map.of(
                    "uniqueKey", uniqueKey,
                    "message", "登录成功"
                ));
            } else {
                return Result.fail("登录失败");
            }
            
        } catch (Exception e) {
            return Result.fail("登录操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 登出站点
     */
    @PostMapping("/sites/{uniqueKey}/logout")
    public Result<Object> logoutSite(
            @PathVariable String uniqueKey,
            @RequestHeader("User-ID") String userId) {
        try {
            // 检查站点所有权
            if (!platformManager.checkSiteOwnership(userId, uniqueKey)) {
                return Result.fail("无权限操作此站点");
            }
            
            // 异步登出
            CompletableFuture<Boolean> future = platformManager.logoutSiteAsync(userId, uniqueKey);
            Boolean result = future.get();
            
            if (result) {
                return Result.ok(Map.of(
                    "uniqueKey", uniqueKey,
                    "message", "登出成功"
                ));
            } else {
                return Result.fail("登出失败");
            }
            
        } catch (Exception e) {
            return Result.fail("登出操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除站点
     */
    @DeleteMapping("/sites/{uniqueKey}")
    public Result<Object> deleteSite(
            @PathVariable String uniqueKey,
            @RequestHeader("User-ID") String userId) {
        try {
            // 检查站点所有权
            if (!platformManager.checkSiteOwnership(userId, uniqueKey)) {
                return Result.fail("无权限操作此站点");
            }
            
            // 删除站点
            boolean result = platformManager.removeSite(userId, uniqueKey);
            
            if (result) {
                return Result.ok(Map.of(
                    "uniqueKey", uniqueKey,
                    "message", "站点删除成功"
                ));
            } else {
                return Result.fail("站点删除失败");
            }
            
        } catch (Exception e) {
            return Result.fail("删除操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量登录站点
     */
    @PostMapping("/sites/batch/login")
    public Result<Object> batchLogin(
            @RequestBody BatchOperationRequest request,
            @RequestHeader("User-ID") String userId) {
        try {
            // 验证所有站点的所有权
            for (String uniqueKey : request.getUniqueKeys()) {
                if (!platformManager.checkSiteOwnership(userId, uniqueKey)) {
                    return Result.fail("无权限操作站点: " + uniqueKey);
                }
            }
            
            // 批量异步登录
            CompletableFuture<Map<String, Boolean>> future = 
                platformManager.batchLoginAsync(userId, request.getUniqueKeys());
            Map<String, Boolean> results = future.get();
            
            return Result.ok(Map.of(
                "results", results,
                "message", "批量登录操作完成"
            ));
            
        } catch (Exception e) {
            return Result.fail("批量登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个站点详情
     */
    @GetMapping("/sites/{uniqueKey}")
    public Result<Object> getSiteDetail(
            @PathVariable String uniqueKey,
            @RequestHeader("User-ID") String userId) {
        try {
            // 检查站点所有权
            if (!platformManager.checkSiteOwnership(userId, uniqueKey)) {
                return Result.fail("无权限访问此站点");
            }
            
            Site site = platformManager.getUserSite(userId, uniqueKey);
            if (site == null) {
                return Result.fail("站点不存在");
            }
            
            Map<String, Object> siteDetail = Map.of(
                "uniqueKey", site.getUniqueKey(),
                "username", site.getUsername(),
                "domain", site.getDomain(),
                "url", site.getUrl(),
                "status", site.getSiteStatus().name(),
                "statusDescription", site.getSiteStatus().getDescription(),
                "rebateRate", site.getRebateRate(),
                "siteType", site.getSiteType().name(),
                "lotteryType", site.getLotteryType().name(),
                "databaseId", site.getDatabaseId()
            );
            
            return Result.ok(siteDetail);
            
        } catch (Exception e) {
            return Result.fail("获取站点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查站点业务唯一性
     */
    @GetMapping("/sites/check")
    public Result<Object> checkSiteUniqueness(
            @RequestParam String url,
            @RequestParam String username,
            @RequestHeader("User-ID") String userId) {
        try {
            String domain = extractDomain(url);
            String domainAccount = domain + username;
            
            boolean canAdd = platformManager.canAddSite(userId, domainAccount);
            
            return Result.ok(Map.of(
                "canAdd", canAdd,
                "domainAccount", domainAccount,
                "message", canAdd ? "可以添加此站点" : "您已经添加过此站点账户"
            ));
            
        } catch (Exception e) {
            return Result.fail("检查站点失败: " + e.getMessage());
        }
    }
    
    /**
     * 从URL提取域名
     */
    private String extractDomain(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost();
        } catch (Exception e) {
            // 简单处理：直接返回URL
            return url.replaceAll("https?://", "").split("/")[0];
        }
    }
    
    /**
     * 站点创建请求DTO
     */
    public static class SiteCreateRequest {
        private String url;
        private String username;
        private String password;
        private Double rebateRate;
        private Integer lotteryType;
        private Integer clientType;
        private Integer clientRole;
        private Double multiplier;
        
        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public Double getRebateRate() { return rebateRate; }
        public void setRebateRate(Double rebateRate) { this.rebateRate = rebateRate; }
        
        public Integer getLotteryType() { return lotteryType; }
        public void setLotteryType(Integer lotteryType) { this.lotteryType = lotteryType; }
        
        public Integer getClientType() { return clientType; }
        public void setClientType(Integer clientType) { this.clientType = clientType; }
        
        public Integer getClientRole() { return clientRole; }
        public void setClientRole(Integer clientRole) { this.clientRole = clientRole; }
        
        public Double getMultiplier() { return multiplier; }
        public void setMultiplier(Double multiplier) { this.multiplier = multiplier; }
    }
    
    /**
     * 批量操作请求DTO
     */
    public static class BatchOperationRequest {
        private List<String> uniqueKeys;
        
        public List<String> getUniqueKeys() { return uniqueKeys; }
        public void setUniqueKeys(List<String> uniqueKeys) { this.uniqueKeys = uniqueKeys; }
    }
} 