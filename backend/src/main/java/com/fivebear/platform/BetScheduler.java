package com.fivebear.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 下注计划器：将批量下注任务分配到不同 Site 并异步执行
 */
public class BetScheduler {
    private final PlatformManager platformManager;
    private final SiteManager siteManager;
    private final OddsManager oddsManager;

    public BetScheduler(PlatformManager platformManager) {
        this.platformManager = platformManager;
        this.siteManager = platformManager.getSiteManager();
        this.oddsManager = platformManager.getOddsManager();
    }

    /**
     * 最优下注分配算法
     */
    public List<BetAllocation> allocateBets(List<BetTask> bets) {
        List<MemberSite> sites = new ArrayList<>();
        for (Site s : platformManager.getSiteManager().getAllSites()) {
            if (s instanceof MemberSite) {
                sites.add((MemberSite) s);
            }
        }
        List<BetAllocation> allocations = new ArrayList<>();
        Map<MemberSite, Map<String, Double>> siteNumberAmount = new HashMap<>(); // 站点-号码-已下注金额
        Map<MemberSite, Double> siteTotalAllocated = new HashMap<>(); // 站点已分配总金额

        for (BetTask bet : bets) {
            double remaining = bet.amount;
            while (remaining >= 1e-6) { // 防止浮点误差
                MemberSite bestSite = null;
                double bestOdds = -1;
                double canBet = 0;
                for (MemberSite site : sites) {
                    if (site.getSiteStatus() != SiteStatus.IDLE)
                        continue;
                    MemberInfo info = site.getMemberInfo();
                    MemberSettingInfo setting = getSettingForNumber(info, bet.number);
                    if (setting == null)
                        continue;
                    double minBet = setting.getMinBet();
                    double oneBetLimit = setting.getOneBetLimit();
                    double maxPerNumber = setting.getOneItemLimit();
                    double siteAvailable = info.getCreditBalance();
                    double alreadyNumber = siteNumberAmount.getOrDefault(site, new HashMap<>()).getOrDefault(bet.number,
                            0.0);
                    double alreadyTotal = siteTotalAllocated.getOrDefault(site, 0.0);
                    double odds = getOddsForSite(site, bet.number, bet.isPackage);
                    // 计算本次最多可分配金额
                    double maxAlloc = Math.min(
                            Math.min(remaining, maxPerNumber - alreadyNumber),
                            Math.min(oneBetLimit, siteAvailable - alreadyTotal));
                    // 向下取 minBet 的整数倍
                    double alloc = Math.floor(maxAlloc / minBet) * minBet;
                    if (alloc < minBet)
                        continue;
                    if (odds > bestOdds) {
                        bestOdds = odds;
                        bestSite = site;
                        canBet = alloc;
                    }
                }
                if (bestSite == null)
                    break; // 没有可用站点
                // 分配
                allocations.add(new BetAllocation(bestSite, bet.number, canBet, bestOdds));
                siteNumberAmount.computeIfAbsent(bestSite, k -> new HashMap<>())
                        .merge(bet.number, canBet, Double::sum);
                siteTotalAllocated.merge(bestSite, canBet, Double::sum);
                remaining -= canBet;
            }
        }
        return allocations;
    }

    /**
     * 获取站点该号码的当前赔率
     */
    private double getOddsForSite(MemberSite site, String number, boolean isPackage) {
        List<NumberOddsGroup> oddsGroups = platformManager.getOddsManager().queryByNumber(number);
        String domain = site.getDomain();
        for (NumberOddsGroup group : oddsGroups) {
            for (NumberOddsGroup.OddsInfo info : group.getOddsList()) {
                if (info.getDomain().equals(domain)) {
                    return info.getOdds();
                }
            }
        }
        return 0;
    }

    /**
     * 根据号码查找对应的 MemberSettingInfo（可根据实际业务调整）
     */
    private MemberSettingInfo getSettingForNumber(MemberInfo info, String number) {
        List<MemberSettingInfo> settings = info.getMemberSettingInfos();
        if (settings == null)
            return null;
        // 这里假设所有号码共用同一组设置，或可根据号码映射玩法ID后查找
        // 如有 dictNoTypeId，可用 dictNoTypeId 匹配
        return settings.isEmpty() ? null : settings.get(0);
    }

    /**
     * 下注任务
     */
    public static class BetTask {
        public final String number;
        public final double amount;
        public final boolean isPackage;

        public BetTask(String number, double amount, boolean isPackage) {
            this.number = number;
            this.amount = amount;
            this.isPackage = isPackage;
        }
    }

    /**
     * 分配结果
     */
    public static class BetAllocation {
        public final MemberSite site;
        public final String number;
        public final double amount;
        public final double odds;

