package com.fivebear.platform;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {
    private static final int MAX_CONSECUTIVE_FAILURES = 3; // 最大连续失败次数
    private static final int MAX_REQUESTS_PER_PROXY = 6; // 每个代理最大请求次数

    private static SiteContent site;
    private static Proxy currentProxy;
    private static int totalConsecutiveFailures = 0;
    private static int currentProxyRequestCount = 0; // 当前代理IP的请求次数

    private Map<String, NumberOddsGroup> oddsGroupMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // testPlatformManager();
        // 构建 Map<Integer, List<Bet>> 输入数据
        // testPackageAnalyzer();
        //testprocessBetScheme();
        fly();
    }

    private static void testprocessBetScheme() {
        System.err.println("开始测试");
        List<Bet> bets = new ArrayList<>();
        // 随机生成4000-9000组唯一号码
        int numBets = (int) (Math.random() * (14600 - 4000 + 1)) + 4000;
        System.err.println(String.format("生成投注方案,注数: %d", numBets));
        // 总金额
        List<String> oddsBets = NumberTemplateGenerator.generateAllNumbers();
        //随机排序
        Collections.shuffle(oddsBets);
        //随机取
        for(int i = 0; i < numBets; i++)
        {
            String number = oddsBets.get(i);
            double amount = NumberTemplateGenerator.getRandomBetMoneyByDictNoTypeId(NumberTemplateGenerator.getDictNoTypeIdByNumber(number));
            bets.add(new Bet(number, amount));
        }
        System.err.println("生成投注方案");
        PlatformManager platformManager = new PlatformManager();
        platformManager.addSiteCallbackListener(new SiteCallback() {
            @Override
            public void onMessage(Site site, String message, MessageType messageType) {
                System.out.println("收到消息: " + message);
            }

            @Override
            public void onPackageOddsInfo(Site site, List<PackageInfo> packageInfoList) {
            }

            @Override
            public void onRebateRateChanged(Site site, double newRate) {
            }

            @Override
            public void onStatusChanged(Site site, SiteStatus newStatus) {
            }

            @Override
            public void onNumOddsInfo(Site site, JSONArray oddsJsonArray) {
            }
        });
        System.err.println("增加所有号码的赔率到多个 domain");
        // 增加所有号码的赔率到多个 domain
        List<String> domains = new ArrayList<>();
        domains.add("domain1");
        domains.add("domain2");
        domains.add("domain3");
        domains.add("domain4");

        //生成包牌方案
        for(String domain : domains)
        {
            for(int i = 7; i < 11; i++)
            {
                PackageInfo pkg200 = new PackageInfo();
                pkg200.setDictTypeId(i);
                pkg200.setPackageId(i * 1000 + 1);
                pkg200.setPackageName("200组");
                pkg200.setPackageCount(200);
                pkg200.setOddsSetting(980.00000);
                pkg200.setOddsMemberFinal(980.00000);
                pkg200.setDomain(domain);

                PackageInfo pkg250 = new PackageInfo();
                pkg250.setDictTypeId(i);
                pkg250.setPackageId(i * 1000 + 2);
                pkg250.setPackageName("250组");
                pkg250.setPackageCount(250);
                pkg250.setOddsSetting(981.00000);
                pkg250.setOddsMemberFinal(981.00000);
                pkg250.setDomain(domain);

                PackageInfo pkg300 = new PackageInfo();
                pkg300.setDictTypeId(i);
                pkg300.setPackageId(i * 1000 + 3);
                pkg300.setPackageName("300组");
                pkg300.setPackageCount(300);
                pkg300.setOddsSetting(982.00000);
                pkg300.setOddsMemberFinal(982.00000);
                pkg300.setDomain(domain);

                PackageInfo pkg350 = new PackageInfo();
                pkg350.setDictTypeId(i);
                pkg350.setPackageId(i * 1000 + 4);
                pkg350.setPackageName("350组");
                pkg350.setPackageCount(350);
                pkg350.setOddsSetting(983.00000);
                pkg350.setOddsMemberFinal(983.00000);
                pkg350.setDomain(domain);

                PackageInfo pkg400 = new PackageInfo();
                pkg400.setDictTypeId(i);
                pkg400.setPackageId(i * 1000 + 5);
                pkg400.setPackageName("400组");
                pkg400.setPackageCount(400);
                pkg400.setOddsSetting(983.00000);
                pkg400.setOddsMemberFinal(983.00000);
                pkg400.setDomain(domain);

                PackageInfo pkg450 = new PackageInfo();
                pkg450.setDictTypeId(i);
                pkg450.setPackageId(i * 1000 + 6);
                pkg450.setPackageName("450组");
                pkg450.setPackageCount(450);
                pkg450.setOddsSetting(983.00000);
                pkg450.setOddsMemberFinal(983.00000);
                pkg450.setDomain(domain);

                PackageInfo pkg500 = new PackageInfo();
                pkg500.setDictTypeId(i);
                pkg500.setPackageId(i * 1000 + 7);
                pkg500.setPackageName("500组");
                pkg500.setPackageCount(500);
                pkg500.setOddsSetting(984.00000);
                pkg500.setOddsMemberFinal(984.00000);
                pkg500.setDomain(domain);

                PackageInfo pkg600 = new PackageInfo();
                pkg600.setDictTypeId(i);
                pkg600.setPackageId(i * 1000 + 8);
                pkg600.setPackageName("600组");
                pkg600.setPackageCount(600);
                pkg600.setOddsSetting(984.00000);
                pkg600.setOddsMemberFinal(984.00000);
                pkg600.setDomain(domain);

                PackageInfo pkg700 = new PackageInfo();
                pkg700.setDictTypeId(i);
                pkg700.setPackageId(i * 1000 + 9);
                pkg700.setPackageName("700组");
                pkg700.setPackageCount(700);
                pkg700.setOddsSetting(985.00000);
                pkg700.setOddsMemberFinal(985.00000);
                pkg700.setDomain(domain);

                PackageInfo pkg800 = new PackageInfo();
                pkg800.setDictTypeId(i);
                pkg800.setPackageId(i * 1000 + 10);
                pkg800.setPackageName("800组");
                pkg800.setPackageCount(800);
                pkg800.setOddsSetting(985.00000);
                pkg800.setOddsMemberFinal(985.00000);
                pkg800.setDomain(domain);

                platformManager.getPackageInfoManager().addPackage(pkg200);
                platformManager.getPackageInfoManager().addPackage(pkg250);
                platformManager.getPackageInfoManager().addPackage(pkg300);
                platformManager.getPackageInfoManager().addPackage(pkg350);
                platformManager.getPackageInfoManager().addPackage(pkg400);
                platformManager.getPackageInfoManager().addPackage(pkg450);
                platformManager.getPackageInfoManager().addPackage(pkg500);
                platformManager.getPackageInfoManager().addPackage(pkg600);
                platformManager.getPackageInfoManager().addPackage(pkg700);
                platformManager.getPackageInfoManager().addPackage(pkg800);

            }

            // 获取所有包牌
            PackageInfo pkg1000 = new PackageInfo();
            pkg1000.setDictTypeId(11);
            pkg1000.setPackageId(1101000);
            pkg1000.setPackageName("1000组");
            pkg1000.setPackageCount(1000);
            pkg1000.setOddsSetting(9810.0);
            pkg1000.setOddsMemberFinal(9810.0);
            pkg1000.setDomain(domain);

            PackageInfo pkg2000 = new PackageInfo();
            pkg2000.setDictTypeId(11);
            pkg2000.setPackageId(1102000);
            pkg2000.setPackageName("2000组");
            pkg2000.setPackageCount(2000);
            pkg2000.setOddsSetting(9820.0);
            pkg2000.setOddsMemberFinal(9830.0);
            pkg2000.setDomain(domain);

            PackageInfo pkg3000 = new PackageInfo();
            pkg3000.setDictTypeId(11);
            pkg3000.setPackageId(1103000);
            pkg3000.setPackageName("3000组");
            pkg3000.setPackageCount(3000);
            pkg3000.setOddsSetting(9840.0);
            pkg3000.setOddsMemberFinal(9840.0);
            pkg3000.setDomain(domain);

            PackageInfo pkg3500 = new PackageInfo();
            pkg3500.setDictTypeId(11);
            pkg3500.setPackageId(1103500);
            pkg3500.setPackageName("3500组");
            pkg3500.setPackageCount(3500);
            pkg3500.setOddsSetting(9850.0);
            pkg3500.setOddsMemberFinal(9850.0);
            pkg3500.setDomain(domain);

            PackageInfo pkg4000 = new PackageInfo();
            pkg4000.setDictTypeId(11);
            pkg4000.setPackageId(1104000);
            pkg4000.setPackageName("4000组");
            pkg4000.setPackageCount(4000);
            pkg4000.setOddsSetting(9860.0);
            pkg4000.setOddsMemberFinal(9860.0);
            pkg4000.setDomain(domain);

            PackageInfo pkg4500 = new PackageInfo();
            pkg4500.setDictTypeId(11);
            pkg4500.setPackageId(1104500);
            pkg4500.setPackageName("4500组");
            pkg4500.setPackageCount(4500);
            pkg4500.setOddsSetting(9870.0);
            pkg4500.setOddsMemberFinal(9870.0);
            pkg4500.setDomain(domain);

            PackageInfo pkg5000 = new PackageInfo();
            pkg5000.setDictTypeId(11);
            pkg5000.setPackageId(1105000);
            pkg5000.setPackageName("5000组");
            pkg5000.setPackageCount(5000);
            pkg5000.setOddsSetting(9880.0);
            pkg5000.setOddsMemberFinal(9880.0);
            pkg5000.setDomain(domain);

            PackageInfo pkg6000 = new PackageInfo();
            pkg6000.setDictTypeId(11);
            pkg6000.setPackageId(1106000);
            pkg6000.setPackageName("6000组");
            pkg6000.setPackageCount(6000);
            pkg6000.setOddsSetting(9890.0);
            pkg6000.setOddsMemberFinal(9890.0);
            pkg6000.setDomain(domain);

            PackageInfo pkg7000 = new PackageInfo();
            pkg7000.setDictTypeId(11);
            pkg7000.setPackageId(1107000);
            pkg7000.setPackageName("7000组");
            pkg7000.setPackageCount(7000);
            pkg7000.setOddsSetting(9900.0);
            pkg7000.setOddsMemberFinal(9900.0);
            pkg7000.setDomain(domain);

            PackageInfo pkg8000 = new PackageInfo();
            pkg8000.setDictTypeId(11);
            pkg8000.setPackageId(1108000);
            pkg8000.setPackageName("8000组");
            pkg8000.setPackageCount(8000);
            pkg8000.setOddsSetting(9910.0);
            pkg8000.setOddsMemberFinal(9910.0);
            pkg8000.setDomain(domain);

            platformManager.getPackageInfoManager().addPackage(pkg1000);
            platformManager.getPackageInfoManager().addPackage(pkg2000);
            platformManager.getPackageInfoManager().addPackage(pkg3000);
            platformManager.getPackageInfoManager().addPackage(pkg3500);
            platformManager.getPackageInfoManager().addPackage(pkg4000);
            platformManager.getPackageInfoManager().addPackage(pkg4500);
            platformManager.getPackageInfoManager().addPackage(pkg5000);
            platformManager.getPackageInfoManager().addPackage(pkg6000);
            platformManager.getPackageInfoManager().addPackage(pkg7000);
            platformManager.getPackageInfoManager().addPackage(pkg8000);
            
        }
        
        String datatime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        //List<String> oddsBets = NumberTemplateGenerator.generateAllNumbers();
        System.err.println("生成所有号码的赔率" + oddsBets.size());
        for (String item : oddsBets) {
            Bet bet = new Bet(item, 0);
            int dictNoTypeId = bet.getDictNoTypeId();
            for(String domain : domains)
            {
                platformManager.getOddsManager().addOdds(bet.getNumber(), dictNoTypeId, domain, NumberTemplateGenerator.getOddsByDictNoTypeId(dictNoTypeId), datatime);
            }
        }
        System.err.println("处理投注方案");
        platformManager.processBetScheme(bets);
    }

    private static void testPackageAnalyzer() {
        List<Bet> bets = new ArrayList<>();
        // 随机生成4000-9000组唯一号码
        int numBets = (int) (Math.random() * (9000 - 4000 + 1)) + 4000;
        // 总金额
        double totalAmount = 0.0;
        java.util.Set<String> usedNumbers = new java.util.HashSet<>();
        while (usedNumbers.size() < numBets) {
            String number = generateTestNumbers(1, 4).get(0);
            if (usedNumbers.add(number)) {
                double amount = Math.round((0.1 + Math.random() * 300.9) * 10.0) / 10.0;
                bets.add(new Bet(number, amount, 11));
                totalAmount += amount;
            }
        }
        // 生成赔率
        for (Bet bet : bets) {
            bet.setOdds(NumberTemplateGenerator.getOddsByDictNoTypeId(bet.getDictNoTypeId()));
        }
        // 总下注金额
        double totalBetAmount = bets.stream().mapToDouble(b -> b.getBetMoney()).sum();
        // 平均赔率
        double avgOdds = bets.stream().mapToDouble(b -> b.getOdds()).average().orElse(0.0);
        BetSettlementManager winManager = new BetSettlementManager();
        winManager.addInBets(bets);

        System.out.println(String.format("总金额: %.2f", totalAmount));
        System.out.println("投注数量: " + numBets);
        // 排序
        bets.sort(Comparator.comparingDouble(Bet::getBetMoney));
        // 输出最小
        Bet minBet = bets.get(0);
        System.out.println("最小金额的Bet: " + minBet.getNumber() + " 投注金额:" + minBet.getBetMoney() + " 玩法:"
                + minBet.getDictNoTypeId());
        // 输出最高金额的Bet
        Bet maxBet = bets.get(bets.size() - 1);
        System.out.println("最高金额的Bet: " + maxBet.getNumber() + " 投注金额:" + maxBet.getBetMoney() + " 玩法:"
                + maxBet.getDictNoTypeId());
        System.out.println(String.format("包牌数: %d", (int) (totalAmount / maxBet.getBetMoney())));
        // 输出金额小于1的个数
        int smallBetCount = (int) bets.stream().filter(b -> b.getBetMoney() < 1).count();
        System.out.println("金额小于1的个数: " + smallBetCount);

        // 构建测试的包牌等级列表
        List<PackageInfo> packageLevels = new ArrayList<>();

        PackageInfo pkg1000 = new PackageInfo();
        pkg1000.setDictTypeId(11);
        pkg1000.setPackageId(1101000);
        pkg1000.setPackageName("1000组");
        pkg1000.setPackageCount(1000);
        pkg1000.setOddsBatchMin(9910.0);
        pkg1000.setOddsSetting(9810.0);
        packageLevels.add(pkg1000);

        PackageInfo pkg2000 = new PackageInfo();
        pkg2000.setDictTypeId(11);
        pkg2000.setPackageId(1102000);
        pkg2000.setPackageName("2000组");
        pkg2000.setPackageCount(2000);
        pkg2000.setOddsBatchMin(9920.0);
        pkg2000.setOddsSetting(9820.0);
        packageLevels.add(pkg2000);

        PackageInfo pkg3000 = new PackageInfo();
        pkg3000.setDictTypeId(11);
        pkg3000.setPackageId(1103000);
        pkg3000.setPackageName("3000组");
        pkg3000.setPackageCount(3000);
        pkg3000.setOddsBatchMin(9930.0);
        pkg3000.setOddsSetting(9830.0);
        packageLevels.add(pkg3000);

        PackageInfo pkg3500 = new PackageInfo();
        pkg3500.setDictTypeId(11);
        pkg3500.setPackageId(1103500);
        pkg3500.setPackageName("3500组");
        pkg3500.setPackageCount(3500);
        pkg3500.setOddsBatchMin(9935.0);
        pkg3500.setOddsSetting(9840.0);
        packageLevels.add(pkg3500);

        PackageInfo pkg4000 = new PackageInfo();
        pkg4000.setDictTypeId(11);
        pkg4000.setPackageId(1104000);
        pkg4000.setPackageName("4000组");
        pkg4000.setPackageCount(4000);
        pkg4000.setOddsBatchMin(9940.0);
        pkg4000.setOddsSetting(9850.0);
        packageLevels.add(pkg4000);

        PackageInfo pkg4500 = new PackageInfo();
        pkg4500.setDictTypeId(11);
        pkg4500.setPackageId(1104500);
        pkg4500.setPackageName("4500组");
        pkg4500.setPackageCount(4500);
        pkg4500.setOddsBatchMin(9945.0);
        pkg4500.setOddsSetting(9860.0);
        packageLevels.add(pkg4500);

        PackageInfo pkg5000 = new PackageInfo();
        pkg5000.setDictTypeId(11);
        pkg5000.setPackageId(1105000);
        pkg5000.setPackageName("5000组");
        pkg5000.setPackageCount(5000);
        pkg5000.setOddsBatchMin(9950.0);
        pkg5000.setOddsSetting(9870.0);
        packageLevels.add(pkg5000);

        PackageInfo pkg6000 = new PackageInfo();
        pkg6000.setDictTypeId(11);
        pkg6000.setPackageId(1106000);
        pkg6000.setPackageName("6000组");
        pkg6000.setPackageCount(6000);
        pkg6000.setOddsBatchMin(9960.0);
        pkg6000.setOddsSetting(9880.0);
        packageLevels.add(pkg6000);

        PackageInfo pkg7000 = new PackageInfo();
        pkg7000.setDictTypeId(11);
        pkg7000.setPackageId(1107000);
        pkg7000.setPackageName("7000组");
        pkg7000.setPackageCount(7000);
        pkg7000.setOddsBatchMin(9970.0);
        pkg7000.setOddsSetting(9890.0);
        packageLevels.add(pkg7000);

        PackageInfo pkg8000 = new PackageInfo();
        pkg8000.setDictTypeId(11);
        pkg8000.setPackageId(1108000);
        pkg8000.setPackageName("8000组");
        pkg8000.setPackageCount(8000);
        pkg8000.setOddsBatchMin(9980.0);
        pkg8000.setOddsSetting(9900.0);
        packageLevels.add(pkg8000);

        double maxSingleBetAmount = 50.0; // 例如单注最高50元
        boolean allowSmallBet = false;
        PackageAnalyzer.PackageGroupResult results = PackageAnalyzer.splitToPackageGroups(
                bets, packageLevels, allowSmallBet, maxSingleBetAmount);

        BetSettlementManager betSettlementManager = new BetSettlementManager();

        // 散货ungroupedBets
        List<Bet> ungroupedBets = results.getUngroupedBets();
        System.out.println("散货数量: " + ungroupedBets.size());
        System.out.println("散货金额: " + ungroupedBets.stream().mapToDouble(Bet::getBetMoney).sum());
        // 包牌等级
        for (PackageAnalyzer.PackageGroup group : results.getGroups()) {
            System.out.println(String.format("包牌等级: %s, 包牌数量: %d, 包牌数量: %d",
                    group.getPackageInfo().getPackageName(),
                    group.getPackageInfo().getPackageCount(),
                    group.getBets().size()));
            for (Bet bet : group.getBets()) {
                bet.setOdds(group.getPackageInfo().getOddsSetting());
                betSettlementManager.addOutBet(bet);
            }
        }

        // ...（后续统计和输出可根据需要补充）...
        for (Bet bet : ungroupedBets) {
            bet.setOdds(NumberTemplateGenerator.getOddsByDictNoTypeId(bet.getDictNoTypeId()));
            betSettlementManager.addOutBet(bet);

        }
        betSettlementManager.addInBets(bets);

        betSettlementManager.analyzeNegative();
        for (Bet bet : betSettlementManager.getInBets()) {
            System.out.println(String.format("入货号码: %s, 金额: %.2f, 盈亏: %.2f",
                    bet.getNumber(),
                    bet.getBetMoney(),
                    bet.getNegative()));
            List<Bet> outBets = betSettlementManager.getOutBetsByNumber(bet.getNumber());
            // 出货总奖金
            double outTotalWinMoney = outBets.stream().mapToDouble(b -> b.getWinMoney()).sum();
            for (Bet bet2 : outBets) {
                System.out.println(String.format("出货号码: %s, 金额: %.2f, 盈亏: %.2f 赔率: %.2f 是否包牌: %s",
                        bet2.getNumber(),
                        bet2.getBetMoney(),
                        bet2.getNegative(),
                        bet2.getOdds(),
                        bet2.isPositive() ? "包牌" : "散货"));
            }
            // 出货盈亏
            double outTotalNegative = outTotalWinMoney - betSettlementManager.getTotalOutAmount();
            System.out.println(String.format("出货盈亏: %.2f", outTotalNegative));
            System.out.println(String.format("总盈亏: %.2f", bet.getNegative() + outTotalNegative));

        }
    }

    /**
     * 生成指定数量、指定长度的随机数字号码
     */
    public static List<String> generateTestNumbers(int count, int length) {
        List<String> numbers = new ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                sb.append(random.nextInt(10));
            }
            numbers.add(sb.toString());
        }
        return numbers;
    }

    private static void testPlatformManager() {
        PlatformManager platformManager = new PlatformManager();
        platformManager.addSiteCallbackListener(new SiteCallback() {
            @Override
            public void onMessage(Site site, String message, MessageType messageType) {
                System.out.println("收到消息: " + message);
                if (message == "同步所有号码赔率完成") {
                    List<String> queryNumbers = new ArrayList<>();
                    queryNumbers.add("12XX");
                    queryNumbers.add("0000");
                    queryNumbers.add("1548");
                    queryNumbers.add("9003");
                    queryNumbers.add("9999");

                    for (String number : queryNumbers) {
                        List<NumberOddsGroup> groups = platformManager.getOddsManager().queryByNumber(number);
                        for (NumberOddsGroup group : groups) {
                            NumberOddsGroup.OddsInfo maxOdds = group.getMaxOddsInfo();
                            System.out.println(String.format("号码: %s, 玩法: %d, 赔率: %.3f",
                                    group.getNumber(),
                                    group.getDictNoTypeId(),
                                    maxOdds != null ? maxOdds.getOdds() : 0.0));
                        }
                    }
                }
            }

            @Override
            public void onPackageOddsInfo(Site site, List<PackageInfo> packageInfoList) {

            }

            @Override
            public void onRebateRateChanged(Site site, double newRate) {
                System.out.println("返点变更: " + newRate);
            }

            @Override
            public void onStatusChanged(Site site, SiteStatus newStatus) {
                System.out.println("状态变更: " + newStatus);
            }

            @Override
            public void onNumOddsInfo(Site site, JSONArray oddsJsonArray) {
                // System.out.println("收到号码赔率: " + oddsJsonArray);
            }
        });
        SiteManager manager = platformManager.getSiteManager();
        String username = "moyu01";
        String password = "Aa112233";
        String baseUrl = "https://f8.pt755b62.xyz";
        FengHuangMember fengHuangMember = new FengHuangMember(username, password, baseUrl, LotteryType.FIVE, 0.013,"1");
        fengHuangMember.setPurpose(MemberPurpose.ODDS_FETCH);
        platformManager.registerSite(fengHuangMember);
        if (fengHuangMember.login()) {
            System.out.println(String.format("登录成功，期号：%s", fengHuangMember.getPeriodNo()));
            System.out.println(fengHuangMember.getMemberInfo());
            fengHuangMember.heartbeat();
            syncAllOdds(fengHuangMember);
            // fengHuangMember.logout();
        }
        FengHuangMember fengHuangMember2 = new FengHuangMember("moyu02", "Aa112233", "https://f1.pw3r3521.xyz/",
                LotteryType.FIVE, 0.013,"1");
        platformManager.registerSite(fengHuangMember2);
        if (fengHuangMember2.login()) {
            System.out.println(String.format("登录成功，期号：%s", fengHuangMember2.getPeriodNo()));
            System.out.println(fengHuangMember2.getMemberInfo());
            fengHuangMember2.heartbeat();
            syncAllOdds(fengHuangMember2);
            // fengHuangMember2.logout();
        }

        FengHuangMember fengHuangMember3 = new FengHuangMember("moyu02i", "Aa112233", "https://f1.p26cf282.xyz/",
                LotteryType.FIVE, 0.008,"1");
        platformManager.registerSite(fengHuangMember3);
        if (fengHuangMember3.login()) {
            System.out.println(String.format("登录成功，期号：%s", fengHuangMember3.getPeriodNo()));
            System.out.println(fengHuangMember3.getMemberInfo());
            fengHuangMember3.heartbeat();
            syncAllOdds(fengHuangMember3);
        }

        FengHuangMember fengHuangMember4 = new FengHuangMember("moyu02", "Aa147258", "https://f1.pr9b63j1.xyz/",
                LotteryType.FIVE, 0.013,"1");
        platformManager.registerSite(fengHuangMember4);
        if (fengHuangMember4.login()) {
            System.out.println(String.format("登录成功，期号：%s", fengHuangMember4.getPeriodNo()));
            System.out.println(fengHuangMember4.getMemberInfo());
            fengHuangMember4.heartbeat();
            syncAllOdds(fengHuangMember4);
        }

        FengHuangMember fengHuangMember5 = new FengHuangMember("moyu011", "Aa112233", "https://f1.pr9b63j1.xyz/",
                LotteryType.FIVE, 0.013,"1");
        platformManager.registerSite(fengHuangMember5);
        if (fengHuangMember5.login()) {
            System.out.println(String.format("登录成功，期号：%s", fengHuangMember5.getPeriodNo()));
            System.out.println(fengHuangMember5.getMemberInfo());
            fengHuangMember5.heartbeat();
            syncAllOdds(fengHuangMember5);
        }

        FengHuangMember fengHuangMember6 = new FengHuangMember("moyu01", "Aa112233", "https://f1.ps888a62.xyz/",
                LotteryType.FIVE, 0.008,"1");
        platformManager.registerSite(fengHuangMember6);
        if (fengHuangMember6.login()) {
            System.out.println(String.format("登录成功，期号：%s", fengHuangMember6.getPeriodNo()));
            System.out.println(fengHuangMember6.getMemberInfo());
            fengHuangMember6.heartbeat();
            syncAllOdds(fengHuangMember6);
        }
        System.out.println(String.format("站点数量: %d", platformManager.getSiteManager().getAllMemberSites().size()));
        // PackageInfoManager.getInstance().getAllPackageNames().forEach(name -> {
        // PackageInfo pkgInfo =
        // PackageInfoManager.getInstance().getMaxOddsPackageByName(name);
        // if(pkgInfo != null)
        // {
        // System.out.println(String.format("域名: %s, ID: %d, 名称: %s, 赔率: %.3f",
        // pkgInfo.getDomain(),
        // pkgInfo.getDictNoTypeId(),
        // pkgInfo.getPackageName(),
        // pkgInfo.getOddsMemberFinal()
        // ));
        // }
        // });

        // System.out.println("输出所有包牌项");
        // System.err.println("--------------------------------");
        // List<PackageInfo> pkgList =
        // platformManager.getPackageInfoManager().getSortedMaxOddsPackageDomains();
        // for (PackageInfo pkgInfo : pkgList) {
        // System.out.println(String.format("域名: %s, ID: %d, 名称: %s, 赔率: %.3f ,组数: %d",
        // pkgInfo.getDomain(),
        // pkgInfo.getDictTypeId(),
        // pkgInfo.getPackageName(),
        // pkgInfo.getOddsMemberFinal(),
        // pkgInfo.getPackageCount()));
        // }

        // System.err.println("--------------------------------");

        // for (NumberOddsGroup group : groups) {
        // NumberOddsGroup.OddsInfo maxOdds = group.getMaxOddsInfo();
        // System.out.println(String.format("号码: %s, 玩法: %d, 赔率: %.3f",
        // group.getNumber(),
        // group.getDictNoTypeId(),
        // maxOdds != null ? maxOdds.getOdds() : 0.0));
        // }

        // 查找所有MemberSite 输入所有额度总和
        double totalCredit = platformManager.getSiteManager().getAllMemberSites().stream()
                .mapToDouble(site -> site.getMemberInfo().getCredit())
                .sum();
        System.out.println("所有站点额度总和: " + totalCredit);

        Map<Integer, List<Bet>> input = new HashMap<>();
        for (int dictNoTypeId = 1; dictNoTypeId <= 11; dictNoTypeId++) {
            List<String> numbers = NumberTemplateGenerator.getNumbersByDictNoTypeId(dictNoTypeId);
            List<Bet> bets = new ArrayList<>();
            for (String number : numbers) {
                BigDecimal betMoney;
                BigDecimal odds;
                if (dictNoTypeId > 6) {
                    betMoney = BetUtils.randomDouble(0.1, 55.0).setScale(1, BigDecimal.ROUND_DOWN);
                } else {
                    betMoney = BigDecimal.valueOf(BetUtils.randomInt(1, 2001));
                }
                if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
                    odds = BetUtils.randomOdds(90, 100);
                } else if (dictNoTypeId >= 7 && dictNoTypeId <= 10) {
                    odds = BetUtils.randomOdds(900, 1000);
                } else {
                    odds = BetUtils.randomOdds(8000, 10000);
                }
                bets.add(new Bet(number, betMoney.doubleValue(), dictNoTypeId)); // Bet构造如需BigDecimal请调整
            }
            input.put(dictNoTypeId, bets);
        }
        BetScheduler scheduler = new BetScheduler(platformManager);
        BetScheduler.AllocationResult result = scheduler.allocateBetsByBestOdds(input);
        List<BetScheduler.SiteBets> siteBetsList = result.siteBetsList;
        List<Bet> unassignedBets = result.unassignedBets;
        for (BetScheduler.SiteBets siteBets : siteBetsList) {
            System.out.println(siteBets.toString());
        }

        // 统计金额
        double inputTotal = input.values().stream()
                .flatMap(List::stream)
                .mapToDouble(b -> b.getBetMoney())
                .sum();
        double assignedTotal = siteBetsList.stream()
                .flatMap(sb -> sb.getBets().stream())
                .mapToDouble(b -> b.getBetMoney())
                .sum();
        double unassignedTotal = unassignedBets.stream()
                .mapToDouble(b -> b.getBetMoney())
                .sum();
        System.out.println("输入总金额: " + inputTotal);
        System.out.println("分配总金额: " + assignedTotal);
        System.out.println("未分配总金额: " + unassignedTotal);
        System.out.println("金额校验: " + (Math.abs(inputTotal - assignedTotal - unassignedTotal) < 1e-6));

        // 统计号码
        long inputNumberCount = input.values().stream().flatMap(List::stream).count();
        long assignedUniqueNumbers = siteBetsList.stream()
                .flatMap(sb -> sb.getBets().stream())
                .map(b -> b.getNumber())
                .distinct()
                .count();
        long unassignedNumberCount = unassignedBets.size();
        System.out.println("输入号码总数: " + inputNumberCount);
        System.out.println("分配到的不同号码数: " + assignedUniqueNumbers);
        System.out.println("未分配号码数: " + unassignedNumberCount);
        // 输出未分配的号码
        int count = 0;
        for (Bet bet : unassignedBets) {
            if (count < 10) {
                System.out.println(String.format("前10条未分配号码: %s, 金额: %.2f", bet.getNumber(), bet.getBetMoney()));
                count++;
            }
        }
        count = 0;
        for (int i = Math.max(0, unassignedBets.size() - 10); i < unassignedBets.size(); i++) {
            Bet bet = unassignedBets.get(i);
            System.out.println(String.format("后10条未分配号码: %s, 金额: %.1f", bet.getNumber(), bet.getBetMoney()));
        }
    }

    // 同步所有赔率
    static void syncAllOdds(MemberSite memberSite) {
        for (int dictNoTypeId = 1; dictNoTypeId <= 6; dictNoTypeId++) {
            try {
                memberSite.GetCollectSummary(dictNoTypeId);
            } catch (Exception e) {
                System.err.println("同步赔率失败: site=" + memberSite.getDomain() + ", dictNoTypeId=" + dictNoTypeId
                        + ", err=" + e.getMessage());
            }
        }

        // Submit bet collection tasks
        List<Map<String, Object>> bets = NumberTemplateGenerator.buildCollectBatchBetsData(Arrays.asList(11), 1);
        memberSite.collectBatchBets(bets);

        List<Map<String, Object>> bets1 = NumberTemplateGenerator
                .buildCollectBatchBetsData(Arrays.asList(6, 7, 8, 9, 10), 1);
        memberSite.collectBatchBets(bets1);

        // Sync package info
        memberSite.syncAllPackageInfo();
    }

    static Site buildSite(String username, String password, String baseUrl, int rebateRate) {
        Site site = new FengHuangMember(username, password, baseUrl, LotteryType.FIVE, 0.013,"1");
        List<String> lines = SiteUtil.generateLineGroup(baseUrl);
        site.setLineUrls(lines);
        return site;
    }

    static void testManager() {
        // 获取 SiteMessageManager 单例
        PlatformManager platformManager = new PlatformManager();
        SiteManager manager = new SiteManager(platformManager);

        // 用List<Site>保存所有member
        List<Site> members = new ArrayList<>();
        // 示例：添加多个member
        String username = "moyu01";
        String password = "Aa112233";
        String baseUrl = "https://f8.pt755b62.xyz";
        List<String> lines = SiteUtil.generateLineGroup(baseUrl);
        Site site1 = new FengHuangMember(username, password, baseUrl, LotteryType.FIVE, 0.013,"1");
        site1.setLineUrls(lines);
        members.add(site1);

        String username1 = "moyu01";
        String password1 = "Aa112233";
        String baseUrl1 = "https://f7.ps888a62.xyz/";
        List<String> lines1 = SiteUtil.generateLineGroup(baseUrl1);
        Site site2 = new FengHuangMember(username1, password1, baseUrl1, LotteryType.FIVE, 0.013,"1");
        site2.setLineUrls(lines1);
        members.add(site2);

        // 添加admin
        String username2 = "pm115a";
        String password2 = "Aa112233";
        String baseUrl2 = "https://b1.pt755b62.xyz/";
        List<String> lines2 = SiteUtil.generateLineGroup(baseUrl2);
        Site site3 = new FengHuangAdmin(username2, password2, baseUrl2, LotteryType.FIVE, 0.013,"1");
        site3.setLineUrls(lines2);
        manager.registerSite(site3);

        // 你可以继续添加更多Site实现类的member
        // Site site2 = new FengHuangMember(...);
        // members.add(site2);

        // 注册所有站点
        for (Site s : members) {
            manager.registerSite(s);
        }

        // 添加全局消息处理器
        // manager.addGlobalMessageHandler(msg -> System.out.println("全局消息: " + msg));
        // manager.addGlobalStatusHandler(status -> System.out.println("全局状态: " +
        // status));
        // manager.addGlobalRebateHandler(rebate -> System.out.println("全局返点: " +
        // rebate));

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n==== 菜单 ====");
            System.out.println("1. 登录所有member");
            System.out.println("2. 退出登录所有member");
            System.out.println("3. 登录所有admin");
            System.out.println("4. 退出登录所有admin");
            System.out.println("5. 退出程序");
            System.out.print("请选择操作: ");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    manager.getAllMemberSites().parallelStream().forEach(MemberSite::login);
                    break;
                case "2":
                    manager.getAllMemberSites().parallelStream().forEach(MemberSite::logout);
                    break;
                case "3":
                    manager.getAllAdminSites().parallelStream().forEach(AdminSite::login);
                    break;
                case "4":
                    manager.getAllAdminSites().parallelStream().forEach(AdminSite::logout);
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("无效选项，请重新输入！");
            }
        }
        manager.shutdown();
        System.out.println("程序已退出。");
    }

    static void fly() {
        List<String> domains = new ArrayList<>();
        domains.add("pt755b62");
        domains.add("ps888a62");

        OddsManager oddsManager = new OddsManager();

        buildOddsData(domains, oddsManager);
        BetNumberManager manager = buildBetData();
        Map<Integer, BigDecimal> typeTotalBetMoney = calcTypeTotalBetMoney(manager);
        outputReport(manager, oddsManager, typeTotalBetMoney);

        // 确保 noTypeData 目录存在
        java.io.File dir = new java.io.File("noTypeData");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 按玩法分组统计输出
        int multiple = 5; // 例如五成
        System.out.println("\n按玩法分组统计:");
        for (BetNumberGroupByType group : manager.getAllGroups()) {
            if (group.getDictNoTypeId() != 11) {
                continue;
            }
            BetNumberNoTypeData noTypeData = group.riskControlAnalysis(multiple, oddsManager);
            BetNumberNoTypeData noTypeData2 = group.oddsCompensationAnalysis(oddsManager);
            BetNumberNoTypeData noTypeData3 = group.greedyFlyMoneyOptimize(oddsManager);
            BetNumberNoTypeData noTypeData4 = group.proportionalFlyMoneyOptimize(oddsManager);
            String fileName = "noTypeData/compare_dictNoTypeId_" + group.getDictNoTypeId() + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
                // 表头
                writer.write(String.format(
                        "%-8s%-10s%-14s%-14s%-14s%-18s%-18s%-18s%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s\n",
                        "号码", "投注额", "风控盈亏", "风控飞货", "风控保留", "赔率补偿盈亏", "赔率补偿飞货", "赔率补偿保留",
                        "贪心补飞盈亏", "贪心补飞飞货", "贪心补飞保留", "贪心补飞出货赔率", "贪心补飞出货盈亏",
                        "比例补飞盈亏", "比例补飞飞货", "比例补飞保留", "比例补飞出货赔率", "比例补飞出货盈亏"));

                int size = Math.min(
                    Math.min(
                        noTypeData.getResultsList().size(),
                        noTypeData2.getResultsList().size()
                    ),
                    Math.min(
                        noTypeData3.getResultsList().size(),
                        noTypeData4.getResultsList().size()
                    )
                );

                for (int i = 0; i < size; i++) {
                    BetNumberResults r1 = noTypeData.getResultsList().get(i);
                    BetNumberResults r2 = noTypeData2.getResultsList().get(i);
                    BetNumberResults r3 = noTypeData3.getResultsList().get(i);
                    BetNumberResults r4 = noTypeData4.getResultsList().get(i);

                    writer.write(String.format(
                            "号码:%-8s\t投注额:%-6.1f\t入货赔率:%-8.1f\t负值:%-10.1f\n" +
                                    "风控盈亏:%12.1f\t风控飞货:%8.1f\t风控保留:%8.1f\t风控出货赔率:%10.1f\t风控出货盈亏:%12.1f\n" +
                                    "赔率补偿盈亏:%12.1f\t赔率补偿飞货:%8.1f\t赔率补偿保留:%8.1f\t赔率补偿出货赔率:%10.1f\t赔率补偿出货盈亏:%12.1f\n" +
                                    "贪心补飞盈亏:%12.1f\t贪心补飞飞货:%8.1f\t贪心补飞保留:%8.1f\t贪心补飞出货赔率:%10.1f\t贪心补飞出货盈亏:%12.1f\n" +
                                    "比例补飞盈亏:%12.1f\t比例补飞飞货:%8.1f\t比例补飞保留:%8.1f\t比例补飞出货赔率:%10.1f\t比例补飞出货盈亏:%12.1f\n",
                            r1.getBetNo(), r1.getBetMoney().doubleValue(), r1.getOdds().doubleValue(), r1.getProfit().doubleValue(),
                            r1.getWin().doubleValue(), r1.getFlyMoney().doubleValue(), r1.getKeepMoney().doubleValue(), r1.getSellOdds().doubleValue(), r1.getSellProfit().doubleValue(),
                            r2.getWin().doubleValue(), r2.getFlyMoney().doubleValue(), r2.getKeepMoney().doubleValue(), r2.getSellOdds().doubleValue(), r2.getSellProfit().doubleValue(),
                            r3.getWin().doubleValue(), r3.getFlyMoney().doubleValue(), r3.getKeepMoney().doubleValue(), r3.getSellOdds().doubleValue(), r3.getSellProfit().doubleValue(),
                            r4.getWin().doubleValue(), r4.getFlyMoney().doubleValue(), r4.getKeepMoney().doubleValue(), r4.getSellOdds().doubleValue(), r4.getSellProfit().doubleValue()
                    ));
                }

                // 汇总
                writer.write(
                        String.format("\n风控总投注: %.1f  赔率补偿总投注: %.1f  贪心补飞总投注: %.1f  比例补飞总投注: %.1f\n",
                                noTypeData.getTotalBetMoney().doubleValue(), noTypeData2.getTotalBetMoney().doubleValue(),
                                noTypeData3.getTotalBetMoney().doubleValue(), noTypeData4.getTotalBetMoney().doubleValue()));

                writer.write(String.format("风控总飞货: %.1f  赔率补偿总飞货: %.1f  贪心补飞总飞货: %.1f  比例补飞总飞货: %.1f\n",
                        noTypeData.getTotalFlyMoney().doubleValue(), noTypeData2.getTotalFlyMoney().doubleValue(),
                        noTypeData3.getTotalFlyMoney().doubleValue(), noTypeData4.getTotalFlyMoney().doubleValue()));

                writer.write(String.format("风控总保留: %.1f  赔率补偿总保留: %.1f  贪心补飞总保留: %.1f  比例补飞总保留: %.1f\n",
                        noTypeData.getTotalKeepMoney().doubleValue(), noTypeData2.getTotalKeepMoney().doubleValue(),
                        noTypeData3.getTotalKeepMoney().doubleValue(), noTypeData4.getTotalKeepMoney().doubleValue()));

                writer.write(String.format("风控负值号码: %d  赔率补偿负值号码: %d  贪心补飞负值号码: %d  比例补飞负值号码: %d\n",
                        noTypeData.getNegativeCount(), noTypeData2.getNegativeCount(),
                        noTypeData3.getNegativeCount(), noTypeData4.getNegativeCount()));

                writer.write(String.format("风控正值号码: %d  赔率补偿正值号码: %d  贪心补飞正值号码: %d  比例补飞正值号码: %d\n",
                        noTypeData.getPositiveCount(), noTypeData2.getPositiveCount(),
                        noTypeData3.getPositiveCount(), noTypeData4.getPositiveCount()));

                // 计算平均值
                BigDecimal r1Sum = noTypeData.getResultsList().stream().map(BetNumberResults::getWin).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal r2Sum = noTypeData2.getResultsList().stream().map(BetNumberResults::getWin).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal r3Sum = noTypeData3.getResultsList().stream().map(BetNumberResults::getWin).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal r4Sum = noTypeData4.getResultsList().stream().map(BetNumberResults::getWin).reduce(BigDecimal.ZERO, BigDecimal::add);
                int r1Size = noTypeData.getResultsList().size();
                int r2Size = noTypeData2.getResultsList().size();
                int r3Size = noTypeData3.getResultsList().size();
                int r4Size = noTypeData4.getResultsList().size();
                BigDecimal r1AvgProfit = r1Size > 0 ? r1Sum.divide(BigDecimal.valueOf(r1Size), 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
                BigDecimal r2AvgProfit = r2Size > 0 ? r2Sum.divide(BigDecimal.valueOf(r2Size), 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
                BigDecimal r3AvgProfit = r3Size > 0 ? r3Sum.divide(BigDecimal.valueOf(r3Size), 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
                BigDecimal r4AvgProfit = r4Size > 0 ? r4Sum.divide(BigDecimal.valueOf(r4Size), 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

                writer.write(String.format(
                        "风控平均出货盈亏: %.1f  赔率补偿平均出货盈亏: %.1f  贪心补飞平均出货盈亏: %.1f  比例补飞平均出货盈亏: %.1f\n",
                        r1AvgProfit.doubleValue(), r2AvgProfit.doubleValue(), r3AvgProfit.doubleValue(), r4AvgProfit.doubleValue()));

                System.out.println("对比结果已写入: " + fileName);
            } catch (IOException e) {
                System.err.println("写入对比文件失败: " + e.getMessage());
            }
        }
    }

    static void buildOddsData(List<String> domains, OddsManager oddsManager) {
        for (int dictNoTypeId = 1; dictNoTypeId <= 11; dictNoTypeId++) {
            List<String> numbers = NumberTemplateGenerator.getNumbersByDictNoTypeId(dictNoTypeId);
            for (String number : numbers) {
                List<NumberOddsGroup.OddsInfo> oddsInfos = new ArrayList<>();
                for (String domain : domains) {
                    BigDecimal odds;
                    if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
                        odds = BetUtils.randomOdds(98, 100);
                    } else if (dictNoTypeId >= 7 && dictNoTypeId <= 10) {
                        odds = BetUtils.randomOdds(980, 1000);
                    } else {
                        odds = BetUtils.randomOdds(9000, 10000);
                    }
                    String now = java.time.LocalDateTime.now().toString();
                    oddsInfos.add(new NumberOddsGroup.OddsInfo(domain, odds.doubleValue(), now));
                }
                NumberOddsGroup group = new NumberOddsGroup(dictNoTypeId, number, oddsInfos);
                oddsManager.addOrUpdateOddsGroup(group);
            }
        }
        System.out.println("已构建所有号码的赔率信息！");
        String[] queryNumbers = { "1234", "15XX", "123X" };
        for (String num : queryNumbers) {
            List<NumberOddsGroup> groups = oddsManager.queryByNumber(num);
            if (groups.isEmpty()) {
                System.out.println("没有找到号码" + num + "的赔率信息");
            } else {
                for (NumberOddsGroup group : groups) {
                    System.out.println("号码: " + group.getNumber() + "，玩法: " + group.getDictNoTypeId());
                    for (NumberOddsGroup.OddsInfo info : group.getOddsList()) {
                        System.out.println("  站点: " + info.getDomain() +
                                "，赔率: " + info.getOdds() +
                                "，更新时间: " + info.getUpdateDatetime());
                    }
                }
            }
        }
    }

    static BetNumberManager buildBetData() {
        BetNumberManager manager = new BetNumberManager();
        for (int dictNoTypeId = 1; dictNoTypeId <= 11; dictNoTypeId++) {
            List<String> numbers = NumberTemplateGenerator.getNumbersByDictNoTypeId(dictNoTypeId);
            for (String number : numbers) {
                BigDecimal betMoney;
                BigDecimal odds;
                if (dictNoTypeId >= 1 && dictNoTypeId <= 6) {
                    betMoney = BigDecimal.valueOf(BetUtils.randomInt(60, 500));
                    odds = BetUtils.randomOdds(90, 100);
                } else if (dictNoTypeId >= 7 && dictNoTypeId <= 10) {
                    betMoney = BetUtils.randomDouble(0.1, 60).setScale(1, BigDecimal.ROUND_DOWN);
                    odds = BetUtils.randomOdds(900, 1000);
                } else {
                    betMoney = BetUtils.randomDouble(0.1, 50).setScale(1, BigDecimal.ROUND_DOWN);
                    odds = BetUtils.randomOdds(8000, 10000);
                }
                BetNumber bet = new BetNumber(dictNoTypeId, number, betMoney, odds);
                manager.addBet(bet);
            }
        }
        System.out.println("总号码数: " + manager.totalCount());
        System.out.println("总投注金额: " + manager.totalBetMoney());
        return manager;
    }

    static Map<Integer, BigDecimal> calcTypeTotalBetMoney(BetNumberManager manager) {
        Map<Integer, BigDecimal> typeTotalBetMoney = new HashMap<>();
        for (BetNumberGroupByType group : manager.getAllGroups()) {
            typeTotalBetMoney.put(group.getDictNoTypeId(), group.getTotalBetMoney());
        }
        return typeTotalBetMoney;
    }

    static void outputReport(BetNumberManager manager, OddsManager oddsManager,
            Map<Integer, BigDecimal> typeTotalBetMoney) {
        int multiple = 5;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("bet_no_type_report.txt"), StandardCharsets.UTF_8))) {
            for (BetNumberGroupByType group : manager.getAllGroups()) {
                BetNumberNoTypeData noTypeData = group.riskControlAnalysis(multiple, oddsManager);
                writer.write("玩法ID: " + noTypeData.getDictNoTypeId() + "\n");
                writer.write("总投注金额: " + noTypeData.getTotalBetMoney() + "\n");
                // ... 其它总计
                for (BetNumberResults result : noTypeData.getResultsList()) {
                    writer.write(String.format(
                            "玩法: %-4d 号码: %-6s 投注金额: %8.2f 赔率: %8.2f 负值: %12.2f 出货组数: %8.2f 剩余组数: %8.2f 盈利: %10.2f 出货赔率: %8.2f 出货盈亏: %10.2f\n",
                            noTypeData.getDictNoTypeId(),
                            result.getBetNo(),
                            result.getBetMoney().doubleValue(),
                            result.getOdds().doubleValue(),
                            result.getProfit().doubleValue(),
                            result.getFlyMoney().doubleValue(),
                            result.getKeepMoney().doubleValue(),
                            result.getWin().doubleValue(),
                            result.getSellOdds().doubleValue(),
                            result.getSellProfit().doubleValue()));
                }
                writer.write("--------------------------------------------------\n");
            }
            writer.flush();
            System.out.println("结果已写入 bet_no_type_report.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void bulieCode() {
        // 构建
        String[] templates = {
                "口口XX", "口X口X", "口XX口", "X口X口", "X口口X", "XX口口",
                "口口口X", "口口X口", "口X口口", "X口口口", "口口口口"
        };
        List<Map<String, Object>> bets = new ArrayList<>();
        for (int dictNoTypeId = 1; dictNoTypeId <= 11; dictNoTypeId++) {
            List<String> numbers = NumberTemplateGenerator.getNumbersByDictNoTypeId(dictNoTypeId);
            // 这里可以批量构建bet对象
            for (String number : numbers) {
                Map<String, Object> bet = new HashMap<>();
                bet.put("dict_no_type_id", dictNoTypeId);
                bet.put("bet_no", number);
                bet.put("bet_money", "5.2");
                bets.add(bet);
            }
        }
        System.out.println("bets.size() = " + bets.size());
        // 输出全部或部分bets
        for (int i = 0; i < Math.min(10, bets.size()); i++) {
            System.out.println(bets.get(i));
        }

        // 验证 getNumbersByDictNoTypeId
        for (int dictNoTypeId = 1; dictNoTypeId <= 3; dictNoTypeId++) {
            List<String> numbers = NumberTemplateGenerator.getNumbersByDictNoTypeId(dictNoTypeId);
            System.out.println(
                    "dictNoTypeId = " + dictNoTypeId + "，号码组前5个: " + numbers.subList(0, Math.min(5, numbers.size())));
        }

        // 验证 getDictNoTypeIdByNumber
        String[] testNumbers = { "00XX", "1X2X", "1XX2", "X1X2", "X12X", "XX12", "123X", "12X3", "1X23", "X123",
                "1234" };
        for (String num : testNumbers) {
            int typeId = NumberTemplateGenerator.getDictNoTypeIdByNumber(num);
            System.out.println("号码 " + num + " 属于 dictNoTypeId = " + typeId);
        }
    }

    private static boolean Menber() {
        // 1. 会员账号密码（可替换为实际输入或配置读取）
        String username = "moyu01";
        String password = "Aa112233";
        String baseUrl = "https://f8.pt755b62.xyz";

        // 2. 创建会员对象
        SiteMember member = new SiteMember(baseUrl);
        // 4. 登录
        boolean loginSuccess = member.indexWithAccount(username, password);
        if (loginSuccess) {

            // 下注
            List<Map<String, Object>> bets = new ArrayList<>();

            Map<String, Object> bet1 = new HashMap<>();
            bet1.put("dict_no_type_id", "11");
            bet1.put("bet_no", "9795");
            bet1.put("bet_money", "0.6");
            bets.add(bet1);

            Map<String, Object> bet2 = new HashMap<>();
            bet2.put("dict_no_type_id", "11");
            bet2.put("bet_no", "9796");
            bet2.put("bet_money", "0.6");
            bets.add(bet2);

            String betResponse = member.batchBet(bets);
            boolean betSuccess = false;
            String serialNo = null;
            int totalCount = 0;
            int completedStatus = 0;
            int lackStatus = 0;
            List<Map<String, Object>> betList = new ArrayList<>();

            if (betResponse != null) {
                JSONObject betJson = new JSONObject(betResponse);
                if (betJson.getInt("Status") == 1) {
                    betSuccess = true;
                    JSONObject data = betJson.optJSONObject("Data");
                    if (data != null) {
                        completedStatus = data.optInt("CompletedStatus", 0);
                        lackStatus = data.optInt("LackStatus", 0);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                    // 获取小票信息
                    Map<String, Object> result = (Map<String, Object>) member.getAllMemberBets();
                    List<Map<String, Object>> allOrders = (List<Map<String, Object>>) result.get("orders");

                    for (Map<String, Object> order : allOrders) {
                        String orderSerialNo = String.valueOf(order.get("serial_no"));
                        System.out.println("serial_no: " + orderSerialNo);
                        serialNo = orderSerialNo;
                        // 如果还想看明细
                        List<Map<String, Object>> details = (List<Map<String, Object>>) order.get("details");
                        for (Map<String, Object> detail : details) {
                            ++totalCount;
                            Map<String, Object> bet = new HashMap<>();
                            bet.put("bet_id", detail.get("bet_id"));
                            bet.put("BetCount", 1); // 通常为1
                            betList.add(bet);
                        }
                    }

                    // 整单退码
                    // if (serialNo != null && totalCount > 0) {
                    // String cancelResp = member.cancelOrder(serialNo, totalCount);
                    // System.out.println("退单响应: " + cancelResp);
                    // }

                    // 单个退码
                    if (betList != null && betList.size() > 0) {
                        String cancelResp = member.cancelMemberBet(betList);
                        System.out.println("退码响应: " + cancelResp);
                    }

                    // 清除小票
                    boolean cleanResult = member.cleanPrint();
                    System.out.println("清除小票结果: " + cleanResult);

                } else {
                    System.out.println("下注失败: " + betJson.optString("Message", "未知错误"));
                }
            } else {
                System.out.println("下注请求无响应");
            }
        }
        return false;
    }

    private static boolean initializeAndLogin() {
        try {
            site.testProxy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean loginSuccess = false;
        while (totalConsecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
            totalConsecutiveFailures++;
            if (currentProxy == null) {
                if (!getNewProxy()) {
                    continue;
                }
            }

            try {

                loginSuccess = site.index();
                if (!loginSuccess) {
                    System.out.println("登录失败，更换代理IP");
                    currentProxy = null;
                }
            } catch (Exception e) {
                System.out.println("登录失败: " + e.getMessage());
                currentProxy = null;
            }
        }

        return loginSuccess;
    }

    private static void processDataCollection() {
        List<SimpleEntry<Integer, Integer>> customTuples = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            customTuples.add(new SimpleEntry<>(40, i));
        }
        System.out.println("待获取数据量: " + customTuples.size());

        for (SimpleEntry<Integer, Integer> tuple : customTuples) {
            if (!processDataItem(tuple)) {
                return; // 如果处理失败，终止任务
            }
        }
    }

    private static boolean processDataItem(SimpleEntry<Integer, Integer> tuple) {
        int consecutiveFailures = 0;
        boolean success = false;

        System.out.println("\n开始处理: fixNum=" + tuple.getKey() + ", pageIndex=" + tuple.getValue());

        while (!success && consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
            if (currentProxy == null) {
                getNewProxy();
            }
            // 尝试获取数据
            if (tryGetData(tuple, MAX_CONSECUTIVE_FAILURES)) {
                success = true;
                consecutiveFailures = 0;
                totalConsecutiveFailures = 0;
            } else {
                consecutiveFailures++;
                totalConsecutiveFailures++;
                if (totalConsecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    System.out.println("数据获取连续失败3次，终止任务");
                    return false;
                }
            }
        }

        return consecutiveFailures < MAX_CONSECUTIVE_FAILURES;
    }

    private static boolean getNewProxy() {
        currentProxy = ProxyUtil.getValidProxy();
        if (currentProxy == null) {
            System.out.println("无法获取有效代理IP");
            return false;
        }

        InetSocketAddress addr = (InetSocketAddress) currentProxy.address();
        site.setProxy(addr.getHostString(), addr.getPort());
        System.out.println("使用新代理: " + addr.getHostString() + ":" + addr.getPort());

        // 重置当前代理的请求计数
        currentProxyRequestCount = 0;
        return true;
    }

    private static boolean tryGetData(SimpleEntry<Integer, Integer> tuple, int maxRetries) {
        int retryCount = 0;
        int proxyCount = 0;
        int maxProxyCount = 20;
        while (retryCount < maxRetries) {
            try {
                String realBetData = site.getRealBetData(tuple.getKey(), tuple.getValue(), 100);
                try {
                    JSONObject jsonObject = new JSONObject(realBetData);
                    if (jsonObject != null && jsonObject.getInt("Status") == 1) {
                        System.out.println("获取数据成功: fixNum=" + tuple.getKey() + ", pageIndex=" + tuple.getValue());
                        return true;
                    } else {
                        System.out.println("响应状态错误: " + (jsonObject != null ? jsonObject.toString() : "null"));
                        return false; // 网站返回的错误状态，直接返回失败，不重试
                    }
                } catch (JSONException e) {
                    System.out.println("响应格式错误: " + realBetData);
                    retryCount++;
                    // 更换ip再试
                    if (!getNewProxy()) {
                        System.out.println("无法获取新代理，继续重试");
                        continue;
                    }
                }
            } catch (IOException e) {
                String errorMessage = e.getMessage();
                if (errorMessage.contains("failed to respond") ||
                        errorMessage.contains("NoHttpResponseException") ||
                        errorMessage.contains("Connection refused") ||
                        errorMessage.contains("timeout")) {
                    System.out.println("代理连接失败，正在进行第" + retryCount + "次重试，最大重试次数：" + maxRetries);
                } else {
                    System.out.println("请求失败，正在进行第" + retryCount + "次重试，最大重试次数：" + maxRetries);
                }
                System.err.println("错误详情: " + errorMessage);
                currentProxy = null;

                // 获取新代理
                if (!getNewProxy()) {
                    System.out.println("无法获取新代理，继续重试");
                    continue;
                }

                // 如果达到最大重试次数，返回失败
                if (proxyCount >= maxProxyCount) {
                    System.out.println("达到最大重试次数" + maxRetries + "次，放弃重试");
                    return false;
                }
            } catch (Exception e) {
                System.out.println("发生未知错误: " + e.getMessage());
                currentProxy = null;
                return false; // 未知错误直接返回失败，不重试
            }
        }
        return false;
    }

    public static int getDictNoTypeIdByNumber(String number) {
        String[] templates = {
                "口口XX", "口X口X", "口XX口", "X口X口", "X口口X", "XX口口",
                "口口口X", "口口X口", "口X口口", "X口口口", "口口口口"
        };
        for (int i = 0; i < templates.length; i++) {
            if (matchesTemplate(number, templates[i])) {
                return i + 1; // dict_no_type_id 从1开始
            }
        }
        return -1; // 未匹配到
    }

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
                return false; // 模板中出现了非"口"或"X"
            }
        }
        return true;
    }
}
