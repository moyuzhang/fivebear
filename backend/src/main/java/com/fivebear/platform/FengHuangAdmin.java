package com.fivebear.platform;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.json.JSONObject;

/**
 * 凤凰管理端站点实现
 */
public class FengHuangAdmin extends AdminSite {
    private static final Logger logger = Logger.getLogger(FengHuangAdmin.class.getName());

    // ====== AdminSite 的核心属性 ======
    private String sessionId;
    private String publicKey;
    private String exponent;
    private String periodNo;
    protected AccountConfig accountConfig = new AccountConfig();

    public FengHuangAdmin(String username, String password, String url, LotteryType lotteryType, double rebateRate, String userId) {
        super(username, password, url, SiteType.FENGHUANG, lotteryType, rebateRate, userId);
    }

    @Override   
    public boolean login() {
        setSiteStatus(SiteStatus.LOGGING_IN);
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
        } catch (IOException | RuntimeException e) {
            logger.warning("登录异常: " + e.getMessage());
            setSiteStatus(SiteStatus.ERROR);
        }
        // ...
        setSiteStatus(SiteStatus.LOGGED_IN);
        return false;
    }

    @Override
    public void logout() {
        // TODO: 实现具体的管理端登出逻辑
        setSiteStatus(SiteStatus.NOT_LOGGED_IN);
        try {
            String logoutUrl = getUrl() + sessionId + "/Member/Logout?_=" + System.currentTimeMillis();
            String resp = sendRequest(logoutUrl, "GET", null, null);
            System.out.println("登出响应: " + resp);
            JSONObject json = parseJsonResponse(resp);
            if (json != null && json.optInt("Status") == 1) {
                setSiteStatus(SiteStatus.NOT_LOGGED_IN);
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
        // TODO: 实现具体的管理端心跳检测逻辑
        return true;
    }

    @Override
    public boolean retriveMemberGetAccount() {
        // TODO: 实现具体的管理端获取会员账号信息逻辑
        try {
            String accountUrl = getUrl() + sessionId + "/Account/RetriveSelfAccount?_" + System.currentTimeMillis();
            String accountResp = sendRequest(accountUrl, "GET", null, null);
            System.out.println("账户信息: " + accountResp);
            JSONObject accountResponse = parseJsonResponse(accountResp);
            if (accountResponse != null && accountResponse.getInt("Status") == 1) {
                JSONObject accountData = accountResponse.getJSONObject("Data");
                this.accountConfig.updateFromJson(accountData);
                return true;
            }
        } catch (IOException e) {
            logger.warning("获取账户信息异常: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String testConnection() {
        List<String> lineUrls = getLineUrls();
        int n = lineUrls.size();
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                .newFixedThreadPool(Math.min(n, 5)); // 最多5线程
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

    // 复制测速方法和辅助类
    private boolean testNetSpeed(String lineUrl, int index) {
        String callbackName = "callback" + index;
        String url = lineUrl + "/Member/GetNetSpeed?jsonp=" + callbackName + "&_=" + System.currentTimeMillis();
        try {
            HttpClientUtil tempClient = new HttpClientUtil();
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            headers.put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            String body = tempClient.doGet(url, headers).getContent();
            System.out.println(String.format("测试线路: %s, 测速结果: %s", url, body));
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(callbackName + "\\((\\{.*?\\})\\);?");
            java.util.regex.Matcher m = p.matcher(body);
            if (m.find()) {
                String json = m.group(1);
                org.json.JSONObject obj = new org.json.JSONObject(json);
                return "net_test".equals(obj.optString("site"));
            }
        } catch (Exception e) {
            System.out.println("测速失败: " + e.getMessage());
        }
        return false;
    }

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
        // 登录响应:{"Status":1,"Data":{"member_id":20540,"member_level":5,"sms_status":-1,"ga_status":-1,"sms_err_msg":null,"ga_err_msg":null}}

        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse == null || jsonResponse.getInt("Status") != 1) {
            String errorMsg = jsonResponse != null ? jsonResponse.getString("Data") : "未知错误";
            this.sendMessage(String.format("站点 %s 登录失败,错误信息:%s", getUniqueKey(), errorMsg), MessageType.SYSTEM_INFO);
            return false;
        }
        JSONObject data = jsonResponse.getJSONObject("Data");
        int member_level = data.getInt("member_level");
        int member_id = data.getInt("member_id");
        int sms_status = data.getInt("sms_status");
        int ga_status = data.getInt("ga_status");
        if (member_level >= 2 && (sms_status > -1 || ga_status > -1)) {
            this.sendMessage(String.format("站点 %s 登录失败,需要二次验证", getUniqueKey()), MessageType.SYSTEM_INFO);
            return false;
        }

        // 登录成功后，自动访问系统公告页
        String sysNoticeUrl = getUrl() + sessionId + "/Member/SysNotice?_=" + System.currentTimeMillis();
        String sysNoticeResp = sendRequest(sysNoticeUrl, "GET", null, null);
        // System.out.println("系统公告页内容: " + sysNoticeResp);

        // 同意协议
        String acceptUrl = getUrl() + sessionId + "/Member/AcceptAgreement?_=" + System.currentTimeMillis();
        String acceptResp = sendRequest(acceptUrl, "GET", null, null);
        JSONObject acceptJson = parseJsonResponse(acceptResp);
        if (acceptJson != null && acceptJson.getInt("Status") == 1) {
            int acceptData = acceptJson.getInt("Data");
            switch (acceptData) {
                case 1, 2 -> {
                    this.sendMessage(String.format("站点 %s 登录失败,需要设置密码", getUniqueKey()), MessageType.SYSTEM_INFO);
                    return false;
                }
                default -> {
                    // 判断公告页还是首页
                    // 1. 设置登录通知（访问公告页并设置 cookie）
                    // 1. 获取后台系统公告
                    String backendNoticeUrl = getUrl() + sessionId + "/Setting/GetBackendSysNotice?g=&_="
                            + System.currentTimeMillis();
                    String backendNoticeResp = sendRequest(backendNoticeUrl, "GET", null, null);
                    JSONObject backendNoticeJson = parseJsonResponse(backendNoticeResp);
                    if (backendNoticeJson == null || backendNoticeJson.getInt("Status") != 1) {
                        this.sendMessage(String.format("站点 %s 获取后台公告失败", getUniqueKey()), MessageType.SYSTEM_INFO);
                        return false;
                    }

                    // 2. 设置登录通知cookie
                    httpClientUtil.setCookie("NOTICE_LOGIN_IN", "1", getDomain(), "/");

                    // 3. 访问首页
                    String indexUrl = getUrl() + sessionId + "/App/Index?_=" + System.currentTimeMillis();
                    String indexResp = sendRequest(indexUrl, "GET", null, null);
                    if (indexResp == null || indexResp.isEmpty()) {
                        this.sendMessage(String.format("站点 %s 访问首页失败", getUniqueKey()), MessageType.SYSTEM_INFO);
                        return false;
                    }
                    // 解析出IS_SUB_ACCOUNT
                    Pattern isSubAccountPattern = Pattern.compile("IS_SUB_ACCOUNT=(\\d+)");
                    Matcher isSubAccountMatcher = isSubAccountPattern.matcher(indexResp);
                    if (isSubAccountMatcher.find() && isSubAccountMatcher.group(1).equals("1")) {
                        this.sendMessage(String.format("站点 %s 登录失败，不支持子账户登录", getUniqueKey()), MessageType.SYSTEM_INFO);
                        return false;
                    }
                    Pattern lotteryPattern = Pattern.compile("var\\s+LOTTERY_TYPE_NAME\\s*=\\s*\"([^\"]+)\";");
                    Matcher lotteryMatcher = lotteryPattern.matcher(indexResp);
                    if (lotteryMatcher.find()) {
                        String lotteryTypeName = lotteryMatcher.group(1);
                        System.out.println("彩种名称: " + lotteryTypeName);
                    }

                    Pattern periodPattern = Pattern.compile("class=\"main\"\\s+[^=]+=\"(\\d+)\"");
                    Matcher periodMatcher = periodPattern.matcher(indexResp);
                    if (periodMatcher.find()) {
                        this.periodNo = periodMatcher.group(1);
                        try {
                            if (getCurrentPeriodStatus()) {
                                this.sendMessage(String.format("站点 %s 获取当前期号成功", getUniqueKey()),
                                        MessageType.SYSTEM_INFO);
                                String onlineUrl = getUrl() + sessionId + "/Member/Online?_="
                                        + System.currentTimeMillis();
                                String onlineResp = sendRequest(onlineUrl, "GET", null, null);
                                System.out.println("在线状态: " + onlineResp);
                                JSONObject onlineJson = parseJsonResponse(onlineResp);
                                if (onlineJson != null && onlineJson.getInt("Status") == 1) {
                                    this.sendMessage(String.format("站点 %s 获取在线状态成功", getUniqueKey()),
                                            MessageType.SYSTEM_INFO);

                                }
                            }
                        } catch (InterruptedException | IOException e) {
                            logger.warning("获取当前期号状态异常: " + e.getMessage());
                        }
                    }
                }
            }
        }

        return false;
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
     * 获取当前期号状态
     * 
     * @return 期号状态信息
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean getCurrentPeriodStatus() throws IOException, InterruptedException {
        if (periodNo == null || periodNo.isEmpty()) {
            return false;
        }
        String url = getUrl() + sessionId + "/drawno/GetCurrentPeriodStatus?period_no=" + periodNo + "&_="
                + System.currentTimeMillis();
        String response = sendRequest(url, "GET", null, null);
        JSONObject jsonResponse = parseJsonResponse(response);
        return jsonResponse != null && jsonResponse.getInt("Status") == 1;
    }

    // 可扩展凤凰管理端专属方法
} 