package com.fivebear.platform;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AccountConfig {
    private int memberId;
    private String account;
    private String nickName;
    private int memberLevel;
    private double credit;
    private double creditBalance;
    private List<BettingSetting> bettingSettings;

    public static class BettingSetting {
        private String noTypeName;
        private int fixNum;
        private int dictNoTypeId;
        private double minBet;
        private double oneBetLimit;
        private double oneItemLimit;
        private double odds1;
        private double odds2;
        private double odds3;
        private double odds4;
        private double holdMoney;

        public BettingSetting(JSONObject setting) {
            this.noTypeName = setting.getString("no_type_name");
            this.fixNum = setting.getInt("fix_num");
            this.dictNoTypeId = setting.getInt("dict_no_type_id");
            this.minBet = setting.getDouble("min_bet");
            this.oneBetLimit = setting.getDouble("one_bet_limit");
            this.oneItemLimit = setting.getDouble("one_item_limit");
            this.odds1 = setting.getDouble("odds1");
            this.odds2 = setting.getDouble("odds2");
            this.odds3 = setting.getDouble("odds3");
            this.odds4 = setting.getDouble("odds4");
            this.holdMoney = setting.getDouble("hold_money");
        }

        public String getNoTypeName() { return noTypeName; }
        public int getFixNum() { return fixNum; }
        public int getDictNoTypeId() { return dictNoTypeId; }
        public double getMinBet() { return minBet; }
        public double getOneBetLimit() { return oneBetLimit; }
        public double getOneItemLimit() { return oneItemLimit; }
        public double getOdds1() { return odds1; }
        public double getOdds2() { return odds2; }
        public double getOdds3() { return odds3; }
        public double getOdds4() { return odds4; }
        public double getHoldMoney() { return holdMoney; }
    }

    public AccountConfig() {
        this.memberId = 0;
        this.account = "";
        this.nickName = "";
        this.memberLevel = 0;
        this.credit = 0.0;
        this.creditBalance = 0.0;
        this.bettingSettings = new ArrayList<>();
    }

    public AccountConfig(JSONObject accountData) {
        JSONArray memberArray = accountData.getJSONArray("Member");
        if (memberArray.length() > 0) {
            JSONObject memberInfo = memberArray.getJSONObject(0);
            this.memberId = memberInfo.getInt("member_id");
            this.account = memberInfo.getString("account");
            this.nickName = memberInfo.getString("nick_name");
            this.memberLevel = memberInfo.getInt("member_level");
            this.credit = memberInfo.getDouble("credit");
            this.creditBalance = memberInfo.getDouble("credit_balance");
        }

        this.bettingSettings = new ArrayList<>();
        JSONArray settingArray = accountData.getJSONArray("Setting");
        for (int i = 0; i < settingArray.length(); i++) {
            bettingSettings.add(new BettingSetting(settingArray.getJSONObject(i)));
        }
    }

    public int getMemberId() { return memberId; }
    public String getAccount() { return account; }
    public String getNickName() { return nickName; }
    public int getMemberLevel() { return memberLevel; }
    public double getCredit() { return credit; }
    public double getCreditBalance() { return creditBalance; }
    public List<BettingSetting> getBettingSettings() { return bettingSettings; }

    public void printConfig() {
        System.out.println("\n会员信息:");
        System.out.println("会员ID: " + memberId);
        System.out.println("账号: " + account);
        System.out.println("昵称: " + nickName);
        System.out.println("会员等级: " + memberLevel);
        System.out.println("信用额度: " + credit);
        System.out.println("可用余额: " + creditBalance);

        System.out.println("\n投注设置:");
        for (BettingSetting setting : bettingSettings) {
            System.out.println("\n玩法: " + setting.getNoTypeName());
            System.out.println("最小投注: " + setting.getMinBet());
            System.out.println("单注限额: " + setting.getOneBetLimit());
            System.out.println("单项限额: " + setting.getOneItemLimit());
            System.out.println("赔率: " + setting.getOdds1());
        }
    }

    // 查找投注设置
    public BettingSetting findBettingSetting(String noTypeName) {
        for (BettingSetting setting : bettingSettings) {
            if (setting.getNoTypeName().equals(noTypeName)) {
                return setting;
            }
        }
        return null;
    }

    // 查找投注设置（通过ID）
    public BettingSetting findBettingSettingById(int dictNoTypeId) {
        for (BettingSetting setting : bettingSettings) {
            if (setting.getDictNoTypeId() == dictNoTypeId) {
                return setting;
            }
        }
        return null;
    }

    // 更新投注设置
    public void updateBettingSetting(String noTypeName, double minBet, double oneBetLimit, 
                                   double oneItemLimit, double odds1) {
        BettingSetting setting = findBettingSetting(noTypeName);
        if (setting != null) {
            setting.minBet = minBet;
            setting.oneBetLimit = oneBetLimit;
            setting.oneItemLimit = oneItemLimit;
            setting.odds1 = odds1;
        }
    }

    // 更新会员信息
    public void updateMemberInfo(int memberId, String account, String nickName, 
                               int memberLevel, double credit, double creditBalance) {
        this.memberId = memberId;
        this.account = account;
        this.nickName = nickName;
        this.memberLevel = memberLevel;
        this.credit = credit;
        this.creditBalance = creditBalance;
    }

    // 从JSON更新整个配置
    public void updateFromJson(JSONObject accountData) {
        JSONArray memberArray = accountData.getJSONArray("Member");
        if (memberArray.length() > 0) {
            JSONObject memberInfo = memberArray.getJSONObject(0);
            updateMemberInfo(
                memberInfo.getInt("member_id"),
                memberInfo.getString("account"),
                memberInfo.getString("nick_name"),
                memberInfo.getInt("member_level"),
                memberInfo.getDouble("credit"),
                memberInfo.getDouble("credit_balance")
            );
        }

        // 清空并重新添加投注设置
        bettingSettings.clear();
        JSONArray settingArray = accountData.getJSONArray("Setting");
        for (int i = 0; i < settingArray.length(); i++) {
            bettingSettings.add(new BettingSetting(settingArray.getJSONObject(i)));
        }
    }

    // 检查配置是否有效
    public boolean isValid() {
        return memberId > 0 && !account.isEmpty() && !bettingSettings.isEmpty();
    }

    // 获取所有玩法名称
    public List<String> getAllGameTypes() {
        List<String> gameTypes = new ArrayList<>();
        for (BettingSetting setting : bettingSettings) {
            gameTypes.add(setting.getNoTypeName());
        }
        return gameTypes;
    }

    // 获取特定玩法的限额信息
    public String getLimitInfo(String noTypeName) {
        BettingSetting setting = findBettingSetting(noTypeName);
        if (setting != null) {
            return String.format("玩法: %s\n最小投注: %.2f\n单注限额: %.2f\n单项限额: %.2f",
                setting.getNoTypeName(),
                setting.getMinBet(),
                setting.getOneBetLimit(),
                setting.getOneItemLimit());
        }
        return "未找到该玩法";
    }

    // 获取特定dict_no_type_id的详细信息
    public String getDictNoTypeInfo(int dictNoTypeId) {
        BettingSetting setting = findBettingSettingById(dictNoTypeId);
        if (setting != null) {
            return String.format(
                "玩法ID: %d\n" +
                "玩法名称: %s\n" +
                "固定号码数: %d\n" +
                "最小投注: %.2f\n" +
                "单注限额: %.2f\n" +
                "单项限额: %.2f\n" +
                "赔率1: %.2f\n" +
                "赔率2: %.2f\n" +
                "赔率3: %.2f\n" +
                "赔率4: %.2f\n" +
                "占成金额: %.2f",
                setting.getDictNoTypeId(),
                setting.getNoTypeName(),
                setting.getFixNum(),
                setting.getMinBet(),
                setting.getOneBetLimit(),
                setting.getOneItemLimit(),
                setting.getOdds1(),
                setting.getOdds2(),
                setting.getOdds3(),
                setting.getOdds4(),
                setting.getHoldMoney()
            );
        }
        return "未找到该玩法ID";
    }

    // 获取所有dict_no_type_id
    public List<Integer> getAllDictNoTypeIds() {
        List<Integer> ids = new ArrayList<>();
        for (BettingSetting setting : bettingSettings) {
            ids.add(setting.getDictNoTypeId());
        }
        return ids;
    }

    // 获取dict_no_type_id和玩法名称的映射
    public Map<Integer, String> getDictNoTypeMap() {
        Map<Integer, String> map = new HashMap<>();
        for (BettingSetting setting : bettingSettings) {
            map.put(setting.getDictNoTypeId(), setting.getNoTypeName());
        }
        return map;
    }

    // 根据dict_no_type_id更新投注设置
    public boolean updateBettingSettingById(int dictNoTypeId, double minBet, double oneBetLimit, 
                                          double oneItemLimit, double odds1, double holdMoney) {
        BettingSetting setting = findBettingSettingById(dictNoTypeId);
        if (setting != null) {
            setting.minBet = minBet;
            setting.oneBetLimit = oneBetLimit;
            setting.oneItemLimit = oneItemLimit;
            setting.odds1 = odds1;
            setting.holdMoney = holdMoney;
            return true;
        }
        return false;
    }

    // 根据dict_no_type_id更新所有赔率
    public boolean updateOddsById(int dictNoTypeId, double odds1, double odds2, 
                                double odds3, double odds4) {
        BettingSetting setting = findBettingSettingById(dictNoTypeId);
        if (setting != null) {
            setting.odds1 = odds1;
            setting.odds2 = odds2;
            setting.odds3 = odds3;
            setting.odds4 = odds4;
            return true;
        }
        return false;
    }

    // 根据dict_no_type_id更新限额
    public boolean updateLimitsById(int dictNoTypeId, double minBet, 
                                  double oneBetLimit, double oneItemLimit) {
        BettingSetting setting = findBettingSettingById(dictNoTypeId);
        if (setting != null) {
            setting.minBet = minBet;
            setting.oneBetLimit = oneBetLimit;
            setting.oneItemLimit = oneItemLimit;
            return true;
        }
        return false;
    }

    // 根据dict_no_type_id更新占成金额
    public boolean updateHoldMoneyById(int dictNoTypeId, double holdMoney) {
        BettingSetting setting = findBettingSettingById(dictNoTypeId);
        if (setting != null) {
            setting.holdMoney = holdMoney;
            return true;
        }
        return false;
    }

    // 获取特定dict_no_type_id的占成金额
    public double getHoldMoneyById(int dictNoTypeId) {
        BettingSetting setting = findBettingSettingById(dictNoTypeId);
        return setting != null ? setting.getHoldMoney() : 0.0;
    }

    // 获取所有占成金额
    public Map<Integer, Double> getAllHoldMoney() {
        Map<Integer, Double> holdMoneyMap = new HashMap<>();
        for (BettingSetting setting : bettingSettings) {
            holdMoneyMap.put(setting.getDictNoTypeId(), setting.getHoldMoney());
        }
        return holdMoneyMap;
    }

    // 生成占成金额更新数据
    public String generateHoldMoneyUpdateData() {
        JSONObject data = new JSONObject();
        
        // 创建member对象
        JSONObject member = new JSONObject();
        member.put("contribution_rate", "0");
        member.put("pr_id", "");
        
        // 初始化所有hold_money为0
        for (int i = 0; i <= 13; i++) {
            member.put("hold_money" + i, "0");
        }
        
        // 更新实际存在的hold_money值
        for (BettingSetting setting : bettingSettings) {
            int index = setting.dictNoTypeId - 1;  // dict_no_type_id从1开始，转换为0开始的索引
            if (index >= 0 && index <= 13) {
                member.put("hold_money" + index, String.valueOf(setting.holdMoney));
            }
        }
        
        data.put("member", member);
        data.put("password", "");
        
        // 创建Setting数组
        JSONArray settingArray = new JSONArray();
        for (BettingSetting setting : bettingSettings) {
            JSONObject settingObj = new JSONObject();
            settingObj.put("dict_no_type_id", String.valueOf(setting.dictNoTypeId));
            settingObj.put("hold_money", String.valueOf(setting.holdMoney));
            settingArray.put(settingObj);
        }
        
        data.put("Setting", settingArray);
        return data.toString();
    }
} 