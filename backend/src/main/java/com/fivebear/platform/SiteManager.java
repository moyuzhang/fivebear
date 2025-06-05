package com.fivebear.platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.json.JSONArray;

/**
 * 站点消息管理器
 */
public class SiteManager implements SiteCallback {
    private static final Logger logger = Logger.getLogger(SiteManager.class.getName());
    // 消息处理线程池
    private final ExecutorService executorService;
    // 站点对象映射
    private final Map<String, Site> siteInstances = new ConcurrentHashMap<>();
    private final SiteCallback mainCallback;

    // 默认线程池大小
    private static final int DEFAULT_POOL_SIZE = 10;

    // 无参构造，使用默认线程池大小
    public SiteManager(SiteCallback mainCallback) {
        this(DEFAULT_POOL_SIZE, mainCallback);
    }

    // 可指定线程池大小的构造方法
    public SiteManager(int poolSize, SiteCallback mainCallback) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.mainCallback = mainCallback;
    }

    /**
     * 注册站点
     */
    public void registerSite(Site site) {
        String key = site.getUniqueKey();
        site.setSiteCallback(this); // 自动设置回调为 SiteMessageManager
        siteInstances.put(key, site); // 自动保存Site对象
        logger.info("站点注册成功: " + key);
    }

    /**
     * 注销站点
     */
    public void unregisterSite(Site site) {
        String key = site.getUniqueKey();
        siteInstances.remove(key); // 自动移除Site对象
        site.setSiteCallback(null);
        logger.info("站点注销成功: " + key);
    }

    // 关闭管理器
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * 获取所有MemberSite
     */
    public List<MemberSite> getAllMemberSites() {
        List<MemberSite> result = new ArrayList<>();
        for (Site s : siteInstances.values()) {
            if (s instanceof MemberSite) {
                result.add((MemberSite) s);
            }
        }
        return result;
    }

    /**
     * 获取所有Site
     */
    public List<Site> getAllSites() {
        return new ArrayList<>(siteInstances.values());
    }

    /**
     * 获取所有AdminSite
     */
    public List<AdminSite> getAllAdminSites() {
        List<AdminSite> result = new ArrayList<>();
        for (Site s : siteInstances.values()) {
            if (s instanceof AdminSite) {
                result.add((AdminSite) s);
            }
        }
        return result;
    }

    /**
     * 根据key获取Site对象
     */
    public Site getSiteByKey(String key) {
        return siteInstances.get(key);
    }

    public void removeSite(Site site) {
        // 可按需实现
    }

    @Override
    public void onStatusChanged(Site site, SiteStatus newStatus) {
        if (mainCallback != null) {
            mainCallback.onStatusChanged(site, newStatus);
        }
        if (newStatus == SiteStatus.LOGGED_OUT) {
            String key = site.getUniqueKey();
            siteInstances.remove(key);
            logger.info("已自动移除退出登录的站点: " + key);
        }
    }

    @Override
    public void onMessage(Site site, String message, MessageType messageType) {
        if (mainCallback != null) {
            mainCallback.onMessage(site, message, messageType);
        }
    }

    @Override
    public void onPackageOddsInfo(Site site, List<PackageInfo> packageInfoList) {
        if (mainCallback != null) {
            mainCallback.onPackageOddsInfo(site, packageInfoList);
        }
    }

    @Override
    public void onRebateRateChanged(Site site, double newRate) {
        if (mainCallback != null) {
            mainCallback.onRebateRateChanged(site, newRate);
        }
    }

    /**
     * 根据domain查询MemberSite，并可选按MemberPurpose过滤
     */
    public List<MemberSite> getMemberSitesByDomain(String domain, MemberPurpose purpose) {
        List<MemberSite> result = new ArrayList<>();
        for (Site s : siteInstances.values()) {
            if (s instanceof MemberSite && domain.equals(s.getDomain())) {
                MemberSite ms = (MemberSite) s;
                if (purpose == null || ms.getPurpose() == purpose) {
                    result.add(ms);
                }
            }
        }
        return result;
    }

    @Override
    public void onNumOddsInfo(Site site, JSONArray oddsJsonArray) {
        if (mainCallback != null) {
            mainCallback.onNumOddsInfo(site, oddsJsonArray);
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}