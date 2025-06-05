package com.fivebear.platform;
import java.util.List;
import java.util.Map;

/**
 * 会员端站点基类
 */
public abstract class MemberSite extends Site {
    protected MemberInfo memberInfo = new MemberInfo();
    protected MemberPurpose purpose = MemberPurpose.BETTING;

    public MemberSite(String username, String password, String url, SiteType siteType, LotteryType lotteryType,
            double rebateRate, String userId) {
        super(username, password, url, siteType, lotteryType, ClientType.MEMBER, rebateRate, userId);
    }

    public MemberInfo getMemberInfo() {
        return memberInfo;
    }

    @Override
    public abstract boolean login();

    @Override
    public abstract void logout();

    @Override
    public abstract boolean heartbeat();

    @Override
    public abstract String testConnection();

    // 获取会员账号
    @Override
    public abstract boolean retriveMemberGetAccount();

    // 获取小票
    public abstract String getMemberPrint(int pageIndex);

    // 获取赔率
    public abstract boolean retriveMemberGetOdds(List<Map<String, Object>> bets);

    // 清除小票
    public abstract boolean clearMemberPrint();

    // 预下注
    public abstract boolean collectBatchBets(List<Map<String, Object>> bets);

    /**
     * 批量正式下注
     * 
     * @param bets      下注列表，每个元素包含 dict_no_type_id、bet_no 和 bet_money
     * @param isPackage 是否包牌（0/1）
     * @param way       下注方式（如103/104），默认103
     * @return 响应JSON字符串
     */
    public abstract String batchBet(List<Map<String, Object>> bets, String isPackage, String way);

    // 清除预下注
    public abstract boolean clearCollectBetPrint();

    // 获取所有预下注小票
    public abstract List<String> getCollectMemberPrint();

    /**
     * 整单退码（撤销整个订单）
     * 
     * @param serialNo   订单流水号
     * @param totalCount 注数
     * @return 响应JSON字符串
     */
    public abstract String cancelOrder(String serialNo, int totalCount);

    /**
     * 批量退码（撤销下注）
     * 
     * @param betList 下注列表，每个元素包含 bet_id 和 BetCount
     * @return 响应JSON字符串
     */
    public abstract String cancelMemberBet(List<Map<String, Object>> betList);

    /**
     * 获取会员汇总
     * 
     * @param dictNoTypeId 彩种类型ID
     * @return 响应JSON字符串
     */
    public abstract String GetCollectSummary(int dictNoTypeId);

    public MemberPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(MemberPurpose purpose) {
        this.purpose = purpose;
    }

    // 删除预下注
    public abstract boolean removeCollectBetsByType(int dictNoTypeId);

    // 同步全部赔率
    public abstract boolean syncAllPackageInfo();
    // 可扩展会员端专属通用方法
}