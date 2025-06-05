package com.fivebear.platform;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 凤凰会员端站点实现（整合 SiteMember 功能）
 */
public class FengHuangMember extends MemberSite {
    private static final Logger logger = Logger.getLogger(FengHuangMember.class.getName());
    private static final int TIMEOUT = 5000;
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY = 3000;

    // ====== SiteMember 的核心属性 ======
    private String sessionId;
    private String publicKey;
    private String exponent;

    public FengHuangMember(String username, String password, String url, LotteryType lotteryType, double rebateRate, String userId) {
        super(username, password, url, SiteType.FENGHUANG, lotteryType, rebateRate, userId);
    }

    @Override
    public boolean login() {
        setSiteStatus(SiteStatus.LOGGING_IN);
        try {
            this.sendMessage(String.format("站点 %s 开始登录,测试线路初始线路是 %s", getUniqueKey(), getUrl()),
                    MessageType.SYSTEM_INFO);
            String bestLine = testConnection();
            if (bestLine == null) {
                this.sendMessage(String.format("站点 %s 测试线路失败,请检查线路是否正常", getUniqueKey()), MessageType.SYSTEM_INFO);
                return false;
            }
            setUrl(bestLine);
            this.sendMessage(String.format("站点 %s 测试线路完成,最快线路是 %s", getUniqueKey(), bestLine), MessageType.SYSTEM_INFO);

            boolean success = doLogin();
            setSiteStatus(success ? SiteStatus.LOGGED_IN : SiteStatus.ERROR);
            return success;
        } catch (Exception e) {
            logger.warning("登录异常: " + e.getMessage());
            setSiteStatus(SiteStatus.ERROR);
        }
        return false;
    }

    @Override
    public void logout() {
        setSiteStatus(SiteStatus.NOT_LOGGED_IN);
        try {
            String logoutUrl = getUrl() + sessionId + "/Member/Logout?_=" + System.currentTimeMillis();
            String resp = sendRequest(logoutUrl, "GET", null, null);
            System.out.println("登出响应: " + resp);
            JSONObject json = parseJsonResponse(resp);
            if (json != null && json.optInt("Status") == 1) {
                setSiteStatus(SiteStatus.LOGGED_OUT);
                this.sendMessage(String.format("站点 %s 已成功登出", getUniqueKey()), MessageType.SYSTEM_INFO);
            } else {
                this.sendMessage(String.format("站点 %s 登出失败", getUniqueKey()), MessageType.SYSTEM_INFO);
            }
            this.sessionId = null;
            this.publicKey = null;
            this.exponent = null;
            this.httpClientUtil.clearCookies();
            this.httpClientUtil.clearProxy();
        } catch (IOException | RuntimeException e) {
            setSiteStatus(SiteStatus.ERROR);
        }
    }

