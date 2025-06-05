<template>
  <div class="test-messages">
    <el-card header="消息通知测试">
      <div class="test-section">
        <h3>WebSocket消息测试</h3>
        <el-space wrap>
          <el-button @click="sendTestMessage('SYSTEM_INFO')" type="primary">
            系统信息
          </el-button>
          <el-button @click="sendTestMessage('SYSTEM_WARNING')" type="warning">
            系统警告
          </el-button>
          <el-button @click="sendTestMessage('SYSTEM_ERROR')" type="danger">
            系统错误
          </el-button>
          <el-button @click="sendTestMessage('ADMIN_ONLINE_USER_COUNT')" type="success">
            用户统计
          </el-button>
          <el-button @click="sendTestMessage('ADMIN_USER_ACTION')" type="info">
            用户活动
          </el-button>
          <el-button @click="sendTestMessage('FORCE_LOGOUT')" type="danger" plain>
            强制登出
          </el-button>
        </el-space>
      </div>

      <div class="test-section">
        <h3>自定义消息测试</h3>
        <el-form :model="customMessage" label-width="100px">
          <el-form-item label="消息类型">
            <el-select v-model="customMessage.type" placeholder="选择消息类型">
              <el-option label="系统信息" value="SYSTEM_INFO" />
              <el-option label="系统警告" value="SYSTEM_WARNING" />
              <el-option label="系统错误" value="SYSTEM_ERROR" />
              <el-option label="业务信息" value="BUSINESS_INFO" />
              <el-option label="业务警告" value="BUSINESS_WARNING" />
              <el-option label="业务错误" value="BUSINESS_ERROR" />
              <el-option label="连接成功" value="CONNECTION_SUCCESS" />
              <el-option label="强制登出" value="FORCE_LOGOUT" />
            </el-select>
          </el-form-item>
          <el-form-item label="消息内容">
            <el-input 
              v-model="customMessage.message" 
              placeholder="输入消息内容"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="消息级别">
            <el-select v-model="customMessage.level" placeholder="选择消息级别">
              <el-option label="信息" value="info" />
              <el-option label="成功" value="success" />
              <el-option label="警告" value="warning" />
              <el-option label="错误" value="error" />
            </el-select>
          </el-form-item>
          <el-form-item label="附加数据">
            <el-input 
              v-model="customMessage.dataJson" 
              type="textarea" 
              placeholder="输入JSON格式的附加数据（可选）"
              :rows="3"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="sendCustomMessage" type="primary">
              发送自定义消息
            </el-button>
            <el-button @click="clearCustomMessage">
              清空
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="test-section">
        <h3>WebSocket连接状态</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="连接状态">
            <el-tag :type="isWebSocketConnected ? 'success' : 'danger'">
              {{ isWebSocketConnected ? '已连接' : '未连接' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息数量">
            {{ messageCount }}
          </el-descriptions-item>
          <el-descriptions-item label="最后消息时间">
            {{ lastMessageTime || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户角色">
            <el-tag type="primary">{{ userRole }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 16px;">
          <el-button @click="reconnectWebSocket" :loading="reconnecting">
            重新连接WebSocket
          </el-button>
          <el-button @click="clearMessageHistory" type="warning">
            清空消息历史
          </el-button>
        </div>
      </div>

      <div class="test-section">
        <h3>消息历史</h3>
        <el-table :data="messageHistory" height="300" style="width: 100%">
          <el-table-column prop="timestamp" label="时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="150">
            <template #default="{ row }">
              <el-tag :type="getTypeColor(row.type)" size="small">
                {{ row.type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="消息内容" />
          <el-table-column prop="level" label="级别" width="80">
            <template #default="{ row }">
              <el-tag :type="getLevelColor(row.level)" size="small">
                {{ row.level }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getGlobalWebSocket } from '@/utils/unifiedWebSocket'

// 用户store
const userStore = useUserStore()

// WebSocket相关
let webSocket: any = null
const isWebSocketConnected = ref(false)
const reconnecting = ref(false)
const messageCount = ref(0)
const lastMessageTime = ref('')

// 消息历史
const messageHistory = ref<any[]>([])

// 自定义消息表单
const customMessage = reactive({
  type: 'SYSTEM_INFO',
  message: '',
  level: 'info',
  dataJson: ''
})

// 计算属性
const userRole = computed(() => {
  if (userStore.isAdmin()) {
    return '管理员'
  } else {
    return '普通用户'
  }
})

// 组件挂载
onMounted(() => {
  setupWebSocket()
})

// 组件卸载
onUnmounted(() => {
  if (webSocket) {
    webSocket.offMessage('*', handleWebSocketMessage)
  }
})

// 设置WebSocket
const setupWebSocket = () => {
  try {
    webSocket = getGlobalWebSocket()
    if (webSocket) {
      isWebSocketConnected.value = true
      webSocket.onMessage('*', handleWebSocketMessage)
      console.log('📡 TestMessages: WebSocket监听器已设置')
    }
  } catch (error) {
    console.error('❌ TestMessages: 设置WebSocket失败:', error)
    isWebSocketConnected.value = false
  }
}

// 处理WebSocket消息
const handleWebSocketMessage = (message: any) => {
  console.log('📩 TestMessages: 收到消息:', message)
  
  messageCount.value++
  lastMessageTime.value = new Date().toLocaleString()
  
  // 添加到消息历史
  messageHistory.value.unshift({
    id: Date.now(),
    timestamp: message.timestamp || new Date().toISOString(),
    type: message.type,
    message: message.message,
    level: message.level || 'info',
    data: message.data
  })
  
  // 限制历史记录数量
  if (messageHistory.value.length > 50) {
    messageHistory.value = messageHistory.value.slice(0, 50)
  }
}

// 发送测试消息
const sendTestMessage = (type: string) => {
  const testMessages: Record<string, any> = {
    'SYSTEM_INFO': {
      type: 'SYSTEM_INFO',
      message: '这是一条系统信息测试消息',
      level: 'info',
      data: {
        server: 'test-server',
        version: '1.0.0'
      }
    },
    'SYSTEM_WARNING': {
      type: 'SYSTEM_WARNING',
      message: '系统资源使用率较高，请注意监控',
      level: 'warning',
      data: {
        cpu: '85%',
        memory: '78%'
      }
    },
    'SYSTEM_ERROR': {
      type: 'SYSTEM_ERROR',
      message: '系统出现严重错误，需要立即处理',
      level: 'error',
      data: {
        errorCode: 'SYS_001',
        errorMessage: 'Database connection failed'
      }
    },
    'ADMIN_ONLINE_USER_COUNT': {
      type: 'ADMIN_ONLINE_USER_COUNT',
      message: '在线用户数量更新',
      level: 'info',
      data: {
        onlineUserCount: 15,
        totalConnections: 23,
        timestamp: Date.now()
      }
    },
    'ADMIN_USER_ACTION': {
      type: 'ADMIN_USER_ACTION',
      message: '用户执行了重要操作',
      level: 'info',
      data: {
        username: 'testuser',
        action: '登录',
        loginTime: new Date().toISOString()
      }
    },
    'FORCE_LOGOUT': {
      type: 'FORCE_LOGOUT',
      message: '检测到其他设备登录，您已被强制下线',
      level: 'error',
      data: {
        reason: '多地登录',
        timestamp: Date.now()
      }
    }
  }

  const messageData = testMessages[type]
  if (messageData) {
    // 模拟WebSocket消息
    const fullMessage = {
      ...messageData,
      timestamp: new Date().toISOString(),
      meta: {
        description: `测试消息 - ${type}`,
        requiresRole: type.startsWith('ADMIN_'),
        requiredRole: type.startsWith('ADMIN_') ? '管理员' : null
      }
    }
    
    // 直接调用消息处理函数来模拟接收到的WebSocket消息
    handleWebSocketMessage(fullMessage)
    
    ElMessage.success(`已发送 ${type} 测试消息`)
  }
}

// 发送自定义消息
const sendCustomMessage = () => {
  if (!customMessage.message.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  let data = null
  if (customMessage.dataJson.trim()) {
    try {
      data = JSON.parse(customMessage.dataJson)
    } catch (error) {
      ElMessage.error('附加数据JSON格式错误')
      return
    }
  }

  const messageData = {
    type: customMessage.type,
    message: customMessage.message,
    level: customMessage.level,
    timestamp: new Date().toISOString(),
    data,
    meta: {
      description: '自定义测试消息',
      requiresRole: customMessage.type.startsWith('ADMIN_'),
      requiredRole: customMessage.type.startsWith('ADMIN_') ? '管理员' : null
    }
  }

  handleWebSocketMessage(messageData)
  ElMessage.success('已发送自定义消息')
}

// 清空自定义消息表单
const clearCustomMessage = () => {
  customMessage.type = 'SYSTEM_INFO'
  customMessage.message = ''
  customMessage.level = 'info'
  customMessage.dataJson = ''
}

// 重新连接WebSocket
const reconnectWebSocket = async () => {
  reconnecting.value = true
  try {
    // 模拟重连过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    setupWebSocket()
    ElMessage.success('WebSocket重连成功')
  } catch (error) {
    ElMessage.error('WebSocket重连失败')
  } finally {
    reconnecting.value = false
  }
}

// 清空消息历史
const clearMessageHistory = () => {
  messageHistory.value = []
  messageCount.value = 0
  lastMessageTime.value = ''
  ElMessage.success('消息历史已清空')
}

// 格式化时间
const formatTime = (timestamp: string) => {
  try {
    return new Date(timestamp).toLocaleString()
  } catch {
    return timestamp
  }
}

// 获取类型颜色
const getTypeColor = (type: string) => {
  if (type.includes('ERROR')) return 'danger'
  if (type.includes('WARNING')) return 'warning'
  if (type.includes('SUCCESS')) return 'success'
  if (type.includes('ADMIN')) return 'primary'
  return 'info'
}

// 获取级别颜色
const getLevelColor = (level: string) => {
  const colorMap: Record<string, string> = {
    'error': 'danger',
    'warning': 'warning',
    'success': 'success',
    'info': 'info'
  }
  return colorMap[level] || 'info'
}
</script>

<style scoped>
.test-messages {
  padding: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.test-section h3 {
  margin: 0 0 20px 0;
  color: #409EFF;
  font-size: 16px;
  font-weight: 600;
}

.test-section:last-child {
  margin-bottom: 0;
}
</style> 