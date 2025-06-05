package com.fivebear.platform;
import java.util.ArrayList;
import java.util.List;

/**
 * NumberTemplateGenerator 用于根据特定模板生成号码、判断号码类型等操作。
 * 模板中"口"代表任意数字（0-9），"X"代表字母X。
 * 适用于批量生成、校验和分类号码等场景。
 */
public class NumberTemplateGenerator {
    /**
     * 号码模板数组。
     * "口" 表示任意数字，"X" 表示字母X。
     */
    private static final String[] TEMPLATES = {
            "口口XX", "口X口X", "口XX口", "X口X口", "X口口X", "XX口口",
            "口口口X", "口口X口", "口X口口", "X口口口", "口口口口"
    };

    public static final String[] DICT_NO_TYPE_NAMES = {
            "", // 占位，0无效
            "口口XX", // 1
            "口X口X", // 2
            "口XX口", // 3
            "X口X口", // 4
            "X口口X", // 5
            "XX口口", // 6
            "口口口X", // 7
            "口口X口", // 8
            "口X口口", // 9
            "X口口口", // 10
            "口口口口" // 11
    };

    /**
     * 根据指定模板生成所有可能的号码组合。
     * 
     * @param template 模板字符串（如"口X口X"）
     * @return 所有符合模板的号码列表
     */
    public static List<String> generateNumbers(String template) {
        List<String> result = new ArrayList<>();
        generateRecursive(template.toCharArray(), 0, new StringBuilder(), result);
        return result;
    }

    /**
     * 递归生成所有符合模板的号码。
     * 
     * @param arr     模板字符数组
     * @param pos     当前处理的位置
     * @param current 当前构建的号码
     * @param result  结果列表
     */
    private static void generateRecursive(char[] arr, int pos, StringBuilder current, List<String> result) {
        if (pos == arr.length) {
            result.add(current.toString());
            return;
        }
        if (arr[pos] == '口') {
            for (char d = '0'; d <= '9'; d++) {
                current.append(d);
                generateRecursive(arr, pos + 1, current, result);
                current.deleteCharAt(current.length() - 1);
            }
        } else {
            current.append(arr[pos]);
            generateRecursive(arr, pos + 1, current, result);
            current.deleteCharAt(current.length() - 1);
        }
    }

    /**
     * 根据模板编号获取对应模板下的所有号码。
     * 
     * @param dictNoTypeId 模板编号（1~11）
     * @return 所有符合该模板的号码列表
     * @throws IllegalArgumentException 如果编号超出范围
     */
    public static List<String> getNumbersByDictNoTypeId(int dictNoTypeId) {
        if (dictNoTypeId < 1 || dictNoTypeId > TEMPLATES.length) {
            throw new IllegalArgumentException("dictNoTypeId超出范围");
        }
        String template = TEMPLATES[dictNoTypeId - 1];
        return generateNumbers(template);
    }

