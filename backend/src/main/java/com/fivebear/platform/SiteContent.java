package com.fivebear.platform;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.apache.http.client.config.RequestConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SiteContent {
    private static final Logger logger = Logger.getLogger(SiteContent.class.getName());
    private static final int TIMEOUT = 5000; // 5 seconds timeout
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY = 3000; // 3 seconds

    private final String baseUrl;
    private final HttpClientUtil httpClientUtil;
    private final RequestConfig defaultConfig;

    private List<LineInfo> lines = new ArrayList<>();
    private String publicKey;
    private String exponent;
    private String sessionId;
    private AccountConfig accountConfig;
    private String periodNo;

    public SiteContent(String baseUrl) {
        this.baseUrl = baseUrl;
        this.accountConfig = new AccountConfig();
        this.defaultConfig = createDefaultConfig();
        this.httpClientUtil = new HttpClientUtil();
    }

    private RequestConfig createDefaultConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .build();
    }

    public boolean setProxy(String proxyHost, int proxyPort) {
        try {
            httpClientUtil.setProxy(proxyHost, proxyPort, null, null);
            return true;
        } catch (Exception e) {
            logger.severe("设置代理失败: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        if (httpClientUtil != null) {
            httpClientUtil.close();
        }
    }

    public boolean testProxyConnection() {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                String testUrl = baseUrl + "/Member/Login?_=" + System.currentTimeMillis();
                HttpClientUtil.Response response = httpClientUtil.doGet(testUrl);

                if (response.getStatusCode() == 200) {
                    return true;
                }
                logger.warning("代理测试返回非200状态码: " + response.getStatusCode());
            } catch (Exception e) {
                logger.warning("代理测试失败 (尝试 " + (retryCount + 1) + "/" + MAX_RETRIES + "): " + e.getMessage());
                retryCount++;

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 发送HTTP请求
     * 
     * @param url     请求URL
     * @param method  请求方法（GET/POST）
     * @param body    请求体（POST请求时使用）
     * @param headers 自定义请求头，如果为null则使用默认请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    private String sendRequest(String url, String method, String body, Map<String, String> headers) throws IOException {
        try {
            // 发送请求
            if ("GET".equalsIgnoreCase(method)) {
                String result = httpClientUtil.doGet(url, headers).getContent();
                if (extractCookie(result)) {
                    return httpClientUtil.doGet(url, headers).getContent();
                }
                return result;
            } else if ("POST".equalsIgnoreCase(method)) {
                String result = httpClientUtil.doPostJson(url, body, headers);
                if (extractCookie(result)) {
                    return httpClientUtil.doPostJson(url, body, headers);
                }
                return result;
            }
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        } catch (Exception e) {
            throw new IOException("Failed to send request: " + e.getMessage(), e);
        }
    }

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public String get(String url) throws IOException {
        return sendRequest(url, "GET", null, null);
    }

    /**
     * 发送POST请求
     * 
     * @param url     请求URL
     * @param body    请求体
     * @param headers 自定义请求头，如果为null则使用默认请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public String post(String url, String body, Map<String, String> headers) throws IOException {
        return sendRequest(url, "POST", body, headers);
    }

    public void setCustomHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpClientUtil.setHeader(entry.getKey(), entry.getValue());
        }
    }

    public boolean index() {
        try {
            System.out.println("开始登录");
            String response = get(baseUrl + "/Member/Login?_=" + System.currentTimeMillis());
            extractLoginValues(response);
            if (sessionId != null && !sessionId.isEmpty()) {
                boolean isLogin = login("mydl02", "Aa112233");
                if (isLogin) {
                    System.out.println("登录成功");
                } else {
                    System.err.println("登录失败");
                }
                return isLogin;
            } else {
                System.err.println("未获取到sessionId");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("登录失败");
            e.printStackTrace();
        }
        return false;
    }

    public boolean indexWithAccount(String username, String password) {
        try {
            System.out.println("开始登录");
            String response = get(baseUrl + "/Member/Login?_=" + System.currentTimeMillis());
            extractLoginValues(response);
            if (sessionId != null && !sessionId.isEmpty()) {
                boolean isLogin = login(username, password);
                if (isLogin) {
                    System.out.println("登录成功");
                } else {
                    System.err.println("登录失败");
                }
                return isLogin;
            } else {
                System.err.println("未获取到sessionId");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("登录失败");
            e.printStackTrace();
        }
        return false;
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
     * 保存内容到文件
     * 
     * @param content   要保存的内容
     * @param filename  文件名
     * @param directory 目录路径（可选）
     * @return 保存的文件路径
     * @throws IOException 如果保存失败
     */
    public String saveToFile(String content, String filename, String... directory) throws IOException {
        // 构建完整的文件路径
        Path dirPath = directory.length > 0 ? Paths.get(directory[0]) : Paths.get("");
        Path filePath = dirPath.resolve(filename);

        // 确保目录存在
        Files.createDirectories(dirPath);

        // 保存内容到文件
        Files.writeString(filePath, content);

        return filePath.toAbsolutePath().toString();
    }

    /**
     * 保存内容到文件（使用默认目录）
     * 
     * @param content  要保存的内容
     * @param filename 文件名
     * @return 保存的文件路径
     * @throws IOException 如果保存失败
     */
    public String saveToFile(String content, String filename) throws IOException {
        return saveToFile(content, filename, "");
    }

    // 获取所有线路
    public List<String> getLines() throws IOException, InterruptedException {
        List<String> lineUrls = new ArrayList<>();
        lineUrls.add("b1.pt755b62.xyz");
        lineUrls.add("b2.pt755b62.xyz");
        lineUrls.add("b3.pt755b62.xyz");
        lineUrls.add("b4.pt755b62.xyz");
        lineUrls.add("b5.pt755b62.xyz");
        lineUrls.add("b6.pt755b62.xyz");
        lineUrls.add("b7.pt755b62.xyz");
        lineUrls.add("b8.pt755b62.xyz");
        lineUrls.add("b9.pt755b62.xyz:8443");
        return lineUrls;
    }

    // 测试单个线路
    private String testLine(LineInfo line) {
        long startTime = System.currentTimeMillis();
        String testUrl = baseUrl + "/Member/Login?_=" + System.currentTimeMillis();

        try {
            HttpClientUtil.Response response = httpClientUtil.doGet(testUrl);
            String responseBody = response.getContent();

            if (response.getStatusCode() == 200) {
                line.latency = System.currentTimeMillis() - startTime;
                line.timeout = false;
                return responseBody;
            } else {
                line.timeout = true;
                return null;
            }
        } catch (Exception e) {
            line.timeout = true;
            return null;
        }
    }

    // 测试所有线路
    public List<LineInfo> testAllLines() throws IOException, InterruptedException {
        List<String> lineUrls = getLines();
        lines.clear();

        // 创建线路信息对象
        for (int i = 0; i < lineUrls.size(); i++) {
            lines.add(new LineInfo(lineUrls.get(i), i));
        }

        // 并发测试所有线路
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (LineInfo line : lines) {
            futures.add(CompletableFuture.supplyAsync(() -> testLine(line)));
        }

        // 等待所有测试完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 排序，找出最快的线路
        Collections.sort(lines, (a, b) -> {
            if (a.timeout && !b.timeout)
                return 1;
            if (!a.timeout && b.timeout)
                return -1;
            return Long.compare(a.latency, b.latency);
        });

        return lines;
    }

    // 获取最佳线路
    public String getBestLine() throws IOException, InterruptedException {
        List<LineInfo> testedLines = testAllLines();
        if (testedLines.isEmpty()) {
            return null;
        }

        // 找到第一个未超时的线路
        for (LineInfo line : testedLines) {
            if (!line.timeout) {
                return line.url;
            }
        }

        // 如果所有线路都超时，返回第一条线路
        return testedLines.get(0).url;
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

    // 解析JSON响应
    private JSONObject parseJsonResponse(String response) {
        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * 访问系统公告页面
     * 
     * @return 公告页面响应
     * @throws IOException
     */
    private String visitSysNoticePage() throws IOException {
        String noticeUrl = baseUrl + sessionId + "/Member/SysNotice?_=" + System.currentTimeMillis();
        return get(noticeUrl);
    }

    /**
     * 检查是否需要等待倒计时
     * 
     * @param response 响应内容
     * @return 是否需要等待
     */
    private boolean needWaitCountdown(String response) {
        Pattern lotteryPattern = Pattern.compile("var LOTTERY_TYPE = (\\d+);");
        Matcher lotteryMatcher = lotteryPattern.matcher(response);
        if (lotteryMatcher.find()) {
            int lotteryType = Integer.parseInt(lotteryMatcher.group(1));
            return lotteryType == 0; // 七星彩需要等待
        }
        return false;
    }

    /**
     * 设置登录通知cookie
     */
    private void setNoticeLoginCookie() {
        String domain = getDomainFromUrl(baseUrl);
        // 确保domain格式正确，去掉开头的点
        String cleanDomain = domain.startsWith(".") ? domain.substring(1) : domain;
        httpClientUtil.setCookie("NOTICE_LOGIN_IN", "1", cleanDomain, "/");
    }

    /**
     * 处理重定向
     * 
     * @param data 响应数据
     * @return 重定向URL
     */
    private String handleRedirect(int data) {
        switch (data) {
            case 1:
                return baseUrl + sessionId + "/Member/Password?s=" + sessionId;
            case 2:
                return baseUrl + sessionId + "/Member/SelfSetting?s=" + sessionId;
            default:
                return baseUrl + sessionId + "/Setting/GetBackendSysNotice?g=&_=" + System.currentTimeMillis();
        }
    }

    /**
     * 获取系统公告
     * 
     * @return 公告响应
     * @throws IOException
     * @throws InterruptedException
     */
    private String getBackendNotice() throws IOException, InterruptedException {
        String backendNoticeUrl = baseUrl + sessionId + "/Setting/GetBackendSysNotice?g=&_="
                + System.currentTimeMillis();
        return get(backendNoticeUrl);
    }

    /**
     * 处理公告页面
     * 
     * @param notices 公告列表
     * @return 公告页面响应
     * @throws IOException
     * @throws InterruptedException
     */
    private String handleNoticePage(JSONArray notices) throws IOException, InterruptedException {
        if (notices.length() > 0) {
            String noticePageUrl = baseUrl + sessionId + "/Htmls/egis-notice.cshtml?s=" + sessionId;
            return get(noticePageUrl);
        }
        return null;
    }

    /**
     * 访问首页
     * 
     * @return 首页响应
     * @throws IOException
     * @throws InterruptedException
     */
    private String visitIndexPage() throws IOException, InterruptedException {
        String indexUrl = baseUrl + sessionId + "/App/Index?_=" + System.currentTimeMillis() + "#!online_account";
        return get(indexUrl);
    }

    /**
     * 获取在线状态
     * 
     * @return 在线状态响应
     * @throws IOException
     * @throws InterruptedException
     */
    private String getOnlineStatus() throws IOException, InterruptedException {
        String onlineUrl = baseUrl + sessionId + "/Member/Online?_=" + System.currentTimeMillis();
        return get(onlineUrl);
    }

    /**
     * 获取账户信息
     * 
     * @return 账户信息响应
     * @throws IOException
     * @throws InterruptedException
     */
    private String getAccountInfo() throws IOException, InterruptedException {
        String accountUrl = baseUrl + sessionId + "/Account/RetriveSelfAccount?_=" + System.currentTimeMillis();
        return get(accountUrl);
    }

    /**
     * 处理协议响应
     * 
     * @param response 协议响应
     * @return 处理结果
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean handleAgreementResponse(String response) throws IOException, InterruptedException {
        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
            int data = jsonResponse.getInt("Data");
            String redirectUrl = handleRedirect(data);
            return processRedirect(redirectUrl);
        }
        return false;
    }

    /**
     * 处理重定向
     * 
     * @param redirectUrl 重定向URL
     * @return 处理结果
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean processRedirect(String redirectUrl) throws IOException, InterruptedException {
        String response = get(redirectUrl);
        response = getBackendNotice();

        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
            JSONArray notices = jsonResponse.getJSONArray("Data");
            handleNoticePage(notices);
            setNoticeLoginCookie();
            return processIndexPage();
        }
        return false;
    }

    /**
     * 处理首页
     * 
     * @return 处理结果
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean processIndexPage() throws IOException, InterruptedException {
        String response = visitIndexPage();
        Pattern pattern = Pattern.compile("class=\"main\"\\s+[^=]+=\"(\\d+)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            this.periodNo = matcher.group(1);
            getCurrentPeriodStatus();
        }
        return processOnlineStatus();
    }

    /**
     * 处理在线状态
     * 
     * @return 处理结果
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean processOnlineStatus() throws IOException, InterruptedException {
        String response = getOnlineStatus();
        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse == null || jsonResponse.getInt("Status") != 1) {
            return false;
        }
        return processAccountInfo();
    }

    /**
     * 处理账户信息
     * 
     * @return 处理结果
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean processAccountInfo() throws IOException, InterruptedException {
        String response = getAccountInfo();
        JSONObject accountResponse = parseJsonResponse(response);
        if (accountResponse != null && accountResponse.getInt("Status") == 1) {
            JSONObject accountData = accountResponse.getJSONObject("Data");
            updateAccountConfig(accountData);
            // this.accountConfig.printConfig();
            try {
                saveToFile(response, "account_config.json");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 处理系统公告
     * 
     * @return 处理是否成功
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean handleSysNotice() throws IOException, InterruptedException {
        if (sessionId == null) {
            return false;
        }

        // 1. 访问系统公告页面
        String response = visitSysNoticePage();

        // 2. 访问协议页面
        String agreementUrl = baseUrl + sessionId + "/Member/Agreement";
        response = get(agreementUrl);

        // 检查是否需要等待倒计时
        if (needWaitCountdown(response)) {
            // 七星彩，需要等待10秒
            // Thread.sleep(10000);
        }

        // 3. 接受协议
        String acceptUrl = baseUrl + sessionId + "/Member/AcceptAgreement?_=" + System.currentTimeMillis();
        response = get(acceptUrl);

        // 设置NOTICE_LOGIN_IN cookie
        setNoticeLoginCookie();

        // 处理协议响应
        return handleAgreementResponse(response);
    }

    /**
     * 访问协议页面
     * 
     * @return 协议页面响应
     * @throws IOException
     */
    private String visitAgreementPage() throws IOException {
        String agreementUrl = baseUrl + sessionId + "/Member/Agreement?_=" + System.currentTimeMillis();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        return get(agreementUrl);
    }

    /**
     * 接受协议
     * 
     * @return 接受协议响应
     * @throws IOException
     */
    private String acceptAgreement() throws IOException {
        String acceptUrl = baseUrl + sessionId + "/Member/AcceptAgreement?_=" + System.currentTimeMillis();
        return get(acceptUrl);
    }

    /**
     * 检查是否需要二次验证
     * 
     * @param data 登录响应数据
     * @return 是否需要二次验证
     */
    private boolean needSecondAuth(JSONObject data) {
        int memberLevel = data.getInt("member_level");
        int smsStatus = data.getInt("sms_status");
        int gaStatus = data.getInt("ga_status");
        return memberLevel >= 2 && (smsStatus == 1 || gaStatus == 1);
    }

    /**
     * 登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录是否成功
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean login(String username, String password) throws IOException, InterruptedException {
        if (exponent == null || publicKey == null || sessionId == null) {
            return false;
        }

        // 构建登录请求
        String requestBody = buildLoginRequestBody(username, password);
        if (requestBody == null) {
            return false;
        }

        String loginUrl = baseUrl + sessionId + "/Member/DoLogin?_=" + System.currentTimeMillis();
        Map<String, String> headers = buildLoginHeaders();

        // 发送登录请求
        String response = post(loginUrl, requestBody, headers);
        System.out.println("登录响应:" + response);

        return handleLoginResponse(response);
    }

    // 获取系统公告
    public String getSysNotice() throws IOException, InterruptedException {
        String noticeUrl = baseUrl + "/Member/SysNotice?_=" + System.currentTimeMillis();
        return get(noticeUrl);
    }

    // 添加获取配置的方法
    public AccountConfig getAccountConfig() {
        return this.accountConfig;
    }

    // 添加更新配置的方法
    public void updateAccountConfig(JSONObject accountData) {
        this.accountConfig.updateFromJson(accountData);
    }

    // 获取特定玩法的限额信息
    public String getGameLimitInfo(String noTypeName) {
        return this.accountConfig.getLimitInfo(noTypeName);
    }

    // 获取所有可用玩法
    public List<String> getAllGameTypes() {
        return this.accountConfig.getAllGameTypes();
    }

    // 检查配置是否有效
    public boolean isConfigValid() {
        return this.accountConfig.isValid();
    }

    // 获取特定dict_no_type_id的详细信息
    public String getDictNoTypeInfo(int dictNoTypeId) {
        return this.accountConfig.getDictNoTypeInfo(dictNoTypeId);
    }

    // 获取所有dict_no_type_id
    public List<Integer> getAllDictNoTypeIds() {
        return this.accountConfig.getAllDictNoTypeIds();
    }

    // 获取dict_no_type_id和玩法名称的映射
    public Map<Integer, String> getDictNoTypeMap() {
        return this.accountConfig.getDictNoTypeMap();
    }

    // 根据dict_no_type_id更新投注设置
    public boolean updateBettingSettingById(int dictNoTypeId, double minBet, double oneBetLimit,
            double oneItemLimit, double odds1, double holdMoney) {
        return this.accountConfig.updateBettingSettingById(dictNoTypeId, minBet, oneBetLimit,
                oneItemLimit, odds1, holdMoney);
    }

    // 根据dict_no_type_id更新所有赔率
    public boolean updateOddsById(int dictNoTypeId, double odds1, double odds2,
            double odds3, double odds4) {
        return this.accountConfig.updateOddsById(dictNoTypeId, odds1, odds2, odds3, odds4);
    }

    // 根据dict_no_type_id更新限额
    public boolean updateLimitsById(int dictNoTypeId, double minBet,
            double oneBetLimit, double oneItemLimit) {
        return this.accountConfig.updateLimitsById(dictNoTypeId, minBet, oneBetLimit, oneItemLimit);
    }

    // 根据dict_no_type_id更新占成金额
    public boolean updateHoldMoneyById(int dictNoTypeId, double holdMoney) {
        return this.accountConfig.updateHoldMoneyById(dictNoTypeId, holdMoney);
    }

    // 获取特定dict_no_type_id的占成金额
    public double getHoldMoneyById(int dictNoTypeId) {
        return this.accountConfig.getHoldMoneyById(dictNoTypeId);
    }

    // 获取所有占成金额
    public Map<Integer, Double> getAllHoldMoney() {
        return this.accountConfig.getAllHoldMoney();
    }

    // 显示所有占成金额
    public void printAllHoldMoney() {
        Map<Integer, Double> holdMoneyMap = getAllHoldMoney();
        Map<Integer, String> gameMap = getDictNoTypeMap();

        for (Map.Entry<Integer, Double> entry : holdMoneyMap.entrySet()) {
            int id = entry.getKey();
            String gameName = gameMap.get(id);
            double holdMoney = entry.getValue();
            System.out.printf("ID: %d - 玩法: %s - 占成金额: %.2f%n",
                    id, gameName, holdMoney);
        }
    }

    // 生成占成金额更新数据
    public String generateHoldMoneyUpdateData() {
        return accountConfig.generateHoldMoneyUpdateData();
    }

    // 获取实时投注数据
    public String getRealBetData(int fixNum, int pageIndex, int pageSize) throws IOException, InterruptedException {
        String url = baseUrl + sessionId + "/RealBet/GetRealBetDealerDataByFixNum?fix_num=" + fixNum +
                "&pagesize=" + pageSize + "&pageindex=" + pageIndex + "&captcha_code=&_=" + System.currentTimeMillis();
        return get(url);
    }

    public boolean getRealBetDataProxy(int fixNum, int pageIndex, int pageSize)
            throws IOException, InterruptedException {
        String url = baseUrl + sessionId + "/RealBet/GetRealBetDealerDataByFixNum?fix_num=" + fixNum +
                "&pagesize=" + pageSize + "&pageindex=" + pageIndex + "&captcha_code=&_=" + System.currentTimeMillis();
        String response = get(url);
        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
            System.out.println("fixNum:" + fixNum + " pageIndex:" + pageIndex + "获取成功");
        }
        return jsonResponse != null && jsonResponse.getInt("Status") == 1;
    }

    // 获取在线账户数据
    public String getOnlineAccountData() throws IOException, InterruptedException {
        String url = baseUrl + sessionId + "/Htmls/online-account.html";
        return get(url);
    }

    public void printOnlineAccountData() throws IOException, InterruptedException {
        String response = getOnlineAccountData();
        Pattern pattern = Pattern.compile("class=\"main\"\\s+[^=]+=\"(\\d+)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            this.periodNo = matcher.group(1);
        }
    }

    public String getPeriodNo() {
        return this.periodNo;
    }

    public void setPeriodNo(String periodNo) {
        this.periodNo = periodNo;
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
        String url = baseUrl + sessionId + "/drawno/GetCurrentPeriodStatus?period_no=" + periodNo + "&_="
                + System.currentTimeMillis();
        String response = get(url);
        JSONObject jsonResponse = parseJsonResponse(response);
        return jsonResponse != null && jsonResponse.getInt("Status") == 1;
    }

    /**
     * 处理用户协议
     * 
     * @return 处理是否成功
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean handleAgreement() throws IOException, InterruptedException {
        if (sessionId == null) {
            return false;
        }

        // 访问协议页面
        visitAgreementPage();

        // 接受协议
        String response = acceptAgreement();

        // 处理协议响应
        return handleAgreementResponse(response);
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

    /**
     * 构建登录请求头
     * 
     * @return 登录请求头
     */
    private Map<String, String> buildLoginHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return headers;
    }

    /**
     * 处理登录响应
     * 
     * @param response 登录响应
     * @return 登录是否成功
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean handleLoginResponse(String response) throws IOException, InterruptedException {
        JSONObject jsonResponse = parseJsonResponse(response);
        if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
            JSONObject data = jsonResponse.getJSONObject("Data");

            if (needSecondAuth(data)) {
                return false;
            } else {
                return handleAgreement();
            }
        } else {
            String errorMsg = jsonResponse != null ? jsonResponse.getString("Message") : "未知错误";
            System.err.println("登录失败: " + errorMsg);
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public static class LineInfo {
        private final String url;
        private final int index;
        private long latency;
        private boolean timeout;

        public LineInfo(String url, int index) {
            this.url = url;
            this.index = index;
            this.latency = Long.MAX_VALUE;
            this.timeout = false;
        }

        public String getUrl() {
            return url;
        }

        public int getIndex() {
            return index;
        }

        public long getLatency() {
            return latency;
        }

        public void setLatency(long latency) {
            this.latency = latency;
        }

        public boolean isTimeout() {
            return timeout;
        }

        public void setTimeout(boolean timeout) {
            this.timeout = timeout;
        }
    }

    private String getDomainFromUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String domain = uri.getHost();
            return domain != null ? domain : baseUrl;
        } catch (Exception e) {
            return baseUrl;
        }
    }

    private boolean extractCookie(String responseBody) throws IOException {
        if (responseBody.contains("document.cookie")) {
            Pattern pattern = Pattern.compile("document\\.cookie='(.*?)'");
            Matcher matcher = pattern.matcher(responseBody);
            if (matcher.find()) {
                String cookie = matcher.group(1);
                // 分割cookie名称和值（包括属性）
                String[] parts = cookie.split("=", 2);
                if (parts.length == 2) {
                    // 只取第一个分号前的内容作为cookie值
                    String cookieValue = parts[1].split(";")[0];
                    String domain = getDomainFromUrl(baseUrl);
                    // 确保domain格式正确，去掉开头的点
                    String cleanDomain = domain.startsWith(".") ? domain.substring(1) : domain;
                    httpClientUtil.setCookie(parts[0], cookieValue, cleanDomain, "/");
                    return true;
                }
            }
        }
        return false;
    }

    public String testProxy() throws IOException {
        String testUrl = "https://httpbin.org/ip"; // 使用 HTTPS 协议
        String response = httpClientUtil.doGet(testUrl, null).getContent();
        System.out.println("代理测试响应: " + response);
        return response;
    }

    public String getRequestHeaders() {
        return httpClientUtil.getHeaders().toString();
    }

    public String getCookies() {
        return httpClientUtil.getCookies().toString();
    }
}