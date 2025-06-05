package com.fivebear.platform;

import java.math.BigDecimal;

/**
 * 单号码统计明细结果
 */
public class BetNumberResults {
    /** 号码 */
    private String betNo;
    /** 投注金额 */
    private BigDecimal betMoney = BigDecimal.ZERO;
    /** 负值（盈亏） */
    private BigDecimal profit = BigDecimal.ZERO;
    /** 投注赔率 */
    private BigDecimal odds = BigDecimal.ZERO;
    /** 出货组数（飞货本金/补飞注数） */
    private BigDecimal flyMoney = BigDecimal.ZERO;
    /** 剩余组数（保留本金） */
    private BigDecimal keepMoney = BigDecimal.ZERO;
    /** 盈利 */
    private BigDecimal win = BigDecimal.ZERO;
    /** 出货赔率（最大可出货赔率） */
    private BigDecimal sellOdds = BigDecimal.ZERO;
    /**出货盈亏 */
    private BigDecimal sellProfit = BigDecimal.ZERO;

    /**
     * 构造方法
     * @param betNo 号码
     * @param betMoney 投注金额
     * @param profit 负值
     * @param odds 投注赔率
     * @param flyMoney 出货组数
     * @param keepMoney 剩余组数
     * @param win 盈利
     * @param sellOdds 出货赔率
     */
    public BetNumberResults(String betNo, BigDecimal betMoney, BigDecimal profit, BigDecimal odds,
                            BigDecimal flyMoney, BigDecimal keepMoney, BigDecimal win, BigDecimal sellOdds) {
        this.betNo = betNo;
        this.betMoney = betMoney;
        this.profit = profit;
        this.odds = odds;
        this.flyMoney = flyMoney;
        this.keepMoney = keepMoney;
        this.win = win;
        this.sellOdds = sellOdds;
    }

    /** 获取号码 */
    public String getBetNo() { return betNo; }
    /** 获取投注金额 */
    public BigDecimal getBetMoney() { return betMoney; }
    /** 获取负值 */
    public BigDecimal getProfit() { return profit; }
    /** 获取赔率 */
    public BigDecimal getOdds() { return odds; }
    /** 获取出货组数 */
    public BigDecimal getFlyMoney() { return flyMoney; }
    /** 获取剩余组数 */
    public BigDecimal getKeepMoney() { return keepMoney; }
    /** 获取盈利 */
    public BigDecimal getWin() { return win; }
    /** 获取出货赔率 */
    public BigDecimal getSellOdds() { return sellOdds; }

    public void  setSellProfit(BigDecimal sellProfit)
    {
        this.sellProfit = sellProfit;
        this.win = profit.add(sellProfit);
    }

    public BigDecimal getSellProfit()
    {
        return sellProfit;
    }

    /**
     * 获取出货奖金（出货赔率 * 出货组数）
     * @return 出货奖金
     */
    public BigDecimal getSellBonus() {
        return this.sellOdds.multiply(this.flyMoney);
    }
} 