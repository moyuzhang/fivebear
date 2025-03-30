<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>设备总数</span>
              <el-icon><Monitor /></el-icon>
            </div>
          </template>
          <div class="stat-value">128</div>
          <div class="stat-footer">
            <span>较上月</span>
            <span class="up">+12%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>在线设备</span>
              <el-icon><Connection /></el-icon>
            </div>
          </template>
          <div class="stat-value">98</div>
          <div class="stat-footer">
            <span>较上月</span>
            <span class="up">+8%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>消息总数</span>
              <el-icon><Message /></el-icon>
            </div>
          </template>
          <div class="stat-value">1,234</div>
          <div class="stat-footer">
            <span>较上月</span>
            <span class="up">+15%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户</span>
              <el-icon><User /></el-icon>
            </div>
          </template>
          <div class="stat-value">32</div>
          <div class="stat-footer">
            <span>较上月</span>
            <span class="up">+5%</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>消息趋势</span>
              <el-radio-group v-model="messageChartType" size="small">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里可以集成 ECharts 等图表库 -->
            <div class="chart-placeholder">消息趋势图表</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>设备类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里可以集成 ECharts 等图表库 -->
            <div class="chart-placeholder">设备类型分布图表</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-card shadow="hover" class="activity-card">
      <template #header>
        <div class="card-header">
          <span>最近活动</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="activity.time"
          :type="activity.type"
        >
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Monitor,
  Connection,
  Message,
  User
} from '@element-plus/icons-vue'

// 消息图表类型
const messageChartType = ref('day')

// 最近活动数据
const recentActivities = ref([
  {
    content: '新设备 "温度传感器" 已连接',
    time: '10分钟前',
    type: 'success'
  },
  {
    content: '用户 "admin" 修改了系统设置',
    time: '30分钟前',
    type: 'primary'
  },
  {
    content: '设备 "湿度传感器" 离线',
    time: '1小时前',
    type: 'warning'
  },
  {
    content: '新消息 "sensors/temperature" 已接收',
    time: '2小时前',
    type: 'info'
  }
])
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0;
}

.stat-footer {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
}

.up {
  color: #67C23A;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  color: #909399;
  font-size: 14px;
}

.activity-card {
  margin-bottom: 20px;
}

:deep(.el-timeline-item__node) {
  background-color: #409EFF;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 13px;
}
</style> 