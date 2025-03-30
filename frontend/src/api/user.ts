import request from '@/api/request'
import type { ApiResponse, LogoutResponse } from '../types/api'

export const userApi = {
  // 退出登录
  logout: () => {
    return request.post<LogoutResponse>('/logout')
  }
} 