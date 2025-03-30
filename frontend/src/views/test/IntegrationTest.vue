<template>
  <div class="integration-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>集成测试</span>
          <div class="header-actions">
            <el-button type="primary" @click="runTests">运行测试</el-button>
            <el-button type="success" @click="generateReport">生成报告</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="测试结果" name="results">
          <el-table :data="testResults" style="width: 100%">
            <el-table-column prop="name" label="测试场景" />
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
        </el-tab-pane>
        
        <el-tab-pane label="测试配置" name="config">
          <el-form :model="testConfig" label-width="120px">
            <el-form-item label="测试环境">
              <el-select v-model="testConfig.environment">
                <el-option label="开发环境" value="dev" />
                <el-option label="测试环境" value="test" />
                <el-option label="生产环境" value="prod" />
              </el-select>
            </el-form-item>
            <el-form-item label="并发用户数">
              <el-input-number v-model="testConfig.concurrentUsers" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="测试时长(分钟)">
              <el-input-number v-model="testConfig.duration" :min="1" :max="60" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('results')

const testResults = ref([
  {
    name: '用户注册流程',
    status: '通过',
    time: '1.2s',
    message: '注册流程完整'
  },
  {
    name: '订单处理流程',
    status: '通过',
    time: '2.5s',
    message: '订单处理正常'
  },
  {
    name: '支付流程',
    status: '失败',
    time: '3.1s',
    message: '支付接口异常'
  }
])

const testConfig = ref({
  environment: 'test',
  concurrentUsers: 10,
  duration: 5
})

const runTests = () => {
  ElMessage.success('开始运行集成测试...')
  // 这里可以添加实际的测试运行逻辑
}

const generateReport = () => {
  ElMessage.success('正在生成测试报告...')
  // 这里可以添加报告生成逻辑
}

const saveConfig = () => {
  ElMessage.success('配置已保存')
  // 这里可以添加配置保存逻辑
}
</script>

<style scoped>
.integration-test {
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