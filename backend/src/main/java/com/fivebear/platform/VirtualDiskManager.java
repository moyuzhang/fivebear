package com.fivebear.platform;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 虚盘管理器：用于管理每个号码的虚拟余额、锁定金额等，支持线程安全操作。
 */
public class VirtualDiskManager {
    // 虚盘数据结构：key=号码#玩法，value=虚盘余额对象
    private final Map<String, VirtualDiskEntry> diskMap = new ConcurrentHashMap<>();
    // 全局保留组数
    private BigDecimal keepCount = BigDecimal.ZERO;

    /**
     * 虚盘余额对象
     */
    public static class VirtualDiskEntry {
        private final String number;
        private final int dictNoTypeId;
        private BigDecimal totalAmount = BigDecimal.ZERO; // 入货总金额
        private BigDecimal inNegative = BigDecimal.ZERO;      // 入货负值
        private BigDecimal inRebate = BigDecimal.ZERO;        // 入货返点
        private BigDecimal inAvgOdds = BigDecimal.ZERO;       // 入货平均赔率
        private BigDecimal inReturnWater = BigDecimal.ZERO;   // 入货回水
        private BigDecimal outTotalAmount = BigDecimal.ZERO;  // 出货总金额
        private BigDecimal outAvgOdds = BigDecimal.ZERO;      // 出货平均赔率
        private BigDecimal outRebateTotal = BigDecimal.ZERO;  // 出货返点总金额
        private BigDecimal keepAmount = BigDecimal.ZERO;      // 保留金额
        private BigDecimal keepRebateTotal = BigDecimal.ZERO; // 保留总返点
        private BigDecimal outProfit = BigDecimal.ZERO;       // 出货盈亏

        public VirtualDiskEntry(String number, int dictNoTypeId) {
            this.number = number;
            this.dictNoTypeId = dictNoTypeId;
        }

        public synchronized BigDecimal getRemainAmount() {
            return totalAmount.subtract(outTotalAmount);
        }

        public synchronized boolean lock(BigDecimal amount) {
            if (getRemainAmount().compareTo(amount) >= 0) {
                totalAmount = totalAmount.subtract(amount);
                return true;
            }
            return false;
        }

        public synchronized void unlock(BigDecimal amount) {
            totalAmount = totalAmount.add(amount);
        }

        public synchronized void add(BigDecimal amount) {
            totalAmount = totalAmount.add(amount);
        }

        public synchronized void subtract(BigDecimal amount) {
            totalAmount = totalAmount.subtract(amount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }
        }

        public String getNumber() { return number; }
        public int getDictNoTypeId() { return dictNoTypeId; }
        public synchronized BigDecimal getTotalAmount() { return totalAmount; }
        public synchronized void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public synchronized void setInNegative(BigDecimal inNegative) { this.inNegative = inNegative; }
        public synchronized BigDecimal getInNegative() { return inNegative; }
        public synchronized void setInRebate(BigDecimal inRebate) { this.inRebate = inRebate; }
        public synchronized BigDecimal getInRebate() { return inRebate; }
        public synchronized void setInAvgOdds(BigDecimal inAvgOdds) { this.inAvgOdds = inAvgOdds; }
        public synchronized BigDecimal getInAvgOdds() { return inAvgOdds; }
        public synchronized void setInReturnWater(BigDecimal inReturnWater) { this.inReturnWater = inReturnWater; }
        public synchronized BigDecimal getInReturnWater() { return inReturnWater; }
        public synchronized void setOutTotalAmount(BigDecimal outTotalAmount) { this.outTotalAmount = outTotalAmount; }
        public synchronized BigDecimal getOutTotalAmount() { return outTotalAmount; }
        public synchronized void setOutAvgOdds(BigDecimal outAvgOdds) { this.outAvgOdds = outAvgOdds; }
        public synchronized BigDecimal getOutAvgOdds() { return outAvgOdds; }
        public synchronized void setOutRebateTotal(BigDecimal outRebateTotal) { this.outRebateTotal = outRebateTotal; }
        public synchronized BigDecimal getOutRebateTotal() { return outRebateTotal; }
        public synchronized void setKeepAmount(BigDecimal keepAmount) { this.keepAmount = keepAmount; }
        public synchronized BigDecimal getKeepAmount() { return keepAmount; }
        public synchronized void setKeepRebateTotal(BigDecimal keepRebateTotal) { this.keepRebateTotal = keepRebateTotal; }
        public synchronized BigDecimal getKeepRebateTotal() { return keepRebateTotal; }
        public synchronized BigDecimal getOutProfit() { return outProfit; }
        public synchronized void setOutProfit(BigDecimal outProfit) { this.outProfit = outProfit; }

        /**
         * 计算该号码的盈亏 = 入货负值-入货返点+出货返点总金额+出货盈亏
         */
        public synchronized BigDecimal getProfit() {
            return inNegative.subtract(inRebate).add(outRebateTotal).add(outProfit);
        }
    }

    /**
     * 增加虚盘余额（仅支持四定玩法）
     */
    public void addAmount(String number, int dictNoTypeId, BigDecimal amount) {
        if (dictNoTypeId != 11) return; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        diskMap.computeIfAbsent(key, k -> new VirtualDiskEntry(number, dictNoTypeId)).add(amount);
    }

    /**
     * 扣减虚盘余额（仅支持四定玩法）
     */
    public void subtractAmount(String number, int dictNoTypeId, BigDecimal amount) {
        if (dictNoTypeId != 11) return; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        diskMap.computeIfAbsent(key, k -> new VirtualDiskEntry(number, dictNoTypeId)).subtract(amount);
    }

