package com.fivebear.platform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.json.JSONArray;

/**
 * 站点抽象类，封装了站点的通用属性和操作。
 */
public abstract class Site {
    private static final Logger logger = Logger.getLogger(Site.class.getName());

    /** 用户名 */
    protected final String username;
    /** 密码 */
    protected final String password;
    /** 站点URL */
    protected String url;
    /** 期号 */
    protected String periodNo;
    /** 站点类型(如:凤凰) */
    protected final SiteType siteType;
    /** 彩票类型(如:排列五) */
    protected final LotteryType lotteryType;
    /** 客户端类型(如:管理) */
    protected final ClientType clientType;
    /** 主域名 */
    protected final String domain;
    /** 返点率 */
    protected volatile double rebateRate;
    /** 站点状态 */
    protected volatile SiteStatus siteStatus = SiteStatus.NOT_LOGGED_IN;
    /** 站点回调 */
    protected volatile SiteCallback siteCallback;

    // 线程安全相关
    private final ReentrantReadWriteLock statusLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock rebateLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock callbackLock = new ReentrantReadWriteLock();
    private final java.util.concurrent.locks.ReentrantReadWriteLock urlLock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    protected final HttpClientUtil httpClientUtil = new HttpClientUtil();

    /** 线路列表 */
    protected final List<String> lineUrls = new ArrayList<>();

    // 新增字段
    private final String userId;        // 创建者用户ID（外键）
    private Long databaseId;           // 数据库ID（可选）

    /**
     * 构造方法，初始化站点对象
     */
    public Site(String username, String password, String url, SiteType siteType,
            LotteryType lotteryType, ClientType clientType, double rebateRate,
            String userId) {
        this.username = username;
        this.password = password;
        String fullUrl = url.startsWith("http://") || url.startsWith("https://") ? url : "http://" + url;
        if (!fullUrl.endsWith("/")) {
            fullUrl = fullUrl + "/";
        }
        if (!isValidUrl(fullUrl)) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
        this.url = fullUrl;
        this.siteType = siteType;
        this.lotteryType = lotteryType;
        this.clientType = clientType;
        this.domain = extractDomain(fullUrl);
        this.rebateRate = rebateRate;
        if (lineUrls.isEmpty()) {
            List<String> lines = SiteUtil.generateLineGroup(fullUrl);
            this.lineUrls.addAll(lines);
        }
        // this.httpClientUtil.setProxy("127.0.0.1", 8888, null, null);
        this.userId = userId;
    }

    /** 登录方法，需由子类实现 */
    public abstract boolean login();

    /** 退出方法，需由子类实现 */
    public abstract void logout();

    /** 心跳检测方法，需由子类实现 */
    public abstract boolean heartbeat();

    /**
     * 测试线路/连接方法，返回最快的线路URL，需由子类实现
     */
    public abstract String testConnection();

    /**
     * 获取会员账号信息
     */
    public abstract boolean retriveMemberGetAccount();

    /**
     * 获取站点状态
     */
    public SiteStatus getSiteStatus() {
        statusLock.readLock().lock();
        try {
            return siteStatus;
        } finally {
            statusLock.readLock().unlock();
        }
    }

    /**
     * 设置站点状态
     */
    protected void setSiteStatus(SiteStatus newStatus) {
        statusLock.writeLock().lock();
        try {
            if (this.siteStatus != newStatus) {
                logger.info(String.format("Site %s status changed from %s to %s",
                        getUniqueKey(), this.siteStatus, newStatus));

                // 生成状态变化消息
                String message = generateStatusChangeMessage(this.siteStatus, newStatus);

                this.siteStatus = newStatus;

                // 通知回调
                callbackLock.readLock().lock();
                try {
                    if (siteCallback != null) {
                        // 状态变化回调
                        siteCallback.onStatusChanged(this, newStatus);
                        // 消息推送回调
                        siteCallback.onMessage(this, message, MessageType.STATUS_CHANGE);

                        // 特殊状态的额外消息
                        if (newStatus == SiteStatus.LOGGED_IN) {
                            siteCallback.onMessage(this, "登录成功", MessageType.LOGIN_SUCCESS);
                        } else if (newStatus == SiteStatus.ERROR) {
                            siteCallback.onMessage(this, "发生错误", MessageType.SYSTEM_ERROR);
                        } else if (newStatus == SiteStatus.NETWORK_ERROR) {
                            siteCallback.onMessage(this, "网络连接异常", MessageType.NETWORK_ERROR);
                        }
                    }
                } finally {
                    callbackLock.readLock().unlock();
                }
            }
        } finally {
            statusLock.writeLock().unlock();
        }
    }

