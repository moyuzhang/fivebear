package com.fivebear.platform;
//下注号码
public class Bet {
    private String number; // 投注号码
    private double betMoney; // 投注金额
    private double odds; // 赔率
    private int dictNoTypeId; // 玩法类型ID
    private boolean isPositive; // 是否包牌
    private double negative; // 盈亏
    private double rebateRate; // 回水率（返点率）

    /**
     * 构造方法
     * @param number 投注号码
     * @param betMoney 投注金额
     * @param dictNoTypeId 玩法类型ID
     */
    public Bet(String number, double betMoney, int dictNoTypeId) {
        this.number = number;
        this.betMoney = betMoney;
        this.dictNoTypeId = dictNoTypeId;
        this.isPositive = false;
        this.rebateRate = 0.0;
    }

    /**
     * 构造方法（自动根据号码推断玩法类型ID）
     * @param number 投注号码
     * @param betMoney 投注金额
     */
    public Bet(String number, double betMoney) {
        this.number = number;
        this.betMoney = betMoney;
        this.dictNoTypeId = NumberTemplateGenerator.getDictNoTypeIdByNumber(number);
    }

    public String getNumber() {
        return number;
    }

    public double getBetMoney() {
        return betMoney;
    }

    public int getDictNoTypeId() {
        return dictNoTypeId;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setBetMoney(double betMoney) {
        this.betMoney = betMoney;
    }

    public void setDictNoTypeId(int dictNoTypeId) {
        this.dictNoTypeId = dictNoTypeId;
    }

    public double getOdds() {
        return odds;
    }

    public void setOdds(double odds) {
        this.odds = odds;
    }

    /**
     * 计算奖金
     * @return 奖金金额
     */
    public double getWinMoney() {
        return betMoney * odds;
    }

    public double getNegative() {
        return negative;
    }

    public void setNegative(double negative) {
        this.negative = negative;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean isPositive) {
        this.isPositive = isPositive;
    }

    /**
     * 获取回水率（返点率）
     * @return 回水率
     */
    public double getRebateRate() {
        return rebateRate;
    }

    /**
     * 设置回水率（返点率）
     * @param rebateRate 回水率
     */
    public void setRebateRate(double rebateRate) {
        this.rebateRate = rebateRate;
    }

    /**
     * 计算回水金额（返点金额）
     * @return 回水金额
     */
    public double getRebateMoney() {
        return betMoney * rebateRate;
    }

    /**
     * 返回投注号码和金额的字符串表示
     */
    @Override
    public String toString() {
        return String.format("号码：%s 金额：%f", number, betMoney);
    }
} 