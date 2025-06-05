package com.fivebear.platform;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

// 会员信息类
public class MemberInfo {
    private int memberId;
    private String account;
    private double credit;
    private double creditBalance;
    private double creditAssigned;
    private int oddsType;
    private int inputMode;
    private int showMode;
    private String storeName;
    private int agentId;
    private int bigAgentId;
    private int shareholderId;
    private int bigShareholderId;
    private int isCash;
    private int showSnapshot;
    private List<MemberSettingInfo> memberSettingInfos;
    private double lockAmount;

    public MemberInfo() {
        // 空构造方法，供反射或序列化使用
    }

    public synchronized int getMemberId() {
        return memberId;
    }

    public synchronized void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public synchronized String getAccount() {
        return account;
    }

    public synchronized void setAccount(String account) {
        this.account = account;
    }

    public synchronized double getCredit() {
        return credit;
    }

    public synchronized void setCredit(double credit) {
        this.credit = credit;
    }

    public synchronized double getCreditBalance() {
        return creditBalance;
    }

    public synchronized void setCreditBalance(double creditBalance) {
        this.creditBalance = creditBalance;
    }

    public synchronized double getCreditAssigned() {
        return creditAssigned;
    }

    public synchronized void setCreditAssigned(double creditAssigned) {
        this.creditAssigned = creditAssigned;
    }

    public synchronized int getOddsType() {
        return oddsType;
    }

    public synchronized void setOddsType(int oddsType) {
        this.oddsType = oddsType;
    }

    public synchronized int getInputMode() {
        return inputMode;
    }

    public synchronized void setInputMode(int inputMode) {
        this.inputMode = inputMode;
    }

    public synchronized int getShowMode() {
        return showMode;
    }

    public synchronized void setShowMode(int showMode) {
        this.showMode = showMode;
    }

    public synchronized String getStoreName() {
        return storeName;
    }

    public synchronized void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public synchronized int getAgentId() {
        return agentId;
    }

    public synchronized void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public synchronized int getBigAgentId() {
        return bigAgentId;
    }

    public synchronized void setBigAgentId(int bigAgentId) {
        this.bigAgentId = bigAgentId;
    }

    public synchronized int getShareholderId() {
        return shareholderId;
    }

    public synchronized void setShareholderId(int shareholderId) {
        this.shareholderId = shareholderId;
    }

    public synchronized int getBigShareholderId() {
        return bigShareholderId;
    }

    public synchronized void setBigShareholderId(int bigShareholderId) {
        this.bigShareholderId = bigShareholderId;
    }

    public synchronized int getIsCash() {
        return isCash;
    }

    public synchronized void setIsCash(int isCash) {
        this.isCash = isCash;
    }

    public synchronized int getShowSnapshot() {
        return showSnapshot;
    }

    public synchronized void setShowSnapshot(int showSnapshot) {
        this.showSnapshot = showSnapshot;
    }

    public synchronized List<MemberSettingInfo> getMemberSettingInfos() {
        return memberSettingInfos;
    }

    public synchronized void setMemberSettingInfos(List<MemberSettingInfo> memberSettingInfos) {
        this.memberSettingInfos = memberSettingInfos;
    }

    public synchronized void updateFromJson(JSONObject data) {
        // 解析 Member
        if (data.has("Member")) {
            JSONArray memberArr = data.getJSONArray("Member");
            if (memberArr.length() > 0) {
                JSONObject m = memberArr.getJSONObject(0);
                this.memberId = m.optInt("member_id");
                this.account = m.optString("account");
                this.credit = m.optDouble("credit");
                this.creditBalance = m.optDouble("credit_balance");
                this.creditAssigned = m.optDouble("credit_assigned");
                this.oddsType = m.optInt("odds_type");
                this.inputMode = m.optInt("input_mode");
                this.showMode = m.optInt("show_mode");
                this.storeName = m.optString("store_name");
                this.agentId = m.optInt("agent_id");
                this.bigAgentId = m.optInt("big_agent_id");
                this.shareholderId = m.optInt("shareholder_id");
                this.bigShareholderId = m.optInt("big_shareholder_id");
                this.isCash = m.optInt("is_cash");
                this.showSnapshot = m.optInt("show_snapshot");
            }
        }
        // 解析 Setting
        if (data.has("Setting")) {
            JSONArray settingArr = data.getJSONArray("Setting");
            List<MemberSettingInfo> settings = new ArrayList<>();
            for (int i = 0; i < settingArr.length(); i++) {
                JSONObject s = settingArr.getJSONObject(i);
                MemberSettingInfo info = new MemberSettingInfo();
                info.setNoTypeName(s.optString("no_type_name"));
                info.setFixNum(s.optInt("fix_num"));
                info.setDictNoTypeId(s.optInt("dict_no_type_id"));
                info.setMinBet(s.optDouble("min_bet"));
                info.setOneBetLimit(s.optDouble("one_bet_limit"));
                info.setOneItemLimit(s.optDouble("one_item_limit"));
                info.setOddsMaxLimit1(s.optDouble("odds_max_limit1"));
                info.setOddsMaxLimit2(s.optDouble("odds_max_limit2"));
                info.setOddsMaxLimit3(s.optDouble("odds_max_limit3"));
                info.setOddsMaxLimit4(s.optDouble("odds_max_limit4"));
                info.setOddsMax1(s.optDouble("odds_max1"));
                info.setOddsMax2(s.optDouble("odds_max2"));
                info.setOddsMax3(s.optDouble("odds_max3"));
                info.setOddsMax4(s.optDouble("odds_max4"));
                info.setOddsMin1(s.optDouble("odds_min1"));
                info.setOddsMin2(s.optDouble("odds_min2"));
                info.setOddsMin3(s.optDouble("odds_min3"));
                info.setOddsMin4(s.optDouble("odds_min4"));
                info.setSelfCurrentOdds1(s.optDouble("self_current_odds1"));
                info.setSelfCurrentOdds2(s.optDouble("self_current_odds2"));
                info.setSelfCurrentOdds3(s.optDouble("self_current_odds3"));
                info.setSelfCurrentOdds4(s.optDouble("self_current_odds4"));
                info.setSelfReturnWaterRate(s.optDouble("self_return_water_rate"));
                info.setReturnWaterSum(s.optDouble("return_water_sum"));
                info.setReturnWaterMax(s.optDouble("return_water_max"));
                info.setReturnWaterMin(s.optDouble("return_water_min"));
                info.setReturnWaterDiff1(s.optDouble("return_water_diff1"));
                info.setReturnWaterDiff2(s.optDouble("return_water_diff2"));
                info.setReturnWaterDiff3(s.optDouble("return_water_diff3"));
                info.setReturnWaterDiff4(s.optDouble("return_water_diff4"));
                settings.add(info);
            }
            this.memberSettingInfos = settings;
        }
    }

    /**
     * 获取当前锁定金额（线程安全）
     */
    public synchronized double getLockAmount() {
        return lockAmount;
    }

    /**
     * 设置当前锁定金额（线程安全）
     */
    public synchronized void setLockAmount(double lockAmount) {
        this.lockAmount = lockAmount;
    }

    /**
     * 增加锁定金额（线程安全）
     * @param amount 增加的金额
     */
    public synchronized void addLockAmount(double amount) {
        this.lockAmount += amount;
    }

    /**
     * 减少锁定金额（线程安全）
     * @param amount 减少的金额
     */
    public synchronized void reduceLockAmount(double amount) {
        this.lockAmount -= amount;
        if (this.lockAmount < 0) this.lockAmount = 0;
    }
}
