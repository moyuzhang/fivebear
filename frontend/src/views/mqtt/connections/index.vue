<template>
  <div class="connections-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>连接管理</span>
          <el-button type="primary" @click="handleAdd">添加连接</el-button>
        </div>
      </template>
      
      <el-table :data="connections" style="width: 100%">
        <el-table-column prop="name" label="连接名称" />
        <el-table-column prop="host" label="主机地址" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'connected' ? 'success' : 'danger'">
              {{ row.status === 'connected' ? '已连接' : '未连接' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button-group>
              <el-button 
                :type="row.status === 'connected' ? 'danger' : 'success'"
                size="small"
                @click="handleToggleConnection(row)"
              >
                {{ row.status === 'connected' ? '断开' : '连接' }}
              </el-button>
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
const connections = ref([
  {
    id: 1,
    name: '本地测试',
    host: 'localhost',
    port: 1883,
    status: 'connected'
  },
  {
    id: 2,
    name: '远程服务器',
    host: 'mqtt.example.com',
    port: 1883,
    status: 'disconnected'
  }
])

const handleAdd = () => {
  ElMessage.info('添加连接功能待实现')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑连接：${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除连接 "${row.name}" 吗？`,
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

const handleToggleConnection = (row: any) => {
  row.status = row.status === 'connected' ? 'disconnected' : 'connected'
  ElMessage.success(`${row.name} ${row.status === 'connected' ? '已连接' : '已断开'}`)
}
</script>

<style scoped>
.connections-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 