    /**
     * 判断给定号码属于哪个模板类型。
     * 
     * @param number 号码字符串
     * @return 模板编号（1~11），不匹配返回-1
     */
    public static int getDictNoTypeIdByNumber(String number) {
        for (int i = 0; i < TEMPLATES.length; i++) {
            if (matchesTemplate(number, TEMPLATES[i])) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * 判断号码是否符合指定模板。
     * 
     * @param number   号码字符串
     * @param template 模板字符串
     * @return 是否匹配
     */
    private static boolean matchesTemplate(String number, String template) {
        if (number.length() != template.length())
            return false;
        for (int i = 0; i < template.length(); i++) {
            char t = template.charAt(i);
            char n = number.charAt(i);
            if (t == '口') {
                if (!Character.isDigit(n))
                    return false;
            } else if (t == 'X') {
                if (n != 'X')
                    return false;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成所有模板下的所有号码。
     * 
     * @return 所有模板下的所有号码列表
     */
    public static List<String> generateAllNumbers() {
        List<String> allNumbers = new ArrayList<>();
        for (String template : TEMPLATES) {
            allNumbers.addAll(generateNumbers(template));
        }
        return allNumbers;
    }

    /**
     * 构建collectBatchBets所需的数据结构
     * 
     * @param numbers      号码列表
     * @param dictNoTypeId 模板编号
     * @param betMoney     下注金额
     * @return List<Map<String, Object>>，每个元素包含dict_no_type_id、bet_no、bet_money
     */
    public static List<java.util.Map<String, Object>> buildCollectBatchBetsData(List<String> numbers, int dictNoTypeId,
            double betMoney) {
        List<java.util.Map<String, Object>> result = new ArrayList<>();
        for (String number : numbers) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("dict_no_type_id", dictNoTypeId);
            map.put("bet_no", number);
            map.put("bet_money", betMoney);
            result.add(map);
        }
        return result;
    }

    /**
     * 构建collectBatchBets所需的数据结构（支持多个模板编号，自动生成所有号码）
     * 
     * @param dictNoTypeIds 模板编号列表
     * @param betMoney      下注金额
     * @return List<Map<String, Object>>，每个元素包含dict_no_type_id、bet_no、bet_money
     */
    public static List<java.util.Map<String, Object>> buildCollectBatchBetsData(List<Integer> dictNoTypeIds,
            double betMoney) {
        List<java.util.Map<String, Object>> result = new ArrayList<>();
        for (int dictNoTypeId : dictNoTypeIds) {
            List<String> numbers = getNumbersByDictNoTypeId(dictNoTypeId);
            result.addAll(buildCollectBatchBetsData(numbers, dictNoTypeId, betMoney));
        }
        return result;
    }

    public static String getDictNoTypeName(int dictNoTypeId) {
        if (dictNoTypeId >= 1 && dictNoTypeId <= 11) {
            return DICT_NO_TYPE_NAMES[dictNoTypeId];
        }
        return "";
    }

    // 生成区间内的double
    public static double getRandomDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    // 生成区间内的int
    public static int getRandomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    // 根据dictNoTypeId生成赔率
    public static double getOddsByDictNoTypeId(int dictNoTypeId) {
        if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
            return getRandomDouble(95.1, 100.0);
        } else if (dictNoTypeId >= 7 && dictNoTypeId <= 10) {
            return getRandomDouble(900.1, 1000.0);
        } else if (dictNoTypeId == 11) {
            return getRandomDouble(9000.1, 10000.0);
        }
        return 0;
    }

    // 根据dictNoTypeId生成随机金额
    public static double getRandomBetMoneyByDictNoTypeId(int dictNoTypeId) {
        if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
            return getRandomDouble(1, 2000.0);
        } else if (dictNoTypeId >= 7 && dictNoTypeId <= 10) {
            return getRandomDouble(0.1, 1000.0);
        } else if (dictNoTypeId == 11) {
            return getRandomDouble(0.1, 150.0);
        }
        return 0;
    }

    // 根据号码生成关联的四星号码
    public static List<String> getRelatedFourStarNumber(String pattern) {
        List<String> result = new ArrayList<>();
        generateAll(pattern.toCharArray(), 0, new StringBuilder(), result);
        return result;
    }

    private static void generateAll(char[] pattern, int idx, StringBuilder current, List<String> result) {
        if (idx == pattern.length) {
            result.add(current.toString());
            return;
        }
        if (pattern[idx] == 'X') {
            for (char d = '0'; d <= '9'; d++) {
                current.append(d);
                generateAll(pattern, idx + 1, current, result);
                current.deleteCharAt(current.length() - 1);
            }
        } else {
            current.append(pattern[idx]);
            generateAll(pattern, idx + 1, current, result);
            current.deleteCharAt(current.length() - 1);
        }
    }

    // 转4定金额转换
    public static double getBetMoneyByDictNoTypeId(int dictNoTypeId, double betMoney) {
        if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
            return betMoney * 0.01;
        } else if (dictNoTypeId >= 7 && dictNoTypeId <= 10) {
            return betMoney * 0.1;
        } else if (dictNoTypeId == 11) {
            return betMoney * 1;
        }
        return betMoney;
    }
}