import SockJS from 'sockjs-client'
import Cookies from 'js-cookie'

export interface WebSocketMessage {
  type: string
  message: string
  timestamp: string
  data?: any
}

export interface MessageHandler {
  (message: WebSocketMessage): void
}

/**
 * 统一的WebSocket客户端
 * 整合登录通知和站点管理功能
 */
export class UnifiedWebSocket {
  private socket: any = null
  private url: string
  private token: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000
  private heartbeatInterval: NodeJS.Timeout | null = null
  
  // 回调函数
  private onConnectedCallback?: () => void
  private onDisconnectedCallback?: () => void
  private onErrorCallback?: (error: any) => void
  private messageHandlers: Map<string, MessageHandler[]> = new Map()

  constructor(token?: string) {
    this.token = token || this.getTokenFromCookie()
    // SockJS需要添加token作为查询参数
    this.url = `http://localhost:8080/ws?token=${encodeURIComponent(this.token)}`
  }

  /**
   * 建立连接
   */
  connect() {
    try {
      console.log('正在建立统一WebSocket连接...')
      
      // 使用SockJS连接
      this.socket = new SockJS(this.url)

      // 连接打开
      this.socket.onopen = () => {
        console.log('✅ 统一WebSocket连接已建立')
        this.reconnectAttempts = 0
        this.startHeartbeat()
        this.onConnectedCallback?.()
      }

      // 接收消息
      this.socket.onmessage = (event: any) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data)
          console.log('📨 收到WebSocket消息:', message)
          this.handleMessage(message)
        } catch (error) {
          console.error('❌ 解析WebSocket消息失败:', error)
        }
      }

      // 连接关闭
      this.socket.onclose = () => {
        console.log('🔌 统一WebSocket连接已关闭')
        this.stopHeartbeat()
        this.onDisconnectedCallback?.()
        this.handleReconnect()
      }

      // 连接错误
      this.socket.onerror = (error: any) => {
        console.error('❌ 统一WebSocket连接错误:', error)
        this.onErrorCallback?.(error)
      }

    } catch (error) {
      console.error('❌ 创建WebSocket连接失败:', error)
      this.onErrorCallback?.(error)
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.stopHeartbeat()
  }

  /**
   * 发送消息
   */
  sendMessage(message: any) {
    if (this.socket && this.socket.readyState === SockJS.OPEN) {
      this.socket.send(JSON.stringify(message))
    } else {
      console.warn('⚠️ WebSocket未连接，无法发送消息')
    }
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(message: WebSocketMessage) {
    const { type } = message

    // 处理特殊消息类型
    switch (type) {
      case 'FORCE_LOGOUT':
        this.handleForceLogout(message)
        break
      case 'SYSTEM_INFO':
        console.log('ℹ️ 系统信息:', message.message, message.data)
        break
      case 'PONG':
        // 心跳响应，无需特殊处理
        break
    }

    // 调用注册的消息处理器
    const typeHandlers = this.messageHandlers.get(type) || []
    const globalHandlers = this.messageHandlers.get('*') || []
    
    const allHandlers = [...typeHandlers, ...globalHandlers]
    allHandlers.forEach(handler => {
      try {
        handler(message)
      } catch (error) {
        console.error('❌ 消息处理器执行失败:', error)
      }
    })
  }

  /**
   * 处理强制登出
   */
  private handleForceLogout(message: WebSocketMessage) {
    console.warn('🚫 收到强制登出通知:', message.message)
    
    // 触发强制登出事件
    const logoutHandlers = this.messageHandlers.get('FORCE_LOGOUT') || []
    logoutHandlers.forEach(handler => handler(message))
    
    // 触发全局的登出处理
    window.dispatchEvent(new CustomEvent('force-logout', { 
      detail: message 
    }))
  }

  /**
   * 开始心跳
   */
  private startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      this.sendMessage({ action: 'PING' })
    }, 30000) // 30秒心跳
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  /**
   * 处理重连
   */
  private handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`🔄 尝试重连... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    } else {
      console.error('❌ 重连失败，已达到最大尝试次数')
    }
  }

  /**
   * 从Cookie获取JWT token
   */
  private getTokenFromCookie(): string {
    return Cookies.get('token') || ''
  }

  // ==================== 事件监听器 ====================

  /**
   * 监听连接状态
   */
  onConnected(callback: () => void) {
    this.onConnectedCallback = callback
  }

  onDisconnected(callback: () => void) {
    this.onDisconnectedCallback = callback
  }

  onError(callback: (error: any) => void) {
    this.onErrorCallback = callback
  }

  /**
   * 注册消息处理器
   */
  onMessage(type: string, handler: MessageHandler) {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, [])
    }
    this.messageHandlers.get(type)!.push(handler)
  }

  /**
   * 移除消息处理器
   */
  offMessage(type: string, handler: MessageHandler) {
    const handlers = this.messageHandlers.get(type)
    if (handlers) {
      const index = handlers.indexOf(handler)
      if (index > -1) {
        handlers.splice(index, 1)
      }
    }
  }

  // ==================== 登录通知功能 ====================

  /**
   * 监听强制登出消息
   */
  onForceLogout(handler: MessageHandler) {
    this.onMessage('FORCE_LOGOUT', handler)
  }

  // ==================== 站点管理功能 ====================

  /**
   * 获取站点摘要
   */
  getSitesSummary() {
    this.sendMessage({ 
      type: 'platform',
      action: 'GET_SITES_SUMMARY' 
    })
  }

  /**
   * 监听站点状态变化
   */
  onSiteStatusChange(handler: MessageHandler) {
    this.onMessage('STATUS_CHANGE', handler)
  }

  /**
   * 监听站点摘要更新
   */
  onSitesSummary(handler: MessageHandler) {
    this.onMessage('SITES_SUMMARY', handler)
  }

  /**
   * 监听返点率变化
   */
  onRebateRateChange(handler: MessageHandler) {
    this.onMessage('REBATE_RATE_CHANGE', handler)
  }

  /**
   * 监听所有站点相关消息
   */
  onPlatformMessage(handler: MessageHandler) {
    this.onMessage('*', (message) => {
      // 只处理站点管理相关的消息
      const platformTypes = [
        'STATUS_CHANGE', 'SITES_SUMMARY', 'REBATE_RATE_CHANGE',
        'ERROR', 'INFO', 'WARNING', 'SUCCESS'
      ]
      if (platformTypes.includes(message.type)) {
        handler(message)
      }
    })
  }
}

// 创建全局单例
let globalWebSocket: UnifiedWebSocket | null = null

/**
 * 获取全局WebSocket实例
 */
export function getGlobalWebSocket(): UnifiedWebSocket {
  if (!globalWebSocket) {
    globalWebSocket = new UnifiedWebSocket()
  }
  return globalWebSocket
}

/**
 * 初始化全局WebSocket连接
 */
export function initGlobalWebSocket(token?: string): UnifiedWebSocket {
  if (globalWebSocket) {
    globalWebSocket.disconnect()
  }
  globalWebSocket = new UnifiedWebSocket(token)
  globalWebSocket.connect()
  return globalWebSocket
}

/**
 * 销毁全局WebSocket连接
 */
export function destroyGlobalWebSocket() {
  if (globalWebSocket) {
    globalWebSocket.disconnect()
    globalWebSocket = null
  }
} 