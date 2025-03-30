<template>
  <div class="roles-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">添加角色</el-button>
        </div>
      </template>
      
      <el-table :data="roles" style="width: 100%">
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="success" size="small" @click="handlePermission(row)">
                权限
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
const roles = ref([
  {
    id: 1,
    name: '超级管理员',
    code: 'SUPER_ADMIN',
    description: '系统最高权限，可以管理所有功能',
    userCount: 1,
    createTime: '2024-03-01 10:00:00'
  },
  {
    id: 2,
    name: '普通用户',
    code: 'USER',
    description: '基础功能访问权限',
    userCount: 10,
    createTime: '2024-03-01 10:00:00'
  },
  {
    id: 3,
    name: '访客',
    code: 'GUEST',
    description: '只读权限，无法修改数据',
    userCount: 5,
    createTime: '2024-03-01 10:00:00'
  }
])

const handleAdd = () => {
  ElMessage.info('添加角色功能待实现')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑角色：${row.name}`)
}

const handlePermission = (row: any) => {
  ElMessage.info(`配置角色权限：${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除角色 "${row.name}" 吗？`,
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
.roles-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 