<template>
  <div class="messages-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handlePublish">发布消息</el-button>
            <el-button type="success" @click="handleSubscribe">订阅主题</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="消息列表" name="list">
          <el-table :data="messages" style="width: 100%">
            <el-table-column prop="topic" label="主题" />
            <el-table-column prop="payload" label="消息内容" show-overflow-tooltip />
            <el-table-column prop="qos" label="QoS" width="80" />
            <el-table-column prop="timestamp" label="时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleResend(row)">
                  重发
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="订阅主题" name="subscribed">
          <el-table :data="subscribedTopics" style="width: 100%">
            <el-table-column prop="topic" label="主题" />
            <el-table-column prop="qos" label="QoS" width="80" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleUnsubscribe(row)">
                  取消订阅
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('list')

// 模拟数据
const messages = ref([
  {
    id: 1,
    topic: 'sensors/temperature',
    payload: '{"value": 25.6, "unit": "°C"}',
    qos: 1,
    timestamp: '2024-03-20 10:30:00'
  },
  {
    id: 2,
    topic: 'sensors/humidity',
    payload: '{"value": 65, "unit": "%"}',
    qos: 1,
    timestamp: '2024-03-20 10:29:00'
  }
])

const subscribedTopics = ref([
  {
    id: 1,
    topic: 'sensors/#',
    qos: 1
  },
  {
    id: 2,
    topic: 'devices/status',
    qos: 0
  }
])

const handlePublish = () => {
  ElMessage.info('发布消息功能待实现')
}

const handleSubscribe = () => {
  ElMessage.info('订阅主题功能待实现')
}

const handleResend = (row: any) => {
  ElMessage.info(`重发消息：${row.topic}`)
}

const handleUnsubscribe = (row: any) => {
  ElMessage.info(`取消订阅：${row.topic}`)
}
</script>

<style scoped>
.messages-container {
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
</style> 