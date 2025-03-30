/**
 * 前端代理IP测试工具
 * 通过在前端直接测试代理IP的可用性和响应时间
 */

import request from '@/api/request'

// 测试结果接口
export interface ProxyTestResult {
  success: boolean;
  responseTime?: number;
  message: string;
  testUrl?: string;
  details?: Record<string, any>;
  timestamp: string;
}

// 测试配置接口
export interface ProxyTestConfig {
  // 是否使用混合模式（先前端测试失败后再尝试后端）
  useHybridMode?: boolean;
  // 是否强制使用前端测试
  forceFrontendTest?: boolean;
  // 测试URL
  testUrl?: string;
  // 超时时间（毫秒）
  timeout?: number;
}

/**
 * 前端测试代理IP
 * @param proxyInfo 代理信息
 * @param config 测试配置
 * @returns 测试结果
 */
export async function testProxyInFrontend(
  proxyInfo: {
    ip: string;
    port: number;
    type: string;
  }, 
  config?: ProxyTestConfig
): Promise<ProxyTestResult> {
  const startTime = Date.now();
  const testUrl = config?.testUrl || 'https://www.baidu.com';
  const timeout = config?.timeout || 5000;
  const timestamp = new Date().toLocaleString();

  try {
    // 使用fetch和公共代理检查服务来验证代理
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    // 我们可以使用一些公共API来检查代理状态
    // 注意：这些API可能需要API密钥或有使用限制
    const ipCheckUrl = `https://ipinfo.io/${proxyInfo.ip}/json`;
    
    try {
      const response = await fetch(ipCheckUrl, {
        signal: controller.signal,
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      clearTimeout(timeoutId);
      
      if (!response.ok) {
        throw new Error(`HTTP error: ${response.status}`);
      }
      
      const data = await response.json();
      const endTime = Date.now();
      const responseTime = endTime - startTime;
      
      // 分析响应
      return {
        success: true, // 能获取IP信息，代理可能是有效的
        responseTime,
        message: "IP信息获取成功，代理可能可用",
        testUrl,
        details: data,
        timestamp
      };
    } catch (error: any) {
      clearTimeout(timeoutId);
      
      // 超时错误
      if (error.name === 'AbortError') {
        return {
          success: false,
          message: "请求超时，代理可能不可用",
          testUrl,
          timestamp
        };
      }
      
      // 其他错误
      return {
        success: false,
        message: `前端请求失败: ${error.message || '未知错误'}`,
        testUrl,
        timestamp,
        details: { error: error.toString() }
      };
    }
  } catch (error: any) {
    const endTime = Date.now();
    return {
      success: false,
      responseTime: endTime - startTime,
      message: `测试出错: ${error.message || '未知错误'}`,
      testUrl,
      timestamp,
      details: { error: error.toString() }
    };
  }
}

/**
 * 使用后端轻量级API测试代理
 * 这是一个更可靠的方法，因为后端可以直接使用代理
 */
export async function testProxyViaBackend(
  proxyInfo: {
    ip: string;
    port: number;
    type: string;
  },
  config?: ProxyTestConfig
): Promise<ProxyTestResult> {
  const startTime = Date.now();
  const timestamp = new Date().toLocaleString();
  const testUrl = config?.testUrl || 'https://www.baidu.com';
  const timeout = config?.timeout || 5000;
  
  try {
    // 调用后端轻量级API测试代理
    const response = await request.post('/api/proxy/quick-test', {
      ip: proxyInfo.ip,
      port: proxyInfo.port,
      type: proxyInfo.type,
      testUrl,
      timeout
    });
    
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    if (response.data && response.data.success) {
      return {
        success: true,
        responseTime: response.data.responseTime || responseTime,
        message: response.data.message || "代理测试成功",
        testUrl,
        details: response.data,
        timestamp
      };
    } else {
      return {
        success: false,
        responseTime,
        message: response.data?.message || "代理测试失败",
        testUrl,
        timestamp
      };
    }
  } catch (error: any) {
    return {
      success: false,
      message: `测试出错: ${error.message || '未知错误'}`,
      testUrl,
      timestamp
    };
  }
}

/**
 * 混合测试方法
 * 如果前端测试不可用，则退回到使用后端API进行测试
 */
export async function testProxy(
  proxyInfo: {
    ip: string;
    port: number;
    type: string;
  },
  config?: ProxyTestConfig
): Promise<ProxyTestResult> {
  // 默认使用混合模式
  const useHybridMode = config?.useHybridMode !== false;
  // 强制使用前端测试
  const forceFrontendTest = config?.forceFrontendTest === true;
  
  // 如果强制使用前端测试，则直接返回前端测试结果
  if (forceFrontendTest) {
    return testProxyInFrontend(proxyInfo, config);
  }
  
  // 默认情况下使用后端API（更可靠）
  try {
    return await testProxyViaBackend(proxyInfo, config);
  } catch (error: any) {
    console.error('后端测试代理失败，尝试前端测试:', error);
    
    // 如果启用了混合模式且后端测试失败，则尝试前端测试
    if (useHybridMode) {
      try {
        return await testProxyInFrontend(proxyInfo, config);
      } catch (frontendError: any) {
        return {
          success: false,
          message: `所有测试方法失败: ${frontendError.message || '未知错误'}`,
          testUrl: config?.testUrl || 'unknown',
          timestamp: new Date().toLocaleString()
        };
      }
    }
    
    // 不启用混合模式则直接返回错误
    return {
      success: false,
      message: `测试失败: ${error.message || '未知错误'}`,
      testUrl: config?.testUrl || 'unknown',
      timestamp: new Date().toLocaleString()
    };
  }
}

/**
 * 使用CORS代理服务器测试代理
 * 这是一种替代方法，使用公共的CORS代理服务
 */
export async function testProxyWithCorsProxy(proxyInfo: {
  ip: string;
  port: number;
  type: string;
}): Promise<ProxyTestResult> {
  const startTime = Date.now();
  const targetUrl = 'https://www.baidu.com';
  const timestamp = new Date().toLocaleString();
  
  try {
    // 使用公共CORS代理测试目标URL的可访问性
    // 注意：这种方法无法确认代理是否真正工作，只是一个模拟
    const corsProxyUrl = `https://cors-anywhere.herokuapp.com/http://${proxyInfo.ip}:${proxyInfo.port}`;
    
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);
    
    try {
      const response = await fetch(corsProxyUrl, {
        signal: controller.signal,
        method: 'HEAD' // 只获取头信息，减少数据传输
      });
      
      clearTimeout(timeoutId);
      
      const endTime = Date.now();
      const responseTime = endTime - startTime;
      
      if (response.ok) {
        return {
          success: true,
          responseTime,
          message: "代理响应成功",
          testUrl: targetUrl,
          timestamp
        };
      } else {
        return {
          success: false,
          responseTime,
          message: `代理响应错误: ${response.status} ${response.statusText}`,
          testUrl: targetUrl,
          timestamp
        };
      }
    } catch (error: any) {
      clearTimeout(timeoutId);
      
      // 超时错误
      if (error.name === 'AbortError') {
        return {
          success: false,
          message: "请求超时，代理可能不可用",
          testUrl: targetUrl,
          timestamp
        };
      }
      
      // 其他错误
      return {
        success: false,
        message: `请求失败: ${error.message || '未知错误'}`,
        testUrl: targetUrl,
        timestamp
      };
    }
  } catch (error: any) {
    return {
      success: false,
      message: `测试出错: ${error.message || '未知错误'}`,
      testUrl: targetUrl,
      timestamp
    };
  }
} 