    @Override
    public boolean heartbeat() {
        // 可实现心跳检测逻辑
        // {"Status":1,"Data":{"period_no":25123,"status":0,"last_seconds":16254}}
        try {
            String heartbeatUrl = getUrl() + sessionId + "/drawno/GetCurrentPeriodStatus?_="
                    + System.currentTimeMillis();
            String heartbeatResp = sendRequest(heartbeatUrl, "GET", null, null);
            System.out.println("心跳检测响应: " + heartbeatResp);
            JSONObject heartbeatResponse = parseJsonResponse(heartbeatResp);
            if (heartbeatResponse != null && heartbeatResponse.getInt("Status") == 1) {
                JSONObject heartbeatData = heartbeatResponse.getJSONObject("Data");
                int periodNo = heartbeatData.getInt("period_no");
                setPeriodNo(String.valueOf(periodNo));
                return true;
            }
        } catch (IOException e) {
            logger.warning("心跳检测异常: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean retriveMemberGetAccount() {
        try {
            String accountUrl = getUrl() + sessionId + "/Member/RetriveMember?_=" + System.currentTimeMillis();
            String accountResp = sendRequest(accountUrl, "GET", null, null);
            JSONObject accountResponse = parseJsonResponse(accountResp);
            if (accountResponse != null && accountResponse.getInt("Status") == 1) {
                JSONObject accountData = accountResponse.getJSONObject("Data");
                this.memberInfo.updateFromJson(accountData);
                return true;
            }
            // 你可以根据实际响应内容判断是否成功并 return true
            // 这里只做演示，实际可根据业务调整
            return true;
        } catch (IOException e) {
            logger.warning("获取账户信息异常: " + e.getMessage());
        }
        return false;
    }

    /**
     * 测试所有线路并返回最快的线路URL
     */
    @Override
    public String testConnection() {
        List<String> lineUrls = getLineUrls();
        int n = lineUrls.size();
        ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(Math.min(n, 5)); // 最多5线程
        List<java.util.concurrent.Future<LineTestResult>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                boolean ok = testNetSpeed(lineUrls.get(idx), idx + 1);
                long elapsed = System.currentTimeMillis() - start;
                return new LineTestResult(lineUrls.get(idx), ok, elapsed);
            }));
        }
        executor.shutdown();
        String bestLine = null;
        long minLatency = Long.MAX_VALUE;
        for (java.util.concurrent.Future<LineTestResult> f : futures) {
            try {
                LineTestResult result = f.get();
                if (result.ok && result.latency < minLatency) {
                    minLatency = result.latency;
                    bestLine = result.url;
                }
            } catch (Exception e) {
                // 忽略单个线路异常
            }
        }
        return bestLine;
    }

    @Override
    public String getMemberPrint(int pageIndex) {
        try {
            // https://f1.pt755b62.xyz/(S(fvhdhir2kiagpxno0kuytv3r))/Member/GetMemberPrint?pageindex=2&_=1747126592680
            String memberPrintUrl = getUrl() + sessionId + "/Member/GetMemberPrint?pageindex=" + pageIndex + "&_="
                    + System.currentTimeMillis();
            String memberPrintResp = sendRequest(memberPrintUrl, "GET", null, null);
            return memberPrintResp;
        } catch (IOException e) {
            logger.warning("获取会员打印异常: " + e.getMessage());
        }
        return null;
    }

    // ====== 业务方法（整合自 SiteMember） ======
    private boolean doLogin() throws IOException {
        // 1. 获取登录页面,提取sessionId
        String loginPage = sendRequest(getUrl() + "/Member/Login?_=" + System.currentTimeMillis(), "GET", null, null);
        extractLoginValues(loginPage);
        if (sessionId == null || sessionId.isEmpty()) {
            this.sendMessage(String.format("站点 %s 登录失败,sessionId为空", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }

        // 2. 构建登录数据
        String requestBody = buildLoginRequestBody(getUsername(), getPassword());
        if (requestBody == null) {
            this.sendMessage(String.format("站点 %s 登录失败,登录数据为空", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }

        // 3. 发送登录请求并检查响应
        String loginUrl = getUrl() + sessionId + "/Member/DoLogin?_=" + System.currentTimeMillis();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Referer", getUrl() + "/Member/Login?..."); // 真实Referer
        headers.put("Origin", getUrl().replaceAll("/$", "")); // 真实Origin
        String response = sendRequest(loginUrl, "POST", requestBody, headers);
        System.out.println("登录响应:" + response);

        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse == null || jsonResponse.getInt("Status") != 1) {
            String errorMsg = jsonResponse != null ? jsonResponse.getString("Data") : "未知错误";
            this.sendMessage(String.format("站点 %s 登录失败,错误信息:%s", getUniqueKey(), errorMsg), MessageType.SYSTEM_INFO);
            return false;
        }

        this.sendMessage(String.format("站点 %s 校验账户成功", getUniqueKey()), MessageType.SYSTEM_INFO);

        // 4. 访问系统公告页面
        String noticeUrl = getUrl() + sessionId + "/Member/SysNotice?_=" + System.currentTimeMillis();
        String systemNotice = sendRequest(noticeUrl, "GET", null, null);
        if (!systemNotice.contains("/Member/AcceptAgreement")) {
            this.sendMessage(String.format("站点 %s 系统公告页面不存在", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }

        // 5. 访问并接受协议
        String acceptUrl = getUrl() + sessionId + "/Member/AcceptAgreement?_=" + System.currentTimeMillis();
        String acceptResponse = sendRequest(acceptUrl, "GET", null, null);
        System.out.println("接受协议:" + acceptResponse);
        JSONObject acceptJson = parseJsonResponse(acceptResponse);
        if (acceptJson == null || acceptJson.getInt("Status") != 1) {
            this.sendMessage(String.format("站点 %s 接受协议失败", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }

        int data = acceptJson.optInt("Data");
        if (data != 0) {
            this.sendMessage(String.format("站点 %s 接受协议失败", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }
        // 6. 设置登录通知cookie
        String sysNoticeUrl = getUrl() + sessionId + "/Member/GetFrontendSysNotice";
        String sysNoticeResp = sendRequest(sysNoticeUrl, "GET", null, null);
        System.out.println("登录通知:" + sysNoticeResp);
        JSONObject sysNoticeJson = parseJsonResponse(sysNoticeResp);
        if (sysNoticeJson == null || sysNoticeJson.getInt("Status") != 1) {
            this.sendMessage(String.format("站点 %s 登录通知失败", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }
        // 登录通知:{"Status":1,"Data":[]}
        // JS 里会设置 cookie
        httpClientUtil.setCookie("NOTICE_LOGIN_IN", "1", getDomain(), "/");

        // 7. 跳转到会员首页（模拟JS的location.href）
        String appIndexUrl = getUrl() + sessionId + "/App/Index?_=" + System.currentTimeMillis();
        String appIndexPage = sendRequest(appIndexUrl, "GET", null, null);
        // 你可以根据appIndexPage内容判断是否登录成功，比如包含用户名、余额等
        // var LOTTERY_TYPE_NAME = "排列五";
        if (appIndexPage != null && appIndexPage.contains("LOTTERY_TYPE_NAME")) {
            // 解析出LOTTERY_TYPE_NAME
            Pattern pattern = Pattern.compile("LOTTERY_TYPE_NAME = \"([^\"]+)\"");
            Matcher matcher = pattern.matcher(appIndexPage);
            if (matcher.find()) {
                String lotteryTypeName = matcher.group(1);
                if (!lotteryTypeName.equals(getLotteryType().getDisplayName())) {
                    this.sendMessage(String.format("站点 %s 登录流程异常，彩种不匹配，期望彩种是 %s，实际是 %s", getUniqueKey(),
                            getLotteryType().name(), lotteryTypeName), MessageType.SYSTEM_INFO);
                    return false;
                }
                this.sendMessage(String.format("站点 %s 登录流程全部完成，已进入会员首页，彩种是 %s", getUniqueKey(), lotteryTypeName),
                        MessageType.SYSTEM_INFO);
            }
        } else {
            this.sendMessage(String.format("站点 %s 登录流程异常，未能进入会员首页", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }
        if (!retriveMemberGetAccount() || !heartbeat()) {
            this.sendMessage(String.format("站点 %s 获取会员信息失败", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }

        return true;
    }

    private void extractLoginValues(String responseBody) {
        // 提取 exponent
        Pattern exponentPattern = Pattern.compile("id=\"_exponentValue\" value=\"([^\"]+)\"");
        Matcher exponentMatcher = exponentPattern.matcher(responseBody);
        if (exponentMatcher.find()) {
            exponent = exponentMatcher.group(1);
        }

        // 提取 publickey
        Pattern publickeyPattern = Pattern.compile("id=\"_publickeyValue\" value=\"([^\"]+)\"");
        Matcher publickeyMatcher = publickeyPattern.matcher(responseBody);
        if (publickeyMatcher.find()) {
            publicKey = publickeyMatcher.group(1);
        }

        // 提取 SESSIONID
        Pattern sessionPattern = Pattern.compile("var SESSIONID = \"([^\"]+)\"");
        Matcher sessionMatcher = sessionPattern.matcher(responseBody);
        if (sessionMatcher.find()) {
            sessionId = sessionMatcher.group(1);
        }
    }

    /**
     * 构建登录请求数据
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录请求体
     */
    private String buildLoginRequestBody(String username, String password) {
        String encryptedUsername = rsaEncrypt(username, publicKey);
        String encryptedPassword = rsaEncrypt(password, publicKey);

        if (encryptedUsername == null || encryptedPassword == null) {
            return null;
        }

        return String.format(
                "Account=%s&Password=%s&publickey=%s&captchacode=",
                encryptedUsername,
                encryptedPassword,
                publicKey);
    }

    // RSA加密
    private String rsaEncrypt(String text, String publicKey) {
        try {
            // 将16进制的公钥转换为BigInteger
            BigInteger modulus = new BigInteger(publicKey, 16);
            BigInteger exp = new BigInteger(exponent, 16);

            // 创建RSA公钥
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exp);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            // 使用PKCS1填充进行加密
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));

            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : encrypted) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 你可以继续整合 SiteMember 的其他业务方法，如投注、公告、持仓等
    // public boolean batchBet(...) { ... }
    // public String getAccountInfo() { ... }
    // ...

    // 可扩展凤凰会员端专属方法

    private boolean testNetSpeed(String lineUrl, int index) {
        String callbackName = "callback" + index;
        String url = lineUrl + "/Member/GetNetSpeed?jsonp=" + callbackName + "&_=" + System.currentTimeMillis();
        try {
            // 每次测速都新建一个 HttpClientUtil 实例，避免复用全局 httpClientUtil
            HttpClientUtil tempClient = new HttpClientUtil();
            // tempClient.setProxy("127.0.0.1", 8888, null, null);
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            String body = tempClient.doGet(url, headers).getContent();
            System.out.println(String.format("测试线路: %s, 测速结果: %s", url, body));
            // 解析 JSONP：callbackN({...});
            Pattern p = Pattern.compile(callbackName + "\\((\\{.*?\\})\\);?");
            Matcher m = p.matcher(body);
            if (m.find()) {
                String json = m.group(1);
                JSONObject obj = new JSONObject(json);
                return "net_test".equals(obj.optString("site"));
            }
        } catch (Exception e) {
            logger.warning("测速失败: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean retriveMemberGetOdds(List<Map<String, Object>> bets) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean clearMemberPrint() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private boolean removeCollectBetsAll(List<Integer> dictNoTypeIds)
    {
        try {
            for (int i : dictNoTypeIds) {
                removeCollectBetsByType(i);
            }
            return true;
        } catch (Exception e) {
            logger.warning("删除预下注异常: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean collectBatchBets(List<Map<String, Object>> bets) {
        try {
            setSiteStatus(SiteStatus.COLLECT_BET);
            //根据bets中的dict_no_type_id，构建需要删除的dictNoTypeIds
            Set<Integer> uniqueIds = new HashSet<>();
            for (Map<String, Object> bet : bets) {
                uniqueIds.add((Integer) bet.get("dict_no_type_id"));
            }
            List<Integer> dictNoTypeIds = new ArrayList<>(uniqueIds);
            System.out.println("需要删除的dictNoTypeIds: " + dictNoTypeIds);
            removeCollectBetsAll(dictNoTypeIds);
            // 构建请求参数
            org.json.JSONObject requestData = new org.json.JSONObject();
            requestData.put("Data", new org.json.JSONArray(bets));
            requestData.put("collect_way", "3"); // 批量模式
            requestData.put("vs", SimpleVsGenerator.generateVs());

            // 发送请求
            String url = getUrl() + sessionId + "/CollectBet/CollectBets";
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // 构建固定格式的请求体
            StringBuilder formData = new StringBuilder();
            formData.append("Data=")
                    .append(java.net.URLEncoder.encode(requestData.getJSONArray("Data").toString(), "UTF-8"))
                    .append("&collect_way=").append(requestData.getString("collect_way"))
                    .append("&vs=").append(requestData.getInt("vs"));

            String response = sendRequest(url, "POST", formData.toString(), headers,60 * 1000);
            JSONObject jsonResponse = parseJsonResponse(response);
            setSiteStatus(SiteStatus.IDLE);
            System.out.println("预下注响应:" + response);
            // 检查响应
            if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
                org.json.JSONObject data = jsonResponse.getJSONObject("Data");
                //获取小票
                List<String> collectMemberPrint = getCollectMemberPrint();
                if (collectMemberPrint != null) {
                    JSONArray oddsJsonArray = new JSONArray();
                    for (String print : collectMemberPrint) 
                    {
                        JSONObject printJson = new JSONObject(print);
                        String betNo = printJson.getString("bet_no");
                        String oddsStr = printJson.getString("odds");
                        String betMoneyStr = printJson.getString("bet_money");
                        double betMoney = Double.parseDouble(betMoneyStr);
                        int dictNoTypeId = printJson.getInt("dict_no_type_id");
                        //"odds" : "1:6100",
                        double odds = Double.parseDouble(oddsStr.split(":")[1]);
                        JSONObject oddsJson = new JSONObject();
                        oddsJson.put("number", betNo);
                        oddsJson.put("odds", odds);
                        oddsJson.put("bet_money", betMoney);
                        oddsJson.put("dict_no_type_id",  dictNoTypeId); 
                        oddsJsonArray.put(oddsJson); 
                    }
                    pushNumOddsInfo(oddsJsonArray);
                }
                removeCollectBetsAll(dictNoTypeIds);
                return true;
            }
        } catch (Exception e) {
            System.err.println("预下注请求失败: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String batchBet(List<Map<String, Object>> bets, String isPackage, String way) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private String getCollectMemberPrintIndex(int index) 
    {
        //https://f1.pt755b62.xyz/(S(fvhdhir2kiagpxno0kuytv3r))/CollectBet/GetMemberPrint?pageindex=2&_=1747303888132
        try {
            String url = getUrl() + sessionId + "/CollectBet/GetMemberPrint?pageindex=" + index + "&_=" + System.currentTimeMillis();
            String resp = sendRequest(url, "GET", null, null);
            JSONObject json = parseJsonResponse(resp);
            if (json == null || json.getInt("Status") != 1) {
                return null;
            }
            return resp;
        } catch (Exception e) {
            logger.warning("获取单页预下注异常: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean clearCollectBetPrint() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<String> getCollectMemberPrint() {
        List<String> allDetails = new ArrayList<>();
        int pageIndex = 1;
        int pageCount = 1;

        do {
            String resp = getCollectMemberPrintIndex(pageIndex);
            if (resp == null) break;

            JSONObject json = parseJsonResponse(resp);
            if (json == null || json.getInt("Status") != 1) break;

            JSONObject data = json.getJSONObject("Data");
            JSONArray details = data.getJSONArray("Details");
            for (int i = 0; i < details.length(); i++) {
                allDetails.add(details.getJSONObject(i).toString());
            }

            pageCount = data.getInt("PageCount");
            pageIndex++;
        } while (pageIndex <= pageCount);

        return allDetails;
    }

    @Override
    public String cancelOrder(String serialNo, int totalCount) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String cancelMemberBet(List<Map<String, Object>> betList) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String GetCollectSummary(int dictNoTypeId) {
        // https://f1.pt755b62.xyz/(S(fvhdhir2kiagpxno0kuytv3r))/CollectBet/GetCollectSummary?dict_no_type_id=1&_=1747197014469
        try {
            String url = getUrl() + sessionId + "/CollectBet/GetCollectSummary?dict_no_type_id=" + dictNoTypeId + "&_="
                    + System.currentTimeMillis();
            String resp = sendRequest(url, "GET", null, null);
            JSONObject json = parseJsonResponse(resp);
            if (json == null || json.getInt("Status") != 1) {
                return null;
            }
            JSONObject data = json.getJSONObject("Data");
            if (data == null) {
                return null;
            }
            // 同时推送出去
            int dict_no_type_id = data.getInt("dict_no_type_id");
            JSONArray packages = data.getJSONArray("packages");
            List<PackageInfo> packageInfoList = new ArrayList<>();
            for (int j = 0; j < packages.length(); j++) {
                JSONObject pkg = packages.getJSONObject(j);
                PackageInfo pkgInfo = PackageInfo.parsePackageInfo(pkg);
                if (pkgInfo != null) {
                    pkgInfo.setDomain(getDomain());
                    pkgInfo.setDictNoTypeId(dict_no_type_id);
                    pkgInfo.setPeriodNo(getPeriodNo());
                    packageInfoList.add(pkgInfo);
                }
            }
            pushPackageOddsInfo(packageInfoList);
            // System.out.println("获取会员汇总:" + resp);
            return resp;
        } catch (Exception e) {
            logger.warning("获取会员汇总异常: " + e.getMessage());
        }
        return null;
    }

    private String GetErZiDingNumOdds(int dictNoTypeId) {
        // https://f1.pw3r3521.xyz/(S(3a2nqxqjyj2avaskfshiospg))/Member/GetErZiDingNumOdds?no_type_id=1&_=1747206406098
        try {
            String url = getUrl() + sessionId + "/Member/GetErZiDingNumOdds?no_type_id=" + dictNoTypeId + "&_="
                    + System.currentTimeMillis();
            String resp = sendRequest(url, "GET", null, null);
            JSONObject json = parseJsonResponse(resp);
            if (json == null || json.getInt("Status") != 1) {
                return null;
            }
            return resp;
        } catch (Exception e) {
            logger.warning("获取二字定赔率异常: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean syncAllPackageInfo() {
        for (int i = 1; i <= 6; i++) {
            String resp = GetErZiDingNumOdds(i);
            if (resp == null) {
                return false;
            }
            JSONObject json = parseJsonResponse(resp);
            if (json == null || json.getInt("Status") != 1) {
                return false;
            }
            JSONObject data = json.getJSONObject("Data");
            JSONArray numbers = data.getJSONArray("numbers");
            JSONArray oddsJsonArray = new JSONArray();
            for (int j = 0; j < numbers.length(); j++) {
                JSONObject num = numbers.getJSONObject(j);
                String betNo = num.getString("bet_no");
                double odds = num.getDouble("odds");
                if(betNo.equals("12XX")) {
                    System.out.println("赔率: " + odds);
                }
                double betMoney = num.getDouble("bet_money");
                JSONObject oddsJson = new JSONObject();
                oddsJson.put("number", betNo);
                oddsJson.put("odds", odds);
                oddsJson.put("bet_money", betMoney);
                oddsJson.put("dict_no_type_id",  i); 
                oddsJsonArray.put(oddsJson);            
            }
            pushNumOddsInfo(oddsJsonArray);
        }
        return true;
    }

    @Override
    public boolean removeCollectBetsByType(int dictNoTypeId) {
        // https://f1.pt755b62.xyz/(S(fvhdhir2kiagpxno0kuytv3r))/CollectBet/RemoveByType?dict_no_type_id=11&_=1747303389134
        try {
            String url = getUrl() + sessionId + "/CollectBet/RemoveByType?dict_no_type_id=" + dictNoTypeId + "&_="
                    + System.currentTimeMillis();
            String resp = sendRequest(url, "GET", null, null);
            JSONObject json = parseJsonResponse(resp);
            if (json == null || json.getInt("Status") != 1) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.warning("删除预下注异常: " + e.getMessage());
        }
        return false;
    }
}