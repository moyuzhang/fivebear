package com.fivebear.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Site相关通用工具类
 */
public class SiteUtil {
    /**
     * 对线路列表去重、清洗（去空格、统一小写）
     * 
     * @param urls 原始线路列表
     * @return 去重、清洗后的线路列表（保留顺序）
     */
    public static List<String> deduplicateAndCleanLines(List<String> urls) {
        if (urls == null)
            return Collections.emptyList();
        Set<String> seen = new HashSet<>();
        List<String> result = new ArrayList<>();
        for (String url : urls) {
            if (url != null) {
                String clean = url.trim().toLowerCase();
                if (!clean.isEmpty() && seen.add(clean)) {
                    result.add(clean);
                }
            }
        }
        return result;
    }

    /**
     * 单条线路清洗（去空格、统一小写）
     * 
     * @param url 原始线路
     * @return 清洗后的线路
     */
    public static String cleanLine(String url) {
        if (url == null)
            return null;
        return url.trim().toLowerCase();
    }

    /**
     * 判断线路是否有效
     */
    public static boolean isValidLine(String url) {
        return url != null && !url.trim().isEmpty();
    }

    /**
     * 根据给定的url自动生成同组线路列表（如b1~b9，b9加:8443）
     * 参考Python实现
     * 
     * @param url 任意一条组内线路
     * @return 该组所有线路
     */
    public static List<String> generateLineGroup(String url) {
        if (url == null || url.trim().isEmpty())
            return new ArrayList<>();
        String tempUrl = url.trim();
        List<String> urls = new ArrayList<>();
        Pattern p = Pattern.compile("(\\d)");
        for (int i = 1; i <= 9; i++) {
            Matcher m = p.matcher(tempUrl);
            String replaced = m.replaceFirst(String.valueOf(i));
            if (i == 9) {
                // 强制去掉已有端口，只保留主域名
                int colonIdx = replaced.indexOf(':', replaced.indexOf("://") + 3);
                if (colonIdx > 0) {
                    replaced = replaced.substring(0, colonIdx);
                }
                replaced += ":8443";
            }
            urls.add(replaced);
        }
        return urls;
    }

    // 解析JSON响应
    public static org.json.JSONObject parseJsonResponse(String response) {
        try {
            return new org.json.JSONObject(response);
        } catch (org.json.JSONException e) {
            return null;
        }
    }
}