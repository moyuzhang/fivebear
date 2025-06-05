<template>
  <Layout>
    <div class="admin-dashboard">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>管理控制台</h1>
        <p>系统概览与监控面板</p>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="24" color="#409EFF"><UserFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="24" color="#67C23A"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.onlineUsers }}</div>
                <div class="stat-label">在线用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="24" color="#E6A23C"><Key /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalRoles }}</div>
                <div class="stat-label">角色数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="24" color="#F56C6C"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.todayLogs }}</div>
                <div class="stat-label">今日日志</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 系统状态 -->
      <el-card v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>系统状态</span>
            <el-button size="small" @click="loadStats">刷新</el-button>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>服务状态</span>
              </template>
              
              <div class="status-item">
                <span>数据库连接</span>
                <el-tag :type="systemStatus.database.connected ? 'success' : 'danger'">
                  {{ systemStatus.database.connected ? '已连接' : '未连接' }}
                </el-tag>
                <div v-if="systemStatus.database.productName" class="status-detail">
                  {{ systemStatus.database.productName }} {{ systemStatus.database.productVersion }}
                </div>
              </div>
              
              <div class="status-item">
                <span>Redis缓存</span>
                <el-tag :type="systemStatus.redis.connected ? 'success' : 'danger'">
                  {{ systemStatus.redis.connected ? '已连接' : '未连接' }}
                </el-tag>
                <div v-if="systemStatus.redis.error" class="status-error">
                  {{ systemStatus.redis.error }}
                </div>
              </div>
              
              <div class="status-item">
                <span>Java版本</span>
                <el-tag type="info">{{ systemStatus.systemInfo.javaVersion }}</el-tag>
              </div>
              
              <div class="status-item">
                <span>操作系统</span>
                <el-tag type="info">{{ systemStatus.systemInfo.osName }}</el-tag>
              </div>
              
              <div class="status-item">
                <span>处理器核心</span>
                <el-tag type="info">{{ systemStatus.systemInfo.processors }} 核</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>系统资源</span>
              </template>
              
              <div class="resource-item">
                <div class="resource-label">内存使用率</div>
                <el-progress 
                  :percentage="Math.round(systemStatus.memory.usagePercent)" 
                  :color="getMemoryColor(systemStatus.memory.usagePercent)"
                />
                <div class="resource-detail">
                  {{ systemStatus.memory.usedMB }}MB / {{ systemStatus.memory.maxMB }}MB
                </div>
              </div>
              
              <div class="resource-item">
                <div class="resource-label">系统运行时间</div>
                <div class="resource-value">{{ formatUptime(systemStatus.systemInfo.uptime) }}</div>
              </div>
              
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 最近活动 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>最近活动</span>
          </template>
          
          <div class="activity-list">
            <div class="activity-item" v-for="activity in recentActivities" :key="activity.id">
              <div class="activity-icon">
                <el-tag :type="getActivityType(activity.type)" size="small">
                  {{ activity.type }}
                </el-tag>
              </div>
              <div class="activity-content">
                <div class="activity-desc">{{ activity.description }}</div>
                <div class="activity-meta">
                  <span class="activity-user">{{ activity.username }}</span>
                  <span class="activity-time">{{ activity.time }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="recentActivities.length === 0" class="no-activities">
              暂无活动记录
            </div>
          </div>
        </el-card>
      </el-card>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { getSystemStatus, getStatistics, getRecentActivities, type Activity } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { 
  UserFilled, 
  User, 
  Key, 
  Document
} from '@element-plus/icons-vue'

// Router removed as navigation is no longer needed

// 统计数据
const stats = reactive({
  totalUsers: 0,
  onlineUsers: 128, // 模拟在线用户数
  totalRoles: 0,
  todayLogs: 0
})

// 系统状态数据
const systemStatus = ref({
  database: { connected: false, productName: '', productVersion: '' },
  redis: { connected: false, error: '' },
  memory: { usagePercent: 0, usedMB: 0, maxMB: 0 },
  systemInfo: { 
    javaVersion: '', 
    osName: '', 
    processors: 0, 
    uptime: 0 
  }
})

// 最近活动数据
const recentActivities = ref<Activity[]>([])

// 加载状态
const loading = ref(false)

// 加载统计数据
const loadStats = async () => {
  try {
    loading.value = true
    
    // 加载系统状态
    const statusRes = await getSystemStatus()
    if (statusRes.code === 200) {
      systemStatus.value = statusRes.data
    }
    
    // 加载统计信息
    const statsRes = await getStatistics()
    if (statsRes.code === 200) {
      stats.totalUsers = statsRes.data.users.total
      stats.totalRoles = statsRes.data.roles.total
      stats.todayLogs = statsRes.data.sites.total
    }
    
    // 加载最近活动
    const activitiesRes = await getRecentActivities()
    if (activitiesRes.code === 200) {
      recentActivities.value = activitiesRes.data
    }
    
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化运行时间
const formatUptime = (uptime: number) => {
  const seconds = Math.floor(uptime / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) {
    return `${days}天 ${hours % 24}小时`
  } else if (hours > 0) {
    return `${hours}小时 ${minutes % 60}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 获取内存使用率颜色
const getMemoryColor = (percentage: number) => {
  if (percentage > 80) return '#F56C6C'
  if (percentage > 60) return '#E6A23C'
  return '#67C23A'
}

// 获取活动类型样式
const getActivityType = (type: string) => {
  switch (type) {
    case 'login': return 'success'
    case 'system': return 'info'
    case 'operation': return 'warning'
    case 'error': return 'danger'
    default: return 'info'
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.admin-dashboard {
  padding: var(--space-6);
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header h1 {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--gray-900);
  margin: 0 0 var(--space-2) 0;
}

.page-header p {
  color: var(--gray-600);
  margin: 0;
}

.stats-row {
  margin-bottom: var(--space-6);
}

.stat-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-icon {
  padding: var(--space-3);
  background: var(--gray-50);
  border-radius: var(--radius-full);
}

.stat-number {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--gray-900);
  line-height: 1;
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--gray-500);
  margin-top: var(--space-1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--gray-200);
}

.status-item:last-child {
  border-bottom: none;
}

.status-detail {
  font-size: var(--text-xs);
  color: var(--gray-500);
  margin-top: var(--space-1);
}

.status-error {
  font-size: var(--text-xs);
  color: var(--error-600);
  margin-top: var(--space-1);
}

.resource-item {
  margin-bottom: var(--space-4);
}

.resource-item:last-child {
  margin-bottom: 0;
}

.resource-label {
  font-size: var(--text-sm);
  color: var(--gray-700);
  margin-bottom: var(--space-2);
}

.resource-detail {
  font-size: var(--text-xs);
  color: var(--gray-500);
  margin-top: var(--space-1);
}

.resource-value {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--gray-900);
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--gray-200);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-content {
  flex: 1;
}

.activity-desc {
  font-size: var(--text-sm);
  color: var(--gray-900);
  margin-bottom: var(--space-1);
}

.activity-meta {
  display: flex;
  gap: var(--space-3);
  font-size: var(--text-xs);
  color: var(--gray-500);
}

.no-activities {
  text-align: center;
  color: var(--gray-500);
  padding: var(--space-6);
}
</style> 