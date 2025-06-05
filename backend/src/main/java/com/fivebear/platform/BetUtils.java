package com.fivebear.platform;

import java.math.BigDecimal;
import java.util.Map;

public class BetUtils {
    /**
     * 计算奖金
     */
    public static BigDecimal calcBonus(BigDecimal betMoney, BigDecimal odds) {
        return betMoney.multiply(odds);
    }

    /**
     * 将Map<String, Object>转为BetNumber对象
     * 要求map包含dictNoTypeId, betNo, betMoney, odds, bonus字段
     * bonus可自动计算
     */
    public static BetNumber mapToBetNumber(Map<String, Object> map) {
        int dictNoTypeId = Integer.parseInt(map.get("dict_no_type_id").toString());
        String betNo = map.get("bet_no").toString();
        BigDecimal betMoney = new BigDecimal(map.get("bet_money").toString());
        BigDecimal odds = map.containsKey("odds") ? new BigDecimal(map.get("odds").toString()) : BigDecimal.ZERO;
        BigDecimal bonus = map.containsKey("bonus") ? new BigDecimal(map.get("bonus").toString()) : calcBonus(betMoney, odds);
        return new BetNumber(dictNoTypeId, betNo, betMoney, odds);
    }

    /**
     * 校验号码是否合法（只包含数字和X，且长度为4）
     */
    public static boolean isValidBetNo(String betNo) {
        return betNo != null && betNo.matches("[0-9X]{4}");
    }

    /**
     * 生成[min, max]区间的随机赔率，保留两位小数
     */
    public static BigDecimal randomOdds(double min, double max) {
        if (min > max) throw new IllegalArgumentException("min不能大于max");
        double value = min + Math.random() * (max - min);
        return BigDecimal.valueOf(Math.round(value * 100.0) / 100.0);
    }

    /**
     * 生成[min, max]区间的随机整数
     */
    public static int randomInt(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min不能大于max");
        return min + (int)(Math.random() * (max - min + 1));
    }

    /**
     * 生成[min, max]区间的随机小数
     */
    public static BigDecimal randomDouble(double min, double max) {
        if (min > max) throw new IllegalArgumentException("min不能大于max");
        double value = min + Math.random() * (max - min);
        return BigDecimal.valueOf(value);
    }
} 