package com.fivebear.platform;
public enum MemberPurpose {
    BETTING("下注"),
    ODDS_FETCH("获取赔率");

    private final String displayName;

    MemberPurpose(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 根据中文名查找枚举
    public static MemberPurpose fromDisplayName(String name) {
        for (MemberPurpose purpose : values()) {
            if (purpose.displayName.equals(name)) {
                return purpose;
            }
        }
        return null;
    }
} 