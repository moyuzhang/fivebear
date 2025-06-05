<template>
  <Layout>
    <div class="log-management">
    <el-card>
      <template #header>
        <div class="page-header">
          <div class="header-title">
            <el-icon><Files /></el-icon>
            <span>日志管理</span>
          </div>
        </div>
      </template>

      <div class="content">
        <h3>🚧 日志管理模块开发中...</h3>
        <p>该模块功能正在开发中，敬请期待</p>
        <el-button @click="$router.push('/admin')">返回管理首页</el-button>
      </div>
    </el-card>
  </div>
  </Layout>
</template>

<script setup lang="ts">
import Layout from '@/components/Layout.vue'
import { Files } from '@element-plus/icons-vue'
</script>

<style scoped>
.log-management {
  padding: 120px 20px 20px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.header-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.content {
  text-align: center;
  padding: 50px;
}
</style> 