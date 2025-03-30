import type { Connection, ConnectionForm } from '../types'

// 生成唯一的客户端ID
export const generateClientId = (): string => {
  return `client_${Math.random().toString(36).substr(2, 9)}`
}

// 验证MQTT连接参数
export const validateConnection = (form: ConnectionForm): string[] => {
  const errors: string[] = []
  
  if (!form.host) {
    errors.push('主机地址不能为空')
  }
  
  if (!form.port || form.port < 1 || form.port > 65535) {
    errors.push('端口号必须在1-65535之间')
  }
  
  if (!form.clientId) {
    errors.push('客户端ID不能为空')
  }
  
  return errors
}

// 格式化连接状态
export const formatConnectionStatus = (status: Connection['status']): string => {
  return status === 'connected' ? '已连接' : '未连接'
}

// 获取连接状态标签类型
export const getStatusTagType = (status: Connection['status']): 'success' | 'danger' => {
  return status === 'connected' ? 'success' : 'danger'
}

// 检查连接是否有效
export const isConnectionValid = (connection: Connection): boolean => {
  return Boolean(
    connection.id &&
    connection.name &&
    connection.host &&
    connection.port &&
    connection.clientId
  )
}

// 格式化连接信息
export const formatConnectionInfo = (connection: Connection): string => {
  return `${connection.name} (${connection.host}:${connection.port})`
} 