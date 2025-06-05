import { request } from '@/utils/request'
import type { LoginParams, LoginResult, UserInfo } from '@/types'

/**
 * 认证相关API
 */
export const authApi = {
  // 用户登录
  login: (data: LoginParams) => {
    return request.post<LoginResult>('/auth/login', data)
  },

  // 用户登出
  logout: () => {
    return request.post('/auth/logout')
  },

  // 获取用户信息
  getUserInfo: () => {
    return request.get<UserInfo>('/auth/user-info')
  },

  // 验证Token
  validateToken: () => {
    return request.post<boolean>('/auth/validate')
  },

  // 检查账户锁定状态
  checkLockStatus: (username: string) => {
    return request.get<{
      isLocked: boolean
      remainingTime?: number
      remainingMinutes?: number
      remainingAttempts?: number
    }>('/auth/security/lock-status', {
      params: { username }
    })
  }
} 