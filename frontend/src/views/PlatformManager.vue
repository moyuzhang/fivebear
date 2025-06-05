<template>
  <div class="platform-manager">
    <!-- 页面标题栏 -->
    <div class="header">
      <div class="header-left">
        <h2>站点管理</h2>
        <div class="connection-status">
          <span :class="['status-indicator', connectionStatus]"></span>
          <span class="status-text">{{ connectionStatusText }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showAddSiteDialog = true">
          <el-icon><Plus /></el-icon>
          添加站点
        </el-button>
        <el-button @click="refreshSites">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 站点统计信息 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ sitesData.totalCount }}</div>
          <div class="stat-label">总站点数</div>
        </div>
        <div class="stat-card logged-in">
          <div class="stat-number">{{ sitesData.statusCounts.LOGGED_IN || 0 }}</div>
          <div class="stat-label">已登录</div>
        </div>
        <div class="stat-card not-logged-in">
          <div class="stat-number">{{ sitesData.statusCounts.NOT_LOGGED_IN || 0 }}</div>
          <div class="stat-label">未登录</div>
        </div>
        <div class="stat-card error">
          <div class="stat-number">{{ 
            (sitesData.statusCounts.ERROR || 0) + 
            (sitesData.statusCounts.NETWORK_ERROR || 0) 
          }}</div>
          <div class="stat-label">异常</div>
        </div>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-operations" v-if="selectedSites.length > 0">
      <span class="selected-count">已选择 {{ selectedSites.length }} 个站点</span>
      <div class="batch-buttons">
        <el-button type="success" @click="batchLogin" :loading="batchLoginLoading">
          批量登录
        </el-button>
        <el-button type="warning" @click="batchLogout" :loading="batchLogoutLoading">
          批量登出
        </el-button>
        <el-button type="danger" @click="batchDelete" :loading="batchDeleteLoading">
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 站点列表 -->
    <div class="sites-table">
      <el-table 
        :data="sitesData.sites" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="domain" label="域名" width="200" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)" 
              size="small">
              {{ scope.row.statusDescription }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rebateRate" label="返点率" width="100">
          <template #default="scope">
            {{ (scope.row.rebateRate * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column prop="siteType" label="站点类型" width="100" />
        <el-table-column prop="lotteryType" label="彩种" width="80" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'NOT_LOGGED_IN'"
              type="success" 
              size="small" 
              @click="loginSite(scope.row)">
              登录
            </el-button>
            <el-button 
              v-if="scope.row.status === 'LOGGED_IN'"
              type="warning" 
              size="small" 
              @click="logoutSite(scope.row)">
              登出
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="viewSiteDetail(scope.row)">
              详情
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteSite(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加站点对话框 -->
    <el-dialog v-model="showAddSiteDialog" title="添加站点" width="600px">
      <el-form :model="newSiteForm" :rules="siteFormRules" ref="siteFormRef" label-width="120px">
        <el-form-item label="站点URL" prop="url">
          <el-input v-model="newSiteForm.url" placeholder="请输入站点URL" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="newSiteForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newSiteForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="返点率">
          <el-input-number 
            v-model="newSiteForm.rebateRate" 
            :min="0" 
            :max="1" 
            :step="0.0001" 
            :precision="4"
            placeholder="0.0000" />
        </el-form-item>
        <el-form-item label="彩种类型">
          <el-select v-model="newSiteForm.lotteryType" placeholder="请选择彩种类型">
            <el-option label="五分彩" :value="1" />
            <el-option label="十分彩" :value="2" />
            <el-option label="十五分彩" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户端类型">
          <el-select v-model="newSiteForm.clientType" placeholder="请选择客户端类型">
            <el-option label="会员端" :value="1" />
            <el-option label="管理端" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddSiteDialog = false">取消</el-button>
          <el-button type="primary" @click="addSite" :loading="addSiteLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 站点详情对话框 -->
    <el-dialog v-model="showSiteDetailDialog" title="站点详情" width="600px">
      <div v-if="currentSiteDetail" class="site-detail">
        <div class="detail-item">
          <label>唯一标识：</label>
          <span>{{ currentSiteDetail.uniqueKey }}</span>
        </div>
        <div class="detail-item">
          <label>用户名：</label>
          <span>{{ currentSiteDetail.username }}</span>
        </div>
        <div class="detail-item">
          <label>域名：</label>
          <span>{{ currentSiteDetail.domain }}</span>
        </div>
        <div class="detail-item">
          <label>完整URL：</label>
          <span>{{ currentSiteDetail.url }}</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="getStatusType(currentSiteDetail.status)">
            {{ currentSiteDetail.statusDescription }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>返点率：</label>
          <span>{{ (currentSiteDetail.rebateRate * 100).toFixed(4) }}%</span>
        </div>
        <div class="detail-item">
          <label>站点类型：</label>
          <span>{{ currentSiteDetail.siteType }}</span>
        </div>
        <div class="detail-item">
          <label>彩种类型：</label>
          <span>{{ currentSiteDetail.lotteryType }}</span>
        </div>
        <div class="detail-item">
          <label>数据库ID：</label>
          <span>{{ currentSiteDetail.databaseId }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 实时消息日志 -->
    <div class="message-log">
      <div class="log-header">
        <h4>实时消息</h4>
        <el-button size="small" @click="clearMessages">清空</el-button>
      </div>
      <div class="log-content" ref="logContent">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          :class="['message-item', message.type.toLowerCase()]">
          <span class="message-time">{{ message.timestamp }}</span>
          <span class="message-content">{{ message.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useLoading, useDebounce } from '@/composables'
import { 
  getUserSites, 
  addSite as addSiteApi, 
  loginSite as loginSiteApi,
  logoutSite as logoutSiteApi,
  deleteSite as deleteSiteApi,
  getSiteDetail,
  batchLogin as batchLoginApi,
  checkSiteUniqueness,
  PlatformWebSocket,
  type SiteDetail,
  type SitesSummary,
  type SiteCreateRequest
} from '@/api/platform'

// 使用加载状态管理
const { loading, withLoading } = useLoading()
const { loading: addSiteLoading, withLoading: withAddSiteLoading } = useLoading()
const { loading: batchLoginLoading, withLoading: withBatchLoginLoading } = useLoading()
const { loading: batchLogoutLoading, withLoading: withBatchLogoutLoading } = useLoading()
const { loading: batchDeleteLoading, withLoading: withBatchDeleteLoading } = useLoading()

// 响应式数据
const sitesData = ref<SitesSummary>({
  totalCount: 0,
  statusCounts: {},
  sites: [],
  userId: '',
  timestamp: 0
})

// WebSocket 连接
const connectionStatus = ref('disconnected')
const connectionStatusText = ref('未连接')
const webSocket = ref<PlatformWebSocket | null>(null)
const messages = ref<Array<{type: string, message: string, timestamp: string}>>([])

// 选择的站点
const selectedSites = ref<SiteDetail[]>([])

// 添加站点表单
const showAddSiteDialog = ref(false)
const newSiteForm = ref<SiteCreateRequest>({
  url: '',
  username: '',
  password: '',
  rebateRate: 0.0001,
  lotteryType: 1,
  clientType: 1
})

const siteFormRules = {
  url: [{ required: true, message: '请输入站点URL', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const siteFormRef = ref()

// 站点详情
const showSiteDetailDialog = ref(false)
const currentSiteDetail = ref<SiteDetail | null>(null)

const logContent = ref()

// 初始化
onMounted(() => {
  loadSites()
  initWebSocket()
})

onUnmounted(() => {
  if (webSocket.value) {
    webSocket.value.disconnect()
  }
})

// 加载站点数据
const loadSites = async () => {
  await withLoading(async () => {
    try {
      const response = await getUserSites()
      if (response.success) {
        sitesData.value = response.data
      } else {
        ElMessage.error(response.message)
      }
    } catch (error) {
      console.error('Failed to load sites:', error)
      ElMessage.error('加载站点数据失败')
    }
  })
}

// 初始化WebSocket
const initWebSocket = () => {
  // 使用JWT token，不再需要传递简单格式的token
  webSocket.value = new PlatformWebSocket()
  
  webSocket.value.onStatusChange((status) => {
    connectionStatus.value = status
    connectionStatusText.value = {
      'connected': '已连接',
      'disconnected': '未连接',
      'error': '连接错误'
    }[status] || '未知状态'
  })
  
  webSocket.value.onMessage((message) => {
    addMessage(message.type, message.message, message.timestamp)
    
    // 如果是站点摘要更新，刷新数据
    if (message.type === 'SITES_SUMMARY' && message.data) {
      sitesData.value = message.data
    }
  })
  
  webSocket.value.connect()
}

// 添加消息到日志
const addMessage = (type: string, content: string, timestamp?: string) => {
  const time = timestamp || new Date().toLocaleTimeString()
  messages.value.unshift({ type, message: content, timestamp: time })
  
  // 限制消息数量
  if (messages.value.length > 100) {
    messages.value = messages.value.slice(0, 100)
  }
  
  // 自动滚动到最新消息
  nextTick(() => {
    if (logContent.value) {
      logContent.value.scrollTop = 0
    }
  })
}

// 清空消息
const clearMessages = () => {
  messages.value = []
}

// 刷新站点数据
const refreshSites = () => {
  loadSites()
  if (webSocket.value) {
    webSocket.value.getSitesSummary()
  }
}

// 处理选择变化
const handleSelectionChange = (selection: SiteDetail[]) => {
  selectedSites.value = selection
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'LOGGED_IN': 'success',
    'NOT_LOGGED_IN': 'info',
    'LOGGING_IN': 'warning',
    'ERROR': 'danger',
    'NETWORK_ERROR': 'danger',
    'LOGGED_OUT': 'info'
  }
  return statusMap[status] || 'info'
}

// 添加站点
const addSite = async () => {
  if (!siteFormRef.value) return
  
  await siteFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    await withAddSiteLoading(async () => {
      try {
        const response = await addSiteApi(newSiteForm.value)
        if (response.success) {
          ElMessage.success('站点添加成功')
          showAddSiteDialog.value = false
          resetSiteForm()
          loadSites()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        console.error('Failed to add site:', error)
        ElMessage.error('添加站点失败')
      }
    })
  })
}

// 重置表单
const resetSiteForm = () => {
  newSiteForm.value = {
    url: '',
    username: '',
    password: '',
    rebateRate: 0.0001,
    lotteryType: 1,
    clientType: 1
  }
  if (siteFormRef.value) {
    siteFormRef.value.resetFields()
  }
}

// 登录站点
const loginSite = async (site: SiteDetail) => {
  try {
    const response = await loginSiteApi(site.uniqueKey)
    if (response.success) {
      ElMessage.success('登录操作已提交')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('Failed to login site:', error)
    ElMessage.error('登录站点失败')
  }
}

// 登出站点
const logoutSite = async (site: SiteDetail) => {
  try {
    const response = await logoutSiteApi(site.uniqueKey)
    if (response.success) {
      ElMessage.success('登出操作已提交')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('Failed to logout site:', error)
    ElMessage.error('登出站点失败')
  }
}

// 删除站点
const deleteSite = async (site: SiteDetail) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除站点 ${site.username}@${site.domain} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteSiteApi(site.uniqueKey)
    if (response.success) {
      ElMessage.success('站点删除成功')
      loadSites()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete site:', error)
      ElMessage.error('删除站点失败')
    }
  }
}

// 查看站点详情
const viewSiteDetail = async (site: SiteDetail) => {
  try {
    const response = await getSiteDetail(site.uniqueKey)
    if (response.success) {
      currentSiteDetail.value = response.data
      showSiteDetailDialog.value = true
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('Failed to get site detail:', error)
    ElMessage.error('获取站点详情失败')
  }
}

// 批量登录
const batchLogin = async () => {
  batchLoginLoading.value = true
  try {
    const uniqueKeys = selectedSites.value.map(site => site.uniqueKey)
    const response = await batchLoginApi({ uniqueKeys })
    if (response.success) {
      ElMessage.success('批量登录操作已提交')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('Failed to batch login:', error)
    ElMessage.error('批量登录失败')
  } finally {
    batchLoginLoading.value = false
  }
}

// 批量登出
const batchLogout = async () => {
  batchLogoutLoading.value = true
  try {
    const uniqueKeys = selectedSites.value.map(site => site.uniqueKey)
    // 这里使用登出API（需要实现批量登出接口）
    ElMessage.success('批量登出功能待实现')
  } catch (error) {
    console.error('Failed to batch logout:', error)
    ElMessage.error('批量登出失败')
  } finally {
    batchLogoutLoading.value = false
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedSites.value.length} 个站点吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    batchDeleteLoading.value = true
    const deletePromises = selectedSites.value.map(site => deleteSiteApi(site.uniqueKey))
    
    try {
      await Promise.all(deletePromises)
      ElMessage.success('批量删除成功')
      loadSites()
    } catch (error) {
      ElMessage.error('部分站点删除失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to batch delete:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    batchDeleteLoading.value = false
  }
}
</script>

<style scoped>
.platform-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h2 {
  margin: 0;
  color: #303133;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-indicator.connected {
  background-color: #67c23a;
}

.status-indicator.disconnected {
  background-color: #f56c6c;
}

.status-indicator.error {
  background-color: #e6a23c;
}

.status-text {
  font-size: 14px;
  color: #606266;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  border-left: 4px solid #409eff;
}

.stat-card.logged-in {
  border-left-color: #67c23a;
}

.stat-card.not-logged-in {
  border-left-color: #909399;
}

.stat-card.error {
  border-left-color: #f56c6c;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.batch-operations {
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-count {
  font-size: 14px;
  color: #606266;
}

.sites-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.site-detail {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  width: 120px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.detail-item span {
  color: #303133;
}

.message-log {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.log-header h4 {
  margin: 0;
  color: #303133;
}

.log-content {
  height: 300px;
  overflow-y: auto;
  padding: 0;
}

.message-item {
  padding: 8px 20px;
  border-bottom: 1px solid #f5f7fa;
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.status_change {
  border-left: 3px solid #409eff;
}

.message-item.system_info {
  border-left: 3px solid #67c23a;
}

.message-item.system_error {
  border-left: 3px solid #f56c6c;
}

.message-time {
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
  width: 80px;
}

.message-content {
  color: #303133;
  flex: 1;
}
</style> 