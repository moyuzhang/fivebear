import request from './request'
import type { IpInfo, PageResult, IpPoolStatus, IpPoolSettings } from '@/types/ipPool'

// 定义接口
export interface IpPoolInfo {
  status: {
    isRunning: boolean
    totalCount: number
    validCount: number
    averageSpeed: number
  }
  settings: {
    enabled: boolean
    threadPoolSize: number
    schedulerPoolSize: number
    initialDelay: number
    period: number
    expireTime: number
    listKey: string
    setKey: string
    maxValidateCount: number
    minAvailableRate: number
    validateInterval: number
    timeout: number
  }
}

/**
 * 获取IP池状态
 */
export const getIpPoolStatus = async (): Promise<IpPoolStatus> => {
  const response = await request.get('/api/ip-pool/status')
  return response.data
}

/**
 * 启动IP池
 */
export const startIpPool = async () => {
  const response = await request.post('/api/ip-pool/start')
  return response.data
}

/**
 * 停止IP池
 */
export const stopIpPool = async () => {
  const response = await request.post('/api/ip-pool/stop')
  return response.data
}

/**
 * 刷新IP池
 */
export const refreshIpPool = async () => {
  const response = await request.post('/api/ip-pool/refresh')
  return response.data
}

/**
 * 获取IP池设置
 */
export const getIpPoolSettings = async (): Promise<IpPoolSettings> => {
  const response = await request.get('/api/IpSettings')
  return response.data
}

/**
 * 保存IP池设置
 */
export const saveIpPoolSettings = async (settings: IpPoolSettings) => {
  const response = await request.put('/api/IpSettings', settings)
  return response.data
}

/**
 * 获取IP列表
 */
export const getIpList = async (params: {
  page: number
  pageSize: number
  validStatus?: boolean
  keyword?: string
}): Promise<PageResult<IpInfo>> => {
  const response = await request.get('/api/proxy/list', { params })
  return response.data
}

/**
 * 获取最快的IP
 */
export const getFastestIp = async (): Promise<IpInfo> => {
  const response = await request.get('/api/proxy/fastest')
  return response.data
}

/**
 * 添加代理IP
 */
export const addProxy = async (proxy: {
  ip: string
  port: number
  type: string
  source?: string
}) => {
  const response = await request.post('/api/proxy', proxy)
  return response.data
}

/**
 * 删除代理IP
 */
export const deleteProxy = async (proxy: {
  ip: string
  port: number
}) => {
  const response = await request.delete('/api/proxy', { data: proxy })
  return response.data
}

/**
 * 批量删除代理IP
 */
export const batchDeleteProxies = async (proxies: Array<{
  ip: string
  port: number
}>) => {
  const response = await request.delete('/api/proxy/batch', { data: { proxies } })
  return response.data
}

/**
 * 测试代理IP
 * @deprecated 请使用前端测试工具替代此方法
 */
export const testProxyDeprecated = async (proxy: {
  ip: string
  port: number
  type: string
}) => {
  const response = await request.post('/api/proxy/test', proxy)
  return response.data
}

/**
 * 快速测试代理IP (轻量级方法)
 * 此方法仅执行简单的连接检查，不执行完整验证
 */
export const quickTestProxy = async (testInfo: {
  ip: string
  port: number
  type: string
  testUrl?: string
  timeout?: number
}) => {
  return await request.post('/api/proxy/quick-test', testInfo)
}

/**
 * 获取IP池统计信息
 */
export const getIpPoolStats = async () => {
  const response = await request.get('/api/ip-pool/stats')
  return response.data
}

/**
 * 获取IP池详细信息
 */
export const getIpPoolDetailedInfo = async () => {
  const response = await request.get('/api/ip-pool/info')
  return response.data
}

/**
 * 获取IP池综合信息
 */
export const getIpPoolInfo = async (): Promise<IpPoolInfo> => {
  try {
    const response = await request.get('/api/ip-pool/info')
    return response.data
  } catch (error) {
    console.error('获取IP池信息失败', error)
    throw error
  }
}

/**
 * 批量删除多个代理IP
 */
export const deleteProxies = async (proxies: Array<{
  ip: string
  port: number
}>) => {
  return await request.delete('/api/proxy/batch', {
    data: { proxies }
  })
}

/**
 * 批量测试多个代理IP
 * @deprecated 请使用前端测试工具替代此方法
 */
export const batchTestProxies = async (proxies: Array<{
  ip: string
  port: number
  type: string
}>) => {
  return await request.post('/api/proxy/batch-test', { proxies })
}

export function getIpPoolList(params: any) {
  return request({
    url: '/api/ip-pool/list',
    method: 'get',
    params
  })
}

export function addIpPool(data: any) {
  return request({
    url: '/api/ip-pool/add',
    method: 'post',
    data
  })
}

export function updateIpPool(data: any) {
  return request({
    url: '/api/ip-pool/update',
    method: 'put',
    data
  })
}

export function deleteIpPool(id: string) {
  return request({
    url: `/api/ip-pool/delete/${id}`,
    method: 'delete'
  })
} 