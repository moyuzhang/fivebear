<template>
    <div class="sites-page">
        <div class="page-header">
            <h2>站点管理</h2>
            <div class="header-actions">
                <div class="connection-status">
                    <span :class="['status-dot', connectionStatus]"></span>
                    {{ connectionText }}
                </div>
                <el-button type="primary" @click="showAddDialog = true">
                    <el-icon>
                        <Plus />
                    </el-icon>
                    添加站点
                </el-button>
                <el-button @click="refreshData">
                    <el-icon>
                        <Refresh />
                    </el-icon>
                    刷新
                </el-button>
            </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-grid">
            <div class="stat-card total">
                <div class="stat-icon">🏢</div>
                <div class="stat-content">
                    <div class="stat-number">{{ sitesData.totalCount }}</div>
                    <div class="stat-label">总站点数</div>
                </div>
            </div>
            <div class="stat-card success">
                <div class="stat-icon">✅</div>
                <div class="stat-content">
                    <div class="stat-number">{{ sitesData.statusCounts.LOGGED_IN || 0 }}</div>
                    <div class="stat-label">已登录</div>
                </div>
            </div>
            <div class="stat-card warning">
                <div class="stat-icon">⏳</div>
                <div class="stat-content">
                    <div class="stat-number">{{ sitesData.statusCounts.NOT_LOGGED_IN || 0 }}</div>
                    <div class="stat-label">未登录</div>
                </div>
            </div>
            <div class="stat-card danger">
                <div class="stat-icon">❌</div>
                <div class="stat-content">
                    <div class="stat-number">{{
                        (sitesData.statusCounts.ERROR || 0) +
                        (sitesData.statusCounts.NETWORK_ERROR || 0)
                        }}</div>
                    <div class="stat-label">异常</div>
                </div>
            </div>
        </div>

        <!-- 站点表格 -->
        <el-card class="table-card">
            <el-table :data="sitesData.sites" v-loading="loading" @selection-change="handleSelectionChange" stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="domain" label="域名" width="200" />
                <el-table-column label="状态" width="120">
                    <template #default="scope">
                        <el-tag :type="getStatusType(scope.row.status)" size="small">
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
                        <el-button v-if="scope.row.status === 'NOT_LOGGED_IN'" type="success" size="small"
                            @click="handleLogin(scope.row)">
                            登录
                        </el-button>
                        <el-button v-if="scope.row.status === 'LOGGED_IN'" type="warning" size="small"
                            @click="handleLogout(scope.row)">
                            登出
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDelete(scope.row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 批量操作 -->
        <div v-if="selectedSites.length > 0" class="batch-actions">
            <span>已选择 {{ selectedSites.length }} 个站点</span>
            <el-button type="success" @click="batchLogin">批量登录</el-button>
            <el-button type="danger" @click="batchDelete">批量删除</el-button>
        </div>

        <!-- 添加站点对话框 -->
        <el-dialog v-model="showAddDialog" title="添加站点" width="500px">
            <el-form :model="newSiteForm" :rules="formRules" ref="formRef" label-width="100px">
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
                    <el-input-number v-model="newSiteForm.rebateRate" :min="0" :max="1" :step="0.0001" :precision="4" />
                </el-form-item>
                <el-form-item label="彩种类型">
                    <el-select v-model="newSiteForm.lotteryType">
                        <el-option label="五分彩" :value="1" />
                        <el-option label="十分彩" :value="2" />
                        <el-option label="十五分彩" :value="3" />
                    </el-select>
                </el-form-item>
                <el-form-item label="客户端类型">
                    <el-select v-model="newSiteForm.clientType">
                        <el-option label="会员端" :value="1" />
                        <el-option label="管理端" :value="2" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showAddDialog = false">取消</el-button>
                <el-button type="primary" @click="handleAddSite" :loading="addLoading">确定</el-button>
            </template>
        </el-dialog>

        <!-- 实时消息 -->
        <el-card class="message-card">
            <template #header>
                <div class="card-header">
                    <span>实时消息</span>
                    <el-button size="small" @click="clearMessages">清空</el-button>
                </div>
            </template>
            <div class="message-list">
                <div v-for="(msg, index) in messages" :key="index" class="message-item">
                    <span class="message-time">{{ msg.timestamp }}</span>
                    <span class="message-content">{{ msg.message }}</span>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
    getUserSites,
    addSite,
    loginSite,
    logoutSite,
    deleteSite,
    batchLogin as batchLoginApi,
    PlatformWebSocket,
    type SiteDetail,
    type SitesSummary,
    type SiteCreateRequest
} from '@/api/platform'

// 数据
const loading = ref(false)
const addLoading = ref(false)
const showAddDialog = ref(false)
const selectedSites = ref<SiteDetail[]>([])
const connectionStatus = ref('disconnected')
const connectionText = ref('未连接')
const messages = ref<Array<{ message: string, timestamp: string }>>([])

const sitesData = ref<SitesSummary>({
    totalCount: 0,
    statusCounts: {},
    sites: [],
    userId: '',
    timestamp: 0
})

const newSiteForm = ref<SiteCreateRequest>({
    url: '',
    username: '',
    password: '',
    rebateRate: 0.0001,
    lotteryType: 1,
    clientType: 1
})

