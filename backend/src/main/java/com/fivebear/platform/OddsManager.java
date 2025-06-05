package com.fivebear.platform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OddsManager {
    private final Map<String, NumberOddsGroup> groupMap = new HashMap<>();

    public OddsManager() {
    }

    // 添加或更新赔率
    public synchronized void addOdds(String number, int dictNoTypeId, String domain, double odds,
            String updateDatetime) {
        String key = number + "_" + dictNoTypeId;
        NumberOddsGroup group = groupMap.computeIfAbsent(key, k -> new NumberOddsGroup(number, dictNoTypeId));
        // 可去重：如果同domain已存在则更新，否则添加
        Optional<NumberOddsGroup.OddsInfo> existing = group.getOddsList().stream()
                .filter(info -> info.getDomain().equals(domain))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setOdds(odds);
            existing.get().setUpdateDatetime(updateDatetime);
        } else {
            group.getOddsList().add(new NumberOddsGroup.OddsInfo(domain, odds, updateDatetime));
        }
    }

    // 获取所有聚合后的赔率
    public synchronized List<NumberOddsGroup> getAggregatedOdds() {
        return new ArrayList<>(groupMap.values());
    }

    // 按号码查询聚合赔率
    public synchronized List<NumberOddsGroup> queryByNumber(String number) {
        List<NumberOddsGroup> result = new ArrayList<>();
        for (NumberOddsGroup group : groupMap.values()) {
            if (group.getNumber().equals(number)) {
                result.add(group);
            }
        }
        return result;
    }

    // 可扩展：按号码/玩法/站点查询、清空、导出等
    public synchronized void clear() {
        groupMap.clear();
    }

    //

    public synchronized int getSize() {
        return groupMap.size();
    }

    public synchronized List<NumberOddsGroup.OddsInfo> getMaxOddsForEachNumber() {
        Map<String, NumberOddsGroup.OddsInfo> maxMap = new HashMap<>();
        for (NumberOddsGroup group : groupMap.values()) {
            NumberOddsGroup.OddsInfo maxInfo = group.getMaxOddsInfo();
            if (maxInfo == null)
                continue;
            String number = group.getNumber();
            // 如果同号码有多个玩法/彩种，取全局最大
            if (!maxMap.containsKey(number) || maxInfo.getOdds() > maxMap.get(number).getOdds()) {
                maxMap.put(number, maxInfo);
            }
        }
        return new ArrayList<>(maxMap.values());
    }

    public synchronized List<NumberOddsGroup.OddsInfo> getMaxOddsForDictNoTypeId(int dictNoTypeId) {
        Map<String, NumberOddsGroup.OddsInfo> maxMap = new HashMap<>();
        for (NumberOddsGroup group : groupMap.values()) {
            if (group.getDictNoTypeId() != dictNoTypeId)
                continue;
            NumberOddsGroup.OddsInfo maxInfo = group.getMaxOddsInfo();
            if (maxInfo == null)
                continue;
            String number = group.getNumber();
            if (!maxMap.containsKey(number) || maxInfo.getOdds() > maxMap.get(number).getOdds()) {
                maxMap.put(number, maxInfo);
            }
        }
        return new ArrayList<>(maxMap.values());
    }

    // 添加或更新NumberOddsGroup
    public synchronized void addOrUpdateOddsGroup(NumberOddsGroup group) {
        String key = group.getNumber() + "_" + group.getDictNoTypeId();
        groupMap.put(key, group);
    }

    // 返回指定号码和玩法下最大赔率的info
    public synchronized NumberOddsGroup.OddsInfo getMaxOddsInfo(String number, int dictNoTypeId) {
        String key = number + "_" + dictNoTypeId;
        NumberOddsGroup group = groupMap.get(key);
        if (group == null)
            return null;
        NumberOddsGroup.OddsInfo maxInfo = null;
        for (NumberOddsGroup.OddsInfo info : group.getOddsList()) {
            if (maxInfo == null || info.getOdds() > maxInfo.getOdds()) {
                maxInfo = info;
            }
        }
        return maxInfo;
    }

    public synchronized void removeDomain(String domain) {
        for (NumberOddsGroup group : groupMap.values()) {
            group.getOddsList().removeIf(info -> info.getDomain().equals(domain));
        }
    }
}