        public BetAllocation(MemberSite site, String number, double amount, double odds) {
            this.site = site;
            this.number = number;
            this.amount = amount;
            this.odds = odds;
        }
    }

    List<BetScheduler.BetTask> generateTasks(Map<Integer, List<Bet>> input) {
        List<BetScheduler.BetTask> tasks = new ArrayList<>();
        for (Map.Entry<Integer, List<Bet>> entry : input.entrySet()) {
            int dictNoTypeId = entry.getKey();
            List<Bet> bets = entry.getValue();
            // 查询所有号码的赔率
            Set<Double> oddsSet = new HashSet<>();
            for (Bet bet : bets) {
                double odds = getOddsForNumber(dictNoTypeId, bet.getNumber());
                oddsSet.add(odds);
            }
            boolean isPackage = oddsSet.size() == 1;
            if (isPackage) {
                // 包牌：合并为一个任务
                for (Bet bet : bets) {
                    tasks.add(new BetScheduler.BetTask(bet.getNumber(), bet.getBetMoney(), true));
                }
            } else {
                // 散货：每个号码单独任务
                for (Bet bet : bets) {
                    tasks.add(new BetScheduler.BetTask(bet.getNumber(), bet.getBetMoney(), false));
                }
            }
        }
        return tasks;
    }

    private double getOddsForNumber(int dictNoTypeId, String number) {
        List<NumberOddsGroup> groups = oddsManager.queryByNumber(number);
        for (NumberOddsGroup group : groups) {
            if (group.getDictNoTypeId() == dictNoTypeId) {
                double maxOdds = 0;
                for (NumberOddsGroup.OddsInfo info : group.getOddsList()) {
                    if (info.getOdds() > maxOdds) {
                        maxOdds = info.getOdds();
                    }
                }
                return maxOdds;
            }
        }
        return 0;
    }

    /**
     * 分配结果结构体，包含已分配和未分配号码
     */
    public static class AllocationResult {
        public final List<SiteBets> siteBetsList;
        public final List<Bet> unassignedBets;

        public AllocationResult(List<SiteBets> siteBetsList, List<Bet> unassignedBets) {
            this.siteBetsList = siteBetsList;
            this.unassignedBets = unassignedBets;
        }
    }

