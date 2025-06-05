<template>
  <div class="sites-page">
    <div class="header">
      <h2>站点管理</h2>
      <el-button type="primary" @click="showDialog = true">添加站点</el-button>
    </div>

    <div class="stats">
      <div class="stat-card">
        <div class="stat-number">{{ sitesData.totalCount }}</div>
        <div class="stat-label">总站点数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ sitesData.statusCounts.LOGGED_IN || 0 }}</div>
        <div class="stat-label">已登录</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ sitesData.statusCounts.NOT_LOGGED_IN || 0 }}</div>
        <div class="stat-label">未登录</div>
      </div>
    </div>

    <el-table :data="sitesData.sites" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="domain" label="域名" />
      <el-table-column label="状态">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.statusDescription }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="handleLogin(scope.row)">登录</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="添加站点">
      <el-form :model="form">
        <el-form-item label="URL">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getUserSites, 
  addSite, 
  loginSite, 
  deleteSite,
  type SitesSummary
} from '@/api/platform'

const loading = ref(false)
const showDialog = ref(false)
const sitesData = ref<SitesSummary>({
  totalCount: 0,
  statusCounts: {},
  sites: [],
  userId: '',
  timestamp: 0
})

const form = ref({
  url: '',
  username: '',
  password: '',
  rebateRate: 0.0001,
  lotteryType: 1,
  clientType: 1
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await getUserSites()
    if (response.success) {
      sitesData.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  return status === 'LOGGED_IN' ? 'success' : 
         status === 'NOT_LOGGED_IN' ? 'info' : 'danger'
}

const handleAdd = async () => {
  try {
    const response = await addSite(form.value)
    if (response.success) {
      ElMessage.success('添加成功')
      showDialog.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleLogin = async (site: any) => {
  try {
    const response = await loginSite(site.uniqueKey)
    if (response.success) {
      ElMessage.success('登录操作已提交')
    }
  } catch (error) {
    ElMessage.error('登录失败')
  }
}

const handleDelete = async (site: any) => {
  try {
    const response = await deleteSite(site.uniqueKey)
    if (response.success) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.sites-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  color: #666;
  margin-top: 8px;
}
</style> 