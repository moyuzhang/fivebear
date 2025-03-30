import type { AxiosInstance, AxiosRequestConfig } from 'axios'

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface RequestInstance {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
}

declare const request: RequestInstance

export default request 