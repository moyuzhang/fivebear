package com.fivebear.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PackageInfoManager {
    // 新数据结构
    private final Map<Integer, Map<String, Map<Integer, List<SiteOddsInfo>>>> oddsData = new HashMap<>();

    public static class SiteOddsInfo {
        private String domain;
        private double odds;

        public SiteOddsInfo(String domain, double odds) {
            this.domain = domain;
            this.odds = odds;
        }

        public String getDomain() {
            return domain;
        }

        public double getOdds() {
            return odds;
        }
    }

    // domain 分组
    private final Map<String, Map<Long, PackageInfo>> domainToPackages = new HashMap<>();

    // 构造方法设为 public
    public PackageInfoManager() {
    }

    // 添加包牌
    public synchronized void addPackage(PackageInfo pkg) {
        // 新结构
        oddsData
                .computeIfAbsent(pkg.getDictTypeId(), k -> new HashMap<>())
                .computeIfAbsent(pkg.getPackageName(), k -> new HashMap<>())
                .computeIfAbsent(pkg.getPackageCount(), k -> new ArrayList<>())
                .add(new SiteOddsInfo(pkg.getDomain(), pkg.getOddsMemberFinal()));
        // 兼容老结构
        domainToPackages
                .computeIfAbsent(pkg.getDomain(), k -> new HashMap<>())
                .put(pkg.getPackageId(), pkg);
    }

    // 获取某 domain 下所有包牌
    public synchronized List<PackageInfo> getPackagesByDomain(String domain) {
        return domainToPackages.getOrDefault(domain, Collections.emptyMap()).values().stream()
                .collect(Collectors.toList());
    }

    // 获取某 domain 下最高赔率的包牌
    public synchronized PackageInfo getMaxOddsPackageByDomain(String domain) {
        return getPackagesByDomain(domain).stream()
                .max(Comparator.comparingDouble(PackageInfo::getOddsMemberFinal))
                .orElse(null);
    }

    // 获取所有已存在的 PackageName，去重
    public synchronized java.util.Set<String> getAllPackageNames() {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Map<Long, PackageInfo> map : domainToPackages.values()) {
            for (PackageInfo pkg : map.values()) {
                names.add(pkg.getPackageName());
            }
        }
        return names;
    }

    // 根据 PackageId 查询所有的 PackageName（去重）
    public synchronized java.util.Set<String> getPackageNamesByPackageId(long packageId) {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Map<Long, PackageInfo> map : domainToPackages.values()) {
            for (PackageInfo pkg : map.values()) {
                if (pkg.getPackageId() == packageId) {
                    names.add(pkg.getPackageName());
                }
            }
        }
        return names;
    }

    // 根据 PackageName 查找出最高赔率的 PackageInfo（全局）
    public synchronized PackageInfo getMaxOddsPackageByName(String packageName) {
        return domainToPackages.values().stream()
                .flatMap(map -> map.values().stream())
                .filter(pkg -> packageName.equals(pkg.getPackageName()))
                .max(Comparator.comparingDouble(PackageInfo::getOddsMemberFinal))
                .orElse(null);
    }

    // 根据 dictNoTypeId 返回赔率最高的 PackageInfo（全局）
    public synchronized PackageInfo getMaxOddsPackageByDictNoTypeId(int dictNoTypeId) {
        return domainToPackages.values().stream()
                .flatMap(map -> map.values().stream())
                .filter(pkg -> pkg.getDictTypeId() == dictNoTypeId)
                .max(Comparator.comparingDouble(PackageInfo::getOddsMemberFinal))
                .orElse(null);
    }

    // 根据 dictNoTypeId，返回每个 PackageName 下赔率最高的 PackageInfo（全局）
    public synchronized List<PackageInfo> getMaxOddsPackagesByDictNoTypeId(int dictNoTypeId) {
        // 先筛选出所有该玩法的包牌
        return domainToPackages.values().stream()
                .flatMap(map -> map.values().stream())
                .filter(pkg -> pkg.getDictTypeId() == dictNoTypeId)
                .collect(Collectors.groupingBy(PackageInfo::getPackageName,
                        Collectors.maxBy(Comparator.comparingDouble(PackageInfo::getOddsMemberFinal))))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }

    // 根据 dictNoTypeId 和 PackageName 查询最高赔率的 PackageInfo（全局）
    public synchronized PackageInfo getMaxOddsPackageByDictNoTypeIdAndName(int dictNoTypeId, String packageName) {
        return domainToPackages.values().stream()
                .flatMap(map -> map.values().stream())
                .filter(pkg -> pkg.getDictTypeId() == dictNoTypeId && packageName.equals(pkg.getPackageName()))
                .max(Comparator.comparingDouble(PackageInfo::getOddsMemberFinal))
                .orElse(null);
    }

    // 删除指定 domain 下的所有包牌
    public synchronized void removeDomain(String domain) {
        domainToPackages.remove(domain);
    }

    // 删除指定期号下的所有包牌
    public synchronized void removeByPeriodNo(String periodNo) {
        for (Map<Long, PackageInfo> map : domainToPackages.values()) {
            // 用迭代器安全删除
            map.values().removeIf(pkg -> periodNo.equals(pkg.getPeriodNo()));
        }
    }

    // 获取所有的包牌 根据包牌名称去，返回最高赔率的
    public synchronized List<PackageInfo> getSortedMaxOddsPackageDomains() {
        List<PackageInfo> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Map<Integer, List<SiteOddsInfo>>>> typeEntry : oddsData.entrySet()) {
            int dictTypeId = typeEntry.getKey();
            for (Map.Entry<String, Map<Integer, List<SiteOddsInfo>>> pkgEntry : typeEntry.getValue().entrySet()) {
                String packageName = pkgEntry.getKey();
                for (Map.Entry<Integer, List<SiteOddsInfo>> countEntry : pkgEntry.getValue().entrySet()) {
                    int packageCount = countEntry.getKey();
                    List<SiteOddsInfo> siteList = countEntry.getValue();
                    SiteOddsInfo max = siteList.stream().max(Comparator.comparingDouble(SiteOddsInfo::getOdds))
                            .orElse(null);
                    if (max != null) {
                        PackageInfo pkg = new PackageInfo();
                        pkg.setDictTypeId(dictTypeId);
                        pkg.setPackageName(packageName);
                        pkg.setPackageCount(packageCount);
                        pkg.setDomain(max.getDomain());
                        pkg.setOddsMemberFinal(max.getOdds());
                        result.add(pkg);
                    }
                }
            }
        }
        // 可按需要排序
        return result;
    }

    // 清空所有包牌数据
    public synchronized void clear() {
        domainToPackages.clear();
    }
}
