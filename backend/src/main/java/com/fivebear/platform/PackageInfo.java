package com.fivebear.platform;

import org.json.JSONObject;

public class PackageInfo {
    // 包牌类型ID
    private int dictTypeId;
    // 包牌ID
    private long packageId;
    // 包牌名称
    private String packageName;
    // 包牌组数
    private int packageCount;
    // 固定赔率最大值
    private double oddsFixMax;
    // 固定赔率最小值
    private double oddsFixMin;
    // 赔率设置值
    private double oddsSetting;
    // 批量最小赔率
    private double oddsBatchMin;
    // 热门号差值
    private double oddsHotDiff;
    // 总监赔率
    private double oddsDirector;
    // 总监非热门赔率
    private double oddsDirectorNoHot;
    // 会员静态赔率
    private double oddsMemberStatic;
    // 会员最终赔率
    private double oddsMemberFinal;
    // 会员赔率类型
    private int oddsTypeMember;
    // 会员返水率
    private double returnWaterMember;
    // 平均持仓金额
    private double holdMoneyAvgHolden;
    // 持仓金额
    private double holdMoneyHolden;
    // 总监平均持仓金额
    private double mainDirectorHoldMoneyAvgHolden;
    // 总监持仓金额
    private double mainDirectorHoldMoneyHolden;
    // 批量最大持仓金额
    private double holdMoneyBatchMax;
    // 热门持仓金额
    private double holdMoneyHot;
    // 是否停用
    private boolean isStop;
    // 是否热门
    private boolean isHot;
    // 是否使用
    private boolean isUse;
    // 域名
    private String domain;
    // 号码类型ID
    private int dictNoTypeId;
    // 期号
    private String periodNo;

    // Getter and Setter methods

    public int getDictTypeId() {
        return dictTypeId;
    }

    public void setDictTypeId(int dictTypeId) {
        this.dictTypeId = dictTypeId;
    }

    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public double getOddsFixMax() {
        return oddsFixMax;
    }

    public void setOddsFixMax(double oddsFixMax) {
        this.oddsFixMax = oddsFixMax;
    }

    public double getOddsFixMin() {
        return oddsFixMin;
    }

    public void setOddsFixMin(double oddsFixMin) {
        this.oddsFixMin = oddsFixMin;
    }

    public double getOddsSetting() {
        return oddsSetting;
    }

    public void setOddsSetting(double oddsSetting) {
        this.oddsSetting = oddsSetting;
    }

    public double getOddsBatchMin() {
        return oddsBatchMin;
    }

    public void setOddsBatchMin(double oddsBatchMin) {
        this.oddsBatchMin = oddsBatchMin;
    }

    public double getOddsHotDiff() {
        return oddsHotDiff;
    }

    public void setOddsHotDiff(double oddsHotDiff) {
        this.oddsHotDiff = oddsHotDiff;
    }

    public double getOddsDirector() {
        return oddsDirector;
    }

    public void setOddsDirector(double oddsDirector) {
        this.oddsDirector = oddsDirector;
    }

    public double getOddsDirectorNoHot() {
        return oddsDirectorNoHot;
    }

    public void setOddsDirectorNoHot(double oddsDirectorNoHot) {
        this.oddsDirectorNoHot = oddsDirectorNoHot;
    }

    public double getOddsMemberStatic() {
        return oddsMemberStatic;
    }

    public void setOddsMemberStatic(double oddsMemberStatic) {
        this.oddsMemberStatic = oddsMemberStatic;
    }

    public double getOddsMemberFinal() {
        return oddsMemberFinal;
    }

    public void setOddsMemberFinal(double oddsMemberFinal) {
        this.oddsMemberFinal = oddsMemberFinal;
    }

    public int getOddsTypeMember() {
        return oddsTypeMember;
    }

    public void setOddsTypeMember(int oddsTypeMember) {
        this.oddsTypeMember = oddsTypeMember;
    }

    public double getReturnWaterMember() {
        return returnWaterMember;
    }

    public void setReturnWaterMember(double returnWaterMember) {
        this.returnWaterMember = returnWaterMember;
    }

    public double getHoldMoneyAvgHolden() {
        return holdMoneyAvgHolden;
    }

