import SockJS from 'sockjs-client'
import { ElMessage, ElMessageBox } from 'element-plus'

interface LoginNotificationMessage {
  type: string
  message: string
  timestamp: number
  newToken?: string
}

/**
 * WebSocket登录通知服务
 */
export class LoginNotificationService {
  private socket: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000
  private isConnecting = false
  private isAuthenticated = false

  constructor() {
    // 不在构造函数中初始化，避免在组件外调用hook
  }

  /**
   * 连接WebSocket
   */
  connect(token: string): void {
    if (this.socket?.readyState === WebSocket.OPEN || this.isConnecting) {
      return
    }

    try {
      this.isConnecting = true
      console.log('🔌 正在连接WebSocket...')

      // 使用SockJS连接，在URL中附加token参数
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      const wsUrl = `${baseUrl}/ws/login-notification?token=${encodeURIComponent(token)}`
      const sockjs = new SockJS(wsUrl)
      this.socket = sockjs as any

      if (this.socket) {
        this.socket.onopen = () => {
          console.log('✅ WebSocket连接已建立，token已验证')
          this.isConnecting = false
          this.reconnectAttempts = 0
          this.isAuthenticated = true
        }

        this.socket.onmessage = (event) => {
          try {
            const data: LoginNotificationMessage = JSON.parse(event.data)
            this.handleMessage(data)
          } catch (error) {
            console.error('❌ 解析WebSocket消息失败:', error)
          }
        }

        this.socket.onerror = (error) => {
          console.error('❌ WebSocket连接错误:', error)
          this.isConnecting = false
        }

        this.socket.onclose = (event) => {
          console.log('🔌 WebSocket连接已关闭:', event.code, event.reason)
          this.isConnecting = false
          this.isAuthenticated = false
          
          // 如果不是主动关闭，尝试重连
          if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.scheduleReconnect(token)
          }
        }
      }

    } catch (error) {
      console.error('❌ 创建WebSocket连接失败:', error)
      this.isConnecting = false
    }
  }



  /**
   * 处理接收到的消息
   */
  private handleMessage(data: LoginNotificationMessage): void {
    console.log('📨 收到WebSocket消息:', data)

    switch (data.type) {
      case 'connected':
        console.log('✅ WebSocket连接确认:', data.message)
        break

      case 'error':
        console.error('❌ WebSocket错误:', data.message)
        ElMessage.error(`WebSocket错误: ${data.message}`)
        break

      case 'force_logout':
        this.handleForceLogout(data)
        break

      case 'pong':
        console.log('🏓 收到心跳响应:', data.message)
        break

      case 'status':
        console.log('📊 WebSocket状态:', data.message)
        break

      default:
        console.log('ℹ️ 未知WebSocket消息类型:', data.type, data)
    }
  }

  /**
   * 处理强制登出消息
   */
  private handleForceLogout(data: LoginNotificationMessage): void {
    console.log('🚨 收到强制登出通知:', data.message)

    // 显示通知对话框
    ElMessageBox.alert(
      '您的账户在其他设备上登录，当前会话将被强制下线。',
      '账户安全提醒',
      {
        confirmButtonText: '我知道了',
        type: 'warning',
        showClose: false,
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    ).then(() => {
      // 用户确认后，执行登出操作
      this.performForceLogout()
    }).catch(() => {
      // 即使用户拒绝，也要执行登出操作
      this.performForceLogout()
    })

    // 显示持续的警告消息
    ElMessage({
      message: '检测到账户在其他地方登录，即将自动退出...',
      type: 'warning',
      duration: 5000,
      showClose: true
    })
  }

  /**
   * 执行强制登出
   */
  private async performForceLogout(): Promise<void> {
    try {
      console.log('🚪 执行强制登出...')
      
      // 断开WebSocket连接
      this.disconnect()
      
      // 清理本地存储
      localStorage.removeItem('fivebear-user-info')
      
      // 清理Cookie中的token
      document.cookie = 'fivebear-token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
      
      // 跳转到登录页
      window.location.href = '/login?message=forced_logout'
      
      console.log('✅ 强制登出完成')
      
    } catch (error) {
      console.error('❌ 强制登出失败:', error)
      // 强制刷新页面
      window.location.reload()
    }
  }

  /**
   * 计划重连
   */
  private scheduleReconnect(token: string): void {
    this.reconnectAttempts++
    const delay = this.reconnectInterval * this.reconnectAttempts

    console.log(`🔄 ${delay}ms后尝试第${this.reconnectAttempts}次重连...`)

    setTimeout(() => {
      if (this.reconnectAttempts <= this.maxReconnectAttempts) {
        this.connect(token)
      } else {
        console.log('❌ WebSocket重连失败，已达到最大重试次数')
        ElMessage.error('网络连接不稳定，实时通知功能暂时不可用')
      }
    }, delay)
  }

  /**
   * 断开WebSocket连接
   */
  disconnect(): void {
    if (this.socket) {
      console.log('🔌 主动断开WebSocket连接')
      this.socket.close(1000, 'User logout')
      this.socket = null
      this.isAuthenticated = false
    }
  }

  /**
   * 检查连接状态
   */
  isConnected(): boolean {
    return this.socket?.readyState === WebSocket.OPEN && this.isAuthenticated
  }

  /**
   * 获取连接状态信息
   */
  getStatus(): { connected: boolean, authenticated: boolean, readyState?: number } {
    return {
      connected: this.socket?.readyState === WebSocket.OPEN,
      authenticated: this.isAuthenticated,
      readyState: this.socket?.readyState
    }
  }
}

// 创建单例实例
export const loginNotificationService = new LoginNotificationService() 