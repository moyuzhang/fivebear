package com.fivebear.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {
    private final PoolingHttpClientConnectionManager connectionManager;
    private HttpHost proxy = null;
    private CredentialsProvider credentialsProvider = null;
    private CloseableHttpClient httpClient;
    private final CookieStore cookieStore;
    private final HttpContext httpContext;
    private final LinkedHashMap<String, String> defaultHeaders;
    
    // 默认配置
    private static final int DEFAULT_MAX_TOTAL = 200;
    private static final int DEFAULT_MAX_PER_ROUTE = 20;
    private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    private static final int MAX_COOKIE_RETRY = 1;

    private final Object proxyLock = new Object();

    /**
     * 使用默认配置创建HttpClientUtil实例
     */
    public HttpClientUtil() {
        this(DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * 使用自定义配置创建HttpClientUtil实例
     *
     * @param maxTotal        最大连接数
     * @param maxPerRoute     每个路由的最大连接数
     * @param connectTimeout  连接超时时间（毫秒）
     * @param socketTimeout   Socket超时时间（毫秒）
     */
    public HttpClientUtil(int maxTotal, int maxPerRoute, int connectTimeout, int socketTimeout) {
        // 配置连接池
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        // 创建Cookie存储
        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        
        // 初始化默认请求头
        defaultHeaders = new LinkedHashMap<>();
        defaultHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        defaultHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        defaultHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        defaultHeaders.put("Connection", "keep-alive");
        
        // 创建HttpClient实例
        createHttpClient();
    }

    private void createHttpClient() {
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                //.setDefaultRequestConfig(baseRequestConfig)
                .setDefaultCookieStore(cookieStore)
                .setProxy(proxy)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setRoutePlanner(proxy != null ? new DefaultProxyRoutePlanner(proxy) : null)
                .build();
    }

    /**
     * 设置Cookie
     *
     * @param name  Cookie名称
     * @param value Cookie值
     */
    public void setCookie(String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath("/");
        cookie.setDomain("127.0.0.1");  // 直接设置domain为127.0.0.1
        cookieStore.addCookie(cookie);
        //System.out.println("Cookie set: " + name + "=" + value + ", domain=127.0.0.1");
    }

    /**
     * 设置Cookie（完整参数）
     *
     * @param name    Cookie名称
     * @param value   Cookie值
     * @param domain  Cookie域名
     * @param path    Cookie路径
     */
    public void setCookie(String name, String value, String domain, String path) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookieStore.addCookie(cookie);
        //System.out.println("Cookie set: " + name + "=" + value + ", domain=" + domain + ", path=" + path);
    }

    /**
     * 获取所有Cookie
     *
     * @return Cookie列表
     */
    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    /**
     * 清除所有Cookie
     */
    public void clearCookies() {
        cookieStore.clear();
    }

    /**
     * 设置代理
     *
     * @param host     代理主机
     * @param port     代理端口
     * @param username 代理用户名（可选）
     * @param password 代理密码（可选）
     */
    public void setProxy(String host, int port, String username, String password) {
        synchronized (proxyLock) {
            proxy = new HttpHost(host, port);
            if (username != null && password != null) {
                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(host, port),
                        new UsernamePasswordCredentials(username, password)
                );
            } else {
                credentialsProvider = null;
            }
            // 输出代理设置信息
            //System.out.println("代理设置信息:");
            //System.out.println("1. 代理主机: " + host);
            //System.out.println("2. 代理端口: " + port);
            //System.out.println("3. 是否需要认证: " + (username != null));
            createHttpClient();
        }
    }

    /**
     * 清除代理设置
     */
    public void clearProxy() {
        synchronized (proxyLock) {
            proxy = null;
            credentialsProvider = null;
            createHttpClient();
        }
    }

    /**
     * 获取当前请求配置
     * @param timeoutMillis 超时时间（毫秒），如果小于等于0则使用默认超时
     */
    private RequestConfig getCurrentRequestConfig(int timeoutMillis) {
        synchronized (proxyLock) {
            RequestConfig.Builder configBuilder = RequestConfig.custom()
                .setConnectTimeout(timeoutMillis > 0 ? timeoutMillis : DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(timeoutMillis > 0 ? timeoutMillis : DEFAULT_SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(timeoutMillis > 0 ? timeoutMillis : DEFAULT_CONNECT_TIMEOUT);
            
            if (proxy != null) {
                configBuilder.setProxy(proxy);
            }

            RequestConfig config = configBuilder.build();
            //System.out.println("请求配置 - " + 
            //    "连接超时:" + config.getConnectTimeout() + "ms, " +
            //    "Socket超时:" + config.getSocketTimeout() + "ms, " +
            //    "请求超时:" + config.getConnectionRequestTimeout() + "ms");
            return config;
        }
    }

    /**
     * 获取当前请求配置（使用默认超时）
     */
    private RequestConfig getCurrentRequestConfig() {
        return getCurrentRequestConfig(-1);
    }

    /**
     * HTTP响应类
     */
    public static class Response {
        private final int statusCode;
        private final String content;
        private final Map<String, String> headers;

        public Response(int statusCode, String content, Map<String, String> headers) {
            this.statusCode = statusCode;
            this.content = content;
            this.headers = headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getContent() {
            return content;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }

    /**
     * 发送GET请求，支持自定义超时时间
     * @param url 请求URL
     * @param headers 自定义请求头，如果为null则使用默认请求头
     * @param timeoutMillis 超时时间（毫秒），如果小于等于0则使用默认超时
     * @return 响应对象
     */
    public Response doGet(String url, Map<String, String> headers, int timeoutMillis) {
        return doGet(url, headers, timeoutMillis, 0);
    }

    private Response doGet(String url, Map<String, String> headers, int timeoutMillis, int retryCount) {
        CloseableHttpResponse response = null;
        String result = null;
        int statusCode = -1;
        Map<String, String> responseHeaders = new java.util.HashMap<>();

        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(getCurrentRequestConfig(timeoutMillis)); // 关键：用本次超时
        
            // 合并请求头，headers优先
            Map<String, String> mergedHeaders = new LinkedHashMap<>(defaultHeaders);
            if (headers != null && !headers.isEmpty()) {
                mergedHeaders.putAll(headers);
            }
            for (Map.Entry<String, String> entry : mergedHeaders.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
            
            response = httpClient.execute(httpGet);
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                boolean cookieSet = extractAndSetCookieFromJs(result, url);
                if (cookieSet && retryCount < MAX_COOKIE_RETRY) {
                    response.close();
                    return doGet(url, headers, timeoutMillis, retryCount + 1);
                }
            }
            for (Header header : response.getAllHeaders()) {
                responseHeaders.put(header.getName(), header.getValue());
            }
            return new Response(statusCode, result, responseHeaders);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send GET request: " + e.getMessage(), e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * 发送GET请求（使用默认请求头）
     * @param url 请求URL
     * @return 响应对象
     */
    public Response doGet(String url) {
        return doGet(url, null, 0);
    }

    /**
     * 发送GET请求
     * @param url 请求URL
     * @param headers 自定义请求头，如果为null则使用默认请求头
     * @return 响应对象
     */
    public Response doGet(String url, Map<String, String> headers) {
        return doGet(url, headers, 0);
    }

    /**
     * 发送POST请求（JSON格式）
     * @param url 请求URL
     * @param json 请求体（JSON格式）
     * @param headers 自定义请求头，如果为null则使用默认请求头
     * @param timeoutMillis 超时时间（毫秒），如果小于等于0则使用默认超时
     * @return 响应内容
     */
    public String doPostJson(String url, String json, Map<String, String> headers, int timeoutMillis) {
        return doPostJson(url, json, headers, timeoutMillis, 0);
    }

    private String doPostJson(String url, String json, Map<String, String> headers, int timeoutMillis, int retryCount) {
        CloseableHttpResponse response = null;
        try {
            // 创建POST请求
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求配置（包括代理和超时）
            RequestConfig requestConfig = getCurrentRequestConfig(timeoutMillis);
            httpPost.setConfig(requestConfig);
            
            // 清空所有头
            for (Header h : httpPost.getAllHeaders()) {
                httpPost.removeHeaders(h.getName());
            }
            // 设置默认请求头
            for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
            // 设置自定义请求头，会覆盖默认请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置请求体
            if (json != null) {
                StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                httpPost.setEntity(entity);
            }
            
            // 记录请求开始时间
            long startTime = System.currentTimeMillis();
            
            // 执行请求
            response = httpClient.execute(httpPost);
            
            // 计算请求耗时
            long endTime = System.currentTimeMillis();
            //System.out.println("请求耗时: " + (endTime - startTime) + "ms");
            
            // 输出响应头
            //System.out.println("\nResponse Headers:");
            for (Header header : response.getAllHeaders()) {
                //System.out.println(header.getName() + ": " + header.getValue());
            }
            
            HttpEntity entity = response.getEntity();
            String result = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            boolean cookieSet = extractAndSetCookieFromJs(result, url);
            if (cookieSet && retryCount < MAX_COOKIE_RETRY) {
                response.close();
                return doPostJson(url, json, headers, timeoutMillis, retryCount + 1);
            }
            return result;
        } catch (Exception e) {
            //System.err.println("请求异常: " + e.getMessage() + 
            //    (e instanceof java.net.SocketTimeoutException ? " (超时)" : ""));
            throw new RuntimeException("Failed to send POST request: " + e.getMessage(), e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送POST请求（JSON格式，使用默认请求头和默认超时）
     * @param url 请求URL
     * @param json 请求体（JSON格式）
     * @return 响应内容
     */
    public String doPostJson(String url, String json) {
        return doPostJson(url, json, null, -1);
    }

    /**
     * 发送POST请求（JSON格式，使用默认超时）
     * @param url 请求URL
     * @param json 请求体（JSON格式）
     * @param headers 自定义请求头
     * @return 响应内容
     */
    public String doPostJson(String url, String json, Map<String, String> headers) {
        return doPostJson(url, json, headers, -1);
    }

    /**
     * 发送POST请求（表单格式）
     *
     * @param url    请求URL
     * @param params 请求参数
     * @return 响应内容
     */
    public String doPostForm(String url, Map<String, String> params) {
        return doPostForm(url, params, 0);
    }

    private String doPostForm(String url, Map<String, String> params, int retryCount) {
        CloseableHttpResponse response = null;
        String result = null;

        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(getCurrentRequestConfig());
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            // 清空所有头
            for (Header h : httpPost.getAllHeaders()) {
                httpPost.removeHeaders(h.getName());
            }
            // 设置默认请求头
            for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8));
            }

            response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                boolean cookieSet = extractAndSetCookieFromJs(result, url);
                if (cookieSet && retryCount < MAX_COOKIE_RETRY) {
                    response.close();
                    return doPostForm(url, params, retryCount + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 设置请求头
     *
     * @param name  请求头名称
     * @param value 请求头值
     */
    public void setHeader(String name, String value) {
        defaultHeaders.put(name, value);
    }
    
    /**
     * 获取请求头
     *
     * @return 请求头列表
     */
    public Map<String, String> getHeaders() {
        return new LinkedHashMap<>(defaultHeaders);
    }
    
    /**
     * 获取请求头Map
     *
     * @return 请求头Map
     */
    public Map<String, String> getHeadersMap() {
        return new LinkedHashMap<>(defaultHeaders);
    }

    /**
     * 清除所有请求头
     */
    public void clearHeaders() {
        defaultHeaders.clear();
    }

    /**
     * 关闭HttpClient实例
     */
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (connectionManager != null) {
            connectionManager.close();
        }
    }

    /**
     * 从响应内容中提取 document.cookie 并自动设置到 CookieStore
     * @param responseBody 响应内容
     * @param baseUrl      当前请求的 baseUrl
     * @return 是否成功提取并设置 Cookie
     */
    public boolean extractAndSetCookieFromJs(String responseBody, String baseUrl) {
        if (responseBody != null && responseBody.contains("document.cookie")) {
            Pattern pattern = Pattern.compile("document\\.cookie='(.*?)'");
            Matcher matcher = pattern.matcher(responseBody);
            if (matcher.find()) {
                String cookie = matcher.group(1);
                String[] parts = cookie.split("=", 2);
                if (parts.length == 2) {
                    String cookieValue = parts[1].split(";", 2)[0];
                    String domain = getDomainFromUrl(baseUrl);
                    String cleanDomain = domain.startsWith(".") ? domain.substring(1) : domain;
                    setCookie(parts[0], cookieValue, cleanDomain, "/");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从URL中提取域名
     * @param url URL字符串
     * @return 域名
     */
    private static String getDomainFromUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            return host != null ? host : "";
        } catch (Exception e) {
            return "";
        }
    }
} 