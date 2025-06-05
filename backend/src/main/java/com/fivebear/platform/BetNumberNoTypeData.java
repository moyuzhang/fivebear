package com.fivebear.platform;

import java.math.BigDecimal;
import java.util.List;

/**
 * 某玩法下所有号码的投注明细及统计数据
 */
public class BetNumberNoTypeData {
    /** 玩法ID */
    private int dictNoTypeId;
    /** 该玩法下所有号码的投注明细 */
    private List<BetNumberResults> resultsList;
    /** 总投注金额 */
    private BigDecimal totalBetMoney = BigDecimal.ZERO;
    /** 总飞货金额 */
    private BigDecimal totalFlyMoney = BigDecimal.ZERO;
    /** 总保留金额 */
    private BigDecimal totalKeepMoney = BigDecimal.ZERO;
    /** 负值（亏损）号码个数 */
    private int negativeCount;
    /** 正值（盈利）号码个数 */
    private int positiveCount;
    /** 成数*/
    private int multiple;

    /**
     * 构造方法，自动统计汇总字段
     * @param dictNoTypeId 玩法ID
     * @param resultsList 该玩法下所有号码的投注明细
     */
    public BetNumberNoTypeData(int dictNoTypeId, List<BetNumberResults> resultsList) {
        this.dictNoTypeId = dictNoTypeId;
        this.resultsList = resultsList;
        // 统计汇总
        this.totalBetMoney = resultsList.stream().map(BetNumberResults::getBetMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalFlyMoney = resultsList.stream().map(BetNumberResults::getFlyMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalKeepMoney = resultsList.stream().map(BetNumberResults::getKeepMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.negativeCount = (int) resultsList.stream().filter(r -> r.getProfit().compareTo(BigDecimal.ZERO) < 0).count();
        this.positiveCount = (int) resultsList.stream().filter(r -> r.getProfit().compareTo(BigDecimal.ZERO) > 0).count();
        // 设置每个号码的出货盈亏
        updateSellProfitForAll();
    }

    public BetNumberNoTypeData(int dictNoTypeId)
    {
        this.dictNoTypeId = dictNoTypeId;
    }

    /** 获取玩法ID */
    public int getDictNoTypeId() { return dictNoTypeId; }
    /** 获取投注明细列表 */
    public List<BetNumberResults> getResultsList() { return resultsList; }
    /** 获取总投注金额 */
    public BigDecimal getTotalBetMoney() { return totalBetMoney; }
    /** 获取总飞货金额 */
    public BigDecimal getTotalFlyMoney() { return totalFlyMoney; }
    /** 获取总保留金额 */
    public BigDecimal getTotalKeepMoney() { return totalKeepMoney; }
    /** 获取负值号码个数 */
    public int getNegativeCount() { return negativeCount; }
    /** 获取正值号码个数 */
    public int getPositiveCount() { return positiveCount; }

    /**设置倍数*/
    public void setMultiple(int multiple)
    {
        this.multiple = multiple;
    }
    /**获取倍数 */
    public int getMultiple() {
        return multiple;
    }

    /**
     * 添加一个投注明细，并自动更新统计字段（保证betNo唯一）
     * @param result 单个号码投注明细
     */
    public void addResult(BetNumberResults result) {
        if (this.resultsList == null) {
            this.resultsList = new java.util.ArrayList<>();
        }
        // 检查是否已存在相同betNo
        boolean exists = this.resultsList.stream()
            .anyMatch(r -> r.getBetNo().equals(result.getBetNo()));
        if (exists) {
            // 已存在该号码，忽略或可选择覆盖
            return;
        }
        this.resultsList.add(result);
        // 增量更新统计字段
        this.totalBetMoney = this.totalBetMoney.add(result.getBetMoney());
        this.totalFlyMoney = this.totalFlyMoney.add(result.getFlyMoney());
        this.totalKeepMoney = this.totalKeepMoney.add(result.getKeepMoney());
        if (result.getProfit().compareTo(BigDecimal.ZERO) < 0) {
            this.negativeCount++;
        } else if (result.getProfit().compareTo(BigDecimal.ZERO) > 0) {
            this.positiveCount++;
        }
        updateSellProfitForAll();
    }

    /**
     * 批量设置所有号码的出货盈亏（出货奖金-出货本金）
     */
    private void updateSellProfitForAll() {
        if (resultsList != null) {
            for (BetNumberResults result : resultsList) {
                BigDecimal sellProfit = result.getSellBonus().subtract(this.totalFlyMoney);
                result.setSellProfit(sellProfit);
            }
        }
    }

    // 如需set方法可补充
} 