<template>
  <Layout>
    <div class="admin-dashboard">
    <el-card>
      <template #header>
        <div class="admin-header">
          <div class="header-title">
            <el-icon><UserFilled /></el-icon>
            <span>管理员控制台</span>
          </div>
          <div class="header-info">
            <el-tag type="success">管理员权限</el-tag>
          </div>
        </div>
      </template>

      <!-- 快速统计 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon user-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.totalUsers }}</div>
                  <div class="stat-label">系统用户</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon online-icon">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.onlineUsers }}</div>
                  <div class="stat-label">在线用户</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon role-icon">
                  <el-icon><Key /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.totalRoles }}</div>
                  <div class="stat-label">系统角色</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon log-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.todayLogs }}</div>
                  <div class="stat-label">今日日志</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 功能模块 -->
      <div class="modules-section">
        <h3>管理功能</h3>
        <el-row :gutter="20">
          <el-col :span="8" v-for="module in adminModules" :key="module.name">
            <el-card class="module-card" shadow="hover" @click="navigateToModule(module)">
              <div class="module-content">
                <div class="module-icon" :class="module.iconClass">
                  <el-icon>
                    <component :is="module.icon" />
                  </el-icon>
                </div>
                <div class="module-info">
                  <h4>{{ module.title }}</h4>
                  <p>{{ module.description }}</p>
                </div>
                <div class="module-arrow">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 系统状态 -->
      <div class="system-status">
        <h3>系统状态</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>实时监控</span>
              </template>
              <div class="status-item">
                <span>WebSocket连接</span>
                <el-tag :type="webSocketStatus ? 'success' : 'danger'">
                  {{ webSocketStatus ? '正常' : '异常' }}
                </el-tag>
              </div>
              <div class="status-item">
                <span>数据库连接</span>
                <el-tag type="success">正常</el-tag>
              </div>
              <div class="status-item">
                <span>系统负载</span>
                <el-tag type="success">正常</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>最近活动</span>
              </template>
              <div class="activity-list">
                <div class="activity-item" v-for="activity in recentActivities" :key="activity.id">
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '@/components/Layout.vue'
import { 
  UserFilled, 
  User, 
  CircleCheck, 
  Key, 
  Document, 
  ArrowRight,
  Setting,
  Monitor,
  TrendCharts,
  Files,
  Lock
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = reactive({
  totalUsers: 0,
  onlineUsers: 0,
  totalRoles: 0,
  todayLogs: 0
})

// WebSocket状态
const webSocketStatus = ref(false)

// 管理模块配置
const adminModules = ref([
  {
    name: 'users',
    title: '用户管理',
    description: '管理系统用户、权限分配',
    icon: User,
    iconClass: 'user-module',
    path: '/admin/users'
  },
  {
    name: 'roles',
    title: '角色权限',
    description: '管理角色和权限配置',
    icon: Key,
    iconClass: 'role-module',
    path: '/admin/roles'
  },
  {
    name: 'settings',
    title: '系统设置',
    description: '系统参数和配置管理',
    icon: Setting,
    iconClass: 'setting-module',
    path: '/admin/settings'
  },
  {
    name: 'monitoring',
    title: '系统监控',
    description: '系统状态和性能监控',
    icon: Monitor,
    iconClass: 'monitor-module',
    path: '/admin/monitoring'
  },
  {
    name: 'logs',
    title: '日志管理',
    description: '查看和管理系统日志',
    icon: Files,
    iconClass: 'log-module',
    path: '/admin/logs'
  },
  {
    name: 'security',
    title: '安全中心',
    description: '系统安全设置和审计',
    icon: Lock,
    iconClass: 'security-module',
    path: '/admin/security'
  }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    time: new Date(Date.now() - 5 * 60 * 1000),
    description: '管理员登录系统'
  },
  {
    id: 2,
    time: new Date(Date.now() - 15 * 60 * 1000),
    description: '新用户注册'
  },
  {
    id: 3,
    time: new Date(Date.now() - 30 * 60 * 1000),
    description: '系统配置更新'
  }
])

// 格式化时间
const formatTime = (time: Date) => {
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  
  const days = Math.floor(hours / 24)
  return `${days}天前`
}

// 导航到模块
const navigateToModule = (module: any) => {
  router.push(module.path)
}

// 加载统计数据
const loadStats = async () => {
  // TODO: 调用API获取真实数据
  stats.totalUsers = 25
  stats.onlineUsers = 8
  stats.totalRoles = 5
  stats.todayLogs = 156
}

// 监控WebSocket状态
const checkWebSocketStatus = () => {
  // 简单的假设WebSocket是连接的，后续可以集成真实的WebSocket状态
  webSocketStatus.value = true
}

onMounted(() => {
  loadStats()
  checkWebSocketStatus()
  
  // 定期检查WebSocket状态
  setInterval(checkWebSocketStatus, 5000)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 120px 20px 20px 20px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.header-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.user-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.online-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.role-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.log-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

/* 功能模块 */
.modules-section {
  margin-bottom: 30px;
}

.modules-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
}

.module-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.module-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.module-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 28px;
  color: white;
}

.user-module { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.role-module { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.setting-module { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.monitor-module { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.log-module { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.security-module { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }

.module-info {
  flex: 1;
}

.module-info h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.module-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.module-arrow {
  color: #ccc;
  font-size: 18px;
}

/* 系统状态 */
.system-status h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-item:last-child {
  border-bottom: none;
}

.activity-list {
  max-height: 200px;
  overflow-y: auto;
}

.activity-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 3px;
}

.activity-desc {
  font-size: 14px;
  color: #333;
}
</style> 