    /**
     * 生成状态变化消息
     */
    private String generateStatusChangeMessage(SiteStatus oldStatus, SiteStatus newStatus) {
        return String.format("站点 %s 状态从 [%s] 变更为 [%s]",
                getUniqueKey(),
                oldStatus.getDescription(),
                newStatus.getDescription());
    }

    /**
     * 发送消息
     */
    protected void sendMessage(String message, MessageType messageType) {
        callbackLock.readLock().lock();
        try {
            if (siteCallback != null) {
                siteCallback.onMessage(this, message, messageType);
            }
        } finally {
            callbackLock.readLock().unlock();
        }
    }

    /**
     * 获取返点率
     */
    public double getRebateRate() {
        rebateLock.readLock().lock();
        try {
            return rebateRate;
        } finally {
            rebateLock.readLock().unlock();
        }
    }

    /**
     * 设置返点率
     */
    public void setRebateRate(double newRate) {
        rebateLock.writeLock().lock();
        try {
            if (this.rebateRate != newRate) {
                logger.info(String.format("Site %s rebate rate changed from %.2f to %.2f",
                        getUniqueKey(), this.rebateRate, newRate));
                this.rebateRate = newRate;

                // 通知回调
                callbackLock.readLock().lock();
                try {
                    if (siteCallback != null) {
                        siteCallback.onRebateRateChanged(this, newRate);
                        // 发送返点率变化消息
                        String message = String.format("站点 %s 返点率从 %.2f%% 调整为 %.2f%%",
                                getUniqueKey(), this.rebateRate, newRate);
                        siteCallback.onMessage(this, message, MessageType.DATA_UPDATE);
                    }
                } finally {
                    callbackLock.readLock().unlock();
                }
            }
        } finally {
            rebateLock.writeLock().unlock();
        }
    }

    /**
     * 设置回调接口
     */
    public void setSiteCallback(SiteCallback callback) {
        callbackLock.writeLock().lock();
        try {
            this.siteCallback = callback;
        } finally {
            callbackLock.writeLock().unlock();
        }
    }

    /** 获取用户名 */
    public String getUsername() {
        return username;
    }

    /** 获取密码 */
    public String getPassword() {
        return password;
    }

    /** 获取站点URL */
    public String getUrl() {
        urlLock.readLock().lock();
        try {
            return this.url;
        } finally {
            urlLock.readLock().unlock();
        }
    }

    /** 获取站点类型 */
    public SiteType getSiteType() {
        return siteType;
    }

    /** 获取彩票类型 */
    public LotteryType getLotteryType() {
        return lotteryType;
    }

    /** 获取客户端类型 */
    public ClientType getClientType() {
        return clientType;
    }

    /** 获取主域名 */
    public String getDomain() {
        return domain;
    }

    /** 获取业务唯一标识（用户范围内唯一，对应domain_account字段） */
    public String getUniqueKey() {
        return domain + username;  // 对应数据库的domain_account字段
    }

    /** 获取全局唯一标识（系统范围内唯一） */
    public String getGlobalKey() {
        return userId + "_" + getUniqueKey();
    }

    /** 获取用户ID */
    public String getUserId() {
        return userId;
    }

