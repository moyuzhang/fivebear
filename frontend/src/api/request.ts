import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosRequestHeaders } from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import type { ApiResponse, CustomAxiosInstance } from '../types/api'

// 创建 axios 实例
const request: CustomAxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 使用环境变量中的 baseURL
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 白名单路径，这些路径不需要添加 token
const whiteList = ['/api/login', '/api/register']

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    const url = config.url || ''
    
    // 如果不在白名单中，添加 token
    if (!whiteList.includes(url)) {
      if (!token) {
        // 如果需要token但没有token，直接跳转到登录页
        router.push('/login')
        return Promise.reject(new Error('请先登录'))
      }
      
      config.headers = config.headers || {} as AxiosRequestHeaders
      // 如果token不包含Bearer前缀，添加它
      config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }

    // 打印请求信息，方便调试
    console.log('Request config:', {
      url: config.url,
      method: config.method,
      baseURL: config.baseURL,
      headers: config.headers,
      data: config.data,
      withCredentials: config.withCredentials,
      fullPath: `${config.baseURL ?? ''}${config.url ?? ''}`
    })
    
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 打印响应信息，方便调试
    console.log('Response:', {
      status: response.status,
      data: response.data,
      headers: response.headers
    })

    // 直接返回整个响应，让组件自己处理数据结构
    return response;
  },
  error => {
    // 打印错误信息，方便调试
    console.error('Response Error:', error)
    
    if (error.response) {
      const { status, data } = error.response
      console.error('Error Response:', { status, data })

      // 尝试从响应中获取错误信息
      const errorMessage = data?.message || '未知错误'

      switch (status) {
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          ElMessage.error(errorMessage || '登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error(errorMessage || '没有权限访问')
          break
        case 404:
          ElMessage.error(errorMessage || '请求的资源不存在')
          break
        case 500:
          ElMessage.error(errorMessage || '服务器错误')
          break
        default:
          ElMessage.error(errorMessage || '请求失败')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('No response received:', error.request)
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 请求配置出错
      console.error('Request setup error:', error.message)
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request 