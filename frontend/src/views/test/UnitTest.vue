<template>
  <div class="unit-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>单元测试</span>
          <el-button type="primary" @click="runTests">运行测试</el-button>
        </div>
      </template>
      
      <el-table :data="testResults" style="width: 100%">
        <el-table-column prop="name" label="测试用例" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === '通过' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="耗时" width="120" />
        <el-table-column prop="message" label="详细信息" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const testResults = ref([
  {
    name: '用户登录测试',
    status: '通过',
    time: '0.2s',
    message: '登录功能正常'
  },
  {
    name: '数据验证测试',
    status: '通过',
    time: '0.1s',
    message: '表单验证正常'
  },
  {
    name: 'API调用测试',
    status: '失败',
    time: '0.3s',
    message: '接口超时'
  }
])

const runTests = () => {
  ElMessage.success('开始运行测试...')
  // 这里可以添加实际的测试运行逻辑
}
</script>

<style scoped>
.unit-test {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 