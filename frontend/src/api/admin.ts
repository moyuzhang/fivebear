import { request } from '@/utils/request'
import type { Result, PageParams, PageResult } from '@/types'

// 管理员接口类型定义
export interface AdminUser {
  id?: number
  username: string
  realName: string
  email: string
  phone: string
  avatar?: string
  status: number
  roles: string[]
  permissions: string[]
  createTime?: string
  updateTime?: string
}

export interface AdminCreateParams {
  username: string
  password: string
  realName: string
  email: string
  phone: string
  roles: string[]
}

export interface AdminUpdateParams {
  id: number
  realName: string
  email: string
  phone: string
  roles: string[]
}

// 管理员API接口
export const adminApi = {
  // 获取管理员列表
  getAdminList(params: PageParams & { status?: number }): Promise<Result<PageResult<AdminUser>>> {
    return request.get('/admin/list', params)
  },

  // 获取管理员详情
  getAdminById(id: number): Promise<Result<AdminUser>> {
    return request.get(`/admin/${id}`)
  },

  // 创建管理员
  createAdmin(data: AdminCreateParams): Promise<Result<AdminUser>> {
    return request.post('/admin', data)
  },

  // 更新管理员
  updateAdmin(id: number, data: AdminUpdateParams): Promise<Result<AdminUser>> {
    return request.put(`/admin/${id}`, data)
  },

  // 删除管理员
  deleteAdmin(id: number): Promise<Result<void>> {
    return request.delete(`/admin/${id}`)
  },

  // 重置密码
  resetPassword(id: number, newPassword: string): Promise<Result<void>> {
    return request.post(`/admin/${id}/reset-password`, { newPassword })
  },

  // 启用/禁用管理员
  toggleStatus(id: number): Promise<Result<void>> {
    return request.post(`/admin/${id}/toggle-status`)
  },

  // 获取管理员权限
  getAdminPermissions(id: number): Promise<Result<string[]>> {
    return request.get(`/admin/${id}/permissions`)
  },

  // 分配权限
  assignPermissions(id: number, permissions: string[]): Promise<Result<void>> {
    return request.post(`/admin/${id}/assign-permissions`, { permissions })
  },

  // 批量删除管理员
  batchDelete(ids: number[]): Promise<Result<void>> {
    return request.post('/admin/batch-delete', { ids })
  },

  // 导出管理员数据
  exportAdmins(params: any): Promise<Result<string>> {
    return request.post('/admin/export', params)
  }
}

// 获取系统状态
export const getSystemStatus = () => {
  return request.get('/api/admin/system-status')
}

// 获取系统统计信息
export const getStatistics = () => {
  return request.get('/api/admin/statistics')
}

// 获取最近活动日志
export const getRecentActivities = () => {
  return request.get('/api/admin/recent-activities')
}

// 系统状态接口类型定义
export interface SystemStatus {
  systemInfo: {
    javaVersion: string
    osName: string
    osArch: string
    processors: number
    uptime: number
    startTime: string
  }
  memory: {
    used: number
    max: number
    total: number
    usedMB: number
    maxMB: number
    totalMB: number
    usagePercent: number
  }
  database: {
    connected: boolean
    productName?: string
    productVersion?: string
    url?: string
    error?: string
  }
  redis: {
    connected: boolean
    error?: string
  }
  timestamp: number
}

// 统计信息接口类型定义
export interface Statistics {
  users: {
    total: number
  }
  roles: {
    total: number
  }
  sites: {
    total: number
    note?: string
  }
  timestamp: number
}

// 活动日志接口类型定义
export interface Activity {
  id: number
  time: string
  type: string
  description: string
  username: string
} 