const formRules = {
    url: [{ required: true, message: '请输入站点URL', trigger: 'blur' }],
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const formRef = ref()
let webSocket: PlatformWebSocket | null = null

// 生命周期
onMounted(() => {
    loadData()
    initWebSocket()
})

onUnmounted(() => {
    if (webSocket) {
        webSocket.disconnect()
    }
})

// 方法
const loadData = async () => {
    loading.value = true
    try {
        const response = await getUserSites()
        if (response.success) {
            sitesData.value = response.data
        } else {
            ElMessage.error(response.message)
        }
    } catch (error) {
        ElMessage.error('加载数据失败')
    } finally {
        loading.value = false
    }
}

const initWebSocket = () => {
    // 使用JWT token，不再需要传递简单格式的token
    webSocket = new PlatformWebSocket()

    webSocket.onStatusChange((status) => {
        connectionStatus.value = status
        connectionText.value = status === 'connected' ? '已连接' :
            status === 'error' ? '连接错误' : '未连接'
    })

    webSocket.onMessage((message) => {
        addMessage(message.message, message.timestamp)
        if (message.type === 'SITES_SUMMARY' && message.data) {
            sitesData.value = message.data
        }
    })

    webSocket.connect()
}

const addMessage = (content: string, timestamp?: string) => {
    const time = timestamp || new Date().toLocaleTimeString()
    messages.value.unshift({ message: content, timestamp: time })
    if (messages.value.length > 50) {
        messages.value = messages.value.slice(0, 50)
    }
}

const clearMessages = () => {
    messages.value = []
}

const refreshData = () => {
    loadData()
    if (webSocket) {
        webSocket.getSitesSummary()
    }
}

const handleSelectionChange = (selection: SiteDetail[]) => {
    selectedSites.value = selection
}

const getStatusType = (status: string) => {
    const map: Record<string, string> = {
        'LOGGED_IN': 'success',
        'NOT_LOGGED_IN': 'info',
        'LOGGING_IN': 'warning',
        'ERROR': 'danger',
        'NETWORK_ERROR': 'danger'
    }
    return map[status] || 'info'
}

const handleAddSite = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
        if (!valid) return

        addLoading.value = true
        try {
            const response = await addSite(newSiteForm.value)
            if (response.success) {
                ElMessage.success('添加成功')
                showAddDialog.value = false
                resetForm()
                loadData()
            } else {
                ElMessage.error(response.message)
            }
        } catch (error) {
            ElMessage.error('添加失败')
        } finally {
            addLoading.value = false
        }
    })
}

const resetForm = () => {
    newSiteForm.value = {
        url: '',
        username: '',
        password: '',
        rebateRate: 0.0001,
        lotteryType: 1,
        clientType: 1
    }
    if (formRef.value) {
        formRef.value.resetFields()
    }
}

const handleLogin = async (site: SiteDetail) => {
    try {
        const response = await loginSite(site.uniqueKey)
        if (response.success) {
            ElMessage.success('登录操作已提交')
        } else {
            ElMessage.error(response.message)
        }
    } catch (error) {
        ElMessage.error('登录失败')
    }
}

const handleLogout = async (site: SiteDetail) => {
    try {
        const response = await logoutSite(site.uniqueKey)
        if (response.success) {
            ElMessage.success('登出操作已提交')
        } else {
            ElMessage.error(response.message)
        }
    } catch (error) {
        ElMessage.error('登出失败')
    }
}

const handleDelete = async (site: SiteDetail) => {
    try {
        await ElMessageBox.confirm(`确定删除站点 ${site.username}@${site.domain} 吗？`, '确认删除')

        const response = await deleteSite(site.uniqueKey)
        if (response.success) {
            ElMessage.success('删除成功')
            loadData()
        } else {
            ElMessage.error(response.message)
        }
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}

const batchLogin = async () => {
    try {
        const uniqueKeys = selectedSites.value.map(site => site.uniqueKey)
        const response = await batchLoginApi({ uniqueKeys })
        if (response.success) {
            ElMessage.success('批量登录操作已提交')
        } else {
            ElMessage.error(response.message)
        }
    } catch (error) {
        ElMessage.error('批量登录失败')
    }
}

const batchDelete = async () => {
    try {
        await ElMessageBox.confirm(`确定删除选中的 ${selectedSites.value.length} 个站点吗？`, '确认批量删除')

        const promises = selectedSites.value.map(site => deleteSite(site.uniqueKey))
        await Promise.all(promises)

        ElMessage.success('批量删除成功')
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('批量删除失败')
        }
    }
}
</script>

<style scoped>
.sites-page {
    padding: 20px;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-header h2 {
    margin: 0;
    color: #303133;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.connection-status {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #606266;
}

.status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
}

.status-dot.connected {
    background-color: #67c23a;
}

.status-dot.disconnected {
    background-color: #f56c6c;
}

.status-dot.error {
    background-color: #e6a23c;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 20px;
}

.stat-card {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 16px;
}

.stat-icon {
    font-size: 32px;
}

.stat-number {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 4px;
}

.stat-label {
    font-size: 14px;
    color: #606266;
}

.total .stat-number {
    color: #409eff;
}

.success .stat-number {
    color: #67c23a;
}

.warning .stat-number {
    color: #e6a23c;
}

.danger .stat-number {
    color: #f56c6c;
}

.table-card {
    margin-bottom: 20px;
}

.batch-actions {
    background: white;
    padding: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
}

.message-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.message-list {
    height: 200px;
    overflow-y: auto;
}

.message-item {
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    gap: 12px;
    font-size: 14px;
}

.message-item:last-child {
    border-bottom: none;
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