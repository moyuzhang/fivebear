<template>
  <div class="home-container">
    <!-- 欢迎信息 -->
    <el-row :gutter="20" class="welcome-section">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎回来，{{ userStore.userInfo?.username }}</h2>
              <p class="subtitle">{{ getGreeting() }}</p>
            </div>
            <div class="welcome-stats">
              <div class="stat-item">
                <el-icon><Timer /></el-icon>
                <span>登录时间：{{ new Date().toLocaleString() }}</span>
              </div>
              <div class="stat-item">
                <el-icon><Location /></el-icon>
                <span>IP地址：{{ ipAddress }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据卡片 -->
    <el-row :gutter="20" class="data-section">
      <el-col :span="6">
        <el-card class="data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
              <el-icon><User /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">1,234</div>
            <div class="trend up">
              <el-icon><ArrowUp /></el-icon>
              <span>12%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日活跃</span>
              <el-icon><Timer /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">256</div>
            <div class="trend up">
              <el-icon><ArrowUp /></el-icon>
              <span>8%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统消息</span>
              <el-icon><Message /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">89</div>
            <div class="trend down">
              <el-icon><ArrowDown /></el-icon>
              <span>3%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统负载</span>
              <el-icon><Monitor /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">45%</div>
            <div class="trend up">
              <el-icon><ArrowUp /></el-icon>
              <span>5%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>访问趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-icon><TrendCharts /></el-icon>
            <span>图表区域</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-icon><PieChart /></el-icon>
            <span>图表区域</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import {
  User,
  Timer,
  Location,
  Message,
  Monitor,
  ArrowUp,
  ArrowDown,
  TrendCharts,
  PieChart
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const ipAddress = ref('192.168.1.1') // 这里应该从后端获取

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好，开始新的一天吧！'
  if (hour < 18) return '下午好，继续加油！'
  return '晚上好，今天辛苦了！'
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #1f4037 0%, #99f2c8 100%);
  color: white;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-text .subtitle {
  margin: 8px 0 0;
  font-size: 16px;
  opacity: 0.9;
}

.welcome-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.9;
}

.data-section {
  margin-bottom: 20px;
}

.data-card {
  transition: all 0.3s;
}

.data-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
}

.number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 10px 0;
}

.trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 14px;
}

.trend.up {
  color: #67c23a;
}

.trend.down {
  color: #f56c6c;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 300px;
}

.chart-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 16px;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

@media screen and (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .welcome-stats {
    flex-direction: column;
    gap: 8px;
  }

  .el-col {
    width: 100%;
    margin-bottom: 16px;
  }
}
</style> 