    /** 获取数据库ID */
    public Long getDatabaseId() {
        return databaseId;
    }

    /** 设置数据库ID */
    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    /** 返回站点信息字符串 */
    @Override
    public String toString() {
        return String.format("<Site %s (%s, %s, %s)>", url, siteType, lotteryType, clientType);
    }

    /** 校验URL格式是否合法 */
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 提取主域名 */
    private String extractDomain(String url) {
        try {
            java.net.URL netUrl = new java.net.URL(url);
            String host = netUrl.getHost();
            String[] parts = host.split("\\.");
            return (parts.length >= 2) ? parts[parts.length - 2] : host;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取线路列表
     */
    public List<String> getLineUrls() {
        return new ArrayList<>(lineUrls);
    }

    /**
     * 设置线路列表（去重）
     */
    public void setLineUrls(List<String> urls) {
        synchronized (lineUrls) {
            lineUrls.clear();
            if (urls != null) {
                for (String url : urls) {
                    if (url != null && !url.isEmpty() && !lineUrls.contains(url)) {
                        lineUrls.add(url);
                    }
                }
            }
        }
    }

    /**
     * 添加单条线路（去重）
     */
    public void addLineUrl(String url) {
        if (url != null && !url.isEmpty()) {
            synchronized (lineUrls) {
                if (!lineUrls.contains(url)) {
                    lineUrls.add(url);
                }
            }
        }
    }

    public void setUrl(String url) {
        urlLock.writeLock().lock();
        try {
            this.url = url;
        } finally {
            urlLock.writeLock().unlock();
        }
    }

    // 解析JSON响应
    protected static org.json.JSONObject parseJsonResponse(String string) {
        try {
            return new org.json.JSONObject(string);
        } catch (org.json.JSONException e) {
            return null;
        }
    }

    // 通用请求方法，支持自动处理 cookie，支持 GET/POST
    public String sendRequest(String url, String method, String body, Map<String, String> headers) throws IOException {
        // 默认超时 5000ms
        return sendRequest(url, method, body, headers, 5000);
    }

    public String sendRequest(String url, String method, String body, Map<String, String> headers, int timeout)
            throws IOException {
        try {
            HttpClientUtil client = this.httpClientUtil;
            if ("GET".equalsIgnoreCase(method)) {
                return client.doGet(url, headers, timeout).getContent();
            } else if ("POST".equalsIgnoreCase(method)) {
                return client.doPostJson(url, body, headers, timeout);
            }
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        } catch (Exception e) {
            throw new IOException("Failed to send request: " + e.getMessage(), e);
        }
    }

    public static class LineTestResult {
        public String url;
        public boolean ok;
        public long latency;

        public LineTestResult(String url, boolean ok, long latency) {
            this.url = url;
            this.ok = ok;
            this.latency = latency;
        }
    }

    private final ReentrantReadWriteLock periodNoLock = new ReentrantReadWriteLock();

    public String getPeriodNo() {
        periodNoLock.readLock().lock();
        try {
            return periodNo;
        } finally {
            periodNoLock.readLock().unlock();
        }
    }

    public void setPeriodNo(String periodNo) {
        periodNoLock.writeLock().lock();
        try {
            this.periodNo = periodNo;
        } finally {
            periodNoLock.writeLock().unlock();
        }
    }

    protected void pushPackageOddsInfo(List<PackageInfo> packageInfoList) {
        callbackLock.readLock().lock();
        try {
            if (siteCallback != null) {
                siteCallback.onPackageOddsInfo(this, packageInfoList);
            }
        } finally {
            callbackLock.readLock().unlock();
        }
    }

    protected void pushNumOddsInfo(JSONArray oddsJsonArray) {
        callbackLock.readLock().lock();
        try {
            if (siteCallback != null) {
                siteCallback.onNumOddsInfo(this, oddsJsonArray);
            }
        } finally {
            callbackLock.readLock().unlock();
        }
    }
}