    /**
     * 按最大赔率优先分配，支持多domain降级分配，并返回未分配号码。
     * 
     * @param input 玩法类型到号码组的映射（dictNoTypeId -> List<Bet>）
     * @return AllocationResult，包含分配结果和未分配号码
     */
    public AllocationResult allocateBetsByBestOdds(Map<Integer, List<Bet>> input) {
        // 归并分配结果
        Map<String, List<Bet>> keyToBets = new HashMap<>();
        Map<String, MemberSite> keyToSite = new HashMap<>();
        List<Bet> unassignedBets = new ArrayList<>();
        for (Map.Entry<Integer, List<Bet>> entry : input.entrySet()) {
            int dictNoTypeId = entry.getKey();
            for (Bet bet : entry.getValue()) {
                double remaining = bet.getBetMoney();
                // 1. 查找所有domain及其赔率，按赔率从高到低排序
                List<NumberOddsGroup> groups = oddsManager.queryByNumber(bet.getNumber());
                List<DomainOdds> domainOddsList = new ArrayList<>();
                for (NumberOddsGroup group : groups) {
                    if (group.getDictNoTypeId() == dictNoTypeId) {
                        for (NumberOddsGroup.OddsInfo info : group.getOddsList()) {
                            domainOddsList.add(new DomainOdds(info.getDomain(), info.getOdds()));
                        }
                    }
                }
                // 按赔率降序排序
                domainOddsList.sort((a, b) -> Double.compare(b.odds, a.odds));
                Set<String> visitedDomain = new HashSet<>();
                for (DomainOdds domainOdds : domainOddsList) {
                    String domain = domainOdds.domain;
                    if (!visitedDomain.add(domain)) continue; // 去重
                    // 获取该domain下所有可用账号，按余额降序排列
                    List<MemberSite> accounts = new ArrayList<>();
                    for (Site s : siteManager.getAllSites()) {
                        if (s instanceof MemberSite && s.getDomain().equals(domain)) {
                            SiteStatus status = s.getSiteStatus();
                            if (status == SiteStatus.IDLE || status == SiteStatus.LOGGED_IN) {
                                accounts.add((MemberSite) s);
                            }
                        }
                    }
                    accounts.sort((a, b) -> Double.compare(
                            b.getMemberInfo().getCreditBalance() - b.getMemberInfo().getCreditAssigned() - b.getMemberInfo().getLockAmount(),
                            a.getMemberInfo().getCreditBalance() - a.getMemberInfo().getCreditAssigned() - a.getMemberInfo().getLockAmount()));
                    // 1. 先找能一次性满足的账号
                    boolean assigned = false;
                    for (MemberSite site : accounts) {
                        double canAssign = site.getMemberInfo().getCreditBalance() - site.getMemberInfo().getCreditAssigned() - site.getMemberInfo().getLockAmount();
                        if (canAssign >= remaining) {
                            // 直接全部分配
                            double assignAmount = Math.min(remaining, canAssign);
                            assignAmount = Math.floor(assignAmount * 10) / 10.0;
                            if (assignAmount < 1e-6) continue;
                            Bet splitBet = new Bet(bet.getNumber(), assignAmount, bet.getDictNoTypeId());
                            String key = site.getUniqueKey();
                            keyToBets.computeIfAbsent(key, k -> new ArrayList<>()).add(splitBet);
                            keyToSite.putIfAbsent(key, site);
                            synchronized (site.getMemberInfo()) {
                                site.getMemberInfo().setCreditAssigned(site.getMemberInfo().getCreditAssigned() + assignAmount);
                            }
                            remaining -= assignAmount;
                            assigned = true;
                            break;
                        }
                    }
                    if (assigned) continue; // 该号码已分配完，进入下一个号码

                    // 2. 没有账号能一次性满足，才分割
                    double minUnit = 1.0;
                    for (MemberSite site : accounts) {
                        double canAssign = site.getMemberInfo().getCreditBalance() - site.getMemberInfo().getCreditAssigned() - site.getMemberInfo().getLockAmount();
                        if (canAssign < minUnit) continue;
                        double assignAmount = Math.min(remaining, canAssign);
                        // 金额只允许一位小数
                        assignAmount = Math.floor(assignAmount * 10) / 10.0;
                        if (assignAmount < minUnit) continue;
                        // 防止出现两位及以上小数
                        String assignStr = String.valueOf(assignAmount);
                        int dotIdx = assignStr.indexOf('.');
                        if (dotIdx != -1 && assignStr.length() - dotIdx - 1 > 1) {
                            assignAmount = Double.parseDouble(assignStr.substring(0, dotIdx + 2));
                        }
                        Bet splitBet = new Bet(bet.getNumber(), assignAmount, bet.getDictNoTypeId());
                        String key = site.getUniqueKey();
                        keyToBets.computeIfAbsent(key, k -> new ArrayList<>()).add(splitBet);
                        keyToSite.putIfAbsent(key, site);
                        synchronized (site.getMemberInfo()) {
                            site.getMemberInfo().setCreditAssigned(site.getMemberInfo().getCreditAssigned() + assignAmount);
                        }
                        remaining -= assignAmount;
                        if (remaining < minUnit) break;
                    }
                }
                if (remaining > 1e-6) {
                    // 还有未分配
                    unassignedBets.add(new Bet(bet.getNumber(), remaining, bet.getDictNoTypeId()));
                }
            }
        }
        // 组装结果
        List<SiteBets> result = new ArrayList<>();
        for (Map.Entry<String, List<Bet>> entry : keyToBets.entrySet()) {
            result.add(new SiteBets(keyToSite.get(entry.getKey()), entry.getValue()));
        }
        return new AllocationResult(result, unassignedBets);
    }

    /**
     * domain及其赔率结构体
     */
    private static class DomainOdds {
        String domain;
        double odds;

        DomainOdds(String domain, double odds) {
            this.domain = domain;
            this.odds = odds;
        }
    }

    /**
     * 站点与其分配的投注列表结构
     */
    public static class SiteBets {
        private final MemberSite site;
        private final List<Bet> bets;

        public SiteBets(MemberSite site, List<Bet> bets) {
            this.site = site;
            this.bets = bets;
        }

        public MemberSite getSite() {
            return site;
        }

        public List<Bet> getBets() {
            return bets;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            double totalAmount = bets.stream().mapToDouble(b -> b.getBetMoney()).sum();
            double creditBalance = site.getMemberInfo().getCreditBalance();
            sb.append("站点: ").append(site.getUniqueKey())
                    .append("（domain: ").append(site.getDomain()).append("）")
                    .append(" 分配号码数: ").append(bets.size())
                    .append("，总金额: ").append(totalAmount)
                    .append("，当前额度: ").append(creditBalance)
                    .append("，已分配额度: ").append(site.getMemberInfo().getCreditAssigned())
                    .append("，锁定金额: ").append(site.getMemberInfo().getLockAmount())
                    .append("\n");
            for (int i = 0; i < Math.min(10, bets.size()); i++) {
                Bet bet = bets.get(i);
                sb.append("  号码: ").append(bet.getNumber()).append(" 金额: ").append(bet.getBetMoney()).append("\n");
            }
            if (bets.size() > 10) {
                sb.append("  ... 共 ").append(bets.size()).append(" 注\n");
            }
            return sb.toString();
        }
    }
}