import { get, post, put, del } from '@/utils/request'
import type { Connection, ConnectionForm, ConnectionResponse, ConnectionQuery } from '../types'

const BASE_URL = '/api/mqtt/connections'

// 获取连接列表
export const getConnections = (params: ConnectionQuery): Promise<ConnectionResponse> => {
  return get('/mqtt/connections', params)
}

// 获取单个连接
export const getConnection = (id: string): Promise<Connection> => {
  return get(`/mqtt/connections/${id}`)
}

// 创建连接
export const createConnection = (data: ConnectionForm): Promise<Connection> => {
  return post('/mqtt/connections', data)
}

// 更新连接
export const updateConnection = (id: string, data: ConnectionForm): Promise<Connection> => {
  return put(`/mqtt/connections/${id}`, data)
}

// 删除连接
export const deleteConnection = (id: string): Promise<void> => {
  return del(`/mqtt/connections/${id}`)
}

// 连接MQTT
export const connectMQTT = (id: string): Promise<void> => {
  return post(`/mqtt/connections/${id}/connect`)
}

// 断开MQTT连接
export const disconnectMQTT = (id: string): Promise<void> => {
  return post(`/mqtt/connections/${id}/disconnect`)
}

// 测试连接
export const testConnection = (data: ConnectionForm): Promise<void> => {
  return post('/mqtt/connections/test', data)
} 