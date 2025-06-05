package com.fivebear.platform;

public class Code {
    private String number;
    private double totalAmount;
    private double lockedAmount;

    public Code(String number, double totalAmount) {
        this.number = number;
        this.totalAmount = totalAmount;
        this.lockedAmount = 0;
    }

    public String getNumber() { return number; }
    public double getTotalAmount() { return totalAmount; }
    public double getLockedAmount() { return lockedAmount; }
    public double getRemainAmount() { return Math.round((totalAmount - lockedAmount) * 1e8) / 1e8; }

    public boolean lock(double amount) {
        if (getRemainAmount() >= amount) {
            lockedAmount += amount;
            return true;
        }
        return false;
    }

    public void unlock(double amount) {
        lockedAmount -= amount;
        if (lockedAmount < 0) lockedAmount = 0;
    }

    public void addAmount(double amount) {
        this.totalAmount += amount;
    }

    public String toString() {
        return number + ": 总金额=" + totalAmount + ", 已锁定=" + lockedAmount + ", 剩余=" + getRemainAmount();
    }
} 