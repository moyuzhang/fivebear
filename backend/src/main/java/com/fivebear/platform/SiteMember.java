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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SiteMember {
    private static final Logger logger = Logger.getLogger(SiteContent.class.getName());
    private static final int TIMEOUT = 5000; // 5 seconds timeout
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY = 3000; // 3 seconds

    private String baseUrl;
    private final HttpClientUtil httpClientUtil;
    private List<LineInfo> lines = new ArrayList<>();
    private String publicKey;
    private String exponent;
    private String sessionId;
    private AccountConfig accountConfig;
    private String periodNo;
    private String account;

    public SiteMember(String baseUrl) {
        this.baseUrl = baseUrl;
        this.accountConfig = new AccountConfig();
        this.httpClientUtil = new HttpClientUtil();
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
     * @param url           请求URL
     * @param body          请求体
     * @param headers       自定义请求头，如果为null则使用默认请求头
     * @param timeoutMillis 超时时间（毫秒），如果小于等于0则使用默认超时
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    private String post(String url, String body, Map<String, String> headers, int timeoutMillis) throws IOException {
        try {
            return httpClientUtil.doPostJson(url, body, headers, timeoutMillis);
        } catch (Exception e) {
            throw new IOException("Failed to send request: " + e.getMessage(), e);
        }
    }

    /**
     * 发送POST请求（使用默认超时）
     * 
     * @param url     请求URL
     * @param body    请求体
     * @param headers 自定义请求头，如果为null则使用默认请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    private String post(String url, String body, Map<String, String> headers) throws IOException {
        return post(url, body, headers, -1);
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
            // 1. 获取最快线路
            String line = getBestLine();
            if (line == null) {
                System.err.println("未找到可用线路");
                return false;
            }
            System.out.println("使用线路: " + line);
            // 更新baseUrl
            this.baseUrl = "https://" + line;

            // 2. 访问登录页面获取加密参数
            String response = get(baseUrl + "/Member/Login?_=" + System.currentTimeMillis());
            extractLoginValues(response);

            if (sessionId != null && !sessionId.isEmpty()) {
                // 3. 执行登录（包括同意协议和访问首页）
                boolean isLogin = login(username, password);
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

    private boolean handlePostLogin() throws IOException, InterruptedException {
        try {
            // 1. 获取当前期号状态
            if (!getCurrentPeriodStatus()) {
                System.err.println("获取期号状态失败");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.severe("登录后处理失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void extractPeriodNo(String response) {
        Pattern pattern = Pattern.compile("class=\"main\"\\s+[^=]+=\"(\\d+)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            this.periodNo = matcher.group(1);
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
        lineUrls.add("f1.pt755b62.xyz");
        lineUrls.add("f2.pt755b62.xyz");
        lineUrls.add("f3.pt755b62.xyz");
        lineUrls.add("f4.pt755b62.xyz");
        lineUrls.add("f5.pt755b62.xyz");
        lineUrls.add("f6.pt755b62.xyz");
        lineUrls.add("f7.pt755b62.xyz");
        lineUrls.add("f8.pt755b62.xyz");
        lineUrls.add("f9.pt755b62.xyz:8443");
        return lineUrls;
    }

    // 测试单个线路
    private String testLine(LineInfo line) {
        long startTime = System.currentTimeMillis();
        String testUrl = "https://" + line.getUrl() + "/Member/Login?_=" + System.currentTimeMillis();

        try {
            HttpClientUtil.Response response = httpClientUtil.doGet(testUrl);
            String responseBody = response.getContent();

            if (response.getStatusCode() == 200) {
                line.latency = System.currentTimeMillis() - startTime;
                line.timeout = false;
                System.out.println("线路 " + line.getUrl() + " 延迟: " + line.latency + "ms");
                return responseBody;
            } else {
                line.timeout = true;
                System.out.println("线路 " + line.getUrl() + " 无响应");
                return null;
            }
        } catch (Exception e) {
            line.timeout = true;
            System.out.println("线路 " + line.getUrl() + " 测试失败: " + e.getMessage());
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

        // 返回最快的可用线路
        for (LineInfo line : lines) {
            if (!line.timeout) {
                System.out.println("选择最快线路: " + line.getUrl() + " (延迟: " + line.latency + "ms)");
                return line.getUrl();
            }
        }

        return null;
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
            // String redirectUrl = handleRedirect(data);
            // return processRedirect(redirectUrl);
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

        try {
            // 1. 构建登录请求
            String requestBody = buildLoginRequestBody(username, password);
            if (requestBody == null) {
                return false;
            }

            String loginUrl = baseUrl + sessionId + "/Member/DoLogin?_=" + System.currentTimeMillis();
            Map<String, String> headers = buildLoginHeaders();

            // 2. 发送登录请求
            String response = post(loginUrl, requestBody, headers);
            System.out.println("登录响应:" + response);

            // 3. 检查登录响应
            JSONObject jsonResponse = parseJsonResponse(response);
            if (jsonResponse == null || jsonResponse.getInt("Status") != 1) {
                String errorMsg = jsonResponse != null ? jsonResponse.getString("Message") : "未知错误";
                System.err.println("登录失败: " + errorMsg);
                return false;
            }

            // 4. 访问系统公告页面
            String noticeUrl = baseUrl + sessionId + "/Member/SysNotice?_=" + System.currentTimeMillis();
            String noticeResponse = get(noticeUrl);
            if (noticeResponse == null) {
                System.err.println("访问系统公告页面失败");
                return false;
            }

            // 5. 访问协议页面
            String agreementUrl = baseUrl + sessionId + "/Member/Agreement";
            String agreementResponse = get(agreementUrl);
            if (agreementResponse == null) {
                System.err.println("访问协议页面失败");
                return false;
            }

            // 6. 接受协议
            String acceptUrl = baseUrl + sessionId + "/Member/AcceptAgreement?_=" + System.currentTimeMillis();
            String acceptResponse = get(acceptUrl);
            JSONObject acceptJson = parseJsonResponse(acceptResponse);
            if (acceptJson == null || acceptJson.getInt("Status") != 1) {
                System.err.println("接受协议失败");
                return false;
            }

            // 7. 设置登录通知cookie
            setNoticeLoginCookie();

            // 8. 获取期号状态和小票信息
            if (!getCurrentPeriodStatus()) {
                System.err.println("获取期号状态和小票信息失败");
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.severe("登录过程发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
        try {
            // 1. 获取首页内容，提取periodStatus
            String indexUrl = baseUrl + sessionId + "/App/Index?_=" + System.currentTimeMillis();
            String indexResponse = get(indexUrl);
            if (indexResponse == null) {
                System.err.println("获取首页失败");
                return false;
            }

            // 提取periodStatus
            Pattern pattern = Pattern.compile("var\\s+periodStatus\\s*=\\s*(\\d+)");
            Matcher matcher = pattern.matcher(indexResponse);
            if (!matcher.find()) {
                System.err.println("未找到periodStatus");
                return false;
            }
            String periodStatus = matcher.group(1);
            System.out.println("获取到periodStatus: " + periodStatus);

            // 2. 获取期号状态
            String statusUrl = baseUrl + sessionId + "/drawno/GetCurrentPeriodStatus?period_no=" + periodStatus + "&_="
                    + System.currentTimeMillis();
            String statusResponse = get(statusUrl);
            System.out.println("期号状态响应: " + statusResponse);

            JSONObject statusJson = parseJsonResponse(statusResponse);
            if (statusJson == null || statusJson.getInt("Status") != 1) {
                System.err.println("获取期号状态失败");
                return false;
            }

            // 3. 从状态响应中获取实际期号
            JSONObject data = statusJson.getJSONObject("Data");
            if (data == null) {
                System.err.println("期号状态数据为空");
                return false;
            }

            this.periodNo = String.valueOf(data.getInt("period_no"));
            System.out.println("当前期号: " + this.periodNo);

            // 4. 获取会员小票信息
            return getMemberPrint();

        } catch (Exception e) {
            logger.severe("获取期号状态时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取会员小票信息
     * 
     * @return 是否成功
     * @throws IOException
     */
    private boolean getMemberPrint() throws IOException {
        try {
            String printUrl = baseUrl + sessionId + "/Member/GetMemberPrint";
            String printResponse = get(printUrl);
            System.out.println("小票信息响应: " + printResponse);

            JSONObject printJson = parseJsonResponse(printResponse);
            if (printJson == null || printJson.getInt("Status") != 1) {
                System.err.println("获取小票信息失败");
                return false;
            }

            // 验证小票信息
            JSONObject printData = printJson.getJSONObject("Data");
            if (printData == null) {
                System.err.println("小票数据为空");
                return false;
            }

            // 验证期号匹配
            String printPeriodNo = printData.getString("period_no");
            if (!this.periodNo.equals(printPeriodNo)) {
                System.err.println("小票期号不匹配");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.severe("获取小票信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量预下注
     * 
     * @param bets 下注列表，每个元素包含 dict_no_type_id、bet_no 和 bet_money
     * @return 是否成功
     */
    public boolean collectBatchBets(List<Map<String, Object>> bets) {
        try {
            // 构建请求参数
            JSONObject requestData = new JSONObject();
            requestData.put("Data", new JSONArray(bets));
            requestData.put("collect_way", "3"); // 批量模式
            requestData.put("vs", SimpleVsGenerator.generateVs()); // 现在直接使用int值

            // 发送请求
            String url = baseUrl + sessionId + "/CollectBet/CollectBets";
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // 构建固定格式的请求体
            StringBuilder formData = new StringBuilder();
            formData.append("Data=")
                    .append(java.net.URLEncoder.encode(requestData.getJSONArray("Data").toString(), "UTF-8"))
                    .append("&collect_way=").append(requestData.getString("collect_way"))
                    .append("&vs=").append(requestData.getInt("vs"));

            String response = post(url, formData.toString(), headers, 30 * 1000);
            JSONObject jsonResponse = parseJsonResponse(response);

            // 检查响应
            if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
                JSONObject data = jsonResponse.getJSONObject("Data");
                return true;
            }
        } catch (Exception e) {
            System.err.println("预下注请求失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取预下注列表
     * 
     * @return 预下注列表
     */
    public String getCollectBets() {
        try {
            String url = baseUrl + sessionId + "/CollectBet/GetCollectBets?_=" + System.currentTimeMillis();
            return get(url);
        } catch (Exception e) {
            System.err.println("获取预下注列表失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除预下注
     * 
     * @param collectBetId 预下注ID
     * @return 是否成功
     */
    public boolean removeCollectBet(String collectBetId) {
        try {
            String url = baseUrl + sessionId + "/CollectBet/RemovePrebet";
            JSONObject requestData = new JSONObject();
            requestData.put("collect_bet_id", collectBetId);

            String response = post(url, requestData.toString(), null);
            JSONObject jsonResponse = parseJsonResponse(response);

            return jsonResponse != null && jsonResponse.getInt("Status") == 1;
        } catch (Exception e) {
            System.err.println("删除预下注失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取预下注汇总信息
     * 
     * @param dictNoTypeId 玩法类型ID
     * @return 汇总信息
     */
    public String getCollectSummary(int dictNoTypeId) {
        try {
            String url = baseUrl + sessionId + "/CollectBet/GetCollectSummary";
            JSONObject requestData = new JSONObject();
            requestData.put("dict_no_type_id", dictNoTypeId);

            return post(url, requestData.toString(), null);
        } catch (Exception e) {
            System.err.println("获取预下注汇总失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 按玩法类型清除预下注
     * 
     * @param dictNoTypeId 玩法类型ID
     * @return 是否成功
     */
    public boolean removeCollectBetsByType(int dictNoTypeId) {
        try {
            String url = baseUrl + sessionId + "/CollectBet/RemoveByType?dict_no_type_id=" + dictNoTypeId + "&_="
                    + System.currentTimeMillis();
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("X-Requested-With", "XMLHttpRequest");

            System.out.println("清除玩法类型预下注请求: " + url);
            String response = get(url);
            System.out.println("清除玩法类型预下注响应: " + response);

            JSONObject jsonResponse = parseJsonResponse(response);
            return jsonResponse != null && jsonResponse.getInt("Status") == 1;
        } catch (Exception e) {
            System.err.println("清除玩法类型预下注失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取预下注历史
     * 
     * @param fixNum 固定号码
     * @return 历史记录
     */
    public String getCollectHistory(int fixNum) {
        try {
            String url = baseUrl + sessionId + "/CollectBet/GetHistory";
            JSONObject requestData = new JSONObject();
            requestData.put("fix_num", fixNum);

            return post(url, requestData.toString(), null);
        } catch (Exception e) {
            System.err.println("获取预下注历史失败: " + e.getMessage());
            return null;
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

    /**
     * 清除预下注打印记录
     * 调用 /CollectBet/CleanPrint 接口清除预下注的打印记录
     * 成功返回 {"Status":1}
     * 
     * @return 是否清除成功
     */
    public boolean clearCollectBetPrint() {
        try {
            // 构建请求URL
            String url = baseUrl + sessionId + "/CollectBet/CleanPrint?_=" + System.currentTimeMillis();

            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("X-Requested-With", "XMLHttpRequest");

            // 发送请求
            System.out.println("开始清除预下注打印记录...");
            String response = get(url);
            System.out.println("清除预下注打印记录响应: " + response);

            // 解析响应
            JSONObject jsonResponse = parseJsonResponse(response);
            if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
                System.out.println("预下注打印记录清除成功");
                return true;
            } else {
                String message = jsonResponse != null ? jsonResponse.optString("Message", "未知错误") : "响应解析失败";
                System.err.println("清除预下注打印记录失败: " + message);
                return false;
            }
        } catch (Exception e) {
            System.err.println("清除预下注打印记录异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取预下注小票信息
     * 
     * @param pageIndex 页码（从1开始）
     * @return 小票信息的JSON字符串
     */
    public String getCollectBetPrint(int pageIndex) {
        try {
            String url = baseUrl + sessionId + "/CollectBet/GetMemberPrint?_=" + System.currentTimeMillis()
                    + "&PageIndex=" + pageIndex;
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("X-Requested-With", "XMLHttpRequest");

            System.out.println("获取预下注小票请求: " + url);
            String response = get(url);

            // 验证响应
            JSONObject jsonResponse = parseJsonResponse(response);
            if (jsonResponse != null && jsonResponse.getInt("Status") == 1) {
                // JSONObject data = jsonResponse.getJSONObject("Data");
                // if (data != null) {
                // // 打印一些关键信息
                // System.out.println("会员账号: " + data.optString("member_account"));
                // System.out.println("期号: " + data.optString("period_no"));
                // System.out.println("总投注金额: " + data.optString("TotalBetMoney"));
                // System.out.println("总记录数: " + data.optInt("RecordsCount"));
                // System.out.println("总页数: " + data.optInt("PageCount"));
                // }
                return response;
            } else {
                System.err.println(
                        "获取预下注小票失败: " + (jsonResponse != null ? jsonResponse.optString("Message", "未知错误") : "响应解析失败"));
                return null;
            }
        } catch (Exception e) {
            System.err.println("获取预下注小票异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有预下注小票信息
     * 
     * @return 所有页的小票信息列表
     */
    public List<String> getAllCollectBetPrints() {
        List<String> allPrints = new ArrayList<>();
        try {
            String firstPage = getCollectBetPrint(1);
            if (firstPage == null)
                return allPrints;

            JSONObject firstPageJson = new JSONObject(firstPage);
            if (firstPageJson.getInt("Status") != 1)
                return allPrints;

            allPrints.add(firstPage);
            int pageCount = firstPageJson.getJSONObject("Data").getInt("PageCount");

            // 多线程采集剩余页面
            ExecutorService executor = Executors.newFixedThreadPool(8); // 8线程，可根据实际调整
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 2; i <= pageCount; i++) {
                final int pageIndex = i;
                futures.add(executor.submit(() -> getCollectBetPrint(pageIndex)));
            }
            for (Future<String> future : futures) {
                String pageData = future.get();
                if (pageData != null) {
                    allPrints.add(pageData);
                }
            }
            executor.shutdown();
        } catch (Exception e) {
            System.err.println("获取所有预下注小票异常: " + e.getMessage());
        }
        return allPrints;
    }

    private String extractMainDomain(String url) {
        String domain = url.replaceFirst("https?://", "").split("/")[0];
        String[] parts = domain.split("\\.");
        if (parts.length >= 3) {
            return parts[1];
        }
        return domain;
    }

    // 预下注并采集赔率
    public void collectOddsFromBatchBet(List<Map<String, Object>> bets) {
        removeCollectBetsByType(11);
        clearCollectBetPrint();
        // 1. 预下注
        boolean success = collectBatchBets(bets);
        if (!success) {
            System.err.println("预下注失败");
            return;
        }

        // 2. 获取小票
        List<String> printList = getAllCollectBetPrints(); // 你已有的方法
        String domain = extractMainDomain(baseUrl);

        for (String printJson : printList) {
            JSONObject printObj = new JSONObject(printJson);
            if (printObj.getInt("Status") != 1)
                continue;
            JSONObject data = printObj.getJSONObject("Data");
            String lottery_name = data.getString("lottery_name");
            String period_no = data.getString("period_no");
            int pageIndex = data.getInt("pageIndex");
            String bet_datetime = data.getString("bet_datetime");
            JSONArray Details = data.optJSONArray("Details");
            if (Details == null)
                continue;

            for (int i = 0; i < Details.length(); i++) {
                JSONObject bet = Details.getJSONObject(i);
                String number = bet.getString("bet_no");
                String oddsStr = bet.getString("odds");
                double oddsValue = Double.parseDouble(oddsStr.split(":")[1]);
                int dictNoTypeId = bet.getInt("dict_no_type_id");
                // OddsManager.getInstance().addOdds(number, dictNoTypeId, domain, oddsValue,
                // bet_datetime);
            }
        }
        // System.out.println("采集赔率完成，赔率数量：" + OddsManager.getInstance().getSize());
    }

    /**
     * 批量正式下注
     * 
     * @param bets      下注列表，每个元素包含 dict_no_type_id、bet_no 和 bet_money
     * @param isPackage 是否包牌（0/1）
     * @param way       下注方式（如103/104），默认103
     * @return 响应JSON字符串
     */
    public String batchBet(List<Map<String, Object>> bets, String isPackage, String way) {
        try {
            cleanPrint();
            int totalCount = bets != null ? bets.size() : 0;
            double totalBetMoney = 0.0;
            for (Map<String, Object> bet : bets) {
                Object moneyObj = bet.get("bet_money");
                if (moneyObj != null) {
                    try {
                        totalBetMoney += Double.parseDouble(moneyObj.toString());
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            int vs = SimpleVsGenerator.generateVs();

            // 构建请求体
            StringBuilder formData = new StringBuilder();
            formData.append("bets=").append(java.net.URLEncoder.encode(new JSONArray(bets).toString(), "UTF-8"))
                    .append("&way=").append(way)
                    .append("&guid=")
                    .append("&is_package=").append(isPackage)
                    .append("&TotalCount=").append(totalCount)
                    .append("&TotalBetMoney=").append(totalBetMoney)
                    .append("&vs=").append(vs);

            String url = baseUrl + sessionId + "/Member/BatchBet";
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            String response = post(url, formData.toString(), headers, 30 * 1000);
            System.out.println("正式下注响应: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("正式下注请求失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 批量退码（撤销下注）
     * 
     * @param betList 下注列表，每个元素包含 bet_id 和 BetCount
     * @return 响应JSON字符串
     */
    public String cancelMemberBet(List<Map<String, Object>> betList) {
        try {
            // 构建 ids 字符串：bet_id|BetCount,bet_id|BetCount,...
            StringBuilder idsBuilder = new StringBuilder();
            for (int i = 0; i < betList.size(); i++) {
                Map<String, Object> bet = betList.get(i);
                String betId = String.valueOf(bet.get("bet_id"));
                String betCount = String.valueOf(bet.get("BetCount"));
                if (i > 0)
                    idsBuilder.append(",");
                idsBuilder.append(betId).append("|").append(betCount);
            }
            String ids = idsBuilder.toString();

            String url = baseUrl + sessionId + "/Member/CancelMemberBet";
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            String formData = "ids=" + java.net.URLEncoder.encode(ids, "UTF-8");
            String response = post(url, formData, headers, 30 * 1000);
            return response;
        } catch (Exception e) {
            System.err.println("退码请求失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有下注订单（自动分页）
     * 
     * @return List<Map<String, Object>> 每个Map为一个订单（注单）
     */
    public Object getAllMemberBets() {
        List<Map<String, Object>> allOrders = new ArrayList<>();
        int pageIndex = 1;
        int pageCount = 1;
        int successPages = 0;
        int failPages = 0;

        do {
            Map<String, Object> orderInfo = getMemberBetsByPage(pageIndex);
            if (orderInfo == null) {
                failPages++;
            } else {
                allOrders.add(orderInfo);
                successPages++;
                // 只需第一页时获取 pageCount
                if (pageIndex == 1) {
                    Object pc = orderInfo.get("PageCount");
                    if (pc instanceof Integer) {
                        pageCount = (Integer) pc;
                    } else if (pc instanceof Number) {
                        pageCount = ((Number) pc).intValue();
                    }
                }
            }
            pageIndex++;
        } while (pageIndex <= pageCount);

        Map<String, Object> result = new HashMap<>();
        result.put("orders", allOrders);
        result.put("successPages", successPages);
        result.put("failPages", failPages);
        return result;
    }

    /**
     * 整单退码（撤销整个订单）
     * 
     * @param serialNo   订单流水号
     * @param totalCount 注数
     * @return 响应JSON字符串
     */
    public String cancelOrder(String serialNo, int totalCount) {
        try {
            String ids = "{" + serialNo + "}|" + totalCount;
            String url = baseUrl + sessionId + "/Member/CancelMemberBet";
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String formData = "ids=" + java.net.URLEncoder.encode(ids, "UTF-8");
            String response = post(url, formData, headers, 30 * 1000);
            return response;
        } catch (Exception e) {
            System.err.println("整单退码请求失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 清除会员小票
     * 
     * @return 是否清除成功
     */
    public boolean cleanPrint() {
        try {
            String url = baseUrl + sessionId + "/Member/CleanPrint?_=" + System.currentTimeMillis();
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("X-Requested-With", "XMLHttpRequest");
            String response = get(url);
            JSONObject json = new JSONObject(response);
            return json.getInt("Status") == 1;
        } catch (Exception e) {
            System.err.println("清除小票失败: " + e.getMessage());
            return false;
        }
    }

    // 默认 isPackage=0, way=103
    public String batchBet(List<Map<String, Object>> bets) {
        return batchBet(bets, "0", "103");
    }

    // 默认 way=103
    public String batchBet(List<Map<String, Object>> bets, String isPackage) {
        return batchBet(bets, isPackage, "103");
    }

    public Map<String, Object> getMemberBetsByPage(int pageIndex) {
        int maxRetries = 3;
        int retry = 0;
        while (retry < maxRetries) {
            try {
                String url = baseUrl + sessionId + "/Member/GetMemberPrint?pageindex=" + pageIndex + "&_="
                        + System.currentTimeMillis();
                String response = get(url);
                JSONObject json = new JSONObject(response);
                if (json.getInt("Status") != 1)
                    return null;

                JSONObject data = json.getJSONObject("Data");

                // 主单信息
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("bet_datetime", data.optString("bet_datetime"));
                orderInfo.put("member_account", data.optString("member_account"));
                orderInfo.put("serial_no", data.optString("serial_no"));
                orderInfo.put("max_id", data.optString("max_id"));
                orderInfo.put("period_no", data.optString("period_no"));
                orderInfo.put("print_no", data.optString("print_no"));
                orderInfo.put("credit", data.optString("credit"));
                orderInfo.put("credit_balance", data.optString("credit_balance"));
                orderInfo.put("credit_assigned", data.optString("credit_assigned"));
                orderInfo.put("is_cash", data.optString("is_cash"));
                orderInfo.put("show_mode", data.optString("show_mode"));
                orderInfo.put("input_mode", data.optString("input_mode"));
                orderInfo.put("lottery_name", data.optString("lottery_name"));
                orderInfo.put("store_name", data.optString("store_name"));
                orderInfo.put("show_snapshot", data.optString("show_snapshot"));

                // 下注明细
                JSONArray details = data.optJSONArray("Details");
                List<Map<String, Object>> detailList = new ArrayList<>();
                if (details != null) {
                    for (int i = 0; i < details.length(); i++) {
                        JSONObject detail = details.getJSONObject(i);
                        Map<String, Object> betDetail = new HashMap<>();
                        betDetail.put("bet_id", detail.optString("bet_id"));
                        betDetail.put("bet_no", detail.optString("bet_no"));
                        betDetail.put("bet_money", detail.optString("bet_money"));
                        betDetail.put("odds", detail.optString("odds"));
                        betDetail.put("dict_no_type_id", detail.optString("dict_no_type_id"));
                        betDetail.put("print_no", detail.optString("print_no"));
                        detailList.add(betDetail);
                    }
                }
                orderInfo.put("details", detailList);

                // 还可以返回 pageCount 等分页信息
                orderInfo.put("PageCount", data.optInt("PageCount", 1));
                orderInfo.put("PageIndex", data.optInt("PageIndex", pageIndex));

                return orderInfo;
            } catch (Exception e) {
                System.err.println("获取下注订单单页失败(第" + (retry + 1) + "次): " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                retry++;
            }
        }
        return null;
    }

}