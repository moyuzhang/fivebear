<template>
  <Layout>
    <div class="dashboard">
      <!-- 欢迎横幅 -->
      <div class="welcome-banner gradient-primary">
        <div class="banner-content">
          <div class="banner-left">
            <h1 class="welcome-title">
              {{ getGreeting() }}，{{ userStore.userInfo?.nickname || '用户' }}！
            </h1>
            <p class="welcome-subtitle">
              今天是 {{ currentDate }}，祝您工作愉快！
            </p>
          </div>
          <div class="banner-right">
            <div class="time-display">
              <div class="current-time">{{ currentTime }}</div>
              <div class="current-day">{{ currentWeekday }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-grid">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in statistics" :key="stat.title">
          <div class="stat-card app-card" :class="`stat-${stat.type}`">
            <div class="stat-icon-wrapper">
              <el-icon :size="28" class="stat-icon">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-details">
              <h3 class="stat-value">{{ stat.value }}</h3>
              <p class="stat-title">{{ stat.title }}</p>
              <div class="stat-trend" v-if="stat.trend">
                <el-icon :class="stat.trend > 0 ? 'trend-up' : 'trend-down'">
                  <CaretTop v-if="stat.trend > 0" />
                  <CaretBottom v-else />
                </el-icon>
                <span>{{ Math.abs(stat.trend) }}%</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :xs="24" :lg="16">
          <div class="chart-card app-card">
            <div class="card-header">
              <h3>📈 业务趋势</h3>
              <el-button-group>
                <el-button size="small" :type="chartPeriod === 'week' ? 'primary' : ''" @click="chartPeriod = 'week'">
                  本周
                </el-button>
                <el-button size="small" :type="chartPeriod === 'month' ? 'primary' : ''" @click="chartPeriod = 'month'">
                  本月
                </el-button>
                <el-button size="small" :type="chartPeriod === 'year' ? 'primary' : ''" @click="chartPeriod = 'year'">
                  本年
                </el-button>
              </el-button-group>
            </div>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon :size="48" class="pulse-animation"><DataAnalysis /></el-icon>
                <p>图表数据加载中...</p>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :lg="8">
          <div class="chart-card app-card">
            <div class="card-header">
              <h3>🎯 任务进度</h3>
            </div>
            <div class="progress-list">
              <div class="progress-item" v-for="task in tasks" :key="task.name">
                <div class="progress-header">
                  <span class="task-name">{{ task.name }}</span>
                  <span class="task-percentage">{{ task.progress }}%</span>
                </div>
                <el-progress 
                  :percentage="task.progress" 
                  :color="getProgressColor(task.progress)"
                  :stroke-width="8"
                />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3 class="section-title">快捷操作</h3>
        <el-row :gutter="20">
          <el-col :xs="12" :sm="8" :md="6" v-for="action in quickActions" :key="action.path">
            <div class="action-card app-card" @click="$router.push(action.path)">
              <el-icon :size="32" :style="{ color: action.color }" class="action-icon">
                <component :is="action.icon" />
              </el-icon>
              <h4>{{ action.title }}</h4>
              <p>{{ action.description }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 最近活动 -->
      <div class="recent-activities app-card">
        <div class="card-header">
          <h3>📋 最近活动</h3>
          <el-link type="primary">查看全部</el-link>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="activity in recentActivities"
            :key="activity.id"
            :timestamp="activity.timestamp"
            :type="activity.type"
            placement="top"
          >
            <div class="activity-content">
              <h4>{{ activity.title }}</h4>
              <p>{{ activity.content }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { 
  User, TrendCharts, Box, Setting, DataAnalysis, Monitor,
  CaretTop, CaretBottom, Document, ShoppingCart
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 当前时间
const currentTime = ref('')
const currentDate = ref('')
const currentWeekday = ref('')
const chartPeriod = ref('week')

// 统计数据
const statistics = ref([
  { 
    title: '总用户数', 
    value: '1,234', 
    icon: User, 
    type: 'primary',
    trend: 12
  },
  { 
    title: '今日订单', 
    value: '456', 
    icon: ShoppingCart, 
    type: 'success',
    trend: -5
  },
  { 
    title: '本月收入', 
    value: '¥89,234', 
    icon: TrendCharts, 
    type: 'warning',
    trend: 23
  },
  { 
    title: '待处理', 
    value: '12', 
    icon: Document, 
    type: 'danger',
    trend: 0
  }
])

// 任务进度
const tasks = ref([
  { name: '订单处理', progress: 85 },
  { name: '库存盘点', progress: 65 },
  { name: '数据分析', progress: 45 },
  { name: '客户回访', progress: 92 }
])

// 快捷操作
const quickActions = ref([
  {
    title: '站点管理',
    description: '管理所有站点',
    icon: Monitor,
    color: '#1890ff',
    path: '/sites'
  },
  {
    title: '用户管理',
    description: '管理系统用户',
    icon: User,
    color: '#52c41a',
    path: '/admin/users'
  },
  {
    title: '报表分析',
    description: '查看数据报表',
    icon: TrendCharts,
    color: '#faad14',
    path: '/report'
  },
  {
    title: '系统设置',
    description: '配置系统参数',
    icon: Setting,
    color: '#722ed1',
    path: '/admin/settings'
  }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    title: '新用户注册',
    content: '用户 张三 完成注册',
    timestamp: '2024-01-20 10:30',
    type: 'success'
  },
  {
    id: 2,
    title: '订单完成',
    content: '订单 #12345 已完成配送',
    timestamp: '2024-01-20 09:15',
    type: 'primary'
  },
  {
    id: 3,
    title: '系统更新',
    content: '系统版本升级至 v2.0.1',
    timestamp: '2024-01-19 18:00',
    type: 'warning'
  }
])

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

// 获取进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage < 30) return '#ff4d4f'
  if (percentage < 70) return '#faad14'
  return '#52c41a'
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  currentWeekday.value = weekdays[now.getDay()]
}

let timer: number

onMounted(() => {
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.dashboard {
  animation: fadeIn 0.5s ease;
}

// 欢迎横幅
.welcome-banner {
  border-radius: $radius-lg;
  padding: $spacing-xl;
  margin-bottom: $spacing-lg;
  color: white;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 60%;
    height: 200%;
    background: rgba(255, 255, 255, 0.1);
    transform: rotate(35deg);
  }
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: $font-size-xxl;
  margin: 0 0 $spacing-sm 0;
  font-weight: 600;
}

.welcome-subtitle {
  font-size: $font-size-md;
  opacity: 0.9;
  margin: 0;
}

.time-display {
  text-align: center;
  padding: $spacing-md;
  background: rgba(255, 255, 255, 0.2);
  border-radius: $radius-lg;
  backdrop-filter: blur(10px);
}

.current-time {
  font-size: $font-size-xxl;
  font-weight: bold;
  margin-bottom: $spacing-xs;
}

.current-day {
  font-size: $font-size-sm;
  opacity: 0.9;
}

// 统计卡片
.stats-grid {
  margin-bottom: $spacing-lg;
}

.stat-card {
  padding: $spacing-lg;
  height: 120px;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  cursor: pointer;
  transition: all $duration-base;
  margin-bottom: $spacing-md;
  
  &:hover {
    transform: translateY(-4px);
    
    .stat-icon-wrapper {
      transform: scale(1.1);
    }
  }
  
  &.stat-primary .stat-icon-wrapper { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  &.stat-success .stat-icon-wrapper { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
  &.stat-warning .stat-icon-wrapper { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
  &.stat-danger .stat-icon-wrapper { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); }
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform $duration-fast;
}

.stat-icon {
  color: white;
}

.stat-details {
  flex: 1;
}

.stat-value {
  font-size: $font-size-xxl;
  font-weight: bold;
  margin: 0 0 $spacing-xs 0;
  color: $text-primary;
}

.stat-title {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0 0 $spacing-xs 0;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  
  .trend-up {
    color: $success-color;
  }
  
  .trend-down {
    color: $error-color;
  }
}

// 图表区域
.charts-row {
  margin-bottom: $spacing-lg;
}

.chart-card {
  padding: $spacing-lg;
  height: 400px;
  margin-bottom: $spacing-md;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  
  h3 {
    margin: 0;
    font-size: $font-size-lg;
    color: $text-primary;
  }
}

.chart-container {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: $text-secondary;
  
  p {
    margin-top: $spacing-md;
  }
}

// 进度列表
.progress-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  padding-top: $spacing-md;
}

.progress-item {
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
  }
  
  .task-name {
    font-size: $font-size-sm;
    color: $text-regular;
  }
  
  .task-percentage {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $text-primary;
  }
}

// 快捷操作
.quick-actions {
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  color: $text-primary;
  margin: 0 0 $spacing-lg 0;
}

.action-card {
  padding: $spacing-lg;
  text-align: center;
  cursor: pointer;
  transition: all $duration-base;
  height: 140px;
  margin-bottom: $spacing-md;
  
  &:hover {
    transform: translateY(-4px);
    
    .action-icon {
      transform: scale(1.2) rotate(5deg);
    }
  }
  
  h4 {
    margin: $spacing-sm 0;
    color: $text-primary;
    font-size: $font-size-md;
  }
  
  p {
    margin: 0;
    color: $text-secondary;
    font-size: $font-size-sm;
  }
}

.action-icon {
  transition: transform $duration-base;
}

// 最近活动
.recent-activities {
  padding: $spacing-lg;
  
  .el-timeline {
    padding-left: 0;
    margin-top: $spacing-lg;
  }
}

.activity-content {
  h4 {
    margin: 0 0 $spacing-xs 0;
    color: $text-primary;
    font-size: $font-size-md;
  }
  
  p {
    margin: 0;
    color: $text-secondary;
    font-size: $font-size-sm;
  }
}

// 动画
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    text-align: center;
  }
  
  .time-display {
    margin-top: $spacing-lg;
  }
  
  .welcome-title {
    font-size: $font-size-xl;
  }
}
</style>