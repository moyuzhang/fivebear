import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 打印请求信息
    console.log('请求配置：', {
      url: config.url,
      method: config.method,
      data: config.data,
      headers: config.headers
    })
    
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 打印响应信息
    console.log('响应数据：', res)
    
    // 如果响应成功，直接返回数据
    if (res.success) {
      return res.data
    }
    
    // 处理业务错误
    const errorMsg = res.message || res.error || '请求失败'
    if (res.code === 400) {
      ElMessage.error('用户名或密码错误')
    } else {
      ElMessage.error(errorMsg)
    }
    return Promise.reject(new Error(errorMsg))
  },
  error => {
    console.error('响应错误：', error)
    console.error('错误详情：', error.response?.data)
    
    // 处理 401 未授权错误
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
      ElMessage.error('用户名或密码错误')
    } else {
      const errorMessage = error.response?.data?.message || error.response?.data?.error || error.message || '请求失败'
      ElMessage.error(errorMessage)
    }
    
    return Promise.reject(error)
  }
)

export default service 