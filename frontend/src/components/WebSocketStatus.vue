<template>
  <div class="websocket-status" v-if="showStatus">
    <el-tooltip :content="statusText" placement="bottom">
      <div class="status-indicator" :class="statusClass">
        <el-icon :size="12">
          <Connection v-if="isConnected" />
          <Close v-else />
        </el-icon>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Connection, Close } from '@element-plus/icons-vue'
import { loginNotificationService } from '@/utils/websocket'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isConnected = ref(false)
const statusInterval = ref<NodeJS.Timeout | null>(null)

// 是否显示状态指示器
const showStatus = computed(() => userStore.isLoggedIn)

// 状态文本
const statusText = computed(() => {
  if (isConnected.value) {
    return '实时通知已连接'
  } else {
    return '实时通知未连接'
  }
})

// 状态样式类
const statusClass = computed(() => {
  return {
    'status-connected': isConnected.value,
    'status-disconnected': !isConnected.value
  }
})

// 检查连接状态
const checkStatus = () => {
  isConnected.value = loginNotificationService.isConnected()
}

onMounted(() => {
  // 立即检查状态
  checkStatus()
  
  // 定期检查状态（每3秒）
  statusInterval.value = setInterval(checkStatus, 3000) as NodeJS.Timeout
})

onUnmounted(() => {
  if (statusInterval.value) {
    clearInterval(statusInterval.value)
  }
})
</script>

<style scoped>
.websocket-status {
  position: fixed;
  top: 75px;
  right: 20px;
  z-index: 999;
}

.status-indicator {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-connected {
  background-color: #67c23a;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
}

.status-disconnected {
  background-color: #f56c6c;
  box-shadow: 0 0 8px rgba(245, 108, 108, 0.5);
}

.status-indicator:hover {
  transform: scale(1.1);
}
</style> 