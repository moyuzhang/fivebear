package com.fivebear.platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlatformManager implements SiteCallback {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PlatformManager.class.getName());
    // 统一管理的两个核心管理器
    private final PackageInfoManager packageInfoManager = new PackageInfoManager();
    private final OddsManager oddsManager = new OddsManager();
    private final SiteManager siteManager;

    // 监听器列表
    private final java.util.List<SiteCallback> externalCallbacks = new java.util.concurrent.CopyOnWriteArrayList<>();
    
    // 数据库服务（需要注入）
    private ExternalSiteService externalSiteService;
    
    // 用户隔离索引
    private final Map<String, Set<String>> userSitesMap = new ConcurrentHashMap<>();      // userId -> globalKeys
    private final Map<String, String> globalKeyToUserMap = new ConcurrentHashMap<>();    // globalKey -> userId
    private final Map<String, String> uniqueKeyToGlobalKeyMap = new ConcurrentHashMap<>(); // userId_uniqueKey -> globalKey
    
    // 操作管理
    private final ExecutorService operationExecutor = Executors.newFixedThreadPool(10);
    private final Map<String, CompletableFuture<Boolean>> pendingOperations = new ConcurrentHashMap<>();

    // 构造方法设为 public
    public PlatformManager() {
        this.siteManager = new SiteManager(this);
        // 其它初始化...
    }

    // 设置数据库服务
    public void setExternalSiteService(ExternalSiteService externalSiteService) {
        this.externalSiteService = externalSiteService;
    }
    
    // 站点相关委托
    public void registerSite(Site site) {
        // 原有的注册逻辑
        siteManager.registerSite(site);
        
        // 新增的用户隔离索引
        String globalKey = site.getGlobalKey();
        String userId = site.getUserId();
        String uniqueKey = site.getUniqueKey();
        
        // 更新索引映射
        userSitesMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(globalKey);
        globalKeyToUserMap.put(globalKey, userId);
        uniqueKeyToGlobalKeyMap.put(userId + "_" + uniqueKey, globalKey);
    }

    public void unregisterSite(Site site) {
        // 清理索引
        String globalKey = site.getGlobalKey();
        String userId = site.getUserId();
        String uniqueKey = site.getUniqueKey();
        
        // 从索引中移除
        Set<String> userSites = userSitesMap.get(userId);
        if (userSites != null) {
            userSites.remove(globalKey);
            if (userSites.isEmpty()) {
                userSitesMap.remove(userId);
            }
        }
        globalKeyToUserMap.remove(globalKey);
        uniqueKeyToGlobalKeyMap.remove(userId + "_" + uniqueKey);
        
        // 原有的注销逻辑
        siteManager.unregisterSite(site);
    }
    
    /**
     * 检查用户是否可以添加指定类型的站点
     */
    public boolean canUserAddSiteType(String userId, ClientType clientType) {
        // 这里需要根据用户角色来判断权限
        // 暂时返回true，实际实现需要查询用户角色
        return true; // TODO: 实现权限检查逻辑
    }
    
    /**
     * 检查站点业务唯一性（用户范围内）
     */
    public boolean canAddSite(String userId, String domainAccount) {
        if (externalSiteService != null) {
            return !externalSiteService.existsByUserIdAndDomainAccount(userId, domainAccount);
        }
        // 如果没有数据库服务，检查内存
        return !uniqueKeyToGlobalKeyMap.containsKey(userId + "_" + domainAccount);
    }
    
    /**
     * 添加新站点（异步）
     */
    public CompletableFuture<Site> addSiteAsync(ExternalSiteService.SiteConfig config, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 业务唯一性检测
                if (!canAddSite(userId, config.getDomainAccount())) {
                    throw new RuntimeException("您已经添加过此站点账户");
                }
                
                // 2. 权限检测 (根据clientType转换为ClientType枚举)
                ClientType clientType = ClientType.values()[config.getClientType() - 1]; // 假设从1开始
                if (!canUserAddSiteType(userId, clientType)) {
                    throw new RuntimeException("您没有权限添加此类型的站点");
                }
                
                // 3. 创建站点实例
                Site site = createSiteFromConfig(config, userId);
                
                // 4. 保存到数据库
                if (externalSiteService != null) {
                    Long databaseId = externalSiteService.saveSite(site);
                    site.setDatabaseId(databaseId);
                }
                
                // 5. 注册到管理器
                registerSite(site);
                
                // 6. 推送站点列表更新
                broadcastUserSiteList(userId);
                
                return site;
            } catch (Exception e) {
                throw new RuntimeException("添加站点失败: " + e.getMessage(), e);
            }
        }, operationExecutor);
    }
    
    /**
     * 用户登录站点（异步）
     */
    public CompletableFuture<Boolean> loginSiteAsync(String userId, String uniqueKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Site site = getUserSite(userId, uniqueKey);
                if (site == null) {
                    throw new RuntimeException("站点不存在或无权限访问");
                }
                
                boolean result = site.login();
                
                // 更新数据库状态
                if (result && site.getDatabaseId() != null && externalSiteService != null) {
                    externalSiteService.updateSiteStatus(
                        site.getDatabaseId(), 
                        site.getSiteStatus().ordinal(), 
                        userId, 
                        "LOGIN"
                    );
                }
                
                return result;
            } catch (Exception e) {
                onMessage(null, "登录失败: " + e.getMessage(), MessageType.SYSTEM_ERROR);
                return false;
            }
        }, operationExecutor);
    }
    
    /**
     * 用户登出站点（异步）
     */
    public CompletableFuture<Boolean> logoutSiteAsync(String userId, String uniqueKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Site site = getUserSite(userId, uniqueKey);
                if (site == null) {
                    throw new RuntimeException("站点不存在或无权限访问");
                }
                
                site.logout();
                
                // 更新数据库状态
                if (site.getDatabaseId() != null && externalSiteService != null) {
                    externalSiteService.updateSiteStatus(
                        site.getDatabaseId(), 
                        site.getSiteStatus().ordinal(), 
                        userId, 
                        "LOGOUT"
                    );
                }
                
                return true;
            } catch (Exception e) {
                onMessage(null, "登出失败: " + e.getMessage(), MessageType.SYSTEM_ERROR);
                return false;
            }
        }, operationExecutor);
    }
    
    /**
     * 删除站点
     */
    public boolean removeSite(String userId, String uniqueKey) {
        Site site = getUserSite(userId, uniqueKey);
        if (site == null) {
            return false;
        }
        
        // 先登出
        if (site.getSiteStatus() == SiteStatus.LOGGED_IN) {
            site.logout();
        }
        
        // 从数据库删除
        if (site.getDatabaseId() != null && externalSiteService != null) {
            externalSiteService.deleteSite(site.getDatabaseId(), userId);
        }
        
        // 从内存移除
        unregisterSite(site);
        
        // 推送站点列表更新
        broadcastUserSiteList(userId);
        
        return true;
    }
    
    /**
     * 获取用户的站点
     */
    public Site getUserSite(String userId, String uniqueKey) {
        String globalKey = uniqueKeyToGlobalKeyMap.get(userId + "_" + uniqueKey);
        if (globalKey == null) {
            return null;
        }
        return siteManager.getSiteByKey(globalKey);
    }
    
    /**
     * 获取用户的所有站点
     */
    public List<Site> getUserSites(String userId) {
        Set<String> globalKeys = userSitesMap.get(userId);
        if (globalKeys == null) {
            return new ArrayList<>();
        }
        
        return globalKeys.stream()
            .map(globalKey -> siteManager.getSiteByKey(globalKey))
            .filter(site -> site != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 检查站点所有权
     */
    public boolean checkSiteOwnership(String userId, String uniqueKey) {
        String globalKey = uniqueKeyToGlobalKeyMap.get(userId + "_" + uniqueKey);
        return globalKey != null && userId.equals(globalKeyToUserMap.get(globalKey));
    }
    
    /**
     * 从数据库配置创建站点实例
     */
    private Site createSiteFromConfig(ExternalSiteService.SiteConfig config, String userId) {
        SiteType siteType = SiteType.FENGHUANG; // 默认凤凰，根据需要调整
        LotteryType lotteryType = LotteryType.values()[config.getLotteryType() - 1];
        ClientType clientType = ClientType.values()[config.getClientType() - 1];
        
        Site site;
        // 根据clientType创建不同类型的站点
        if (clientType == ClientType.ADMIN) {
            site = new FengHuangAdmin(
                config.getUsername(),
                config.getPassword(),
                config.getUrl(),
                lotteryType,
                config.getRebateRate(),
                userId
            );
        } else {
            site = new FengHuangMember(
                config.getUsername(),
                config.getPassword(),
                config.getUrl(),
                lotteryType,
                config.getRebateRate(),
                userId
            );
        }
        
        return site;
    }
    
    /**
     * 使用反射设置Site的userId（临时解决方案）
     */
    @SuppressWarnings("unused")
    private void setUserId(Site site, String userId) {
        try {
            java.lang.reflect.Field userIdField = Site.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(site, userId);
        } catch (Exception e) {
            logger.warning("Failed to set userId: " + e.getMessage());
        }
    }
    
    // WebSocket处理器（可选依赖）
    private PlatformWebSocketHandler webSocketHandler;
    
    /**
     * 设置WebSocket处理器（可选）
     */
    public void setWebSocketHandler(PlatformWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    
    /**
     * 推送用户站点列表（WebSocket）
     */
    private void broadcastUserSiteList(String userId) {
        if (webSocketHandler != null) {
            // 通过WebSocket实时推送站点摘要
            webSocketHandler.sendUserSitesSummary(userId);
        }
    }
    
    /**
     * 批量登录站点（异步）
     */
    public CompletableFuture<Map<String, Boolean>> batchLoginAsync(String userId, List<String> uniqueKeys) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Boolean> results = new HashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (String uniqueKey : uniqueKeys) {
                CompletableFuture<Void> future = loginSiteAsync(userId, uniqueKey)
                    .thenAccept(result -> results.put(uniqueKey, result));
                futures.add(future);
            }
            
            // 等待所有登录操作完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return results;
        }, operationExecutor);
    }
    
    /**
     * 批量登出站点（异步）
     */
    public CompletableFuture<Map<String, Boolean>> batchLogoutAsync(String userId, List<String> uniqueKeys) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Boolean> results = new HashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (String uniqueKey : uniqueKeys) {
                CompletableFuture<Void> future = logoutSiteAsync(userId, uniqueKey)
                    .thenAccept(result -> results.put(uniqueKey, result));
                futures.add(future);
            }
            
            // 等待所有登出操作完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return results;
        }, operationExecutor);
    }
    
    /**
     * 从数据库加载用户的所有站点
     */
    public CompletableFuture<Void> loadUserSitesFromDatabase(String userId) {
        return CompletableFuture.runAsync(() -> {
            if (externalSiteService == null) {
                logger.warning("ExternalSiteService is not configured, cannot load sites from database");
                return;
            }
            
            try {
                List<ExternalSiteService.SiteConfig> siteConfigs = externalSiteService.loadSitesByUserId(userId);
                
                for (ExternalSiteService.SiteConfig config : siteConfigs) {
                    try {
                        Site site = createSiteFromConfig(config, userId);
                        site.setDatabaseId(config.getId());
                        registerSite(site);
                        
                        logger.info(String.format("Loaded site %s for user %s", 
                            config.getDomainAccount(), userId));
                    } catch (Exception e) {
                        logger.warning(String.format("Failed to load site %s for user %s: %s", 
                            config.getDomainAccount(), userId, e.getMessage()));
                    }
                }
                
                // 推送站点列表更新
                broadcastUserSiteList(userId);
                
            } catch (Exception e) {
                logger.severe("Failed to load sites from database for user " + userId + ": " + e.getMessage());
            }
        }, operationExecutor);
    }
    
    /**
     * 获取用户站点的状态摘要
     */
    public Map<String, Object> getUserSitesSummary(String userId) {
        List<Site> userSites = getUserSites(userId);
        Map<String, Object> summary = new HashMap<>();
        
        // 统计各种状态的站点数量
        Map<SiteStatus, Integer> statusCounts = new HashMap<>();
        for (SiteStatus status : SiteStatus.values()) {
            statusCounts.put(status, 0);
        }
        
        List<Map<String, Object>> siteDetails = new ArrayList<>();
        for (Site site : userSites) {
            // 更新状态统计
            SiteStatus status = site.getSiteStatus();
            statusCounts.put(status, statusCounts.get(status) + 1);
            
            // 添加站点详情
            Map<String, Object> siteDetail = new HashMap<>();
            siteDetail.put("uniqueKey", site.getUniqueKey());
            siteDetail.put("username", site.getUsername());
            siteDetail.put("domain", site.getDomain());
            siteDetail.put("status", status.name());
            siteDetail.put("statusDescription", status.getDescription());
            siteDetail.put("rebateRate", site.getRebateRate());
            siteDetail.put("siteType", site.getSiteType().name());
            siteDetail.put("lotteryType", site.getLotteryType().name());
            siteDetail.put("databaseId", site.getDatabaseId());
            siteDetails.add(siteDetail);
        }
        
        summary.put("totalCount", userSites.size());
        summary.put("statusCounts", statusCounts);
        summary.put("sites", siteDetails);
        summary.put("userId", userId);
        summary.put("timestamp", System.currentTimeMillis());
        
        return summary;
    }
    
    // 包牌赔率相关
    public void addPackageInfo(PackageInfo pkg) {
        packageInfoManager.addPackage(pkg);
    }

    public List<PackageInfo> getPackagesByDomain(String domain) {
        return packageInfoManager.getPackagesByDomain(domain);
    }

    // 号码赔率相关
    public void addOdds(String number, int dictNoTypeId, String domain, double odds, String updateDatetime) {
        oddsManager.addOdds(number, dictNoTypeId, domain, odds, updateDatetime);
    }

    public List<NumberOddsGroup> getNumberOdds(String number) {
        return oddsManager.queryByNumber(number);
    }
    // ...可扩展更多赔率相关方法...

    // 获取管理器本身（如需更细粒度操作）
    public SiteManager getSiteManager() {
        return siteManager;
    }

    public OddsManager getOddsManager() {
        return oddsManager;
    }

    public PackageInfoManager getPackageInfoManager() {
        return packageInfoManager;
    }

    public void shutdown() {
        // 清理用户隔离相关资源
        operationExecutor.shutdown();
        try {
            if (!operationExecutor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                operationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            operationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 清理索引映射
        userSitesMap.clear();
        globalKeyToUserMap.clear();
        uniqueKeyToGlobalKeyMap.clear();
        pendingOperations.clear();
        
        // 清理原有资源
        siteManager.shutdown();
        oddsManager.clear();
        packageInfoManager.clear();
        // 如果 PackageInfoManager、OddsManager 有 shutdown 方法，也一并调用
        // packageInfoManager.shutdown();
        // oddsManager.shutdown();
    }

    // 注册外部回调
    public void addSiteCallbackListener(SiteCallback callback) {
        externalCallbacks.add(callback);
    }

    public void removeSiteCallbackListener(SiteCallback callback) {
        externalCallbacks.remove(callback);
    }

    // 统一回调处理
    @Override
    public void onMessage(Site site, String message, MessageType messageType) {
        // 1. PlatformManager 内部处理
        // ...你的业务逻辑...
        System.out.println("PlatformManager收到消息: " + message);

        // 2. 通知所有外部监听器
        for (SiteCallback cb : externalCallbacks) {
            cb.onMessage(site, message, messageType);
        }
    }

    @Override
    public void onPackageOddsInfo(Site site, List<PackageInfo> packageInfoList) {
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.getDictNoTypeId() == 11) {
                packageInfo.setOddsMemberFinal(packageInfo.getOddsMemberFinal() + (site.getRebateRate() * 10000));
            } else if (packageInfo.getDictNoTypeId() >= 7 && packageInfo.getDictNoTypeId() <= 11) {
                packageInfo.setOddsMemberFinal(packageInfo.getOddsMemberFinal() + (site.getRebateRate() * 1000));
            } else if (packageInfo.getDictNoTypeId() >= 1 && packageInfo.getDictNoTypeId() <= 6) {
                packageInfo.setOddsMemberFinal(packageInfo.getOddsMemberFinal() + (site.getRebateRate() * 100));
            }
            addPackageInfo(packageInfo);
        }
        for (SiteCallback cb : externalCallbacks) {
            cb.onPackageOddsInfo(site, packageInfoList);
        }
    }

    @Override
    public void onRebateRateChanged(Site site, double newRate) {
        for (SiteCallback cb : externalCallbacks) {
            cb.onRebateRateChanged(site, newRate);
        }
    }

    @Override
    public void onStatusChanged(Site site, SiteStatus newStatus) {
        if (newStatus == SiteStatus.LOGGED_OUT) {
            System.out.println("PlatformManager收到站点登录状态: " + newStatus);
            // 查询Domain 是否存在
            String domain = site.getDomain();
            if (domain != null) {
                // 查询Domain 是否存在
                if (siteManager.getMemberSitesByDomain(domain, MemberPurpose.BETTING).isEmpty()) {
                    // 删除赔率
                    oddsManager.removeDomain(domain);
                    // 删除包牌
                    packageInfoManager.removeDomain(domain);
                }
            }
        }
        for (SiteCallback cb : externalCallbacks) {
            cb.onStatusChanged(site, newStatus);
        }
    }

    @Override
    public void onNumOddsInfo(Site site, JSONArray oddsJsonArray) {
        for (SiteCallback cb : externalCallbacks) {
            cb.onNumOddsInfo(site, oddsJsonArray);
        }
        String domain = site.getDomain();
        double rebateRate = site.getRebateRate();
        // 处理号码赔率信息
        for (int i = 0; i < oddsJsonArray.length(); i++) {
            JSONObject oddsJson = oddsJsonArray.getJSONObject(i);
            String number = oddsJson.getString("number");
            double odds = oddsJson.getDouble("odds");
            int dictNoTypeId = oddsJson.getInt("dict_no_type_id");
            double rebateFactor;
            if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
                rebateFactor = 100;
            } else if (dictNoTypeId >= 7 && dictNoTypeId < 11) {
                rebateFactor = 1000;
            } else if (dictNoTypeId == 11) {
                rebateFactor = 10000;
            } else {
                rebateFactor = 0;
            }
            odds += rebateRate * rebateFactor;
            String updateDatetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            addOdds(number, dictNoTypeId, domain, odds, updateDatetime);
        }
    }

    public String syncAllOddsForSite(Site site) {
        if (site instanceof MemberSite) {
            MemberSite memberSite = (MemberSite) site;

            // Submit tasks to thread pool
            siteManager.getExecutorService().submit(() -> {
                // Sync odds for types 1-6
                for (int dictNoTypeId = 1; dictNoTypeId <= 6; dictNoTypeId++) {
                    try {
                        memberSite.GetCollectSummary(dictNoTypeId);
                    } catch (Exception e) {
                        System.err.println("同步赔率失败: site=" + site.getDomain() + ", dictNoTypeId=" + dictNoTypeId
                                + ", err=" + e.getMessage());
                    }
                }

                // Submit bet collection tasks
                List<Map<String, Object>> bets = NumberTemplateGenerator.buildCollectBatchBetsData(Arrays.asList(11),
                        1);
                memberSite.collectBatchBets(bets);

                List<Map<String, Object>> bets1 = NumberTemplateGenerator
                        .buildCollectBatchBetsData(Arrays.asList(6, 7, 8, 9, 10), 1);
                memberSite.collectBatchBets(bets1);

                // Sync package info
                memberSite.syncAllPackageInfo();
                System.out.println("同步赔率完成: " + site.getDomain());
                // 推送任务完成消息
                onMessage(site, "同步所有号码赔率完成", MessageType.SYSTEM_INFO);
            });
        }
        return null;
    }

    /**
     * 处理投注号码的方案
     * 
     * @param bets 投注号码列表
     * @return 处理后的投注号码列表（如过滤、分组、风控等）
     *         逻辑：
     *         - 对于有1个X（三星）的号码，计算其展开为10个四星号码后的平均中奖金额，
     *         如果大于原三星中奖金额，则转换为四星，否则保留为三星。
     *         - 其它号码可按原有逻辑处理。
     */
    public List<Bet> processBetScheme(List<Bet> bets) {
        List<Bet> result = new ArrayList<>();
        // 输出总金额
        System.out.println(String.format("开始处理投注方案,总金额: %.2f  注数: %d",
                bets.stream().mapToDouble(b -> b.getBetMoney()).sum(), bets.size()));
        for (Bet bet : bets) {
            int typeId = bet.getDictNoTypeId();
            if (typeId >= 7 && typeId <= 10) { // 三星
                String number = bet.getNumber();
                double betMoney = bet.getBetMoney();
                // 获取三星最大赔率
                NumberOddsGroup.OddsInfo odds3Info = oddsManager.getMaxOddsInfo(number, typeId);
                double odds3 = odds3Info != null ? odds3Info.getOdds() : bet.getOdds();
                // 展开为10个四星号码
                List<String> fourStarNums = NumberTemplateGenerator.getRelatedFourStarNumber(number);
                List<Double> odds4List = new ArrayList<>();
                for (String num : fourStarNums) {
                    NumberOddsGroup.OddsInfo odds4Info = oddsManager.getMaxOddsInfo(num, 11);
                    odds4List.add(odds4Info != null ? odds4Info.getOdds() : bet.getOdds());
                }
                // 计算四星平均中奖金额
                double avgOdds4 = odds4List.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double avgFourStarWin = (betMoney / 10.0) * avgOdds4;
                // 三星中奖金额
                double threeStarWin = betMoney * odds3;
                if (avgFourStarWin > threeStarWin) {
                    // 转换为10个四星投注
                    for (String num : fourStarNums) {
                        Bet fourStarBet = new Bet(num, betMoney / 10.0, 11);
                        NumberOddsGroup.OddsInfo odds4Info = oddsManager.getMaxOddsInfo(num, 11);
                        fourStarBet.setOdds(odds4Info != null ? odds4Info.getOdds() : bet.getOdds());
                        // System.out.println(String.format("原号码%s 金额: %.2f 赔率: %.2f 转换为四星投注: %s 金额:
                        // %.2f 赔率: %.2f", number, betMoney, odds3, num, betMoney / 10.0, odds4Info !=
                        // null ? odds4Info.getOdds() : bet.getOdds()));
                        result.add(fourStarBet);
                    }
                } else {
                    // 保留为三星投注
                    result.add(bet);
                }
            } else if (typeId >= 1 && typeId <= 6) { // 二星
                // 直接散货
                result.add(bet);
            } else if (typeId == 11) { // 四星
                // 直接四星投注
                result.add(bet);
            } else {
                // 其它类型按原有逻辑处理
                result.add(bet);
            }
        }
        System.err.println("合并前注数:" + result.size());
        // 合并号码
        Map<String, Double> numberToMoney = new HashMap<>();
        for (Bet bet : result) {
            numberToMoney.merge(bet.getNumber(), bet.getBetMoney(), Double::sum);
        }
        result.clear();
        for (Map.Entry<String, Double> entry : numberToMoney.entrySet()) {
            result.add(new Bet(entry.getKey(), entry.getValue(), 11));
        }

        // 获取包牌方案
        System.out.println(String.format("处理后总金额: %.2f  注数: %d",
                result.stream().mapToDouble(b -> b.getBetMoney()).sum(), result.size()));
        return result;
    }

}