import { get, post, put, del } from '@/utils/request'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/api'

// 用户相关接口
export const userApi = {
  // 登录
  login(data: LoginRequest): Promise<LoginResponse> {
    return post('/user/login', data)
  },

  // 获取用户信息
  getUserInfo(): Promise<UserInfo> {
    return get('/user/info')
  },

  // 更新用户信息
  updateUserInfo(data: Partial<UserInfo>): Promise<UserInfo> {
    return put('/user/info', data)
  },

  // 修改密码
  changePassword(oldPassword: string, newPassword: string): Promise<void> {
    return put('/user/password', { oldPassword, newPassword })
  }
}

// 系统相关接口
export const systemApi = {
  // 获取系统状态
  getSystemStatus() {
    return get('/system/status')
  },

  // 获取系统配置
  getSystemConfig() {
    return get('/system/config')
  },

  // 更新系统配置
  updateSystemConfig(data: any) {
    return put('/system/config', data)
  }
}

// 日志相关接口
export const logApi = {
  // 获取操作日志
  getOperationLogs(params: any) {
    return get('/log/operation', params)
  },

  // 获取登录日志
  getLoginLogs(params: any) {
    return get('/log/login', params)
  }
}

// 导出所有API
export default {
  user: userApi,
  system: systemApi,
  log: logApi
} 