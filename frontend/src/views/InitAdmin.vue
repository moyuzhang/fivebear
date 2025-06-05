<template>
  <div class="init-admin-page">
    <div class="init-container">
      <div class="init-header">
        <h1>🔧 管理员账户初始化</h1>
        <p>检查并创建默认管理员账户</p>
      </div>

      <el-card v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>系统状态</span>
            <el-button size="small" @click="checkAdminStatus">刷新</el-button>
          </div>
        </template>

        <div class="status-section">
          <div class="status-item">
            <span>管理员用户数量:</span>
            <el-tag :type="adminCount > 0 ? 'success' : 'warning'">
              {{ adminCount }} 个
            </el-tag>
          </div>

          <div v-if="adminList.length > 0" class="admin-list">
            <h4>现有管理员:</h4>
            <el-table :data="adminList" style="width: 100%">
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="nickname" label="昵称" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" />
            </el-table>
          </div>

          <div v-if="adminCount === 0" class="no-admin">
            <el-alert
              title="未找到管理员用户"
              type="warning"
              :closable="false"
              show-icon
            />
            
            <div class="init-actions">
              <el-button 
                type="primary" 
                size="large" 
                @click="initAdmin"
                :loading="initLoading"
              >
                创建默认管理员
              </el-button>
            </div>
          </div>

          <div v-if="adminCount > 0" class="success-info">
            <el-alert
              title="管理员账户已存在"
              type="success"
              :closable="false"
              show-icon
            />
            
            <div class="login-link">
              <el-button type="primary" @click="goToLogin">
                前往登录
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="initResult" class="init-result">
          <el-alert
            :title="initResult.message"
            :type="initResult.success ? 'success' : 'error'"
            :closable="false"
            show-icon
          />
          
          <div v-if="initResult.success && initResult.credentials" class="credentials">
            <h4>默认登录信息:</h4>
            <el-descriptions border :column="1">
              <el-descriptions-item label="用户名">{{ initResult.credentials.username }}</el-descriptions-item>
              <el-descriptions-item label="密码">{{ initResult.credentials.password }}</el-descriptions-item>
            </el-descriptions>
            <el-alert
              title="请妥善保管登录信息，并在首次登录后立即修改密码"
              type="warning"
              :closable="false"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'

const router = useRouter()

// 定义类型
interface AdminInfo {
  id: number
  username: string
  nickname: string
  email: string
  role_id: number
  status: number
  deleted: boolean
  created_at: string
}

interface InitResult {
  success: boolean
  message: string
  credentials?: {
    username: string
    password: string
  } | null
}

// 响应式数据
const loading = ref(false)
const initLoading = ref(false)
const adminCount = ref(0)
const adminList = ref<AdminInfo[]>([])
const initResult = ref<InitResult | null>(null)

// 检查管理员状态
const checkAdminStatus = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/system/check-admin')
    if (response.code === 200) {
      adminCount.value = response.data.adminCount
      adminList.value = response.data.admins
      ElMessage.success('状态检查完成')
    } else {
      ElMessage.error('检查失败: ' + response.message)
    }
  } catch (error) {
    console.error('检查管理员状态失败:', error)
    ElMessage.error('检查失败，请确保后端服务已启动')
  } finally {
    loading.value = false
  }
}

// 初始化管理员
const initAdmin = async () => {
  initLoading.value = true
  try {
    const response = await request.post('/api/system/init-admin', {})
    
    if (response.code === 200) {
      initResult.value = {
        success: true,
        message: response.message,
        credentials: response.data.username ? {
          username: response.data.username,
          password: response.data.password
        } : null
      }
      
      // 刷新管理员状态
      await checkAdminStatus()
      
      ElMessage.success('管理员初始化成功')
    } else {
      initResult.value = {
        success: false,
        message: response.message
      }
      ElMessage.error('初始化失败: ' + response.message)
    }
  } catch (error) {
    console.error('初始化管理员失败:', error)
    initResult.value = {
      success: false,
      message: '初始化失败，请检查后端服务'
    }
    ElMessage.error('初始化失败，请检查后端服务')
  } finally {
    initLoading.value = false
  }
}

// 前往登录页
const goToLogin = () => {
  router.push('/login')
}

// 组件挂载时检查状态
onMounted(() => {
  checkAdminStatus()
})
</script>

<style scoped>
.init-admin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

.init-container {
  max-width: 800px;
  width: 100%;
}

.init-header {
  text-align: center;
  margin-bottom: var(--space-6);
  color: white;
}

.init-header h1 {
  font-size: var(--text-3xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--space-2) 0;
}

.init-header p {
  font-size: var(--text-lg);
  opacity: 0.9;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-section {
  padding: var(--space-4) 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0;
  font-size: var(--text-base);
}

.admin-list {
  margin-top: var(--space-4);
}

.admin-list h4 {
  margin-bottom: var(--space-3);
  color: var(--gray-700);
}

.no-admin {
  text-align: center;
  padding: var(--space-6) 0;
}

.init-actions {
  margin-top: var(--space-4);
}

.success-info {
  text-align: center;
  padding: var(--space-4) 0;
}

.login-link {
  margin-top: var(--space-4);
}

.init-result {
  margin-top: var(--space-4);
}

.credentials {
  margin-top: var(--space-4);
  padding: var(--space-4);
  background: var(--gray-50);
  border-radius: var(--radius-lg);
}

.credentials h4 {
  margin-bottom: var(--space-3);
  color: var(--gray-700);
}
</style> 