package com.fivebear.platform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BetNumberGroupByType {
    private int dictNoTypeId;
    private List<BetNumber> betNumbers;
    private BigDecimal totalBetMoney = BigDecimal.ZERO;

    // 统计字段
    private int positive;
    private int negative;
    private int both;
    private BigDecimal flyTotal = BigDecimal.ZERO;
    private BigDecimal leaveTotal = BigDecimal.ZERO;

    public BetNumberGroupByType(int dictNoTypeId, List<BetNumber> betNumbers) {
        this.dictNoTypeId = dictNoTypeId;
        this.betNumbers = betNumbers;
        recalculateTotalBetMoney();
    }

    public void recalculateTotalBetMoney() {
        this.totalBetMoney = betNumbers.stream().map(b -> b.getBetMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
        // 统一设置每个 BetNumber 的 profit 字段
        for (BetNumber bet : betNumbers) {
            bet.setProfit(totalBetMoney.subtract(bet.getBonus()));
        }
    }

    /**
     * 统计正值、负值、均衡、飞货、吃货等，并返回所有号码的统计明细和总计（不修改原始数据）
     * @param multiple 补飞倍数（如5.0表示五成）
     * @param oddsManager 用于获取最大出货赔率
     * @return BetNumberNoTypeData 玩法下所有号码的统计明细和总计
     */
    public BetNumberNoTypeData riskControlAnalysis(int multiple, OddsManager oddsManager) {
        BetNumberNoTypeData noTypeData = new BetNumberNoTypeData(dictNoTypeId);
        noTypeData.setMultiple(multiple);
        for (BetNumber bet : betNumbers) {
            BigDecimal profit = bet.getProfit();
            BigDecimal sellOdds = bet.getOdds();
            if (oddsManager != null) {
                NumberOddsGroup.OddsInfo maxOddsInfo = oddsManager.getMaxOddsInfo(bet.getBetNo(), bet.getDictNoTypeId());
                if (maxOddsInfo != null) sellOdds = BigDecimal.valueOf(maxOddsInfo.getOdds());
            }
            BigDecimal realityMaxLose = profit.subtract(totalBetMoney).abs().subtract(totalBetMoney.multiply(BigDecimal.valueOf(multiple).divide(BigDecimal.TEN)));
            BigDecimal flyMoney = realityMaxLose.compareTo(BigDecimal.ZERO) > 0 ? realityMaxLose.divide(sellOdds, 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
            BigDecimal keepMoney = bet.getBetMoney().subtract(flyMoney);
            BetNumberResults result = new BetNumberResults(
                bet.getBetNo(),
                bet.getBetMoney(),
                profit,
                bet.getOdds(),
                flyMoney,
                keepMoney,
                BigDecimal.ZERO,
                sellOdds
            );
            noTypeData.addResult(result);
        }
        return noTypeData;
    }
    /**
     * 赔率补偿分析
     * 分析每个号码的赔率补偿情况，计算出货盈亏
     * @param oddsManager 赔率管理器，用于获取最大出货赔率
     * @return 返回该玩法下所有号码的统计明细和总计
     */
    public BetNumberNoTypeData oddsCompensationAnalysis(OddsManager oddsManager)
    {
        // 创建该玩法的统计数据对象
        BetNumberNoTypeData noTypeData = new BetNumberNoTypeData(dictNoTypeId);
        
        // 遍历该玩法下的所有投注号码
        for (BetNumber bet : betNumbers) {
            // 获取当前号码的最大出货赔率
            BigDecimal sellOdds = bet.getOdds();
            if (oddsManager != null) {
                NumberOddsGroup.OddsInfo maxOddsInfo = oddsManager.getMaxOddsInfo(bet.getBetNo(), bet.getDictNoTypeId());
                if (maxOddsInfo != null) {
                    sellOdds = BigDecimal.valueOf(maxOddsInfo.getOdds());
                }
            }
            
            // 计算出货注数 = 入货奖金/出货赔率
            BigDecimal flyMoney = bet.getBonus().divide(sellOdds, 10, BigDecimal.ROUND_HALF_UP);
            
            // 计算出货盈亏 = 出货注数 * 出货赔率 - 出货注数
            BigDecimal sellProfit = flyMoney.multiply(sellOdds).subtract(flyMoney);

            BigDecimal keepMoney = bet.getBetMoney().subtract(flyMoney);
            
            // 创建单个号码的统计结果
            BetNumberResults result = new BetNumberResults(
                bet.getBetNo(),      // 号码
                bet.getBetMoney(),   // 投注金额
                bet.getProfit(),     // 盈亏
                bet.getOdds(),       // 投注赔率
                flyMoney,            // 出货注数
                keepMoney,   // 剩余组数(保留本金)
                BigDecimal.ZERO,                   // 盈利
                sellOdds             // 出货赔率
            );
            
            // 设置出货盈亏
            result.setSellProfit(sellProfit);
            
            // 添加到统计数据中
            noTypeData.addResult(result);
        }
        
        return noTypeData;
    }

    /**
     * 贪心补飞优化算法
     * 
     * 算法思路：
     * 1. 首先使用赔率补偿法计算每个号码的初始亏损和飞货金额
     * 2. 统计所有正值号码的总盈利作为可分配的套利空间
     * 3. 将所有号码按亏损金额从大到小排序
     * 4. 从亏损最大的号码开始，依次进行补飞：
     *    - 计算每个亏损号码需要的补飞金额
     *    - 从套利空间中分配资金进行补飞
     *    - 更新剩余套利空间
     * 5. 补飞原则：
     *    - 优先补亏损最大的号码
     *    - 每个号码的补飞金额不超过其亏损金额
     *    - 补飞金额不超过剩余套利空间
     *    - 补飞后的飞货金额不超过投注金额
     * 
     * 优化目标：
     * - 最大化减少亏损号码的亏损金额
     * - 在套利空间有限的情况下，优先处理亏损较大的号码
     * - 保证每个号码的补飞金额合理，避免过度补飞
     */
    public BetNumberNoTypeData greedyFlyMoneyOptimize(OddsManager oddsManager) {
        // 1. 先用赔率补偿法算出每个号码的亏损和初始flyMoney，并统一获取sellOdds
        List<BetNumberResults> baseResults = new java.util.ArrayList<>();
        for (BetNumber bet : betNumbers) {
            BigDecimal sellOdds = bet.getOdds();
            if (oddsManager != null) {
                NumberOddsGroup.OddsInfo maxOddsInfo = oddsManager.getMaxOddsInfo(bet.getBetNo(), bet.getDictNoTypeId());
                if (maxOddsInfo != null) {
                    sellOdds = BigDecimal.valueOf(maxOddsInfo.getOdds());
                }
            }
            BigDecimal flyMoney = bet.getBonus().divide(sellOdds, 10, BigDecimal.ROUND_HALF_UP);
            BigDecimal keepMoney = bet.getBetMoney().subtract(flyMoney);
            BetNumberResults result = new BetNumberResults(
                bet.getBetNo(),
                bet.getBetMoney(),
                bet.getProfit(),
                bet.getOdds(),
                flyMoney,
                keepMoney,
                BigDecimal.ZERO,
                sellOdds
            );
            baseResults.add(result);
        }

        // 2. 统计所有正值号码的总盈利作为可分配套利空间
        BigDecimal totalProfit = BigDecimal.ZERO;
        for (BetNumberResults r : baseResults) {
            if (r.getWin().compareTo(BigDecimal.ZERO) > 0) totalProfit = totalProfit.add(r.getWin());
        }

        // 3. 按亏损从大到小排序
        baseResults.sort((a, b) -> a.getWin().compareTo(b.getWin()));

        // 4. 贪心补飞
        for (int i = 0; i < baseResults.size(); i++) {
            BetNumberResults r = baseResults.get(i);
            if (r.getWin().compareTo(BigDecimal.ZERO) < 0 && totalProfit.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal need = r.getWin().abs();
                BigDecimal supply = need.min(totalProfit);
                BigDecimal sellOdds = r.getOdds();
                BigDecimal deltaFly = supply.divide(sellOdds.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);
                BigDecimal newFlyMoney = r.getFlyMoney().add(deltaFly);
                BigDecimal newKeepMoney = r.getBetMoney().subtract(newFlyMoney);
                // 重新构造新的 BetNumberResults，sellOdds 保持一致
                BetNumberResults newR = new BetNumberResults(
                    r.getBetNo(),
                    r.getBetMoney(),
                    r.getProfit(),
                    r.getOdds(),
                    newFlyMoney,
                    newKeepMoney,
                    BigDecimal.ZERO,
                    sellOdds
                );
                baseResults.set(i, newR);
                totalProfit = totalProfit.subtract(supply);
            }
        }

        // 5. 封装结果
        BetNumberNoTypeData noTypeData = new BetNumberNoTypeData(dictNoTypeId);
        for (BetNumberResults r : baseResults) {
            noTypeData.addResult(r);
        }
        return noTypeData;
    }

    /**
     * 按比例分配套利空间的优化方法
     * 与贪心算法不同，该方法按照每个负值号码的亏损比例来分配套利空间
     * 这样可以保证亏损越大的号码获得越多的补飞金额
     * 
     * @param oddsManager 赔率管理器，用于获取当前赔率信息
     * @return 优化后的投注数据
     */
    public BetNumberNoTypeData proportionalFlyMoneyOptimize(OddsManager oddsManager) {
        // 1. 获取基础数据：使用赔率补偿法计算初始的flyMoney
        BetNumberNoTypeData baseData = this.oddsCompensationAnalysis(oddsManager);
        List<BetNumberResults> results = new java.util.ArrayList<>(baseData.getResultsList());

        // 2. 统计套利空间
        // totalProfit: 所有正值号码的总盈利，作为可分配的套利空间
        // totalLoss: 所有负值号码的总亏损，用于计算分配比例
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        for (BetNumberResults r : results) {
            if (r.getWin().compareTo(BigDecimal.ZERO) > 0) totalProfit = totalProfit.add(r.getWin());
            if (r.getWin().compareTo(BigDecimal.ZERO) < 0) totalLoss = totalLoss.subtract(r.getWin());
        }

        // 3. 按比例分配套利空间
        for (int i = 0; i < results.size(); i++) {
            BetNumberResults r = results.get(i);
            // 只处理负值号码，且确保有可分配的套利空间
            if (r.getWin().compareTo(BigDecimal.ZERO) < 0 && totalProfit.compareTo(BigDecimal.ZERO) > 0 && totalLoss.compareTo(BigDecimal.ZERO) > 0) {
                // 计算该号码应获得的套利金额：总套利空间 * (该号码亏损/总亏损)
                BigDecimal supply = totalProfit.multiply(r.getWin().abs().divide(totalLoss.abs(), 10, BigDecimal.ROUND_HALF_UP));
                // 计算需要增加的飞货金额：套利金额/(出货赔率-1)
                BigDecimal sellOdds = r.getOdds();
                BigDecimal deltaFly = supply.divide(sellOdds.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);
                // 更新飞货金额和保留金额
                BigDecimal newFlyMoney = r.getFlyMoney().add(deltaFly);
                BigDecimal newKeepMoney = r.getBetMoney().subtract(newFlyMoney);
                
                // 创建新的投注结果对象
                BetNumberResults newR = new BetNumberResults(
                    r.getBetNo(),      // 号码
                    r.getBetMoney(),   // 投注金额
                    r.getProfit(),     // 负值
                    r.getOdds(),       // 投注赔率
                    newFlyMoney,       // 新的飞货金额
                    newKeepMoney,      // 新的保留金额
                    BigDecimal.ZERO,                 // 盈利初始化为0，后续由BetNumberNoTypeData计算
                    sellOdds             // 出货赔率
                );
                results.set(i, newR);
            }
        }

        // 4. 封装结果
        BetNumberNoTypeData noTypeData = new BetNumberNoTypeData(dictNoTypeId);
        for (BetNumberResults r : results) {
            noTypeData.addResult(r);
        }
        return noTypeData;
    }


    public int getDictNoTypeId() {
        return dictNoTypeId;
    }

    public List<BetNumber> getBetNumbers() {
        return betNumbers;
    }

    public BigDecimal getTotalBetMoney() {
        return totalBetMoney;
    }

    public int getPositive() {
        return positive;
    }

    public int getNegative() {
        return negative;
    }

    public int getBoth() {
        return both;
    }

    public BigDecimal getFlyTotal() {
        return flyTotal;
    }

    public BigDecimal getLeaveTotal() {
        return leaveTotal;
    }
} 