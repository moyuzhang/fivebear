import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import { useUserStore } from '@/stores/user'
import type { Result } from '@/types'

// 创建axios实例
const service: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        console.log('🚀 Request:', config.method?.toUpperCase(), config.url)

        // 添加token
        const token = Cookies.get('token')
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`
        }

        // 添加请求时间戳，防止缓存
        if (config.method === 'get') {
            config.params = {
                _t: Date.now(),
                ...config.params
            }
        }

        // 请求体加密（如果需要）
        if (config.data && typeof config.data === 'object') {
            // 这里可以添加数据加密逻辑
            console.log('📤 Request Data:', config.data)
        }

        return config
    },
    (error: AxiosError) => {
        console.error('❌ Request Error:', error)
        ElMessage.error('请求配置错误')
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse<Result>) => {
        console.log('✅ Response:', response.status, response.config.url)

        const { data } = response
        const { code, message } = data

        // 根据后端统一响应格式处理
        if (code === 200) {
            // 请求成功，直接返回原始response，但修改data为业务数据
                console.log('📥 Response Data:', data.data)
            response.data = data
                return response
        } else {
            // 业务错误处理
            console.error('❌ Business Error:', code, message)

            switch (code) {
            case 401: {
                // 未授权，清除token并跳转登录
                    console.log('🔒 Token已过期或无效')
                ElMessage.error(message || '登录已过期，请重新登录')
                const userStore = useUserStore()
                    
                    // 异步登出，不等待完成
                    userStore.logout(true).catch(console.error)
                    
                    // 保存当前页面地址作为重定向地址
                    const currentPath = window.location.pathname + window.location.search
                    if (currentPath !== '/login') {
                        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
                    } else {
                window.location.href = '/login'
                    }
                return Promise.reject(new Error(message || '未授权'))
            }

            case 403:
                // 无权限
                ElMessage.error(message || '无权限访问')
                return Promise.reject(new Error(message || '无权限'))

            case 404:
                // 资源不存在
                ElMessage.error(message || '请求的资源不存在')
                return Promise.reject(new Error(message || '资源不存在'))

            case 500:
                    // 服务器错误 - 检查是否是登录相关的特殊错误
                    if (response.config?.url?.includes('/auth/login')) {
                        // 登录接口的错误，显示特殊样式的错误消息
                        if (message?.includes('锁定')) {
                            ElMessage({
                                message: message,
                                type: 'warning',
                                duration: 5000
                            })
                        } else if (message?.includes('还可尝试')) {
                            ElMessage({
                                message: message,
                                type: 'error',
                                duration: 4000
                            })
                        } else if (message?.includes('其他地方登录')) {
                            ElMessage({
                                message: message,
                                type: 'warning',
                                duration: 5000
                            })
                        } else {
                            ElMessage.error(message || '登录失败')
                        }
                    } else {
                ElMessage.error(message || '服务器内部错误')
                    }
                return Promise.reject(new Error(message || '服务器错误'))

            default:
                    // 其他业务错误 - 检查是否是登录相关的错误
                    if (response.config?.url?.includes('/auth/login')) {
                        // 登录接口的错误，不显示通用错误消息，由上面的特殊处理负责
                        // 这样避免重复显示
                    } else {
                ElMessage.error(message || '请求失败')
                    }
                return Promise.reject(new Error(message || '请求失败'))
            }
        }
    },
    (error: AxiosError) => {
        console.error('❌ Response Error:', error)

        // 网络错误处理
        if (!error.response) {
            ElMessage.error('网络连接失败，请检查网络设置')
            return Promise.reject(error)
        }

        const { status, statusText } = error.response

        switch (status) {
            case 400:
                ElMessage.error('请求参数错误')
                break
            case 401: {
                console.log('🔒 网络请求401 - Token已过期或无效')
                ElMessage.error('登录已过期，请重新登录')
                // 清除用户信息并跳转登录页
                const userStore = useUserStore()
                userStore.logout(true).catch(console.error)
                
                // 保存当前页面地址作为重定向地址
                const currentPath = window.location.pathname + window.location.search
                if (currentPath !== '/login') {
                    window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
                } else {
                window.location.href = '/login'
                }
                break
            }
            case 403:
                ElMessage.error('无权限访问该资源')
                break
            case 404:
                ElMessage.error('请求的资源不存在')
                break
            case 408:
                ElMessage.error('请求超时')
                break
            case 500:
                ElMessage.error('服务器内部错误')
                break
            case 502:
                ElMessage.error('网关错误')
                break
            case 503:
                ElMessage.error('服务不可用')
                break
            case 504:
                ElMessage.error('网关超时')
                break
            default:
                ElMessage.error(statusText || '请求失败')
        }

        return Promise.reject(error)
    }
)

// 文件上传请求
export const uploadRequest = (config: AxiosRequestConfig) => {
    const uploadService = axios.create({
        baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
        timeout: 60000, // 文件上传延长超时时间
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })

    // 添加上传进度拦截器
    uploadService.interceptors.request.use((config) => {
        const token = Cookies.get('token')
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    })

    return uploadService(config)
}

// 导出请求实例
export default service

// 封装常用请求方法
export const request = {
    get<T = any>(url: string, config?: any): Promise<Result<T>> {
        return service.get(url, config).then(response => response.data)
    },

    post<T = any>(url: string, data?: object): Promise<Result<T>> {
        return service.post(url, data).then(response => response.data)
    },

    put<T = any>(url: string, data?: object): Promise<Result<T>> {
        return service.put(url, data).then(response => response.data)
    },

    delete<T = any>(url: string, config?: any): Promise<Result<T>> {
        return service.delete(url, config).then(response => response.data)
    },

    upload(url: string, data: FormData, onUploadProgress?: (progressEvent: any) => void): Promise<Result> {
        return uploadRequest({
            url,
            method: 'post',
            data,
            onUploadProgress
        }).then(response => response.data)
    }
} 