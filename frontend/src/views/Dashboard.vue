<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.totalUsers">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日访问</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.todayVisits">
              <template #prefix>
                <el-icon><View /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统消息</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.systemMessages">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待处理任务</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.pendingTasks">
              <template #prefix>
                <el-icon><List /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>访问趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里可以添加图表组件 -->
            <div class="placeholder">访问趋势图表</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
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
              :type="activity.type">
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, View, Message, List } from '@element-plus/icons-vue'

const statistics = ref({
  totalUsers: 1234,
  todayVisits: 567,
  systemMessages: 89,
  pendingTasks: 12
})

const recentActivities = ref([
  {
    content: '系统更新完成',
    time: '2024-03-20 10:00:00',
    type: 'success'
  },
  {
    content: '新用户注册',
    time: '2024-03-20 09:30:00',
    type: 'primary'
  },
  {
    content: '系统警告',
    time: '2024-03-20 09:00:00',
    type: 'warning'
  },
  {
    content: '数据库备份',
    time: '2024-03-20 08:30:00',
    type: 'info'
  }
])

onMounted(() => {
  // 这里可以添加获取实际数据的逻辑
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  color: #909399;
  font-size: 14px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 15px 20px;
}
</style> 