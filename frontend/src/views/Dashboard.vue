<template>
  <Layout>
    <div class="dashboard">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📊 FiveBear企业管理系统仪表盘</span>
        </div>
      </template>

      <div class="welcome-section">
        <h2>🎉 欢迎使用 FiveBear企业管理系统</h2>
        <p>系统已成功启动，所有功能模块正常运行</p>

        <!-- 用户信息卡片 -->
        <el-card class="user-info-card" v-if="userStore.userInfo">
          <div class="user-welcome">
            <el-avatar :size="60" class="welcome-avatar">
              {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="welcome-text">
              <h3>您好，{{ userStore.userInfo.nickname }}！</h3>
              <p>角色：{{ userStore.userInfo.roleName }}</p>
              <p>账户状态：<el-tag type="success">正常</el-tag></p>
            </div>
          </div>
        </el-card>

        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon">
                  <User />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-title">管理员模块</div>
                  <div class="stat-value">正常运行</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon">
                  <TrendCharts />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-title">报表模块</div>
                  <div class="stat-value">正常运行</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon">
                  <Box />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-title">出货模块</div>
                  <div class="stat-value">正常运行</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon">
                  <Setting />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-title">系统状态</div>
                  <div class="stat-value">正常</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="action-buttons">
          <el-button type="primary" @click="$router.push('/admin')">
            <el-icon>
              <User />
            </el-icon>
            管理员管理
          </el-button>
          <el-button type="success" @click="$router.push('/report')">
            <el-icon>
              <TrendCharts />
            </el-icon>
            报表分析
          </el-button>
          <el-button type="warning" @click="$router.push('/shipment')">
            <el-icon>
              <Box />
            </el-icon>
            出货管理
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
  </Layout>
</template>

<script setup lang="ts">
import { User, TrendCharts, Box, Setting } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.dashboard {
  padding: 0; /* 移除padding，由Layout组件处理 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.welcome-section {
  text-align: center;
  padding: 20px;
}

.welcome-section h2 {
  color: #409eff;
  margin-bottom: 10px;
}

.user-info-card {
  margin: 20px auto;
  max-width: 400px;
}

.user-welcome {
  display: flex;
  align-items: center;
  text-align: left;
  padding: 20px;
}

.welcome-avatar {
  background-color: #409eff;
  color: white;
  font-weight: bold;
  margin-right: 20px;
}

.welcome-text h3 {
  margin: 0 0 10px 0;
  color: #409eff;
}

.welcome-text p {
  margin: 5px 0;
  color: #666;
}

.stats-row {
  margin: 30px 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 30px;
  color: #409eff;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.action-buttons {
  margin-top: 30px;
}

.action-buttons .el-button {
  margin: 0 10px;
}
</style>