    public void setHoldMoneyAvgHolden(double holdMoneyAvgHolden) {
        this.holdMoneyAvgHolden = holdMoneyAvgHolden;
    }

    public double getHoldMoneyHolden() {
        return holdMoneyHolden;
    }

    public void setHoldMoneyHolden(double holdMoneyHolden) {
        this.holdMoneyHolden = holdMoneyHolden;
    }

    public double getMainDirectorHoldMoneyAvgHolden() {
        return mainDirectorHoldMoneyAvgHolden;
    }

    public void setMainDirectorHoldMoneyAvgHolden(double mainDirectorHoldMoneyAvgHolden) {
        this.mainDirectorHoldMoneyAvgHolden = mainDirectorHoldMoneyAvgHolden;
    }

    public double getMainDirectorHoldMoneyHolden() {
        return mainDirectorHoldMoneyHolden;
    }

    public void setMainDirectorHoldMoneyHolden(double mainDirectorHoldMoneyHolden) {
        this.mainDirectorHoldMoneyHolden = mainDirectorHoldMoneyHolden;
    }

    public double getHoldMoneyBatchMax() {
        return holdMoneyBatchMax;
    }

    public void setHoldMoneyBatchMax(double holdMoneyBatchMax) {
        this.holdMoneyBatchMax = holdMoneyBatchMax;
    }

    public double getHoldMoneyHot() {
        return holdMoneyHot;
    }

    public void setHoldMoneyHot(double holdMoneyHot) {
        this.holdMoneyHot = holdMoneyHot;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }

    public boolean isHot() {
        return isHot;
    }

    public void setHot(boolean isHot) {
        this.isHot = isHot;
    }

    public boolean isUse() {
        return isUse;
    }

    public void setUse(boolean isUse) {
        this.isUse = isUse;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getDictNoTypeId() {
        return dictNoTypeId;
    }

    public void setDictNoTypeId(int dictNoTypeId) {
        this.dictNoTypeId = dictNoTypeId;
    }

    public String getPeriodNo() {
        return periodNo;
    }

    public void setPeriodNo(String periodNo) {
        this.periodNo = periodNo;
    }

    static PackageInfo parsePackageInfo(JSONObject json) {
        try {
            PackageInfo info = new PackageInfo();
            info.setDictTypeId(json.optInt("DictTypeId"));
            info.setPackageId(json.optLong("PackageId"));
            info.setPackageName(json.optString("PackageName"));
            info.setPackageCount(json.optInt("PackageCount"));
            info.setOddsFixMax(json.optDouble("OddsFixMax"));
            info.setOddsFixMin(json.optDouble("OddsFixMin"));
            info.setOddsSetting(json.optDouble("OddsSetting"));
            info.setOddsBatchMin(json.optDouble("OddsBatchMin"));
            info.setOddsHotDiff(json.optDouble("OddsHotDiff"));
            info.setOddsDirector(json.optDouble("OddsDirector"));
            info.setOddsDirectorNoHot(json.optDouble("OddsDirectorNoHot"));
            info.setOddsMemberStatic(json.optDouble("OddsMemberStatic"));
            info.setOddsMemberFinal(json.optDouble("OddsMemberFinal"));
            info.setOddsTypeMember(json.optInt("OddsTypeMember"));
            info.setReturnWaterMember(json.optDouble("ReturnWaterMember"));
            info.setHoldMoneyAvgHolden(json.optDouble("HoldMoneyAvgHolden"));
            info.setHoldMoneyHolden(json.optDouble("HoldMoneyHolden"));
            info.setMainDirectorHoldMoneyAvgHolden(json.optDouble("MainDirectorHoldMoneyAvgHolden"));
            info.setMainDirectorHoldMoneyHolden(json.optDouble("MainDirectorHoldMoneyHolden"));
            info.setHoldMoneyBatchMax(json.optDouble("HoldMoneyBatchMax"));
            info.setHoldMoneyHot(json.optDouble("HoldMoneyHot"));
            info.setStop(json.optBoolean("IsStop"));
            info.setHot(json.optBoolean("IsHot"));
            info.setUse(json.optBoolean("IsUse"));
            return info;
        } catch (Exception e) {
            // 记录异常日志，返回 null 或抛出自定义异常
            System.err.println("解析PackageInfo异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}