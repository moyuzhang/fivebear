package com.fivebear.platform;

/**
 * 管理端站点基类
 */
public abstract class AdminSite extends Site {

    private AccountConfig accountConfig = new AccountConfig();

    public AdminSite(String username, String password, String url, SiteType siteType, LotteryType lotteryType, double rebateRate, String userId) {
        super(username, password, url, siteType, lotteryType, ClientType.ADMIN, rebateRate, userId);
    }

    public AccountConfig getAccountConfig() {
        return accountConfig;
    }

    @Override
    public abstract boolean login();

    @Override
    public abstract void logout();

    @Override
    public abstract boolean heartbeat();

    @Override
    public abstract String testConnection();

    @Override
    public abstract boolean retriveMemberGetAccount();
    // 可扩展管理端专属通用方法
}