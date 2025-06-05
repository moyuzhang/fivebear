package com.fivebear.platform;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PackageAnalyzer {
    /**
     * 计算最大包牌组数 = 总金额 / 最大单注金额，向下取整
     * 
     * @param bets 投注列表
     * @return 最大包牌组数
     */
    public static int getMaxPackageGroupCount(List<Bet> bets) {
        if (bets == null || bets.isEmpty()) {
            return 0;
        }
        double maxBetMoney = 0;
        double totalBetMoney = 0;
        for (Bet bet : bets) {
            if (bet.getBetMoney() > maxBetMoney) {
                maxBetMoney = bet.getBetMoney();
            }
            totalBetMoney += bet.getBetMoney();
        }
        if (maxBetMoney == 0)
            return 0;
        return (int) (totalBetMoney / maxBetMoney);
    }

    /**
     * 计算一批投注在所有包牌等级下最大可包牌组数对应的PackageInfo
     * 只统计唯一号码，避免重复号码影响包牌组数。
     * 
     * @param bets          投注列表
     * @param packageLevels 包牌等级列表
     * @return 最大可包牌组数对应的PackageInfo（如无则返回null）
     */
    public static PackageInfo getMaxPackageGroupCount(List<Bet> bets, List<PackageInfo> packageLevels) {
        if (bets == null || bets.isEmpty() || packageLevels == null || packageLevels.isEmpty()) {
            return null;
        }
        // 1. 对 packageLevels 排序（大到小）
        packageLevels.sort((a, b) -> Integer.compare(b.getPackageCount(), a.getPackageCount()));
        // 只统计唯一号码
        Set<String> uniqueNumbers = new HashSet<>();
        for (Bet bet : bets) {
            uniqueNumbers.add(bet.getNumber());
        }
        int uniqueCount = uniqueNumbers.size();
        PackageInfo bestLevel = null;
        double maxBetMoney = 0;
        double totalBetMoney = 0;
        for (Bet bet : bets) {
            if (bet.getBetMoney() > maxBetMoney) {
                maxBetMoney = bet.getBetMoney();
            }
            totalBetMoney += bet.getBetMoney();
        }
        int maxGroup = (int) (totalBetMoney / maxBetMoney);
        for (PackageInfo pkg : packageLevels) {
            double threshold = pkg.getPackageCount();
            if (threshold > 0) {
                if (maxGroup > threshold) {
                    bestLevel = pkg;
                }
            }
        }
        return bestLevel;
    }

    /**
     * 按包牌等级依次分割投注列表，输出所有包牌组和所有散货（支持多轮分组）
     * 
     * @param bets               投注列表
     * @param packageLevels      包牌等级列表
     * @param allowSmallBet      是否允许角票（金额<1）参与包牌分组
     * @param maxSingleBetAmount 单注最高金额限制
     * @return PackageGroupResult，包含所有包牌组和所有未分组的散货
     */
    public static PackageGroupResult splitToPackageGroups(
            List<Bet> bets,
            List<PackageInfo> packageLevels,
            boolean allowSmallBet,
            double maxSingleBetAmount) {
        List<PackageGroup> allGroups = new ArrayList<>();
        List<Bet> remainBets = new ArrayList<>(bets);
        BigDecimal lostAmount = BigDecimal.ZERO;
        if (bets == null || bets.isEmpty() || packageLevels == null || packageLevels.isEmpty()) {
            return new PackageGroupResult(allGroups, bets == null ? new ArrayList<>() : new ArrayList<>(bets), 0.0);
        }
        while (true) {
            SplitOneResult result = splitOnePackageGroup(remainBets, packageLevels, allowSmallBet, maxSingleBetAmount);
            if (result.group != null) {
                allGroups.add(result.group);
                remainBets = result.remainBets;
            } else {
                break;
            }
        }
        return new PackageGroupResult(allGroups, remainBets, lostAmount.doubleValue());
    }

    /**
     * 尝试从投注列表中分出一组包牌，返回分组和剩余投注
     */
    private static SplitOneResult splitOnePackageGroup(
            List<Bet> bets,
            List<PackageInfo> packageLevels,
            boolean allowSmallBet,
            double maxSingleBetAmount) {
        if (bets == null || bets.isEmpty() || packageLevels == null || packageLevels.isEmpty()) {
            return new SplitOneResult(null, bets == null ? new ArrayList<>() : new ArrayList<>(bets));
        }
        List<Bet> validBets = bets;
        if (!allowSmallBet) {
            validBets = bets.stream().filter(b -> BigDecimal.valueOf(b.getBetMoney()).compareTo(BigDecimal.ONE) >= 0).collect(java.util.stream.Collectors.toList());
        }
        List<Bet> packageBets = new ArrayList<>();
        for (Bet bet : validBets) {
            BigDecimal useAmount = BigDecimal.valueOf(bet.getBetMoney()).min(BigDecimal.valueOf(maxSingleBetAmount));
            if (useAmount.compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                packageBets.add(new Bet(bet.getNumber(), useAmount.doubleValue(), bet.getDictNoTypeId()));
            }
        }
        List<PackageInfo> sortedLevels = new ArrayList<>(packageLevels);
        sortedLevels.sort((a, b) -> Integer.compare(b.getPackageCount(), a.getPackageCount()));
        PackageInfo bestPkg = null;
        List<Bet> bestGroup = null;
        BigDecimal targetAmount = BigDecimal.ZERO;
        for (PackageInfo pkg : sortedLevels) {
            int level = pkg.getPackageCount();
            List<BigDecimal> uniqueAmounts = packageBets.stream()
                .map(b -> BigDecimal.valueOf(b.getBetMoney()))
                .distinct()
                .sorted((a, b) -> b.compareTo(a))
                .collect(java.util.stream.Collectors.toList());
            for (BigDecimal ta : uniqueAmounts) {
                List<Bet> group = packageBets.stream()
                    .map(b -> new Bet(b.getNumber(), b.getBetMoney() < ta.doubleValue() ? b.getBetMoney() : ta.doubleValue(), b.getDictNoTypeId()))
                    .collect(java.util.stream.Collectors.toList());
                BigDecimal sumAmount = group.stream().map(b -> BigDecimal.valueOf(b.getBetMoney())).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (group.size() >= level && sumAmount.divide(ta, 10, BigDecimal.ROUND_HALF_UP).compareTo(BigDecimal.valueOf(level)) >= 0) {
                    bestPkg = pkg;
                    bestGroup = group;
                    targetAmount = ta;
                    break;
                }
            }
            if (bestGroup != null) break;
        }
        if (bestGroup != null && bestPkg != null) {
            java.util.Map<String, BigDecimal> usedMap = new java.util.HashMap<>();
            for (Bet bet : bestGroup) {
                String key = bet.getNumber() + "#" + bet.getDictNoTypeId();
                usedMap.put(key, BigDecimal.valueOf(bet.getBetMoney()).min(targetAmount));
            }
            List<Bet> remain = new ArrayList<>();
            for (Bet bet : bets) {
                String key = bet.getNumber() + "#" + bet.getDictNoTypeId();
                BigDecimal used = usedMap.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal remainAmount = BigDecimal.valueOf(bet.getBetMoney()).subtract(used);
                if (remainAmount.compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                    remain.add(new Bet(bet.getNumber(), remainAmount.doubleValue(), bet.getDictNoTypeId()));
                }
            }
            return new SplitOneResult(new PackageGroup(bestGroup, bestPkg), remain);
        } else {
            return new SplitOneResult(null, bets);
        }
    }

    /**
     * 辅助类：分出一组包牌的结果
     */
    private static class SplitOneResult {
        public final PackageGroup group;
        public final List<Bet> remainBets;
        public SplitOneResult(PackageGroup group, List<Bet> remainBets) {
            this.group = group;
            this.remainBets = remainBets;
        }
    }

    // 检查包牌条件（金额模式）
    private static boolean checkPackage(List<Bet> group, double tryAmount, List<PackageInfo> packageLevels) {
        double sum = group.stream().mapToDouble(Bet::getBetMoney).sum();
        int groupCount = group.size();
        for (PackageInfo pkg : packageLevels) {
            if (groupCount >= pkg.getPackageCount() && sum / tryAmount >= pkg.getPackageCount()) {
                return true;
            }
        }
        return false;
    }

    // 查找对应包牌档位
    private static PackageInfo findPackageLevel(int groupCount, List<PackageInfo> packageLevels) {
        for (PackageInfo pkg : packageLevels) {
            if (groupCount >= pkg.getPackageCount()) {
                return pkg;
            }
        }
        return null;
    }

    // Overload for backward compatibility
    public static PackageGroupResult splitToPackageGroups(List<Bet> bets, List<PackageInfo> packageLevels,
            boolean allowSmallBet) {
        return splitToPackageGroups(bets, packageLevels, allowSmallBet, 50.0);
    }

    /**
     * 包牌分组结果
     */
    public static class PackageGroup {
        public final List<Bet> bets;
        public final PackageInfo packageInfo;

        public PackageGroup(List<Bet> bets, PackageInfo packageInfo) {
            this.bets = bets;
            this.packageInfo = packageInfo;
            for (Bet bet : bets) {
                bet.setOdds(packageInfo.getOddsSetting());
                bet.setPositive(true);  
            }
        }

        public List<Bet> getBets() 
        {
            return bets;
        }

        public PackageInfo getPackageInfo() {
            return packageInfo;
        }

        // 总金额
        public double getTotalBetMoney() {
            return bets.stream().mapToDouble(Bet::getBetMoney).sum();
        }

        // 总注数
        public int getTotalBetCount() {
            return bets.size();
        }

        // 总包数
        public int getTotalPackageCount() {
            return packageInfo.getPackageCount();
        }

        // 最小金额
        public double getMinBetMoney() {
            if (bets == null || bets.isEmpty()) {
                return 0.0;
            }
            return bets.stream().mapToDouble(Bet::getBetMoney).min().orElse(0.0);
        }

        // 最大金额
        public double getMaxBetMoney() {
            if (bets == null || bets.isEmpty()) {
                return 0.0;
            }
            return bets.stream().mapToDouble(Bet::getBetMoney).max().orElse(0.0);
        }

        // 单注注数
        public int getSingleBetCount() {
            return bets.size();
        }

        @Override
        public String toString() {
            return getPackageInfoString();
        }

        String getPackageInfoString() {
            // 取bets前5个号码和后5个号码，输出号码 金额
            List<Bet> first5 = bets.subList(0, 5);
            List<Bet> last5 = bets.subList(bets.size() - 5, bets.size());
            // 前面5个信息
            String first5Info = first5.stream().map(Bet::toString).collect(Collectors.joining("\n"));
            // 后面5个信息
            String last5Info = last5.stream().map(Bet::toString).collect(Collectors.joining("\n"));
            // 包牌等级
            String packageInfoString = String.format("包牌等级：%s，总金额：%f，总注数：%d，总包数：%d，最小金额：%f，最大金额：%f 检验：%s",
                    packageInfo.getPackageName(), getTotalBetMoney(), getTotalBetCount(), getTotalPackageCount(),
                    getMinBetMoney(), getMaxBetMoney(),(int)(getTotalBetMoney() / getMaxBetMoney()));

            // 返回
            return String.format("%s\n 前5个：\n%s\n 后5个：\n%s", packageInfoString, first5Info, last5Info);
        }
    }

    /**
     * 分组整体结果，包含包牌等级、所有包牌组和未分组的散货
     */
    public static class PackageGroupResult {
        public final List<PackageGroup> groups;
        public final List<Bet> ungroupedBets;
        private final double lostAmount; // 新增：未分配金额

        public PackageGroupResult(List<PackageGroup> groups, List<Bet> ungroupedBets, double lostAmount) {
            this.groups = groups;
            this.ungroupedBets = ungroupedBets;
            this.lostAmount = lostAmount;
        }

        public List<PackageGroup> getGroups() {
            return groups;
        }

        public String getPackageGroupString() {
            return groups.stream().map(PackageGroup::toString).collect(Collectors.joining("\n"));
        }

        public String getUngroupedBetsString() {
            return ungroupedBets.stream().map(Bet::toString).collect(Collectors.joining("\n"));
        }

        public List<Bet> getUngroupedBets() {
            return ungroupedBets;
        }

        public double getTotalAmount() {
            double totalAmount = 0;
            for (PackageGroup group : groups) {
                totalAmount += group.getTotalBetMoney();
            }
            for (Bet bet : ungroupedBets) {
                totalAmount += bet.getBetMoney();
            }
            return totalAmount;
        }

        // 新增：添加散货，若已存在则金额相加
        public void addUngroupedBet(Bet bet) {
            for (Bet ungrouped : this.ungroupedBets) {
                if (ungrouped.getNumber().equals(bet.getNumber())
                        && ungrouped.getDictNoTypeId() == bet.getDictNoTypeId()) {
                    ungrouped.setBetMoney(ungrouped.getBetMoney() + bet.getBetMoney());
                    return;
                }
            }
            this.ungroupedBets.add(bet);
        }

        public double getLostAmount() {
            return lostAmount;
        }
    }
}