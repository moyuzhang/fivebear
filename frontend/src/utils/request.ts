import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { Result } from '@/types'

// 创建axios实例
const service: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
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
        const token = localStorage.getItem('fivebear-token')
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
    (response: AxiosResponse<any>) => {
        console.log('✅ Response:', response.status, response.config.url)

        const { data } = response
        
        // 统一的Result格式: {code: 200, message: "success", data: {...}}
        if (data && typeof data === 'object' && 'code' in data) {
            const code = data.code
            const message = data.message || '未知错误'
            
            if (code === 200) {
                console.log('✅ Success response:', message)
                return response
            } else {
                console.error('❌ Business error:', code, message)
                ElMessage.error(message)
                return Promise.reject(new Error(message))
            }
        } else {
            // 兼容直接返回数据的情况
            console.log('✅ Direct data response')
            return response
        }
    },
    (error: AxiosError) => {
        console.error('❌ HTTP Error:', error.response?.status, error.config?.url, error.message)
        
        let message = '网络错误'
        
        if (error.response) {
            const status = error.response.status
            const data = error.response.data as any
            
            switch (status) {
                case 401:
                    message = '未授权，请重新登录'
                    // 清除token
                    localStorage.removeItem('fivebear-token')
                    localStorage.removeItem('fivebear-user-info')
                    // 跳转登录页
                    window.location.href = '/login'
                    break
                case 403:
                    message = '拒绝访问'
                    break
                case 404:
                    message = '请求地址不存在'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                default:
                    message = data?.message || `HTTP ${status} 错误`
            }
        } else if (error.request) {
            message = '网络连接失败，请检查网络'
        }
        
        ElMessage.error(message)
        return Promise.reject(error)
    }
)

// 文件上传请求
export const uploadRequest = (config: AxiosRequestConfig) => {
    const uploadService = axios.create({
        baseURL: import.meta.env.VITE_API_BASE_URL || '',
        timeout: 60000, // 文件上传延长超时时间
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })

    // 添加上传进度拦截器
    uploadService.interceptors.request.use((config) => {
        const token = localStorage.getItem('fivebear-token')
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