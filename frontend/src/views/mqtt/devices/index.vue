<template>
  <div class="devices-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-button type="primary" @click="handleAdd">添加设备</el-button>
        </div>
      </template>
      
      <el-table :data="devices" style="width: 100%">
        <el-table-column prop="name" label="设备名称" />
        <el-table-column prop="deviceId" label="设备ID" />
        <el-table-column prop="type" label="设备类型" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'info'">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastSeen" label="最后在线" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 模拟数据
const devices = ref([
  {
    id: 1,
    name: '温度传感器',
    deviceId: 'TEMP001',
    type: '传感器',
    status: 'online',
    lastSeen: '2024-03-20 10:30:00'
  },
  {
    id: 2,
    name: '湿度传感器',
    deviceId: 'HUM001',
    type: '传感器',
    status: 'offline',
    lastSeen: '2024-03-20 09:15:00'
  }
])

const handleAdd = () => {
  ElMessage.info('添加设备功能待实现')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑设备：${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除设备 "${row.name}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}
</script>

<style scoped>
.devices-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 