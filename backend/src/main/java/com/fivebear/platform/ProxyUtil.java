package com.fivebear.platform;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

public class ProxyUtil {
    private static final String API_URL = "http://v2.api.juliangip.com/company/postpay/getips";
    private static final String TRADE_NO = "6724154819349193";
    private static final String SIGN = "1e32692327fbba52b2fe680c080da3e2";
    private static final int MAX_RETRY_COUNT = 5; // 最大重试次数
    private static final int TIMEOUT = 5000; // 超时时间5秒

    private static Proxy currentProxy;
    private static long lastUpdateTime;
    private static final long PROXY_UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(5); // 5分钟更新一次

    /**
     * 获取可用的代理IP
     * 
     * @return 可用的Proxy对象，如果获取失败返回null
     */
    public static Proxy getValidProxy() {
        try {
            // 构建API URL
            String url = String.format("%s?auto_white=1&num=1&pt=1&result_type=text&split=1&trade_no=%s&sign=%s",
                    API_URL, TRADE_NO, SIGN);

            // 获取代理IP
            String response = new SiteContent("").get(url).trim();
            if (response != null && !response.isEmpty()) {
                String[] parts = response.split(":");
                if (parts.length == 2) {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    System.out.println("获取到代理IP: " + host + ":" + port);
                    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                }
            }
            System.out.println("获取代理IP失败，API响应: " + response);
        } catch (Exception e) {
            System.err.println("获取代理IP出错: " + e.getMessage());
        }
        return null;
    }

    /**
     * 验证代理IP是否可用
     * 
     * @param proxyStr 代理IP地址，格式为 "ip:port"
     * @return 如果代理可用返回true，否则返回false
     */
    private static boolean isProxyValid(String proxyStr) {
        try {
            String[] parts = proxyStr.split(":");
            if (parts.length != 2) {
                return false;
            }

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            // 创建代理对象
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

            // 测试连接百度
            URL testUrl = new URL("https://www.baidu.com");
            URLConnection testConnection = testUrl.openConnection(proxy);
            testConnection.setConnectTimeout(TIMEOUT);
            testConnection.setReadTimeout(TIMEOUT);
            testConnection.connect();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前代理，如果过期则自动更新
     * 
     * @return Proxy对象，如果获取失败返回null
     */
    public static Proxy getCurrentProxy() {
        if (currentProxy == null ||
                System.currentTimeMillis() - lastUpdateTime > PROXY_UPDATE_INTERVAL) {
            return getValidProxy();
        }
        return currentProxy;
    }

    /**
     * 获取代理IP地址
     * 
     * @return IP地址，如果获取失败返回null
     */
    public static String getProxyHost() {
        Proxy proxy = getCurrentProxy();
        if (proxy != null && proxy.address() instanceof InetSocketAddress) {
            return ((InetSocketAddress) proxy.address()).getHostString();
        }
        return null;
    }

    /**
     * 获取代理端口
     * 
     * @return 端口号，如果获取失败返回-1
     */
    public static int getProxyPort() {
        Proxy proxy = getCurrentProxy();
        if (proxy != null && proxy.address() instanceof InetSocketAddress) {
            return ((InetSocketAddress) proxy.address()).getPort();
        }
        return -1;
    }

    /**
     * 获取完整的代理地址（IP:PORT格式）
     * 
     * @return 代理地址，如果获取失败返回null
     */
    public static String getProxyAddress() {
        String host = getProxyHost();
        int port = getProxyPort();
        if (host != null && port != -1) {
            return host + ":" + port;
        }
        return null;
    }

    /**
     * 强制更新代理
     * 
     * @return 新的Proxy对象，如果获取失败返回null
     */
    public static Proxy forceUpdateProxy() {
        currentProxy = null;
        return getValidProxy();
    }
}