import request from '@/utils/request'

export interface SiteCreateRequest {
  url: string
  username: string
  password: string
  rebateRate?: number
  lotteryType?: number
  clientType?: number
  clientRole?: number
  multiplier?: number
}

export interface SiteDetail {
  uniqueKey: string
  username: string
  domain: string
  url: string
  status: string
  statusDescription: string
  rebateRate: number
  siteType: string
  lotteryType: string
  databaseId: number
}

export interface SitesSummary {
  totalCount: number
  statusCounts: { [key: string]: number }
  sites: SiteDetail[]
  userId: string
  timestamp: number
}

export interface BatchOperationRequest {
  uniqueKeys: string[]
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: number
}

// 获取用户的所有站点
export function getUserSites(): Promise<ApiResponse<SitesSummary>> {
  return request({
    url: '/api/platform/sites',
    method: 'get',
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 添加新站点
export function addSite(data: SiteCreateRequest): Promise<ApiResponse<any>> {
  return request({
    url: '/api/platform/sites',
    method: 'post',
    data,
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 登录站点
export function loginSite(uniqueKey: string): Promise<ApiResponse<any>> {
  return request({
    url: `/api/platform/sites/${uniqueKey}/login`,
    method: 'post',
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 登出站点
export function logoutSite(uniqueKey: string): Promise<ApiResponse<any>> {
  return request({
    url: `/api/platform/sites/${uniqueKey}/logout`,
    method: 'post',
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 删除站点
export function deleteSite(uniqueKey: string): Promise<ApiResponse<any>> {
  return request({
    url: `/api/platform/sites/${uniqueKey}`,
    method: 'delete',
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 获取单个站点详情
export function getSiteDetail(uniqueKey: string): Promise<ApiResponse<SiteDetail>> {
  return request({
    url: `/api/platform/sites/${uniqueKey}`,
    method: 'get',
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 批量登录
export function batchLogin(data: BatchOperationRequest): Promise<ApiResponse<any>> {
  return request({
    url: '/api/platform/sites/batch/login',
    method: 'post',
    data,
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// 检查站点唯一性
export function checkSiteUniqueness(url: string, username: string): Promise<ApiResponse<any>> {
  return request({
    url: '/api/platform/sites/check',
    method: 'get',
    params: { url, username },
    headers: {
      'User-ID': localStorage.getItem('userId') || '72'
    }
  })
}

// WebSocket连接类
export class PlatformWebSocket {
  private ws: WebSocket | null = null
  private url: string
  private token: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000
  private onMessageCallback?: (message: any) => void
  private onStatusChangeCallback?: (status: string) => void

  constructor(token?: string) {
    // 从localStorage获取JWT token
    this.token = token || this.getTokenFromLocalStorage()
    this.url = `ws://localhost:8080/platform/ws?token=${this.token}`
  }

  private getTokenFromLocalStorage(): string {
    // 从localStorage获取token
    return localStorage.getItem('fivebear-token') || ''
  }

  connect() {
    try {
      this.ws = new WebSocket(this.url)
      
      this.ws.onopen = () => {
        console.log('Platform WebSocket connected')
        this.reconnectAttempts = 0
        this.onStatusChangeCallback?.('connected')
      }
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('Platform WebSocket message:', message)
          this.onMessageCallback?.(message)
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error)
        }
      }
      
      this.ws.onclose = () => {
        console.log('Platform WebSocket disconnected')
        this.onStatusChangeCallback?.('disconnected')
        this.handleReconnect()
      }
      
      this.ws.onerror = (error) => {
        console.error('Platform WebSocket error:', error)
        this.onStatusChangeCallback?.('error')
      }
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error)
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  sendMessage(message: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    }
  }

  onMessage(callback: (message: any) => void) {
    this.onMessageCallback = callback
  }

  onStatusChange(callback: (status: string) => void) {
    this.onStatusChangeCallback = callback
  }

  private handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    }
  }

  // 发送心跳
  ping() {
    this.sendMessage({ action: 'PING' })
  }

  // 获取站点摘要
  getSitesSummary() {
    this.sendMessage({ action: 'GET_SITES_SUMMARY' })
  }
} 