    /**
     * 锁定虚盘金额（仅支持四定玩法）
     */
    public boolean lockAmount(String number, int dictNoTypeId, BigDecimal amount) {
        if (dictNoTypeId != 11) return false; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        return diskMap.computeIfAbsent(key, k -> new VirtualDiskEntry(number, dictNoTypeId)).lock(amount);
    }

    /**
     * 解锁虚盘金额（仅支持四定玩法）
     */
    public void unlockAmount(String number, int dictNoTypeId, BigDecimal amount) {
        if (dictNoTypeId != 11) return; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        diskMap.computeIfAbsent(key, k -> new VirtualDiskEntry(number, dictNoTypeId)).unlock(amount);
    }

    /**
     * 查询虚盘余额（仅支持四定玩法）
     */
    public BigDecimal getRemainAmount(String number, int dictNoTypeId) {
        if (dictNoTypeId != 11) return BigDecimal.ZERO; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        return diskMap.getOrDefault(key, new VirtualDiskEntry(number, dictNoTypeId)).getRemainAmount();
    }

    /**
     * 查询虚盘总余额（仅支持四定玩法）
     */
    public BigDecimal getTotalAmount(String number, int dictNoTypeId) {
        if (dictNoTypeId != 11) return BigDecimal.ZERO; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        return diskMap.getOrDefault(key, new VirtualDiskEntry(number, dictNoTypeId)).getTotalAmount();
    }

    /**
     * 查询虚盘锁定金额（仅支持四定玩法）
     */
    public BigDecimal getLockedAmount(String number, int dictNoTypeId) {
        if (dictNoTypeId != 11) return BigDecimal.ZERO; // 只管理四定
        String key = buildKey(number, dictNoTypeId);
        return diskMap.getOrDefault(key, new VirtualDiskEntry(number, dictNoTypeId)).getTotalAmount();
    }

    /**
     * 获取所有四定号码的锁定总金额
     */
    public BigDecimal getTotalLockedAmount() {
        return diskMap.values().stream()
                .map(VirtualDiskEntry::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取所有四定号码的入货总金额（即虚盘总余额）
     */
    public BigDecimal getTotalInAmount() {
        return diskMap.values().stream()
                .map(VirtualDiskEntry::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取所有四定号码的出货总金额（即所有已锁定金额的累加，视为已出货）
     */
    public BigDecimal getTotalOutAmount() {
        // 这里假设所有锁定金额都已出货，否则可根据业务调整
        return getTotalLockedAmount();
    }

    /**
     * 按号码、玩法统计所有虚盘余额
     */
    public Map<String, VirtualDiskEntry> getAllEntries() {
        return diskMap;
    }

    /**
     * 分页获取虚盘条目，支持排序（按号码、余额、锁定金额）
     * @param page 页码（从1开始）
     * @param pageSize 每页数量
     * @param sortBy 排序字段（number/totalAmount/lockedAmount）
     * @param asc 是否升序（true升序，false降序）
     * @return 指定页的虚盘条目列表
     */
    public java.util.List<VirtualDiskEntry> getEntriesByPage(int page, int pageSize, String sortBy, boolean asc) {
        if (page < 1 || pageSize < 1) return java.util.Collections.emptyList();
        java.util.Comparator<VirtualDiskEntry> comparator;
        if ("totalAmount".equalsIgnoreCase(sortBy)) {
            comparator = java.util.Comparator.comparing(VirtualDiskEntry::getTotalAmount);
        } else {
            comparator = java.util.Comparator.comparing(VirtualDiskEntry::getNumber);
        }
        if (!asc) {
            comparator = comparator.reversed();
        }
        return diskMap.values().stream()
                .sorted(comparator)
                .skip((long)(page - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    /**
     * 计算所有四定号码的总盈亏
     * =入货负值-入货返点+出货返点总金额+出货盈亏
     */
    public BigDecimal calcTotalProfit() {
        return diskMap.values().stream()
                .map(VirtualDiskEntry::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildKey(String number, int dictNoTypeId) {
        return number + "#" + dictNoTypeId;
    }

    // 保留组数（全局）
    public synchronized BigDecimal getKeepCount() { return keepCount; }
    public synchronized void setKeepCount(BigDecimal keepCount) { this.keepCount = keepCount; }

    /**
     * 出货操作，考虑全局保留组数
     * @param number 号码
     * @param amount 出货金额
     * @param odds 出货赔率
     * @param rebate 出货返点
     * @return 出货是否成功
     */
    public synchronized boolean deliver(String number, BigDecimal amount, BigDecimal odds, BigDecimal rebate) {
        int dictNoTypeId = 11;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        VirtualDiskEntry entry = diskMap.computeIfAbsent(buildKey(number, dictNoTypeId), k -> new VirtualDiskEntry(number, dictNoTypeId));
        // 只允许出货 (totalAmount - keepCount) 以内的组数
        BigDecimal maxDeliver = entry.getTotalAmount().subtract(this.keepCount);
        if (maxDeliver.compareTo(BigDecimal.ZERO) < 0) maxDeliver = BigDecimal.ZERO;
        if (amount.compareTo(maxDeliver) > 0) return false; // 超过可出货组数
        entry.subtract(amount); // 扣减余额
        // 更新出货相关字段
        entry.setOutTotalAmount(entry.getOutTotalAmount().add(amount));
        // 计算新的平均赔率
        BigDecimal totalOut = entry.getOutTotalAmount();
        BigDecimal newAvgOdds = totalOut.compareTo(BigDecimal.ZERO) == 0 ? odds :
            (entry.getOutAvgOdds().multiply(totalOut.subtract(amount)).add(odds.multiply(amount))).divide(totalOut, 8, BigDecimal.ROUND_HALF_UP);
        entry.setOutAvgOdds(newAvgOdds);
        // 累加返点
        entry.setOutRebateTotal(entry.getOutRebateTotal().add(rebate));
        return true;
    }
} 