package com.fivebear.platform;

import java.math.BigDecimal;

public class BetNumber {
    // 玩法ID
    private int dictNoTypeId;
    // 投注号码
    private String betNo;
    // 投注金额
    private BigDecimal betMoney = BigDecimal.ZERO;
    // 投注赔率
    private BigDecimal odds = BigDecimal.ZERO;
    // 中奖奖金
    private BigDecimal bonus = BigDecimal.ZERO;
    // 负值（奖金-玩法总投注金额）
    private BigDecimal profit = BigDecimal.ZERO;

    // 飞货相关字段
    private BigDecimal compensateCount = BigDecimal.ZERO; // 补飞注数
    private BigDecimal flyMoney = BigDecimal.ZERO;        // 飞货本金
    private BigDecimal flyBonus = BigDecimal.ZERO;        // 飞货中奖
    private BigDecimal keepMoney = BigDecimal.ZERO;       // 保留本金

    // 出货赔率（补飞时实际可用的最大赔率）
    private BigDecimal sellOdds = BigDecimal.ZERO;

    // 成数（如 0.5 表示五成，或 50 表示50%）
    private BigDecimal percentage = BigDecimal.ZERO;

    // 构造方法
    public BetNumber(int dictNoTypeId, String betNo, BigDecimal betMoney, BigDecimal odds) {
        this.dictNoTypeId = dictNoTypeId;
        this.betNo = betNo;
        this.betMoney = betMoney;
        this.odds = odds;
        this.bonus = betMoney.multiply(odds);
    }

    public int getDictNoTypeId() { return dictNoTypeId; }
    public void setDictNoTypeId(int dictNoTypeId) { this.dictNoTypeId = dictNoTypeId; }

    public String getBetNo() { return betNo; }
    public void setBetNo(String betNo) { this.betNo = betNo; }

    public BigDecimal getBetMoney() { return betMoney; }
    public void setBetMoney(BigDecimal betMoney) { this.betMoney = betMoney; }

    public BigDecimal getOdds() { return odds; }
    public void setOdds(BigDecimal odds) { this.odds = odds; }

    public BigDecimal getBonus() { return bonus; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }

    // 补飞注数
    public BigDecimal getCompensateCount() { return compensateCount; }
    public void setCompensateCount(BigDecimal compensateCount) { this.compensateCount = compensateCount; }

    // 飞货本金
    public BigDecimal getFlyMoney() { return flyMoney; }
    public void setFlyMoney(BigDecimal flyMoney) { this.flyMoney = flyMoney; }

    // 飞货中奖
    public BigDecimal getFlyBonus() { return flyBonus; }
    public void setFlyBonus(BigDecimal flyBonus) { this.flyBonus = flyBonus; }

    // 保留本金
    public BigDecimal getKeepMoney() { return keepMoney; }
    public void setKeepMoney(BigDecimal keepMoney) { this.keepMoney = keepMoney; }

    // 出货赔率
    public BigDecimal getSellOdds() { return sellOdds; }
    public void setSellOdds(BigDecimal sellOdds) { this.sellOdds = sellOdds; }

    // 成数
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

    @Override
    public String toString() {
        return "BetNumber{" +
                "dictNoTypeId=" + dictNoTypeId +
                ", betNo='" + betNo + '\'' +
                ", betMoney=" + betMoney +
                ", odds=" + odds +
                ", bonus=" + bonus +
                ", profit=" + profit +
                ", compensateCount=" + compensateCount +
                ", flyMoney=" + flyMoney +
                ", flyBonus=" + flyBonus +
                ", keepMoney=" + keepMoney +
                ", sellOdds=" + sellOdds +
                ", percentage=" + percentage +
                '}';
    }
} 