package com.fivebear.platform;
import java.util.ArrayList;
import java.util.List;

public class NumberOddsGroup {
    private String number;
    private int dictNoTypeId;
    private List<OddsInfo> oddsList = new ArrayList<>();

    public static class OddsInfo {
        private String domain;
        private double odds;
        private String updateDatetime;

        public OddsInfo(String domain, double odds, String updateDatetime) {
            this.domain = domain;
            this.odds = odds;
            this.updateDatetime = updateDatetime;
        }
        public String getDomain() { return domain; }
        public double getOdds() { return odds; }
        public String getUpdateDatetime() { return updateDatetime; }
        public void setDomain(String domain) { this.domain = domain; }
        public void setOdds(double odds) { this.odds = odds; }
        public void setUpdateDatetime(String updateDatetime) { this.updateDatetime = updateDatetime; }
    }

    public NumberOddsGroup(String number, int dictNoTypeId) {
        this.number = number;
        this.dictNoTypeId = dictNoTypeId;
    }

    public NumberOddsGroup(int dictNoTypeId, String number, List<OddsInfo> oddsList) {
        this.dictNoTypeId = dictNoTypeId;
        this.number = number;
        this.oddsList = oddsList;
    }

    public String getNumber() { return number; }
    public int getDictNoTypeId() { return dictNoTypeId; }
    public List<OddsInfo> getOddsList() { return oddsList; }
    public void setNumber(String number) { this.number = number; }
    public void setDictNoTypeId(int dictNoTypeId) { this.dictNoTypeId = dictNoTypeId; }
    public void setOddsList(List<OddsInfo> oddsList) { this.oddsList = oddsList; }

    public OddsInfo getMaxOddsInfo() {
        if (oddsList == null || oddsList.isEmpty()) return null;
        OddsInfo max = oddsList.get(0);
        for (OddsInfo info : oddsList) {
            if (info.getOdds() > max.getOdds()) {
                max = info;
            }
        }
        return max;
    }
} 