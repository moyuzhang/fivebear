package com.fivebear.platform;

import java.util.ArrayList;
import java.util.List;

public class BettingStrategy {

    // 结果数据结构，表示每个号码的投注情况
    public static class BetResult {
        public String number; // 号码
        public double bet;    // 下注金额
        public double odds;   // 赔率
        public double profit; // 盈利
        private int noTypeId; // 类型编号

        /**
         * 构造函数
         * @param number 号码
         * @param odds 赔率
         * @param noTypeId 类型编号
         */
        public BetResult(String number, double odds, int noTypeId) {
            this.number = number;
            this.odds = odds;
            this.noTypeId = noTypeId;
        }

        /**
         * 获取奖金
         * @return 奖金
         */
        public double getBonus() {
            return bet * odds;
        }
    }

    // 计划容器，负责分配下注、计算盈利、统计汇总等
    public static class BetPlan {
        private List<BetResult> betResults; // 所有号码的投注结果
        private double totalBet;            // 总下注金额
        private int noTypeId;               // 类型编号
        private double positivePercent;     // 盈利为正的号码占比
        private double maxProfit;           // 最大盈利
        private double minProfit;           // 最小盈利

        /**
         * 构造函数
         * @param betResults 投注结果列表
         * @param totalBet 总下注金额
         * @param noTypeId 类型编号
         */
        public BetPlan(List<BetResult> betResults, double totalBet, int noTypeId) {
            this.betResults = betResults;
            this.totalBet = totalBet;
            this.noTypeId = noTypeId;
        }

        /**
         * 分配每个号码的下注金额，使奖金均衡
         */
        public void allocateBets() {
            double sumReciprocal = 0.0;
            for (BetResult r : betResults) sumReciprocal += 1.0 / r.odds;
            double X = totalBet / sumReciprocal;
            for (BetResult r : betResults) {
                r.bet = X / r.odds;
            }
        }

        /**
         * 计算每个号码的盈利
         */
        public void calculateProfits() {
            for (BetResult r : betResults) {
                r.profit = r.getBonus() - totalBet;
            }
        }

        /**
         * 统计汇总信息，包括最大/最小盈利、盈利为正的号码占比等
         */
        public void calculateSummary() {
            minProfit = Double.MAX_VALUE;
            maxProfit = -Double.MAX_VALUE;
            int positiveCount = 0;
            for (BetResult r : betResults) {
                double profit = r.profit;
                if (profit < minProfit) minProfit = profit;
                if (profit > maxProfit) maxProfit = profit;
                if (profit > 0) positiveCount++;
            }
            positivePercent = 100.0 * positiveCount / betResults.size();
        }

        /**
         * 打印每个号码的详细投注结果
         */
        public void printResults() {
            for (BetResult r : betResults) {
                System.out.printf("号码 %s: 下注 %.4f 元, 赔率 %.2f, 奖金 %.2f 元, 盈利 %.2f 元\n",
                        r.number, r.bet, r.odds, r.getBonus(), r.profit);
            }
        }

        /**
         * 打印汇总信息
         */
        public void printSummary() {
            calculateSummary();
            double totalBetSum = totalBet;
            double minBonus = Double.MAX_VALUE, maxBonus = -Double.MAX_VALUE, sumBonus = 0, sumProfit = 0;
            for (BetResult r : betResults) {
                double bonus = r.getBonus();
                sumBonus += bonus;
                sumProfit += r.profit;
                if (bonus < minBonus) minBonus = bonus;
                if (bonus > maxBonus) maxBonus = bonus;
            }
            double avgProfit = sumProfit / betResults.size();
            double avgBonus = sumBonus / betResults.size();
            double profitRate = sumProfit / totalBetSum;

            System.out.println("--- 汇总信息 ---");
            System.out.printf("总下注金额: %.2f\n", totalBetSum);
            System.out.printf("最小奖金: %.2f, 最大奖金: %.2f, 平均奖金: %.2f\n", minBonus, maxBonus, avgBonus);
            System.out.printf("最小盈利: %.2f, 最大盈利: %.2f, 平均盈利: %.2f\n", minProfit, maxProfit, avgProfit);
            System.out.printf("总盈利: %.2f, 盈亏率: %.4f\n", sumProfit, profitRate);
            System.out.printf("盈利为正的号码占比: %.2f%%\n", positivePercent);
        }

        /**
         * 获取所有投注结果
         * @return 投注结果列表
         */
        public List<BetResult> getBetResults() {
            return betResults;
        }

        /**
         * 获取类型编号
         * @return 类型编号
         */
        public int getNoTypeId() {
            return noTypeId;
        }

        /**
         * 获取盈利为正的号码占比
         * @return 百分比
         */
        public double getPositivePercent() { return positivePercent; }
        /**
         * 获取最大盈利
         * @return 最大盈利
         */
        public double getMaxProfit() { return maxProfit; }
        /**
         * 获取最小盈利
         * @return 最小盈利
         */
        public double getMinProfit() { return minProfit; }

        /**
         * toString方法，输出格式为 00XX=金额，01XX=金额 ...
         * 金额格式根据noTypeId决定：1~7取整数，其他保留一位小数
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < betResults.size(); i++) {
                BetResult r = betResults.get(i);
                String betStr;
                if (noTypeId >= 1 && noTypeId <= 7) {
                    betStr = String.valueOf((int) r.bet); // 只取整数部分
                } else {
                    betStr = String.format("%.1f", r.bet); // 保留一位小数
                }
                sb.append(r.number).append("=").append(betStr);
                if (i != betResults.size() - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }

    /**
     * 主方法，演示如何使用BetPlan进行批量投注分配和统计
     */
    public static void main(String[] args) {
        List<BetResult> betResults = new ArrayList<>();
        int noTypeId = 1;
        for (int i = 0; i < 100; i++) {
            String number = String.format("%02dXX", i);
            double odds = 100.1;
            if (i == 95) odds = 99.3;
            if (i == 96) odds = 99.4;
            if (i == 97) odds = 99.5;
            if (i == 98 || i == 99) odds = 99.8;
            betResults.add(new BetResult(number, odds, noTypeId));
        }
        double totalBet = 200000.0;
        BetPlan plan = new BetPlan(betResults, totalBet, noTypeId);
        plan.allocateBets(); // 分配下注
        plan.calculateProfits(); // 计算盈利
        plan.printResults(); // 输出每个号码的详细结果
        plan.printSummary(); // 输出汇总信息
        System.out.println(plan.toString()); // 输出格式化的投注字符串
        System.out.println("盈利为正的号码占比: " + plan.getPositivePercent() + "%");
        System.out.println("最大盈利: " + plan.getMaxProfit());
        System.out.println("最小盈利: " + plan.getMinProfit());
    }
}