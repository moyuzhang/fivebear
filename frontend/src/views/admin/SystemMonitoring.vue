<template>
  <Layout>
    <div class="system-monitoring">
      <!-- 系统统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon online">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ onlineStats.onlineUserCount }}</h3>
              <p>在线用户</p>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon connections">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ onlineStats.totalConnections }}</h3>
              <p>总连接数</p>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon status">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ systemStatus.status }}</h3>
              <p>系统状态</p>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon uptime">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ systemStatus.uptime }}</h3>
              <p>运行时间</p>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 实时消息监控 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Message /></el-icon>
                <span>系统消息</span>
                <el-button 
                  type="text" 
                  @click="clearSystemMessages"
                  style="float: right; padding: 3px 0;"
                >
                  清空
                </el-button>
              </div>
            </template>
            
            <div class="message-container">
              <div 
                v-for="(message, index) in systemMessages" 
                :key="index"
                class="message-item"
                :class="message.level"
              >
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
                <div class="message-content">{{ message.message }}</div>
              </div>
              <div v-if="systemMessages.length === 0" class="no-messages">
                暂无系统消息
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><UserFilled /></el-icon>
                <span>用户活动</span>
                <el-button 
                  type="text" 
                  @click="clearUserActions"
                  style="float: right; padding: 3px 0;"
                >
                  清空
                </el-button>
              </div>
            </template>
            
            <div class="message-container">
              <div 
                v-for="(action, index) in userActions" 
                :key="index"
                class="message-item user-action"
              >
                <div class="message-time">{{ formatTime(action.timestamp) }}</div>
                <div class="message-content">
                  <span class="username">{{ action.data.username }}</span>
                  <span class="action">{{ action.data.action }}</span>
                </div>
              </div>
              <div v-if="userActions.length === 0" class="no-messages">
                暂无用户活动
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- WebSocket连接状态 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <el-icon><Link /></el-icon>
            <span>WebSocket连接状态</span>
            <el-tag 
              :type="wsConnected ? 'success' : 'danger'" 
              style="float: right;"
            >
              {{ wsConnected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
        </template>
        
        <div class="ws-info">
          <p><strong>连接状态:</strong> {{ wsConnected ? '正常' : '断开' }}</p>
          <p><strong>连接时间:</strong> {{ wsConnectTime || '未连接' }}</p>
          <p><strong>接收消息数:</strong> {{ messageCount }}</p>
          <p><strong>最后更新:</strong> {{ lastUpdateTime || '无' }}</p>
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { 
  Monitor, User, Connection, Clock, Message, UserFilled, Link 
} from '@element-plus/icons-vue'
import { getGlobalWebSocket } from '@/utils/unifiedWebSocket'
import type { WebSocketMessage } from '@/utils/unifiedWebSocket'

// 响应式数据
const onlineStats = reactive({
  onlineUserCount: 0,
  totalConnections: 0
})

const systemStatus = reactive({
  status: '正常',
  uptime: '0h 0m'
})

const systemMessages = ref<any[]>([])
const userActions = ref<any[]>([])
const wsConnected = ref(false)
const wsConnectTime = ref<string>('')
const messageCount = ref(0)
const lastUpdateTime = ref<string>('')

// WebSocket实例
let webSocket: any = null

// 格式化时间
const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 清空系统消息
const clearSystemMessages = () => {
  systemMessages.value = []
}

// 清空用户活动
const clearUserActions = () => {
  userActions.value = []
}

// 处理WebSocket消息
const handleWebSocketMessage = (message: WebSocketMessage) => {
  messageCount.value++
  lastUpdateTime.value = new Date().toLocaleString()
  
  console.log('📊 SystemMonitoring 收到消息:', message)
  
  switch (message.type) {
    case 'ADMIN_ONLINE_USER_COUNT':
      if (message.data) {
        onlineStats.onlineUserCount = message.data.onlineUserCount || 0
        onlineStats.totalConnections = message.data.totalConnections || 0
      }
      break
      
    case 'ADMIN_USER_ACTION':
      userActions.value.unshift({
        ...message,
        timestamp: message.timestamp
      })
      // 只保留最新的50条记录
      if (userActions.value.length > 50) {
        userActions.value = userActions.value.slice(0, 50)
      }
      break
      
    case 'ADMIN_SYSTEM_STATUS':
      systemStatus.status = message.data?.status || '正常'
      systemMessages.value.unshift({
        ...message,
        timestamp: message.timestamp
      })
      // 只保留最新的50条记录
      if (systemMessages.value.length > 50) {
        systemMessages.value = systemMessages.value.slice(0, 50)
      }
      break
      
    case 'SYSTEM_INFO':
    case 'SYSTEM_WARNING':
    case 'SYSTEM_ERROR':
      systemMessages.value.unshift({
        ...message,
        timestamp: message.timestamp
      })
      if (systemMessages.value.length > 50) {
        systemMessages.value = systemMessages.value.slice(0, 50)
      }
      break
  }
}

// 初始化WebSocket连接
const initWebSocket = () => {
  try {
    webSocket = getGlobalWebSocket()
    
    // 监听连接状态
    webSocket.onConnected(() => {
      wsConnected.value = true
      wsConnectTime.value = new Date().toLocaleString()
      console.log('🔗 SystemMonitoring WebSocket连接成功')
    })
    
    webSocket.onDisconnected(() => {
      wsConnected.value = false
      console.log('🔗 SystemMonitoring WebSocket连接断开')
    })
    
    // 监听管理员专用消息
    webSocket.onMessage('ADMIN_ONLINE_USER_COUNT', handleWebSocketMessage)
    webSocket.onMessage('ADMIN_USER_ACTION', handleWebSocketMessage)
    webSocket.onMessage('ADMIN_SYSTEM_STATUS', handleWebSocketMessage)
    webSocket.onMessage('SYSTEM_INFO', handleWebSocketMessage)
    webSocket.onMessage('SYSTEM_WARNING', handleWebSocketMessage)
    webSocket.onMessage('SYSTEM_ERROR', handleWebSocketMessage)
    
    console.log('📊 SystemMonitoring WebSocket监听器已注册')
  } catch (error) {
    console.error('❌ SystemMonitoring WebSocket初始化失败:', error)
  }
}

// 更新运行时间
const updateUptime = () => {
  const startTime = new Date().getTime() - 60000 * Math.random() * 100 // 模拟运行时间
  const updateUptimeDisplay = () => {
    const now = new Date().getTime()
    const diff = now - startTime
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    systemStatus.uptime = `${hours}h ${minutes}m`
  }
  
  updateUptimeDisplay()
  setInterval(updateUptimeDisplay, 60000) // 每分钟更新一次
}

onMounted(() => {
  console.log('📊 SystemMonitoring 组件挂载')
  initWebSocket()
  updateUptime()
})

onUnmounted(() => {
  console.log('📊 SystemMonitoring 组件卸载')
  // 清理资源
})
</script>

<style scoped>
.system-monitoring {
  padding: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.online {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.connections {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.status {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.uptime {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info h3 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-info p {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
}

.card-header .el-icon {
  margin-right: 8px;
}

.message-container {
  max-height: 300px;
  overflow-y: auto;
}

.message-item {
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  border-left: 4px solid #ddd;
}

.message-item.info {
  background-color: #f0f9ff;
  border-left-color: #3b82f6;
}

.message-item.warning {
  background-color: #fffbeb;
  border-left-color: #f59e0b;
}

.message-item.error {
  background-color: #fef2f2;
  border-left-color: #ef4444;
}

.message-item.user-action {
  background-color: #f8fafc;
  border-left-color: #6366f1;
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.message-content {
  font-size: 14px;
  color: #333;
}

.username {
  font-weight: bold;
  color: #4f46e5;
}

.action {
  margin-left: 8px;
  color: #666;
}

.no-messages {
  text-align: center;
  color: #999;
  padding: 20px;
  font-style: italic;
}

.ws-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.ws-info p {
  margin: 8px 0;
  color: #666;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .ws-info {
    grid-template-columns: 1fr;
  }
}
</style> 