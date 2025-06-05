package com.fivebear.platform;
public enum LotteryType {
    FIVE("排列五"),
    SEVEN("七星彩"),
    LUCK("幸运五");

    private final String displayName;

    LotteryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 根据中文名查找枚举
    public static LotteryType fromDisplayName(String name) {
        for (LotteryType type : values()) {
            if (type.displayName.equals(name)) {
                return type;
            }
        }
        return null;
    }
}