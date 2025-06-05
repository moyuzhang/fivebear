package com.fivebear.platform;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BetNumberManager {
    private List<BetNumberGroupByType> groupList = new ArrayList<>();

    // 添加单个投注
    public void addBet(BetNumber bet) {
        for (BetNumberGroupByType group : groupList) {
            if (group.getDictNoTypeId() == bet.getDictNoTypeId()) {
                group.getBetNumbers().add(bet);
                group.recalculateTotalBetMoney();
                return;
            }
        }
        // 没有该玩法分组则新建
        List<BetNumber> bets = new ArrayList<>();
        bets.add(bet);
        groupList.add(new BetNumberGroupByType(bet.getDictNoTypeId(), bets));
    }

    // 批量添加
    public void addBets(List<BetNumber> bets) {
        for (BetNumber bet : bets) {
            addBet(bet);
        }
    }

    // 获取所有分组
    public List<BetNumberGroupByType> getAllGroups() {
        return groupList;
    }

    // 按号码查询
    public List<BetNumber> queryByNumber(String betNo) {
        List<BetNumber> result = new ArrayList<>();
        for (BetNumberGroupByType group : groupList) {
            for (BetNumber bet : group.getBetNumbers()) {
                if (bet.getBetNo().equals(betNo)) {
                    result.add(bet);
                }
            }
        }
        return result;
    }

    // 按玩法类型查询
    public List<BetNumber> queryByDictNoTypeId(int dictNoTypeId) {
        for (BetNumberGroupByType group : groupList) {
            if (group.getDictNoTypeId() == dictNoTypeId) {
                return new ArrayList<>(group.getBetNumbers());
            }
        }
        return new ArrayList<>();
    }

    // 统计总投注金额
    public BigDecimal totalBetMoney() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BetNumberGroupByType group : groupList) {
            sum = sum.add(group.getTotalBetMoney());
        }
        return sum;
    }

    // 统计总奖金
    public BigDecimal totalBonus() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BetNumberGroupByType group : groupList) {
            for (BetNumber bet : group.getBetNumbers()) {
                sum = sum.add(bet.getBonus());
            }
        }
        return sum;
    }

    // 统计投注号码总数
    public int totalCount() {
        int count = 0;
        for (BetNumberGroupByType group : groupList) {
            count += group.getBetNumbers().size();
        }
        return count;
    }
}