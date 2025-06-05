<template>
  <transition name="status-fade">
    <div v-if="showStatus" class="websocket-status" :class="[statusClass, { expanded: isExpanded }]">
      <div class="status-indicator" @click="toggleExpand">
        <div class="status-dot" :class="{ pulse: isConnected }"></div>
        <span class="status-text">{{ statusText }}</span>
        <el-icon class="expand-icon">
          <ArrowDown v-if="!isExpanded" />
          <ArrowUp v-else />
        </el-icon>
      </div>
      
      <transition name="slide-down">
        <div v-if="isExpanded" class="status-details">
          <div class="detail-item">
            <span class="detail-label">连接状态：</span>
            <el-tag :type="isConnected ? 'success' : 'danger'" size="small">
              {{ isConnected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
          <div class="detail-item" v-if="connectionInfo.url">
            <span class="detail-label">服务器：</span>
            <span class="detail-value">{{ connectionInfo.url }}</span>
          </div>
          <div class="detail-item" v-if="connectionInfo.lastMessage">
            <span class="detail-label">最后消息：</span>
            <span class="detail-value">{{ formatTime(connectionInfo.lastMessage) }}</span>
          </div>
          <div class="detail-actions">
            <el-button 
              v-if="!isConnected" 
              type="primary" 
              size="small" 
              @click="reconnect"
              :loading="isReconnecting"
            >
              <el-icon><Refresh /></el-icon>
              重新连接
            </el-button>
            <el-button 
              v-else 
              type="danger" 
              size="small" 
              @click="disconnect"
            >
              <el-icon><Close /></el-icon>
              断开连接
            </el-button>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowDown, ArrowUp, Refresh, Close } from '@element-plus/icons-vue'
import { getGlobalWebSocket } from '@/utils/unifiedWebSocket'

// 状态
const isExpanded = ref(false)
const isConnected = ref(false)
const isReconnecting = ref(false)
const showStatus = ref(true)
const connectionInfo = ref({
  url: '',
  lastMessage: null as Date | null
})

// 计算属性
const statusClass = computed(() => {
  return isConnected.value ? 'status-connected' : 'status-disconnected'
})

const statusText = computed(() => {
  if (isReconnecting.value) {
    return '重连中...'
  }
  return isConnected.value ? 'WebSocket 已连接' : 'WebSocket 未连接'
})

// 格式化时间
const formatTime = (date: Date | null) => {
  if (!date) return '无'
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)} 分钟前`
  } else {
    return date.toLocaleTimeString()
  }
}

// 切换展开状态
const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

// 重新连接
const reconnect = async () => {
  isReconnecting.value = true
  const ws = getGlobalWebSocket()
  if (ws) {
    try {
      await ws.connect()
    } catch (error) {
      console.error('重连失败:', error)
    }
  }
  isReconnecting.value = false
}

// 断开连接
const disconnect = () => {
  const ws = getGlobalWebSocket()
  if (ws) {
    ws.disconnect()
  }
}

// 监听WebSocket状态
const updateStatus = () => {
  const ws = getGlobalWebSocket()
  if (ws) {
    // WebSocket没有直接的isConnected方法，需要通过其他方式判断
    // 这里暂时通过监听事件来更新状态
  }
}

// 生命周期
let statusInterval: number

onMounted(() => {
  updateStatus()
  // 定期更新状态
  statusInterval = window.setInterval(updateStatus, 1000)
  
  // 监听WebSocket事件
  const ws = getGlobalWebSocket()
  if (ws) {
    ws.onConnected(() => {
      isConnected.value = true
      connectionInfo.value.lastMessage = new Date()
    })
    
    ws.onDisconnected(() => {
      isConnected.value = false
    })
    
    ws.onError((error) => {
      console.error('WebSocket错误:', error)
      isConnected.value = false
    })
  }
})

onUnmounted(() => {
  if (statusInterval) {
    clearInterval(statusInterval)
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.websocket-status {
  position: fixed;
  bottom: $spacing-lg;
  right: $spacing-lg;
  background: white;
  border-radius: $radius-round;
  box-shadow: $shadow-lg;
  transition: all $duration-base;
  z-index: $z-index-sticky;
  min-width: 200px;
  
  &.expanded {
    border-radius: $radius-lg;
    min-width: 300px;
  }
  
  &.status-connected {
    .status-dot {
      background-color: $success-color;
    }
  }
  
  &.status-disconnected {
    .status-dot {
      background-color: $error-color;
    }
  }
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  cursor: pointer;
  user-select: none;
  
  &:hover {
    .expand-icon {
      transform: translateY(2px);
    }
  }
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  transition: all $duration-fast;
  
  &.pulse {
    animation: pulse-dot 2s ease-in-out infinite;
  }
}

.status-text {
  flex: 1;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.expand-icon {
  color: $text-secondary;
  transition: transform $duration-fast;
}

.status-details {
  padding: $spacing-md;
  border-top: 1px solid $divider-color;
  background: $bg-color;
  border-radius: 0 0 $radius-lg $radius-lg;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;
  font-size: $font-size-sm;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.detail-label {
  color: $text-secondary;
  margin-right: $spacing-sm;
  min-width: 80px;
}

.detail-value {
  color: $text-regular;
  word-break: break-all;
}

.detail-actions {
  margin-top: $spacing-md;
  display: flex;
  justify-content: center;
}

// 动画
@keyframes pulse-dot {
  0% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(82, 196, 26, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0);
  }
}

.status-fade-enter-active,
.status-fade-leave-active {
  transition: all $duration-base;
}

.status-fade-enter-from,
.status-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all $duration-base;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.slide-down-enter-to,
.slide-down-leave-from {
  opacity: 1;
  max-height: 200px;
}

// 响应式设计
@media (max-width: 768px) {
  .websocket-status {
    bottom: $spacing-md;
    right: $spacing-md;
    left: $spacing-md;
    
    &.expanded {
      min-width: auto;
    }
  }
}
</style> 