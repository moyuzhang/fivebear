package com.fivebear.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BetSettlementManager {
    private final List<Bet> inBets = new ArrayList<>(); // 入货（投注）
    private final List<Bet> outBets = new ArrayList<>(); // 出货（中奖）


    //计算负值
    public void  analyzeNegative() {
        double totalInAmount = getTotalInAmount();
        double totalOutAmount = getTotalOutAmount();
        for (Bet bet : inBets) {
            bet.setNegative(totalInAmount - bet.getWinMoney());
        }
        for (Bet bet : outBets) {
            bet.setNegative(bet.getWinMoney() - totalOutAmount);
        }
    }

    // 添加入货
    public synchronized void addInBet(Bet bet) {
        if (bet != null) inBets.add(bet);
    }
    public synchronized void addInBets(List<Bet> bets) {
        if (bets != null) inBets.addAll(bets);
    }

    // 添加出货
    public synchronized void addOutBet(Bet bet) {
        if (bet != null) outBets.add(bet);
    }
    public synchronized void addOutBets(List<Bet> bets) {
        if (bets != null) outBets.addAll(bets);
    }

    // 总入货金额
    public double getTotalInAmount() {
        return inBets.stream().mapToDouble(Bet::getBetMoney).sum();
    }

    // 总出货金额
    public double getTotalOutAmount() {
        return outBets.stream().mapToDouble(Bet::getBetMoney).sum();
    }

    // 盈亏（入货-出货）
    public double getProfit() {
        return getTotalInAmount() - getTotalOutAmount();
    }

    // 获取所有入货Bets
    public List<Bet> getInBets() {
        return new ArrayList<>(inBets);
    }

    // 获取所有出货Bets
    public List<Bet> getOutBets() {
        return new ArrayList<>(outBets);
    }

    // 按玩法统计总入货金额
    public double getTotalInAmountByTypeId(int dictNoTypeId) {
        return inBets.stream()
                .filter(b -> b.getDictNoTypeId() == dictNoTypeId)
                .mapToDouble(Bet::getBetMoney)
                .sum();
    }

    // 按玩法统计总出货金额
    public double getTotalOutAmountByTypeId(int dictNoTypeId) {
        return outBets.stream()
                .filter(b -> b.getDictNoTypeId() == dictNoTypeId)
                .mapToDouble(Bet::getBetMoney)
                .sum();
    }

    // 按玩法统计盈亏
    public double getProfitByTypeId(int dictNoTypeId) {
        return getTotalInAmountByTypeId(dictNoTypeId) - getTotalOutAmountByTypeId(dictNoTypeId);
    }

    // 获取每个号码+玩法的盈亏，key为"号码#玩法"
    public Map<String, Double> getProfitByNumberAndTypeId() {
        Map<String, Double> profitMap = new java.util.HashMap<>();
        // 统计入货
        for (Bet bet : inBets) {
            String key = bet.getNumber() + "#" + bet.getDictNoTypeId();
            profitMap.put(key, profitMap.getOrDefault(key, 0.0) + bet.getBetMoney());
        }
        // 统计出货
        for (Bet bet : outBets) {
            String key = bet.getNumber() + "#" + bet.getDictNoTypeId();
            profitMap.put(key, profitMap.getOrDefault(key, 0.0) - bet.getBetMoney());
        }
        return profitMap;
    }

    // 查询号码出货号码
    public List<Bet> getOutBetsByNumber(String number) {
        return outBets.stream()
                .filter(b -> b.getNumber().equals(number))
                .collect(java.util.stream.Collectors.toList());
    }

    //查询入货号码
    public List<Bet> getInBetsByNumber(String number) {
        return inBets.stream()
                .filter(b -> b.getNumber().equals(number))
                .collect(java.util.stream.Collectors.toList());
    }

    // 获取指定号码+玩法的盈亏
    public double getProfitByNumberAndTypeId(String number, int dictNoTypeId) {
        String key = number + "#" + dictNoTypeId;
        return getProfitByNumberAndTypeId().getOrDefault(key, 0.0);
    }

    // 可扩展：按号码、玩法等统计
} 