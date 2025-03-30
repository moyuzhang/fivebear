<template>
  <div class="performance-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>性能测试</span>
          <div class="header-actions">
            <el-button type="primary" @click="startTest">开始测试</el-button>
            <el-button type="danger" @click="stopTest">停止测试</el-button>
          </div>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>实时监控</span>
              </div>
            </template>
            <div class="monitor-content">
              <div class="monitor-item">
                <span class="label">CPU使用率</span>
                <el-progress 
                  :percentage="monitorData.cpu" 
                  :color="getProgressColor(monitorData.cpu)"
                />
              </div>
              <div class="monitor-item">
                <span class="label">内存使用率</span>
                <el-progress 
                  :percentage="monitorData.memory" 
                  :color="getProgressColor(monitorData.memory)"
                />
              </div>
              <div class="monitor-item">
                <span class="label">响应时间</span>
                <span class="value">{{ monitorData.responseTime }}ms</span>
              </div>
              <div class="monitor-item">
                <span class="label">并发用户数</span>
                <span class="value">{{ monitorData.concurrentUsers }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="16">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>测试结果</span>
              </div>
            </template>
            <el-table :data="testResults" style="width: 100%">
              <el-table-column prop="name" label="测试指标" />
              <el-table-column prop="value" label="数值" width="120" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const monitorData = ref({
  cpu: 45,
  memory: 60,
  responseTime: 120,
  concurrentUsers: 50
})

const testResults = ref([
  {
    name: '平均响应时间',
    value: '120ms',
    status: '正常'
  },
  {
    name: 'TPS',
    value: '100',
    status: '正常'
  },
  {
    name: '错误率',
    value: '0.1%',
    status: '正常'
  },
  {
    name: '并发用户数',
    value: '50',
    status: '正常'
  }
])

const getProgressColor = (percentage: number) => {
  if (percentage < 60) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

const startTest = () => {
  ElMessage.success('开始性能测试...')
  // 这里可以添加实际的测试启动逻辑
}

const stopTest = () => {
  ElMessage.warning('停止性能测试...')
  // 这里可以添加实际的测试停止逻辑
}
</script>

<style scoped>
.performance-test {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.monitor-content {
  padding: 10px;
}

.monitor-item {
  margin-bottom: 20px;
}

.monitor-item .label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
}

.monitor-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
</style> 